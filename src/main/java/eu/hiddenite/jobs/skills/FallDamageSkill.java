package eu.hiddenite.jobs.skills;

import org.bukkit.event.entity.EntityDamageEvent;

public class FallDamageSkill extends Skill {
    public FallDamageSkill(int requiredLevel) {
        super(requiredLevel);
    }

    @Override
    public String getType() {
        return "fall-damage";
    }

    @Override
    public double getBonus(int level) {
        return (double)Math.min(100, level) / 200.0;
    }

    public void handleEvent(EntityDamageEvent event, int level) {
        if (level < getRequiredLevel()) {
            return;
        }

        if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            event.setDamage(event.getDamage() * (1.0 - getBonus(level)));
        }
    }
}
