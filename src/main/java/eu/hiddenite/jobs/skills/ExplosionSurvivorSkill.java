package eu.hiddenite.jobs.skills;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ExplosionSurvivorSkill extends CooldownSkill {
    public ExplosionSurvivorSkill(int requiredLevel) {
        super(requiredLevel);
    }

    @Override
    public String getType() {
        return "explosion-survivor";
    }

    @Override
    public int getCooldown(int level) {
        return 300 - Math.min(100, level) * 2;
    }

    public void handleEvent(EntityDamageByEntityEvent event, int level) {
        if (level < getRequiredLevel()) {
            return;
        }

        if (!event.getCause().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) &&
                !event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            return;
        }

        Player player = (Player)event.getEntity();
        if (event.getFinalDamage() < player.getHealth()) {
            return;
        }

        if (!applyCooldown(player.getUniqueId(), level)) {
            return;
        }

        event.setCancelled(true);
        if (player.getHealth() > 1.0) {
            player.setLastDamageCause(event);
            player.damage(player.getHealth() - 1.0);
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.DAMAGE_RESISTANCE, 60, 0, false, false, true
            ));
        }
    }
}
