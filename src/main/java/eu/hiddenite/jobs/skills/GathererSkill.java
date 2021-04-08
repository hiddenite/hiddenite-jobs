package eu.hiddenite.jobs.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Random;

public class GathererSkill extends Skill {
    private final HashSet<Material> allowedMaterials;
    private final Random random = new Random();

    public GathererSkill(int level, HashSet<Material> allowedMaterials) {
        super(level);
        this.allowedMaterials = allowedMaterials;
    }

    @Override
    public String getType() {
        return "gatherer";
    }

    @Override
    public double getBonus(int level) {
        return (double)Math.min(100, level) / 100.0;
    }

    public void apply(ItemStack item, int level) {
        if (level < getRequiredLevel()) {
            return;
        }
        double chance = getBonus(level);
        if (random.nextDouble() >= chance) {
            return;
        }
        if (allowedMaterials != null && !allowedMaterials.contains(item.getType())) {
            return;
        }
        if (item.getAmount() * 2 > item.getType().getMaxStackSize()) {
            return;
        }
        item.setAmount(item.getAmount() * 2);
    }
}
