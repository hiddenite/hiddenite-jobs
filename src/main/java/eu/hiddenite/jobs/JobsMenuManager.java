package eu.hiddenite.jobs;

import eu.hiddenite.jobs.jobs.FishingManager;
import eu.hiddenite.jobs.jobs.MiningManager;
import eu.hiddenite.jobs.jobs.WoodcuttingManager;
import eu.hiddenite.jobs.skills.CalculatorSkill;
import eu.hiddenite.jobs.skills.CarefulSkill;
import eu.hiddenite.jobs.skills.GathererSkill;
import eu.hiddenite.jobs.skills.ImpatientSkill;
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

        ItemStack fishing = createJobItem(menu.player, FishingManager.JOB_TYPE, FishingManager.JOB_TYPE.equals(menu.selectedJob));
        menu.inventory.setItem(2, fishing);
    }

    private void drawSkills(InventoryMenu menu) {
        if (menu.selectedJob == null) {
            return;
        }

        int slot = 18;

        switch (menu.selectedJob) {
            case WoodcuttingManager.JOB_TYPE:
            case MiningManager.JOB_TYPE:
            case FishingManager.JOB_TYPE:
                ItemStack careful = createSkillItem(menu.player, menu.selectedJob, CarefulSkill.NAME);
                menu.inventory.setItem(++slot, careful);

                ItemStack gatherer = createSkillItem(menu.player, menu.selectedJob, GathererSkill.NAME);
                menu.inventory.setItem(++slot, gatherer);
                break;
        }

        if (WoodcuttingManager.JOB_TYPE.equals(menu.selectedJob)) {
            ItemStack calculator1 = createSkillItem(menu.player, menu.selectedJob, "calculator1");
            menu.inventory.setItem(++slot, calculator1);

            ItemStack calculator2 = createSkillItem(menu.player, menu.selectedJob, "calculator2");
            menu.inventory.setItem(++slot, calculator2);

            ItemStack calculator3 = createSkillItem(menu.player, menu.selectedJob, "calculator3");
            menu.inventory.setItem(++slot, calculator3);
        }

        if (FishingManager.JOB_TYPE.equals(menu.selectedJob)) {
            ItemStack impatient = createSkillItem(menu.player, menu.selectedJob, "impatient");
            menu.inventory.setItem(++slot, impatient);
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
        int requiredLevel = 0;
        double bonus = 0;
        int cooldown = 0;

        switch (skill) {
            case CarefulSkill.NAME:
                bonus = CarefulSkill.getChance(level);
                requiredLevel = CarefulSkill.REQUIRED_LEVEL;
                break;
            case GathererSkill.NAME:
                bonus = GathererSkill.getChance(level);
                requiredLevel = GathererSkill.REQUIRED_LEVEL;
                break;
            case ImpatientSkill.NAME:
                bonus = ImpatientSkill.getBonus(level);
                requiredLevel = ImpatientSkill.REQUIRED_LEVEL;
                break;
            case CalculatorSkill.NAME + "1":
                cooldown = CalculatorSkill.getCooldown(level, 1);
                requiredLevel = CalculatorSkill.getRequiredLevelForRank(1);
                break;
            case CalculatorSkill.NAME + "2":
                cooldown = CalculatorSkill.getCooldown(level, 2);
                requiredLevel = CalculatorSkill.getRequiredLevelForRank(2);
                break;
            case CalculatorSkill.NAME + "3":
                cooldown = CalculatorSkill.getCooldown(level, 3);
                requiredLevel = CalculatorSkill.getRequiredLevelForRank(3);
                break;
        }

        Material itemType = Material.valueOf(plugin.getConfig().getString(jobType + ".skills." + skill + ".icon"));
        ItemStack itemStack = new ItemStack(itemType);
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.displayName(plugin.formatComponent(jobType + ".skills." + skill + ".name"));

        if (level < requiredLevel) {
            meta.lore(plugin.formatComponents("menu.hidden-skill.description",
                    "{LEVEL}", requiredLevel));
        } else {
            meta.lore(plugin.formatComponents(jobType + ".skills." + skill + ".description",
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

        if (event.getClick() == ClickType.LEFT) {
            if (event.getSlot() == 0) {
                drawEntirePage(menu, WoodcuttingManager.JOB_TYPE);
            }
            if (event.getSlot() == 1) {
                drawEntirePage(menu, MiningManager.JOB_TYPE);
            }
            if (event.getSlot() == 2) {
                drawEntirePage(menu, FishingManager.JOB_TYPE);
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
