package eu.hiddenite.jobs.jobs;

import eu.hiddenite.jobs.skills.Skill;

import java.util.List;

public abstract class Job {
    public abstract String getType();

    public abstract List<Skill> getSkills();
}
