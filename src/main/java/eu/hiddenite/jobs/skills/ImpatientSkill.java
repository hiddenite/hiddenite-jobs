package eu.hiddenite.jobs.skills;

import org.bukkit.entity.FishHook;

public class ImpatientSkill {
    public static final String NAME = "impatient";
    public static final int REQUIRED_LEVEL = 10;

    public static double getBonus(int level) {
        return (double)Math.min(100, level) / 100.0 / 2.0;
    }

    public static void apply(FishHook hook, int level) {
        if (level < REQUIRED_LEVEL) {
            return;
        }
        hook.setMaxWaitTime((int)Math.round((double)hook.getMaxWaitTime() * getBonus(level)));
        hook.setMinWaitTime((int)Math.round((double)hook.getMinWaitTime() * getBonus(level)));
    }
}
