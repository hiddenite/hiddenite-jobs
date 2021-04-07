package eu.hiddenite.jobs.jobs;

import eu.hiddenite.jobs.JobsPlugin;
import eu.hiddenite.jobs.skills.CarefulSkill;
import eu.hiddenite.jobs.skills.GathererSkill;
import eu.hiddenite.jobs.skills.ImpatientSkill;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class FishingManager implements Listener {
    public static final String JOB_TYPE = "fishing";

    private final JobsPlugin plugin;
    private final HashMap<UUID, Integer> clicksWhileFishing = new HashMap<>();
    private final HashMap<UUID, Integer> consecutiveShallow = new HashMap<>();

    public FishingManager(JobsPlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerFish(PlayerFishEvent event) {
        FishHook hook = event.getHook();

        if (event.getState() == PlayerFishEvent.State.FISHING) {
            clicksWhileFishing.put(event.getPlayer().getUniqueId(), 0);

            int level = plugin.getExperienceManager().getPlayerLevel(event.getPlayer(), JOB_TYPE);
            ImpatientSkill.apply(hook, level);
        }

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() instanceof Item) {
            Item item = (Item)event.getCaught();
            int clicks = clicksWhileFishing.getOrDefault(event.getPlayer().getUniqueId(), 0);

            if (clicks <= 1) {
                sendDeepWaterHint(event.getPlayer(), hook);
                applyExpAndBuffs(event.getPlayer(), hook, item.getItemStack());
            }
        }

        if (event.getState() == PlayerFishEvent.State.REEL_IN ||
                event.getState() == PlayerFishEvent.State.IN_GROUND ||
                event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY ||
                event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            clicksWhileFishing.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && clicksWhileFishing.containsKey(playerId)) {
            clicksWhileFishing.put(playerId, clicksWhileFishing.get(playerId) + 1);
        }
    }

    private void sendDeepWaterHint(Player player, FishHook hook) {
        if (!hook.isInOpenWater()) {
            consecutiveShallow.put(player.getUniqueId(), consecutiveShallow.getOrDefault(player.getUniqueId(), 0) + 1);
            int shallowCatches = consecutiveShallow.get(player.getUniqueId());
            if (shallowCatches > 0 && shallowCatches % 3 == 0) {
                plugin.sendActionBar(player, "fishing.hint-deep-water");
            }
        } else {
            consecutiveShallow.remove(player.getUniqueId());
        }
    }

    private void applyExpAndBuffs(Player player, FishHook hook, ItemStack itemStack) {
        int enchants = itemStack.getEnchantments().size();
        int exp = (hook.isInOpenWater() ? 20 : 5) + (enchants * 5);

        int level = plugin.getExperienceManager().getPlayerLevel(player, JOB_TYPE);
        GathererSkill.apply(itemStack, level);

        plugin.getExperienceManager().gainExp(player, JOB_TYPE, exp);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        int level = plugin.getExperienceManager().getPlayerLevel(event.getPlayer(), JOB_TYPE);

        if (CarefulSkill.shouldApply(event.getItem(), level, JOB_TYPE)) {
            event.setCancelled(true);
        }
    }
}
