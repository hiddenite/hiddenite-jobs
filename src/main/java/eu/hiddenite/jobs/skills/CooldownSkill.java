package eu.hiddenite.jobs.skills;

import java.util.HashMap;
import java.util.UUID;

public abstract class CooldownSkill extends Skill {
    private final HashMap<UUID, Long> cooldown = new HashMap<>();

    public CooldownSkill(int requiredLevel) {
        super(requiredLevel);
    }

    public boolean applyCooldown(UUID playerId, int level) {
        long now = System.currentTimeMillis();
        boolean isCooldownActive = cooldown.getOrDefault(playerId, 0L) > now;
        if (isCooldownActive) {
            return false;
        }
        cooldown.put(playerId, now + getCooldown(level) * 1000L);
        return true;
    }

    @Override
    public abstract int getCooldown(int level);
}
