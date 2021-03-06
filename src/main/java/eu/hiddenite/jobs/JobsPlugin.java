package eu.hiddenite.jobs;

import eu.hiddenite.jobs.jobs.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JobsPlugin extends JavaPlugin {
    private Database database;
    public Database getDatabase() {
        return database;
    }

    private NaturalBlocksManager naturalBlocksManager;
    public NaturalBlocksManager getNaturalBlocksManager() {
        return naturalBlocksManager;
    }

    private ExperienceManager experienceManager;
    public ExperienceManager getExperienceManager() {
        return experienceManager;
    }

    private List<Job> jobs = new ArrayList<>();
    public List<Job> getJobs() {
        return jobs;
    }

    private JobsMenuManager jobsMenuManager;

    private YamlConfiguration translationConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadTranslations();

        database = new Database(getConfig(), getLogger());
        if (!database.open()) {
            getLogger().warning("Could not connect to the database. Plugin not enabled.");
            return;
        }

        naturalBlocksManager = new NaturalBlocksManager(this);
        experienceManager = new ExperienceManager(this);
        jobsMenuManager = new JobsMenuManager(this);

        jobs = new ArrayList<>(Arrays.asList(
                new WoodcuttingJob(this),
                new MiningJob(this),
                new FarmingJob(this),
                new HuntingJob(this),
                new FishingJob(this)
                // new EnchantingJob(this)
        ));
    }

    @Override
    public void onDisable() {
        if (experienceManager != null) {
            experienceManager.close();
        }
        if (jobsMenuManager != null) {
            jobsMenuManager.close();
        }
        database.close();
    }

    private void loadTranslations() {
        String localeFilename = "messages_" + getConfig().getString("language") + ".yml";
        try (InputStream stream = getResource(localeFilename)) {
            if (stream != null) {
                try (InputStreamReader reader = new InputStreamReader(stream)) {
                    translationConfig = YamlConfiguration.loadConfiguration(reader);
                    getLogger().info("Loaded internal translation file: " + localeFilename);
                }
            } else {
                getLogger().warning("Missing internal translation file: " + localeFilename);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage(String configPath) {
        String defaultValue = Objects.toString(getConfig().getString(configPath), "");
        if (translationConfig != null) {
            return translationConfig.getString(configPath, defaultValue);
        } else {
            return defaultValue;
        }
    }

    public String formatMessage(String key, Object... parameters) {
        String msg = getMessage(key);
        for (int i = 0; i < parameters.length - 1; i += 2) {
            msg = msg.replace(parameters[i].toString(), parameters[i + 1].toString());
        }
        return msg;
    }

    public Component formatComponent(String key, Object... parameters) {
        return LegacyComponentSerializer.legacySection().deserialize(formatMessage(key, parameters))
                .decoration(TextDecoration.ITALIC, false);
    }

    public List<Component> formatComponents(String key, Object... parameters) {
        String message = formatMessage(key, parameters);
        return Arrays.stream(message.split("\n"))
                .map(x -> LegacyComponentSerializer.legacySection().deserialize(x)
                        .decoration(TextDecoration.ITALIC, false))
                .collect(Collectors.toList());
    }

    public void sendActionBar(Player player, String key, Object... parameters) {
        Component component = formatComponent(key, parameters);
        player.sendActionBar(component);
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> player.sendActionBar(component), 40);
    }
}
