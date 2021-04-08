package eu.hiddenite.jobs.jobs;

import eu.hiddenite.jobs.JobsPlugin;
import eu.hiddenite.jobs.helpers.BlockPosition;
import eu.hiddenite.jobs.helpers.MaterialTypes;
import eu.hiddenite.jobs.skills.*;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FarmingJob extends Job implements Listener {
    public static final String JOB_TYPE = "farming";

    private final JobsPlugin plugin;

    private BlockPosition lastBrokenBlock = null;

    private final CarefulSkill careful = new CarefulSkill(1, MaterialTypes.HOES);
    private final GathererSkill gatherer = new GathererSkill(5, MaterialTypes.merge(MaterialTypes.CROP_ITEMS, MaterialTypes.FLOWERS));
    private final CropsFeatherSkill cropsFeather = new CropsFeatherSkill(30);

    private final List<Skill> skills = new ArrayList<>(Arrays.asList(
            careful,
            gatherer,
            cropsFeather
    ));

    public FarmingJob(JobsPlugin plugin) {
        this.plugin = plugin;

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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (MaterialTypes.CROP_TILES.contains(event.getBlock().getType())) {
            Ageable ageable = (Ageable)event.getBlock().getBlockData();
            if (ageable.getAge() == ageable.getMaximumAge()) {
                plugin.getExperienceManager().gainExp(event.getPlayer(), JOB_TYPE, event.getBlock().getType() == Material.WHEAT ? 15 : 10);
                lastBrokenBlock = new BlockPosition(event.getBlock());
            }
            return;
        }

        if (plugin.getNaturalBlocksManager().isNatural(event.getBlock())) {
            if (MaterialTypes.FLOWERS.contains(event.getBlock().getType())) {
                plugin.getExperienceManager().gainExp(event.getPlayer(), JOB_TYPE, 5);
                lastBrokenBlock = new BlockPosition(event.getBlock());
            } else if (event.getBlock().getType() == Material.GRASS) {
                plugin.getExperienceManager().gainExp(event.getPlayer(), JOB_TYPE, 1);
                lastBrokenBlock = new BlockPosition(event.getBlock());
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockFertilize(BlockFertilizeEvent event) {
        plugin.getExperienceManager().gainExp(event.getPlayer(), JOB_TYPE, 5);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }
        if (event.getClickedBlock() == null || !event.getClickedBlock().getType().equals(Material.FARMLAND)) {
            return;
        }
        int level = plugin.getExperienceManager().getPlayerLevel(event.getPlayer(), JOB_TYPE);
        if (level >= cropsFeather.getRequiredLevel()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (!new BlockPosition(event.getBlock()).equals(lastBrokenBlock)) {
            return;
        }

        int level = plugin.getExperienceManager().getPlayerLevel(event.getPlayer(), JOB_TYPE);
        for (Item item : event.getItems()) {
            gatherer.apply(item.getItemStack(), level);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        int level = plugin.getExperienceManager().getPlayerLevel(event.getPlayer(), JOB_TYPE);

        if (careful.shouldApply(event.getItem(), level)) {
            event.setCancelled(true);
        }
    }
}
