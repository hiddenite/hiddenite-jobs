package eu.hiddenite.jobs.skills;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LavaResistanceSkill extends CooldownSkill {
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

        if (!applyCooldown(player.getUniqueId(), level)) {
            return;
        }

        int ticks = (int)Math.round(getTime(level) * 20.0);
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.FIRE_RESISTANCE, ticks, 4, false, false, true
        ));
    }
}
