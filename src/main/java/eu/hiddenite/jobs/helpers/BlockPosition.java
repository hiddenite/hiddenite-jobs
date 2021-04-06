package eu.hiddenite.jobs.helpers;

import org.bukkit.block.Block;

public class BlockPosition {
    int x;
    int y;
    int z;

    public BlockPosition(Block block) {
        this(block.getX(), block.getY(), block.getZ());
    }

    public BlockPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlockPosition that = (BlockPosition) o;
        return x == that.x && z == that.z && y == that.y;
    }

    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + x;
        hash = hash * 31 + y;
        hash = hash * 31 + z;
        return hash;
    }
}
