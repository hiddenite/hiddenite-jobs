package eu.hiddenite.jobs.skills;

public class CropsFeatherSkill extends Skill {
    public CropsFeatherSkill(int requiredLevel) {
        super(requiredLevel);
    }

    @Override
    public String getType() {
        return "crops-feather";
    }
}
