package eu.hiddenite.jobs.skills;

import eu.hiddenite.jobs.jobs.FishingManager;
import eu.hiddenite.jobs.jobs.MiningManager;
import eu.hiddenite.jobs.jobs.WoodcuttingManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class CarefulSkill {
    public static final String NAME = "careful";
    public static final int REQUIRED_LEVEL = 1;

    private static final Random random = new Random();

    private static final HashSet<Material> axes = new HashSet<>(Arrays.asList(
            Material.WOODEN_AXE,
            Material.STONE_AXE,
            Material.IRON_AXE,
            Material.GOLDEN_AXE,
            Material.DIAMOND_AXE,
            Material.NETHERITE_AXE
    ));

    private static final HashSet<Material> pickaxes = new HashSet<>(Arrays.asList(
            Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE
    ));

    public static double getChance(int level) {
        return (double)Math.min(100, level) / 100.0 / 2.0;
    }

    public static boolean shouldApply(ItemStack item, int level, String jobType) {
        if (level < REQUIRED_LEVEL) {
            return false;
        }
        double chance = getChance(level);
        if (random.nextDouble() >= chance) {
            return false;
        }
        if (jobType.equals(WoodcuttingManager.JOB_TYPE) && !axes.contains(item.getType())) {
            return false;
        }
        if (jobType.equals(MiningManager.JOB_TYPE) && !pickaxes.contains(item.getType())) {
            return false;
        }
        if (jobType.equals(FishingManager.JOB_TYPE) && item.getType() != Material.FISHING_ROD) {
            return false;
        }
        return true;
    }
}
