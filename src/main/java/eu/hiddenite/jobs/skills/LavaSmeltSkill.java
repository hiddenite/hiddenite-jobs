package eu.hiddenite.jobs.skills;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class LavaSmeltSkill extends Skill {
    public LavaSmeltSkill(int requiredLevel) {
        super(requiredLevel);
    }

    @Override
    public String getType() {
        return "lava-smelt";
    }

    public void apply(PlayerInventory inventory, ItemStack itemStack, int level) {
        if (level < getRequiredLevel()) {
            return;
        }
        if (inventory.getItemInMainHand().getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0) {
            return;
        }
        if (!inventory.contains(Material.LAVA_BUCKET)) {
            return;
        }
        if (itemStack.getType().equals(Material.RAW_IRON)) {
            itemStack.setType(Material.IRON_INGOT);
        }
        if (itemStack.getType().equals(Material.RAW_GOLD)) {
            itemStack.setType(Material.GOLD_INGOT);
        }
    }
}
