package eu.hiddenite.jobs.skills;

import eu.hiddenite.jobs.helpers.MaterialTypes;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class DefoliatorSkill extends Skill {
    public DefoliatorSkill(int requiredLevel) {
        super(requiredLevel);
    }

    @Override
    public String getType() {
        return "defoliator";
    }

    public boolean shouldApply(Block block, ItemStack tool, int level) {
        if (level < getRequiredLevel()) {
            return false;
        }
        if (!MaterialTypes.AXES.contains(tool.getType())) {
            return false;
        }
        return MaterialTypes.LEAVES.contains(block.getType());
    }
}
