package eu.hiddenite.jobs;

import eu.hiddenite.jobs.helpers.BlockPosition;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class NaturalBlocksManager implements Listener {
    private final JobsPlugin plugin;
    private final HashMap<UUID, HashSet<BlockPosition>> dirtyBlocks = new HashMap<>();

    public NaturalBlocksManager(JobsPlugin plugin) {
        this.plugin = plugin;

        registerWorld("world");
        registerWorld("world_nether");
        registerWorld("world_the_end");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void registerWorld(String worldName) {
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World " + worldName + " not found!");
            return;
        }
        dirtyBlocks.put(world.getUID(), new HashSet<>());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        markAsDirty(event.getBlock());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        markAsDirty(event.getBlock());
        if (event.getBlock().getBlockData() instanceof Bisected) {
            markAsDirty(event.getBlock().getRelative(BlockFace.UP));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        handlePistonEvent(event.getBlocks(), event.getDirection());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        handlePistonEvent(event.getBlocks(), event.getDirection());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onStructureGrow(StructureGrowEvent event) {
        for (BlockState blockState : event.getBlocks()) {
            markAsNatural(blockState.getBlock());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockFertilize(BlockFertilizeEvent event) {
        for (BlockState blockState : event.getBlocks()) {
            markAsNatural(blockState.getBlock());
        }
    }

    private void handlePistonEvent(List<Block> blocks, BlockFace direction) {
        for (Block block : blocks) {
            markAsDirty(block);
            markAsDirty(block.getRelative(direction));
        }
    }

    public boolean isNatural(Block block) {
        if (!dirtyBlocks.containsKey(block.getWorld().getUID())) {
            return false;
        }
        return !dirtyBlocks.get(block.getWorld().getUID()).contains(new BlockPosition(block));
    }

    public void markAsDirty(Block block) {
        if (!dirtyBlocks.containsKey(block.getWorld().getUID())) {
            return;
        }
        dirtyBlocks.get(block.getWorld().getUID()).add(new BlockPosition(block));
    }

    public void markAsNatural(Block block) {
        if (!dirtyBlocks.containsKey(block.getWorld().getUID())) {
            return;
        }
        dirtyBlocks.get(block.getWorld().getUID()).remove(new BlockPosition(block));
    }
}
