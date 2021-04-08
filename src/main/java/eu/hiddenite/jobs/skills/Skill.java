package eu.hiddenite.jobs.skills;

public abstract class Skill {
    private final int requiredLevel;

    public Skill(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public abstract String getType();

    public int getCooldown(int level) {
        return 0;
    }

    public double getBonus(int level) {
        return 0.0;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }
}
