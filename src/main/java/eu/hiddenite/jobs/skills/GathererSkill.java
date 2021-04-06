package eu.hiddenite.jobs.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class GathererSkill {
    private static final Random random = new Random();

    private static final HashSet<Material> allowedMaterials = new HashSet<>(Arrays.asList(
            Material.ACACIA_LOG,
            Material.BIRCH_LOG,
            Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG,
            Material.OAK_LOG,
            Material.SPRUCE_LOG,
            Material.CRIMSON_STEM,
            Material.WARPED_STEM
    ));

    public static double getChance(int level) {
        return (double)Math.min(100, level) / 100.0;
    }

    public static void apply(ItemStack item, int level) {
        double chance = getChance(level);
        if (random.nextDouble() >= chance) {
            return;
        }
        if (!allowedMaterials.contains(item.getType())) {
            return;
        }
        if (item.getAmount() != 1) {
            return;
        }
        item.setAmount(item.getAmount() + 1);
    }
}
