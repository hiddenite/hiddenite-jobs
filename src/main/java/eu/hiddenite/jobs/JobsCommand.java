package eu.hiddenite.jobs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class JobsCommand implements CommandExecutor, TabCompleter {
    private final JobsMenuManager manager;

    public JobsCommand(JobsMenuManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@Nonnull final CommandSender sender,
                             @Nonnull final Command command,
                             @Nonnull final String alias,
                             @Nonnull final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player)sender;
        manager.openMenu(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull final CommandSender sender,
                                      @Nonnull final Command command,
                                      @Nonnull final String alias,
                                      @Nonnull final String[] args) {
        return Collections.emptyList();
    }
}
