package eu.hiddenite.jobs.skills;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class MineSpawnerSkill extends Skill {
    public MineSpawnerSkill(int requiredLevel) {
        super(requiredLevel);
    }

    @Override
    public String getType() {
        return "mine-spawner";
    }

    public void handleBreak(BlockBreakEvent event, int level) {
        if (level < getRequiredLevel()) {
            return;
        }

        Block block = event.getBlock();
        ItemStack item = new ItemStack(Material.SPAWNER, 1);
        BlockStateMeta meta = (BlockStateMeta)item.getItemMeta();
        meta.setBlockState(block.getState());
        item.setItemMeta(meta);

        block.getWorld().dropItemNaturally(block.getLocation().toCenterLocation(), item);

        event.setDropItems(false);
        event.setExpToDrop(0);
    }

    public void handlePlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!item.getType().equals(Material.SPAWNER)) {
            event.setCancelled(true);
            return;
        }

        EntityType type = ((CreatureSpawner)((BlockStateMeta)item.getItemMeta()).getBlockState()).getSpawnedType();

        Block block = event.getBlockPlaced();
        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        spawner.setSpawnedType(type);
        spawner.update();
    }
}
