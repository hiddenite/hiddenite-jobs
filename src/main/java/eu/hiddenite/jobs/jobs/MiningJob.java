package eu.hiddenite.jobs.jobs;

import eu.hiddenite.jobs.JobsPlugin;
import eu.hiddenite.jobs.helpers.BlockPosition;
import eu.hiddenite.jobs.helpers.MaterialTypes;
import eu.hiddenite.jobs.skills.*;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MiningJob extends Job implements Listener {
    public static final String JOB_TYPE = "mining";

    private final JobsPlugin plugin;
    private final HashMap<Material, Integer> expPerMaterial = new HashMap<>();

    private final CarefulSkill careful = new CarefulSkill(1, MaterialTypes.PICKAXES);
    private final GathererSkill gatherer = new GathererSkill(5, MaterialTypes.ORES);
    private final LavaSmeltSkill lavaSmelt = new LavaSmeltSkill(20);
    private final InspirationSkill inspiration = new InspirationSkill(30,
            PotionEffectType.FAST_DIGGING, 1);
    private final LavaResistanceSkill lavaResistance = new LavaResistanceSkill(40);

    private final List<Skill> skills = new ArrayList<>(Arrays.asList(
            careful,
            gatherer,
            lavaSmelt,
            inspiration,
            lavaResistance
    ));

    private BlockPosition lastBrokenBlock = null;

    public MiningJob(JobsPlugin plugin) {
        this.plugin = plugin;

        loadMaterials();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getType() {
        return JOB_TYPE;
    }

    @Override
    public List<Skill> getSkills() {
        return skills;
    }

    private void loadMaterials() {
        expPerMaterial.put(Material.COAL_ORE, 3);
        expPerMaterial.put(Material.COPPER_ORE, 5);
        expPerMaterial.put(Material.IRON_ORE, 5);
        expPerMaterial.put(Material.REDSTONE_ORE, 5);
        expPerMaterial.put(Material.LAPIS_ORE, 10);
        expPerMaterial.put(Material.GOLD_ORE, 10);
        expPerMaterial.put(Material.DIAMOND_ORE, 30);
        expPerMaterial.put(Material.EMERALD_ORE, 50);

        expPerMaterial.put(Material.NETHER_GOLD_ORE, 10);
        expPerMaterial.put(Material.NETHER_QUARTZ_ORE, 8);
        expPerMaterial.put(Material.ANCIENT_DEBRIS, 80);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        int expPerBlock = expPerMaterial.getOrDefault(event.getBlock().getType(), 0);
        if (expPerBlock == 0) {
            return;
        }

        if (!plugin.getNaturalBlocksManager().isNatural(event.getBlock())) {
            return;
        }

        plugin.getExperienceManager().gainExp(event.getPlayer(), JOB_TYPE, expPerBlock);
        lastBrokenBlock = new BlockPosition(event.getBlock());

        int level = plugin.getExperienceManager().getPlayerLevel(event.getPlayer(), JOB_TYPE);
        inspiration.apply(event.getPlayer(), level);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (!new BlockPosition(event.getBlock()).equals(lastBrokenBlock)) {
            return;
        }

        int level = plugin.getExperienceManager().getPlayerLevel(event.getPlayer(), JOB_TYPE);
        for (Item item : event.getItems()) {
            gatherer.apply(item.getItemStack(), level);
            lavaSmelt.apply(event.getPlayer().getInventory(), item.getItemStack(), level);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        int level = plugin.getExperienceManager().getPlayerLevel(event.getPlayer(), JOB_TYPE);

        if (careful.shouldApply(event.getItem(), level)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.LAVA) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        int level = plugin.getExperienceManager().getPlayerLevel(player, JOB_TYPE);
        lavaResistance.apply(player, level);
    }
}
