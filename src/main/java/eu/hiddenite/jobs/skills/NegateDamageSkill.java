package eu.hiddenite.jobs.skills;

import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class NegateDamageSkill extends Skill {
    EntityDamageEvent.DamageCause damageCause;

    public NegateDamageSkill(int requiredLevel, EntityDamageEvent.DamageCause damageCause) {
        super(requiredLevel);
        this.damageCause = damageCause;
    }

    @Override
    public String getType() {
        return "negate-damage";
    }

    public void handleEvent(EntityDamageByBlockEvent event, int level) {
        if (level < getRequiredLevel()) {
            return;
        }

        if (event.getCause().equals(damageCause)) {
            event.setCancelled(true);
        }
    }
}
