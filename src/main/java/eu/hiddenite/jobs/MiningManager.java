package eu.hiddenite.jobs;

import eu.hiddenite.jobs.helpers.BlockPosition;
import eu.hiddenite.jobs.skills.CarefulSkill;
import eu.hiddenite.jobs.skills.GathererSkill;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;

import java.util.HashMap;

public class MiningManager implements Listener {
    public static final String JOB_TYPE = "mining";

    private final JobsPlugin plugin;
    private final HashMap<Material, Integer> expPerMaterial = new HashMap<>();

    private BlockPosition lastBrokenBlock = null;

    public MiningManager(JobsPlugin plugin) {
        this.plugin = plugin;

        loadMaterials();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void loadMaterials() {
        expPerMaterial.put(Material.COAL_ORE, 10);
        expPerMaterial.put(Material.IRON_ORE, 15);
        expPerMaterial.put(Material.REDSTONE_ORE, 15);
        expPerMaterial.put(Material.LAPIS_ORE, 20);
        expPerMaterial.put(Material.GOLD_ORE, 20);
        expPerMaterial.put(Material.DIAMOND_ORE, 40);
        expPerMaterial.put(Material.EMERALD_ORE, 80);

        expPerMaterial.put(Material.NETHER_GOLD_ORE, 20);
        expPerMaterial.put(Material.NETHER_QUARTZ_ORE, 20);
        expPerMaterial.put(Material.ANCIENT_DEBRIS, 140);
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
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (!new BlockPosition(event.getBlock()).equals(lastBrokenBlock)) {
            return;
        }

        int level = plugin.getExperienceManager().getPlayerLevel(event.getPlayer(), JOB_TYPE);
        for (Item item : event.getItems()) {
            GathererSkill.apply(item.getItemStack(), level);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        int level = plugin.getExperienceManager().getPlayerLevel(event.getPlayer(), JOB_TYPE);

        if (CarefulSkill.shouldApply(event.getItem(), level, JOB_TYPE)) {
            event.setCancelled(true);
        }
    }
}
