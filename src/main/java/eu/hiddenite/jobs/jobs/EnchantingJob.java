package eu.hiddenite.jobs.jobs;

import eu.hiddenite.jobs.JobsPlugin;
import eu.hiddenite.jobs.skills.Skill;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;

public class EnchantingJob extends Job implements Listener {
    public static final String JOB_TYPE = "enchanting";

    private final JobsPlugin plugin;

    public EnchantingJob(JobsPlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String getType() {
        return JOB_TYPE;
    }

    @Override
    public List<Skill> getSkills() {
        return Arrays.asList();
    }
}
