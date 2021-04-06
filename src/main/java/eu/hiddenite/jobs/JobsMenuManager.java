package eu.hiddenite.jobs;

import eu.hiddenite.jobs.skills.CarefulSkill;
import eu.hiddenite.jobs.skills.GathererSkill;
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
        menu.inventory = plugin.getServer().createInventory(player, 36, plugin.formatComponent("menu.title"));

        drawEntirePage(menu, null);

        player.openInventory(menu.inventory);
        openMenus.put(menu.inventory, menu);
    }

    private void drawEntirePage(InventoryMenu menu, String jobType) {
        menu.selectedJob = jobType;
        menu.inventory.clear();

        drawJobs(menu);
        drawSkills(menu);

        fillEmptySpace(menu);
    }

    private void drawJobs(InventoryMenu menu) {
        ItemStack woodcutting = createJobItem(menu.player, WoodcuttingManager.JOB_TYPE, WoodcuttingManager.JOB_TYPE.equals(menu.selectedJob));
        menu.inventory.setItem(0, woodcutting);

        ItemStack mining = createJobItem(menu.player, MiningManager.JOB_TYPE, MiningManager.JOB_TYPE.equals(menu.selectedJob));
        menu.inventory.setItem(1, mining);
    }

    private void drawSkills(InventoryMenu menu) {
        if (menu.selectedJob == null) {
            return;
        }

        switch (menu.selectedJob) {
            case WoodcuttingManager.JOB_TYPE:
            case MiningManager.JOB_TYPE:
                ItemStack gatherer = createSkillItem(menu.player, menu.selectedJob, "gatherer");
                menu.inventory.setItem(19, gatherer);

                ItemStack careful = createSkillItem(menu.player, menu.selectedJob, "careful");
                menu.inventory.setItem(20, careful);
                break;
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
            drawEntirePage(menu, null);
            return;
        }

        if (event.getClick() == ClickType.LEFT) {
            if (event.getSlot() == 0) {
                drawEntirePage(menu, WoodcuttingManager.JOB_TYPE);
            }
            if (event.getSlot() == 1) {
                drawEntirePage(menu, MiningManager.JOB_TYPE);
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
