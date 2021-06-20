package eu.hiddenite.jobs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class JobsCommand implements CommandExecutor, TabCompleter {
    private final JobsMenuManager manager;

    public JobsCommand(JobsMenuManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender,
                             final @NotNull Command command,
                             final @NotNull String alias,
                             final String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        manager.openMenu(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(final @NotNull CommandSender sender,
                                      final @NotNull Command command,
                                      final @NotNull String alias,
                                      final String[] args) {
        return Collections.emptyList();
    }
}
