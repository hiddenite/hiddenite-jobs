package eu.hiddenite.jobs.skills;

import org.bukkit.entity.FishHook;

public class ImpatientSkill extends Skill {
    public ImpatientSkill(int requiredLevel) {
        super(requiredLevel);
    }

    @Override
    public String getType() {
        return "impatient";
    }

    @Override
    public double getBonus(int level) {
        return (double)Math.min(100, level) / 100.0 / 2.0;
    }

    public void apply(FishHook hook, int level) {
        if (level < getRequiredLevel()) {
            return;
        }
        hook.setMinWaitTime((int)Math.round((double)hook.getMinWaitTime() * (1.0 - getBonus(level))));
        hook.setMaxWaitTime((int)Math.round((double)hook.getMaxWaitTime() * (1.0 - getBonus(level))));
    }
}
