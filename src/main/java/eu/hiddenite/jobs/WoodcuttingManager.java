package eu.hiddenite.jobs;

import eu.hiddenite.jobs.helpers.BlockPosition;
import eu.hiddenite.jobs.skills.CalculatorSkill;
import eu.hiddenite.jobs.skills.CarefulSkill;
import eu.hiddenite.jobs.skills.GathererSkill;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class WoodcuttingManager implements Listener {
    public static final String JOB_TYPE = "woodcutting";

    private final JobsPlugin plugin;
    private final HashMap<Material, Integer> expPerMaterial = new HashMap<>();
    private final HashMap<UUID, Long> skillCooldown = new HashMap<>();

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

        if (tryToApplyCalculatorSkill(event, expPerBlock)) {
            return;
        }

        plugin.getExperienceManager().gainExp(event.getPlayer(), JOB_TYPE, expPerBlock);
        lastBrokenBlock = new BlockPosition(event.getBlock());
    }

    private boolean tryToApplyCalculatorSkill(BlockBreakEvent event, int expPerBlock) {
        Player player = event.getPlayer();
        int level = plugin.getExperienceManager().getPlayerLevel(player, JOB_TYPE);
        long now = System.currentTimeMillis();
        boolean isCooldownActive = skillCooldown.getOrDefault(player.getUniqueId(), 0L) > now;

        if (isCooldownActive) {
            return false;
        }

        List<Block> tree = CalculatorSkill.computeTree(plugin.getNaturalBlocksManager(), event.getBlock());
        if (tree == null || tree.size() == 0) {
            return false;
        }

        int requiredRank = CalculatorSkill.getRequiredRankForTreeSize(tree.size());
        if (requiredRank == -1) {
            return false;
        }

        int requiredLevel = CalculatorSkill.getRequiredLevelForRank(requiredRank);
        if (level < requiredLevel) {
            return false;
        }

        event.setCancelled(true);
        skillCooldown.put(player.getUniqueId(), now + CalculatorSkill.getCooldown(level, requiredRank) * 1000);

        plugin.getExperienceManager().gainExp(event.getPlayer(), JOB_TYPE, expPerBlock * tree.size());
        for (Block block : tree) {
            for (ItemStack drop : block.getDrops(event.getPlayer().getInventory().getItemInMainHand())) {
                GathererSkill.apply(drop, level);
                block.getWorld().dropItemNaturally(block.getLocation().toCenterLocation(), drop);
            }
            block.setType(Material.AIR);
        }
        return true;
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
