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

public class WoodcuttingManager implements Listener {
    public static final String JOB_TYPE = "woodcutting";

    private final JobsPlugin plugin;
    private final HashMap<Material, Integer> expPerMaterial = new HashMap<>();

    private BlockPosition lastBrokenBlock = null;

    public WoodcuttingManager(JobsPlugin plugin) {
        this.plugin = plugin;

        loadMaterials();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void loadMaterials() {
        expPerMaterial.put(Material.ACACIA_LOG, 10);
        expPerMaterial.put(Material.BIRCH_LOG, 10);
        expPerMaterial.put(Material.DARK_OAK_LOG, 10);
        expPerMaterial.put(Material.JUNGLE_LOG, 10);
        expPerMaterial.put(Material.OAK_LOG, 10);
        expPerMaterial.put(Material.SPRUCE_LOG, 10);

        expPerMaterial.put(Material.CRIMSON_STEM, 20);
        expPerMaterial.put(Material.WARPED_STEM, 20);

        expPerMaterial.put(Material.ACACIA_LEAVES, 1);
        expPerMaterial.put(Material.BIRCH_LEAVES, 1);
        expPerMaterial.put(Material.DARK_OAK_LEAVES, 1);
        expPerMaterial.put(Material.JUNGLE_LEAVES, 1);
        expPerMaterial.put(Material.OAK_LEAVES, 1);
        expPerMaterial.put(Material.SPRUCE_LEAVES, 1);
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
