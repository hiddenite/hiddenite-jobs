package eu.hiddenite.jobs.helpers;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class MaterialTypes {
    public static HashSet<Material> merge(HashSet<Material> a, HashSet<Material> b) {
        HashSet<Material> result = new HashSet<>();
        result.addAll(a);
        result.addAll(b);
        return result;
    }

    public static final HashSet<Material> AXES = new HashSet<>(Arrays.asList(
            Material.WOODEN_AXE,
            Material.STONE_AXE,
            Material.IRON_AXE,
            Material.GOLDEN_AXE,
            Material.DIAMOND_AXE,
            Material.NETHERITE_AXE
    ));

    public static final HashSet<Material> PICKAXES = new HashSet<>(Arrays.asList(
            Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE
    ));

    public static final HashSet<Material> HOES = new HashSet<>(Arrays.asList(
            Material.WOODEN_HOE,
            Material.STONE_HOE,
            Material.IRON_HOE,
            Material.GOLDEN_HOE,
            Material.DIAMOND_HOE,
            Material.NETHERITE_HOE
    ));

    public static final HashSet<Material> FISHING_RODS = new HashSet<>(Collections.singletonList(
            Material.FISHING_ROD
    ));

    public static final HashSet<Material> LEAVES = new HashSet<>(Arrays.asList(
            Material.ACACIA_LEAVES,
            Material.BIRCH_LEAVES,
            Material.JUNGLE_LEAVES,
            Material.OAK_LEAVES,
            Material.SPRUCE_LEAVES,
            Material.DARK_OAK_LEAVES
    ));

    public static final HashSet<Material> LOGS = new HashSet<>(Arrays.asList(
            Material.ACACIA_LOG,
            Material.BIRCH_LOG,
            Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG,
            Material.OAK_LOG,
            Material.SPRUCE_LOG,
            Material.CRIMSON_STEM,
            Material.WARPED_STEM
    ));

    public static final HashSet<Material> ORES = new HashSet<>(Arrays.asList(
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
            Material.ANCIENT_DEBRIS
    ));

    public static final HashSet<Material> FISH = new HashSet<>(Arrays.asList(
            Material.COD,
            Material.SALMON,
            Material.TROPICAL_FISH,
            Material.PUFFERFISH
    ));

    public static final HashSet<Material> CROP_TILES = new HashSet<>(Arrays.asList(
            Material.WHEAT,
            Material.BEETROOTS,
            Material.CARROTS,
            Material.POTATOES
    ));

    public static final HashSet<Material> CROP_ITEMS = new HashSet<>(Arrays.asList(
            Material.WHEAT,
            Material.WHEAT_SEEDS,
            Material.POTATO,
            Material.CARROT,
            Material.BEETROOT,
            Material.BEETROOT_SEEDS
    ));

    public static final HashSet<Material> FLOWERS = new HashSet<>(Arrays.asList(
            Material.DANDELION,
            Material.POPPY,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.ORANGE_TULIP,
            Material.PINK_TULIP,
            Material.RED_TULIP,
            Material.WHITE_TULIP,
            Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY,
            Material.SUNFLOWER,
            Material.LILAC,
            Material.PEONY,
            Material.ROSE_BUSH
    ));

    public static final HashSet<Material> GRASS = new HashSet<>(Arrays.asList(
            Material.GRASS,
            Material.TALL_GRASS,
            Material.FERN,
            Material.LARGE_FERN
    ));
}
