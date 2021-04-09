package eu.hiddenite.jobs.skills;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class LavaResistanceSkill extends Skill {
    private final HashMap<UUID, Long> cooldown = new HashMap<>();

    public LavaResistanceSkill(int requiredLevel) {
        super(requiredLevel);
    }

    @Override
    public String getType() {
        return "lava-resistance";
    }

    @Override
    public double getTime(int level) {
        return 2.0 + (double)Math.min(100, level) / 10.0;
    }

    @Override
    public int getCooldown(int level) {
        return 300 - Math.min(100, level) * 2;
    }

    public void apply(Player player, int level) {
        if (level < getRequiredLevel()) {
            return;
        }

        long now = System.currentTimeMillis();
        boolean isCooldownActive = cooldown.getOrDefault(player.getUniqueId(), 0L) > now;
        if (isCooldownActive) {
            return;
        }

        cooldown.put(player.getUniqueId(), now + getCooldown(level) * 1000);

        int ticks = (int)Math.round(getTime(level) * 20.0);
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.FIRE_RESISTANCE, ticks, 4, false, false, true
        ));
    }
}
