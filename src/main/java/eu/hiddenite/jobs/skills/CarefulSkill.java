package eu.hiddenite.jobs.skills;

import eu.hiddenite.jobs.MiningManager;
import eu.hiddenite.jobs.WoodcuttingManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class CarefulSkill {
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
        return true;
    }
}
