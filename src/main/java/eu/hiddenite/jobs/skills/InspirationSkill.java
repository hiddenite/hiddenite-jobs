package eu.hiddenite.jobs.skills;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class InspirationSkill extends Skill {
    public InspirationSkill(int requiredLevel) {
        super(requiredLevel);
    }

    @Override
    public String getType() {
        return "inspiration";
    }

    @Override
    public double getTime(int level) {
        return 5.0 + (double)(Math.min(100, level) - getRequiredLevel()) / 7.0;
    }

    public void apply(Player player, int level) {
        if (level < getRequiredLevel()) {
            return;
        }
        int ticks = (int)Math.round(getTime(level) * 20.0);
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.FAST_DIGGING, ticks, 3, false, false, true
        ));
    }
}
