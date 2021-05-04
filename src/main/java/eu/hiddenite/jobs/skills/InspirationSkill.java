package eu.hiddenite.jobs.skills;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class InspirationSkill extends CooldownSkill {
    private final PotionEffectType effectType;
    private final int amplifier;

    public InspirationSkill(int requiredLevel, PotionEffectType effectType, int amplifier) {
        super(requiredLevel);
        this.effectType = effectType;
        this.amplifier = amplifier;
    }

    @Override
    public String getType() {
        return "inspiration";
    }

    @Override
    public double getTime(int level) {
        double timePerLevel = 0.1;
        if (effectType == PotionEffectType.INCREASE_DAMAGE) {
            timePerLevel = 0.05;
        }
        return (double)Math.min(100, level) * timePerLevel;
    }

    @Override
    public int getCooldown(int level) {
        return (int)Math.ceil(getTime(level));
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
                effectType, ticks, amplifier, false, false, true
        ));
    }
}
