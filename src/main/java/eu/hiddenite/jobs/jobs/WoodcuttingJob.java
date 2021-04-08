package eu.hiddenite.jobs.jobs;

import eu.hiddenite.jobs.JobsPlugin;
import eu.hiddenite.jobs.helpers.BlockPosition;
import eu.hiddenite.jobs.helpers.MaterialTypes;
import eu.hiddenite.jobs.skills.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class WoodcuttingJob extends Job implements Listener {
    public static final String JOB_TYPE = "woodcutting";

    private final JobsPlugin plugin;
    private final HashMap<Material, Integer> expPerMaterial = new HashMap<>();
    private final HashMap<UUID, Long> skillCooldown = new HashMap<>();
    private final HashSet<UUID> defoliatedRecently = new HashSet<>();

    private final CarefulSkill careful = new CarefulSkill(1, MaterialTypes.AXES);
    private final GathererSkill gatherer = new GathererSkill(5, MaterialTypes.LOGS);
    private final CalculatorSkill calculator1 = new CalculatorSkill(30, 1);
    private final DefoliatorSkill defoliator = new DefoliatorSkill(40);
    private final CalculatorSkill calculator2 = new CalculatorSkill(60, 2);
    private final CalculatorSkill calculator3 = new CalculatorSkill(90, 3);

    private final List<Skill> skills = new ArrayList<>(Arrays.asList(
            careful,
            gatherer,
            calculator1,
            defoliator,
            calculator2,
            calculator3
    ));

    private BlockPosition lastBrokenBlock = null;

    public WoodcuttingJob(JobsPlugin plugin) {
        this.plugin = plugin;

        loadMaterials();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public String getType() {
        return JOB_TYPE;
    }

    @Override
    public List<Skill> getSkills() {
        return skills;
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

        CalculatorSkill skill = null;
        if (calculator1.isValidTreeSize(tree.size())) {
            skill = calculator1;
        }
        if (calculator2.isValidTreeSize(tree.size())) {
            skill = calculator2;
        }
        if (calculator3.isValidTreeSize(tree.size())) {
            skill = calculator3;
        }

        if (skill == null || level < skill.getRequiredLevel()) {
            return false;
        }

        event.setCancelled(true);
        skillCooldown.put(player.getUniqueId(), now + skill.getCooldown(level) * 1000);

        plugin.getExperienceManager().gainExp(event.getPlayer(), JOB_TYPE, expPerBlock * tree.size());
        for (Block block : tree) {
            for (ItemStack drop : block.getDrops(event.getPlayer().getInventory().getItemInMainHand())) {
                gatherer.apply(drop, level);
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
            gatherer.apply(item.getItemStack(), level);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        int level = plugin.getExperienceManager().getPlayerLevel(event.getPlayer(), JOB_TYPE);

        if (defoliatedRecently.remove(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        if (careful.shouldApply(event.getItem(), level)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        if (!plugin.getNaturalBlocksManager().isNatural(event.getBlock())) {
            return;
        }

        int level = plugin.getExperienceManager().getPlayerLevel(event.getPlayer(), JOB_TYPE);

        if (defoliator.shouldApply(event.getBlock(), event.getItemInHand(), level)) {
            event.setInstaBreak(true);
            defoliatedRecently.add(event.getPlayer().getUniqueId());
        }
    }
}
