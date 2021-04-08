package eu.hiddenite.jobs.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Random;

public class CarefulSkill extends Skill {
    private final Random random = new Random();
    private final HashSet<Material> tools;

    public CarefulSkill(int requiredLevel, HashSet<Material> tools) {
        super(requiredLevel);
        this.tools = tools;
    }

    @Override
    public String getType() {
        return "careful";
    }

    @Override
    public double getBonus(int level) {
        return (double)Math.min(100, level) / 100.0 / 2.0;
    }

    public boolean shouldApply(ItemStack item, int level) {
        if (level < getRequiredLevel()) {
            return false;
        }
        double chance = getBonus(level);
        if (random.nextDouble() >= chance) {
            return false;
        }
        return tools.contains(item.getType());
    }
}
