package eu.hiddenite.jobs;

import eu.hiddenite.jobs.jobs.Job;
import eu.hiddenite.jobs.skills.Skill;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class JobsMenuManager implements Listener {
    private static class InventoryMenu {
        public Inventory inventory;
        public Player player;
        public Job selectedJob;
    }

    private final JobsPlugin plugin;
    private final HashMap<Inventory, InventoryMenu> openMenus = new HashMap<>();

    private final DecimalFormat bonusDecimalFormat = new DecimalFormat("#.##");

    public JobsMenuManager(JobsPlugin plugin) {
        this.plugin = plugin;

        Objects.requireNonNull(plugin.getCommand("jobs")).setExecutor(new JobsCommand(this));

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void close() {
        for (InventoryMenu menu : openMenus.values()) {
            menu.player.closeInventory();
        }
        openMenus.clear();
    }

    public void openMenu(Player player) {
        InventoryMenu menu = new InventoryMenu();
        menu.player = player;
        menu.inventory = plugin.getServer().createInventory(player, 36, plugin.formatComponent("menu.title"));

        drawEntirePage(menu, null);

        player.openInventory(menu.inventory);
        openMenus.put(menu.inventory, menu);
    }

    private void drawEntirePage(InventoryMenu menu, Job job) {
        menu.selectedJob = job;
        menu.inventory.clear();

        drawJobs(menu);
        drawSkills(menu);

        fillEmptySpace(menu);
    }

    private void drawJobs(InventoryMenu menu) {
        int slot = 0;
        for (Job job : plugin.getJobs()) {
            ItemStack jobButton = createJobItem(menu.player, job.getType(), job.equals(menu.selectedJob));
            menu.inventory.setItem(slot++, jobButton);
        }
    }

    private void drawSkills(InventoryMenu menu) {
        if (menu.selectedJob == null) {
            return;
        }

        int slot = 19;
        List<Skill> skills = menu.selectedJob.getSkills();

        for (Skill skill : skills) {
            ItemStack skillButton = createSkillItem(menu.player, menu.selectedJob.getType(), skill);
            menu.inventory.setItem(slot++, skillButton);
        }
    }

    private void fillEmptySpace(InventoryMenu menu) {
        ItemStack emptyItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = emptyItem.getItemMeta();
        meta.displayName(Component.empty());
        emptyItem.setItemMeta(meta);

        for (int i = 0; i < menu.inventory.getSize(); ++i) {
            if (menu.inventory.getItem(i) == null) {
                menu.inventory.setItem(i, emptyItem.clone());
            }
        }
    }

    private ItemStack createJobItem(Player player, String jobType, boolean isSelected) {
        long exp = plugin.getExperienceManager().getPlayerExperience(player, jobType);
        int level = ExperienceManager.getLevelFromExp(exp);
        long target = ExperienceManager.getExpAtLevel(level + 1);
        long floor = ExperienceManager.getExpAtLevel(level);

        Material itemType = Material.valueOf(plugin.getConfig().getString(jobType + ".icon"));
        ItemStack itemStack = new ItemStack(itemType);
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.displayName(plugin.formatComponent(jobType + ".name"));
        meta.lore(plugin.formatComponents("menu.job-lore" + (isSelected ? "-selected" : ""),
                "{LEVEL}", level,
                "{CURRENT_EXP}", exp - floor,
                "{TARGET_EXP}", target - floor,
                "{TOTAL_EXP}", exp
        ));
        if (isSelected) {
            meta.addEnchant(Enchantment.LURE, 1, true);
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private ItemStack createSkillItem(Player player, String jobType, Skill skill) {
        int level = plugin.getExperienceManager().getPlayerLevel(player, jobType);
        int requiredLevel = skill.getRequiredLevel();
        double bonus = skill.getBonus(level);
        int cooldown = skill.getCooldown(level);

        Material itemType = Material.valueOf(plugin.getConfig().getString(jobType + ".skills." + skill.getType() + ".icon"));
        ItemStack itemStack = new ItemStack(itemType);
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.displayName(plugin.formatComponent(jobType + ".skills." + skill.getType() + ".name"));

        if (level < requiredLevel) {
            meta.lore(plugin.formatComponents("menu.hidden-skill.description",
                    "{LEVEL}", requiredLevel));
        } else {
            meta.lore(plugin.formatComponents(jobType + ".skills." + skill.getType() + ".description",
                    "{BONUS}", bonusDecimalFormat.format(bonus * 100),
                    "{COOLDOWN}", cooldown));
        }

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {
        InventoryMenu menu = openMenus.get(event.getInventory());
        if (menu == null) {
            return;
        }

        event.setCancelled(true);

        if (menu.selectedJob != null && event.getClick() == ClickType.RIGHT) {
            drawEntirePage(menu, null);
            return;
        }

        if (event.getClick() == ClickType.LEFT && event.getInventory().equals(event.getClickedInventory())) {
            if (event.getSlot() >= 0 && event.getSlot() < plugin.getJobs().size()) {
                drawEntirePage(menu, plugin.getJobs().get(event.getSlot()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(final InventoryDragEvent event) {
        if (!openMenus.containsKey(event.getInventory())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (!openMenus.containsKey(event.getInventory())) {
            return;
        }
        openMenus.remove(event.getInventory());
    }
}
