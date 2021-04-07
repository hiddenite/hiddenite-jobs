package eu.hiddenite.jobs.skills;

import eu.hiddenite.jobs.NaturalBlocksManager;
import eu.hiddenite.jobs.helpers.BlockPosition;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class CalculatorSkill {
    public static final String NAME = "calculator";

    private static final HashSet<Material> logs = new HashSet<>(Arrays.asList(
            Material.ACACIA_LOG,
            Material.BIRCH_LOG,
            Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG,
            Material.OAK_LOG,
            Material.SPRUCE_LOG,
            Material.CRIMSON_STEM,
            Material.WARPED_STEM
    ));

    public static int getCooldown(int level, int rank) {
        return 110 + rank * 10 - Math.min(100, level);
    }

    public static int getRequiredRankForTreeSize(int treeSize) {
        if (treeSize <= 8) {
            return 1;
        }
        if (treeSize <= 16) {
            return 2;
        }
        if (treeSize <= 64) {
            return 3;
        }
        return -1;
    }

    public static int getRequiredLevelForRank(int rank) {
        switch (rank) {
            case 1: return 30;
            case 2: return 60;
            case 3: return 90;
        }
        return -1;
    }

    public static List<Block> computeTree(NaturalBlocksManager naturalBlocks, Block block) {
        if (!logs.contains(block.getType())) {
            return null;
        }
        List<Block> found = new ArrayList<>();
        searchRecursively(naturalBlocks, block, block.getType(), new HashSet<>(), found);
        return found;
    }

    private static void searchRecursively(NaturalBlocksManager naturalBlocks,
                                          Block block,
                                          Material expectedType,
                                          HashSet<BlockPosition> searched,
                                          List<Block> found) {
        if (block.getType() != expectedType) {
            return;
        }
        if (searched.contains(new BlockPosition(block))) {
            return;
        }
        searched.add(new BlockPosition(block));
        if (!naturalBlocks.isNatural(block)) {
            return;
        }

        found.add(block);

        for (int dx = -1; dx <= 1; ++dx) {
            for (int dy = -1; dy <= 1; ++dy) {
                for (int dz = -1; dz <= 1; ++dz) {
                    if (dx != 0 || dy != 0 || dz != 0) {
                        searchRecursively(naturalBlocks, block.getRelative(dx, dy, dz), expectedType, searched, found);
                    }
                }
            }
        }
    }
}
