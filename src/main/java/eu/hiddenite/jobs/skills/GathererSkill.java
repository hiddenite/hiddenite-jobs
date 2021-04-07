package eu.hiddenite.jobs.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class GathererSkill {
    public static final String NAME = "gatherer";
    public static final int REQUIRED_LEVEL = 5;

    private static final Random random = new Random();

    private static final HashSet<Material> allowedMaterials = new HashSet<>(Arrays.asList(
            Material.ACACIA_LOG,
            Material.BIRCH_LOG,
            Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG,
            Material.OAK_LOG,
            Material.SPRUCE_LOG,
            Material.CRIMSON_STEM,
            Material.WARPED_STEM,

            Material.COAL_ORE,
            Material.IRON_ORE,
            Material.REDSTONE_ORE,
            Material.LAPIS_ORE,
            Material.GOLD_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE,

            Material.COAL,
            Material.IRON_NUGGET,
            Material.REDSTONE,
            Material.LAPIS_LAZULI,
            Material.GOLD_NUGGET,
            Material.DIAMOND,
            Material.EMERALD,

            Material.NETHER_GOLD_ORE,
            Material.NETHER_QUARTZ_ORE,
            Material.ANCIENT_DEBRIS,

            Material.COD,
            Material.SALMON,
            Material.TROPICAL_FISH,
            Material.PUFFERFISH
    ));

    public static double getChance(int level) {
        return (double)Math.min(100, level) / 100.0;
    }

    public static void apply(ItemStack item, int level) {
        if (level < REQUIRED_LEVEL) {
            return;
        }
        double chance = getChance(level);
        if (random.nextDouble() >= chance) {
            return;
        }
        if (!allowedMaterials.contains(item.getType())) {
            return;
        }
        if (item.getAmount() > 5) {
            return;
        }
        item.setAmount(item.getAmount() * 2);
    }
}
