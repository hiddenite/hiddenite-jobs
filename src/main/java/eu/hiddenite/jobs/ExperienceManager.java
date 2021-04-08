package eu.hiddenite.jobs;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class ExperienceManager {
    private static class ExpBossBar {
        public BossBar bossBar = null;
        public int taskId = 0;
        public int level = 0;
    }

    private final JobsPlugin plugin;

    private final HashMap<String, HashMap<UUID, Long>> playersExperience = new HashMap<>();
    private HashMap<String, HashMap<UUID, Long>> updatedExperience = new HashMap<>();
    private final HashMap<String, HashMap<Player, ExpBossBar>> currentBossBars = new HashMap<>();
    private final BukkitTask saveTask;

    private static final int BOSS_BAR_TIMEOUT = 100; // 5 seconds
    private static final int DATABASE_SAVE_INTERVAL = 20 * 60 * 5; // 5 minutes

    public ExperienceManager(JobsPlugin plugin) {
        this.plugin = plugin;
        this.loadFromDatabase();

        saveTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::saveToDatabase,
                DATABASE_SAVE_INTERVAL, DATABASE_SAVE_INTERVAL);
    }

    public void close() {
        clearAllBossBars();
        saveTask.cancel();
        saveToDatabase();
    }

    private void loadFromDatabase() {
        try (PreparedStatement ps = plugin.getDatabase().prepareStatement(
                "SELECT player_id, job_type, current_exp FROM jobs"
        )) {
            int totalLoaded = 0;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID playerId = UUID.fromString(rs.getString(1));
                    String jobType = rs.getString(2);
                    long exp = rs.getLong(3);
                    if (!playersExperience.containsKey(jobType)) {
                        playersExperience.put(jobType, new HashMap<>());
                    }
                    playersExperience.get(jobType).put(playerId, exp);

                    totalLoaded++;
                }
            }
            plugin.getLogger().info("Loaded " + totalLoaded + " experience values");
        } catch (SQLException e) {
            plugin.getLogger().warning("Could not retrieve the player experience values");
            e.printStackTrace();
        }
    }

    private void saveToDatabase() {
        HashMap<String, HashMap<UUID, Long>> valuesToUpdate;
        synchronized (playersExperience) {
            valuesToUpdate = updatedExperience;
            updatedExperience = new HashMap<>();
        }

        for (var jobEntry : valuesToUpdate.entrySet()) {
            for (var expEntry : jobEntry.getValue().entrySet()) {
                String jobType = jobEntry.getKey();
                UUID playerId = expEntry.getKey();
                long exp = expEntry.getValue();
                plugin.getLogger().info("Saving " + playerId + ", job " + jobType + ", exp " + exp);

                try (PreparedStatement ps = plugin.getDatabase().prepareStatement(
                        "INSERT INTO jobs (player_id, job_type, current_exp) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE current_exp = ?"
                )) {
                    ps.setString(1, playerId.toString());
                    ps.setString(2, jobType);
                    ps.setLong(3, exp);
                    ps.setLong(4, exp);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public static long getExpAtLevel(int level) {
        if (level < 0) throw new IllegalArgumentException();
        return (long)(level - 1) * (long)level * 50L;
    }

    public static int getLevelFromExp(long exp) {
        if (exp < 0) throw new IllegalArgumentException();
        int level = 1;
        while (getExpAtLevel(level) <= exp) level++;
        return level - 1;
    }

    public long getPlayerExperience(Player player, String jobType) {
        if (!playersExperience.containsKey(jobType)) {
            playersExperience.put(jobType, new HashMap<>());
        }
        return playersExperience.get(jobType).getOrDefault(player.getUniqueId(), 0L);
    }

    public int getPlayerLevel(Player player, String jobType) {
        return getLevelFromExp(getPlayerExperience(player, jobType));
    }

    public void gainExp(Player player, String jobType, int amount) {
        if (!playersExperience.containsKey(jobType)) {
            playersExperience.put(jobType, new HashMap<>());
        }

        long previousExp = playersExperience.get(jobType).getOrDefault(player.getUniqueId(), 0L);
        int previousLevel = getLevelFromExp(previousExp);
        long previousLow = getExpAtLevel(previousLevel);
        long previousHigh = getExpAtLevel(previousLevel + 1);
        float previousProgress = (float)(previousExp - previousLow) / (float)(previousHigh - previousLow);

        long currentExp = previousExp + amount;
        int currentLevel = getLevelFromExp(currentExp);
        long currentLow = getExpAtLevel(currentLevel);
        long currentHigh = getExpAtLevel(currentLevel + 1);
        float currentProgress = (float)(currentExp - currentLow) / (float)(currentHigh - currentLow);

        playersExperience.get(jobType).put(player.getUniqueId(), currentExp);
        displayBossBar(player, jobType, currentLevel, previousProgress, currentProgress);

        if (previousLevel != currentLevel) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            plugin.sendActionBar(player, "messages.levelup",
                    "{JOB}", plugin.getMessage(jobType + ".name"),
                    "{LEVEL}", currentLevel
            );
        }

        synchronized (playersExperience) {
            if (!updatedExperience.containsKey(jobType)) {
                updatedExperience.put(jobType, new HashMap<>());
            }
            updatedExperience.get(jobType).put(player.getUniqueId(), currentExp);
        }
    }

    private void displayBossBar(Player player, String jobType, int level, float from, float to) {
        if (!currentBossBars.containsKey(jobType)) {
            currentBossBars.put(jobType, new HashMap<>());
        }

        ExpBossBar expBar = currentBossBars.get(jobType).get(player);

        if (expBar == null) {
            expBar = new ExpBossBar();
            expBar.level = level;
            expBar.bossBar = BossBar.bossBar(
                    plugin.formatComponent(jobType + ".name").append(Component.text(" " + level)),
                    from,
                    BossBar.Color.valueOf(plugin.getConfig().getString(jobType + ".bar-color")),
                    BossBar.Overlay.NOTCHED_10
            );
            player.showBossBar(expBar.bossBar);
            currentBossBars.get(jobType).put(player, expBar);
        }

        if (expBar.taskId != 0) {
            plugin.getServer().getScheduler().cancelTask(expBar.taskId);
        }
        expBar.taskId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(
                plugin,
                () -> clearBossBar(player, jobType),
                BOSS_BAR_TIMEOUT
        );

        if (expBar.level != level) {
            expBar.bossBar.name(plugin.formatComponent(jobType + ".name").append(Component.text(" " + level)));
        }
        expBar.bossBar.progress(to);
    }

    private void clearBossBar(Player player, String jobType) {
        ExpBossBar expBar = currentBossBars.get(jobType).remove(player);
        if (expBar != null) {
            player.hideBossBar(expBar.bossBar);
            expBar.taskId = 0;
        }
    }

    private void clearAllBossBars() {
        for (var jobEntry : currentBossBars.entrySet()) {
            for (var barEntry : jobEntry.getValue().entrySet()) {
                barEntry.getKey().hideBossBar(barEntry.getValue().bossBar);
            }
        }
        currentBossBars.clear();
    }
}
