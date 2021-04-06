package eu.hiddenite.jobs;

import eu.hiddenite.jobs.skills.CarefulSkill;
import eu.hiddenite.jobs.skills.GathererSkill;
import org.bukkit.Material;
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
import java.util.Objects;

public class JobsMenuManager implements Listener {
    private static class InventoryMenu {
        public Inventory inventory;
        public Player player;
        public String selectedJob;
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
        menu.inventory = plugin.getServer().createInventory(player, 54, plugin.formatComponent("menu.title"));

        openMainPage(menu);

        player.openInventory(menu.inventory);
        openMenus.put(menu.inventory, menu);
    }

    public void openMainPage(InventoryMenu menu) {
        menu.selectedJob = null;
        menu.inventory.clear();

        ItemStack woodcutting = createJobItem(menu.player, "woodcutting", true);
        menu.inventory.setItem(10, woodcutting);
    }

    public void openJobPage(InventoryMenu menu, String jobType) {
        menu.selectedJob = jobType;
        menu.inventory.clear();

        if (jobType.equals("woodcutting")) {
            ItemStack woodcutting = createJobItem(menu.player, "woodcutting", false);
            menu.inventory.setItem(4, woodcutting);

            ItemStack gatherer = createSkillItem(menu.player, "woodcutting", "gatherer");
            menu.inventory.setItem(19, gatherer);

            ItemStack careful = createSkillItem(menu.player, "woodcutting", "careful");
            menu.inventory.setItem(20, careful);
        }
    }

    private ItemStack createJobItem(Player player, String jobType, boolean shortDescription) {
        long exp = plugin.getExperienceManager().getPlayerExperience(player, jobType);
        int level = ExperienceManager.getLevelFromExp(exp);
        long target = ExperienceManager.getExpAtLevel(level + 1);
        long floor = ExperienceManager.getExpAtLevel(level);

        Material itemType = Material.valueOf(plugin.getConfig().getString(jobType + ".icon"));
        ItemStack itemStack = new ItemStack(itemType);
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.displayName(plugin.formatComponent(jobType + ".name"));
        meta.lore(plugin.formatComponents("menu.job-lore-" + (shortDescription ? "short" : "long"),
                "{LEVEL}", level,
                "{CURRENT_EXP}", exp - floor,
                "{TARGET_EXP}", target - floor,
                "{TOTAL_EXP}", exp
        ));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private ItemStack createSkillItem(Player player, String jobType, String skill) {
        int level = plugin.getExperienceManager().getPlayerLevel(player, jobType);

        double bonus = 0;
        switch (skill) {
            case "gatherer":
                bonus = GathererSkill.getChance(level);
                break;
            case "careful":
                bonus = CarefulSkill.getChance(level);
                break;
        }

        Material itemType = Material.valueOf(plugin.getConfig().getString(jobType + ".skills." + skill + ".icon"));
        ItemStack itemStack = new ItemStack(itemType);
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.displayName(plugin.formatComponent(jobType + ".skills." + skill + ".name"));
        meta.lore(plugin.formatComponents(jobType + ".skills." + skill + ".description",
                "{BONUS}", bonusDecimalFormat.format(bonus * 100)));
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
            openMainPage(menu);
            return;
        }

        if (menu.selectedJob == null && event.getClick() == ClickType.LEFT) {
            if (event.getSlot() == 10) {
                openJobPage(menu, "woodcutting");
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
