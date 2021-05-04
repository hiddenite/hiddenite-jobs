package eu.hiddenite.jobs.jobs;

import eu.hiddenite.jobs.JobsPlugin;
import eu.hiddenite.jobs.helpers.MaterialTypes;
import eu.hiddenite.jobs.skills.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HuntingJob extends Job implements Listener {
    public static final String JOB_TYPE = "hunting";

    private final JobsPlugin plugin;
    private final HashMap<EntityType, Integer> expPerEntity = new HashMap<>();

    private final CarefulSkill careful = new CarefulSkill(1,
            MaterialTypes.merge(MaterialTypes.SWORDS, MaterialTypes.BOWS));
    private final EggDropSkill eggDrop = new EggDropSkill(10);
    private final FallDamageSkill fallDamage = new FallDamageSkill(30);
    private final MineSpawnerSkill mineSpawner = new MineSpawnerSkill(50);
    private final InspirationSkill inspiration = new InspirationSkill(60,
            PotionEffectType.INCREASE_DAMAGE, 0);
    private final ExplosionSurvivorSkill explosionSurvivor = new ExplosionSurvivorSkill(70);

    private final List<Skill> skills = new ArrayList<>(Arrays.asList(
            careful,
            eggDrop,
            fallDamage,
            mineSpawner,
            inspiration,
            explosionSurvivor
    ));

    public HuntingJob(JobsPlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Passive mobs
        expPerEntity.put(EntityType.BAT, 1);
        expPerEntity.put(EntityType.CAT, 1);
        expPerEntity.put(EntityType.CHICKEN, 5);
        expPerEntity.put(EntityType.COD, 1);
        expPerEntity.put(EntityType.COW, 5);
        expPerEntity.put(EntityType.DONKEY, 5);
        expPerEntity.put(EntityType.FOX, 10);
        expPerEntity.put(EntityType.HORSE, 1);
        expPerEntity.put(EntityType.MUSHROOM_COW, 10);
        expPerEntity.put(EntityType.MULE, 0);
        expPerEntity.put(EntityType.OCELOT, 1);
        expPerEntity.put(EntityType.PARROT, 1);
        expPerEntity.put(EntityType.PIG, 5);
        expPerEntity.put(EntityType.PUFFERFISH, 0);
        expPerEntity.put(EntityType.RABBIT, 5);
        expPerEntity.put(EntityType.SALMON, 1);
        expPerEntity.put(EntityType.SHEEP, 5);
        expPerEntity.put(EntityType.SKELETON_HORSE, 0);
        expPerEntity.put(EntityType.SNOWMAN, 1);
        expPerEntity.put(EntityType.SQUID, 5);
        expPerEntity.put(EntityType.STRIDER, 10);
        expPerEntity.put(EntityType.TROPICAL_FISH, 0);
        expPerEntity.put(EntityType.TURTLE, 5);

        // Neutral mobs
        expPerEntity.put(EntityType.BEE, 5);
        expPerEntity.put(EntityType.CAVE_SPIDER, 30);
        expPerEntity.put(EntityType.DOLPHIN, 5);
        expPerEntity.put(EntityType.ENDERMAN, 25);
        expPerEntity.put(EntityType.IRON_GOLEM, 15);
        expPerEntity.put(EntityType.LLAMA, 5);
        expPerEntity.put(EntityType.PIGLIN, 10);
        expPerEntity.put(EntityType.PANDA, 5);
        expPerEntity.put(EntityType.POLAR_BEAR, 10);
        expPerEntity.put(EntityType.SPIDER, 10);
        expPerEntity.put(EntityType.WOLF, 10);
        expPerEntity.put(EntityType.ZOMBIFIED_PIGLIN, 15);

        // Hostile mobs
        expPerEntity.put(EntityType.BLAZE, 30);
        expPerEntity.put(EntityType.CREEPER, 15);
        expPerEntity.put(EntityType.DROWNED, 15);
        expPerEntity.put(EntityType.ELDER_GUARDIAN, 250);
        expPerEntity.put(EntityType.ENDERMITE, 30);
        expPerEntity.put(EntityType.EVOKER, 30);
        expPerEntity.put(EntityType.GHAST, 60);
        expPerEntity.put(EntityType.GUARDIAN, 50);
        expPerEntity.put(EntityType.HOGLIN, 20);
        expPerEntity.put(EntityType.HUSK, 15);
        expPerEntity.put(EntityType.MAGMA_CUBE, 5);
        expPerEntity.put(EntityType.PHANTOM, 15);
        expPerEntity.put(EntityType.PIGLIN_BRUTE, 40);
        expPerEntity.put(EntityType.PILLAGER, 25);
        expPerEntity.put(EntityType.RAVAGER, 150);
        expPerEntity.put(EntityType.SHULKER, 60);
        expPerEntity.put(EntityType.SILVERFISH, 5);
        expPerEntity.put(EntityType.SKELETON, 10);
        expPerEntity.put(EntityType.SLIME, 5);
        expPerEntity.put(EntityType.STRAY, 15);
        expPerEntity.put(EntityType.VEX, 5);
        expPerEntity.put(EntityType.VINDICATOR, 40);
        expPerEntity.put(EntityType.WITCH, 30);
        expPerEntity.put(EntityType.WITHER_SKELETON, 25);
        expPerEntity.put(EntityType.ZOGLIN, 20);
        expPerEntity.put(EntityType.ZOMBIE, 10);
        expPerEntity.put(EntityType.ZOMBIE_VILLAGER, 10);
    }

    @Override
    public String getType() {
        return JOB_TYPE;
    }

    @Override
    public List<Skill> getSkills() {
        return skills;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        int level = plugin.getExperienceManager().getPlayerLevel(event.getPlayer(), JOB_TYPE);

        if (careful.shouldApply(event.getItem(), level)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }

        Player player = event.getEntity().getKiller();

        int exp = expPerEntity.getOrDefault(event.getEntityType(), 0);
        if (event.getEntityType() == EntityType.ENDERMAN && player.getWorld().getEnvironment() == World.Environment.THE_END) {
            exp = 3;
        }

        int level = plugin.getExperienceManager().getPlayerLevel(player, JOB_TYPE);
        eggDrop.apply(event, level);

        if (exp == 0) {
            return;
        }

        inspiration.apply(player, level);
        plugin.getExperienceManager().gainExp(player, JOB_TYPE, exp);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType().equals(Material.SPAWNER)) {
            int level = plugin.getExperienceManager().getPlayerLevel(event.getPlayer(), JOB_TYPE);
            mineSpawner.handleBreak(event, level);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType().equals(Material.SPAWNER)) {
            mineSpawner.handlePlace(event);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player)event.getEntity();
        int level = plugin.getExperienceManager().getPlayerLevel(player, JOB_TYPE);

        fallDamage.handleEvent(event, level);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player)event.getEntity();
        int level = plugin.getExperienceManager().getPlayerLevel(player, JOB_TYPE);

        explosionSurvivor.handleEvent(event, level);
    }
}
