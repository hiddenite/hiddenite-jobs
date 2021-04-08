package eu.hiddenite.jobs.skills;

import eu.hiddenite.jobs.NaturalBlocksManager;
import eu.hiddenite.jobs.helpers.BlockPosition;
import eu.hiddenite.jobs.helpers.MaterialTypes;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CalculatorSkill extends Skill {
    private final int rank;

    public CalculatorSkill(int requiredLevel, int rank) {
        super(requiredLevel);
        this.rank = rank;
    }

    @Override
    public String getType() {
        return "calculator" + rank;
    }

    @Override
    public int getCooldown(int level) {
        return 110 + rank * 10 - Math.min(100, level);
    }

    public boolean isValidTreeSize(int treeSize) {
        if (rank == 1) return treeSize >= 1 && treeSize <= 8;
        if (rank == 2) return treeSize >= 9 && treeSize <= 16;
        if (rank == 3) return treeSize >= 17 && treeSize <= 64;
        return false;
    }

    public static List<Block> computeTree(NaturalBlocksManager naturalBlocks, Block block) {
        if (!MaterialTypes.LOGS.contains(block.getType())) {
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
