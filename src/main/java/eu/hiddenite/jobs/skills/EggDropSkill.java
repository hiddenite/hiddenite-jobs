package eu.hiddenite.jobs.skills;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Random;

public class EggDropSkill extends Skill {
    private final Random random = new Random();
    private final HashMap<EntityType, Material> eggFromEntity = new HashMap<>();

    public EggDropSkill(int requiredLevel) {
        super(requiredLevel);

        eggFromEntity.put(EntityType.BAT, Material.BAT_SPAWN_EGG);
        eggFromEntity.put(EntityType.CAT, Material.CAT_SPAWN_EGG);
        eggFromEntity.put(EntityType.CHICKEN, Material.CHICKEN_SPAWN_EGG);
        eggFromEntity.put(EntityType.COD, Material.COD_SPAWN_EGG);
        eggFromEntity.put(EntityType.COW, Material.COW_SPAWN_EGG);
        eggFromEntity.put(EntityType.DONKEY, Material.DONKEY_SPAWN_EGG);
        eggFromEntity.put(EntityType.FOX, Material.FOX_SPAWN_EGG);
        eggFromEntity.put(EntityType.HORSE, Material.HORSE_SPAWN_EGG);
        eggFromEntity.put(EntityType.MUSHROOM_COW, Material.MOOSHROOM_SPAWN_EGG);
        eggFromEntity.put(EntityType.MULE, Material.MULE_SPAWN_EGG);
        eggFromEntity.put(EntityType.OCELOT, Material.OCELOT_SPAWN_EGG);
        eggFromEntity.put(EntityType.PARROT, Material.PARROT_SPAWN_EGG);
        eggFromEntity.put(EntityType.PIG, Material.PIG_SPAWN_EGG);
        eggFromEntity.put(EntityType.PUFFERFISH, Material.PUFFERFISH_SPAWN_EGG);
        eggFromEntity.put(EntityType.RABBIT, Material.RABBIT_SPAWN_EGG);
        eggFromEntity.put(EntityType.SALMON, Material.SALMON_SPAWN_EGG);
        eggFromEntity.put(EntityType.SHEEP, Material.SHEEP_SPAWN_EGG);
        eggFromEntity.put(EntityType.SQUID, Material.SQUID_SPAWN_EGG);
        eggFromEntity.put(EntityType.STRIDER, Material.STRIDER_SPAWN_EGG);
        eggFromEntity.put(EntityType.TROPICAL_FISH, Material.TROPICAL_FISH_SPAWN_EGG);
        eggFromEntity.put(EntityType.TURTLE, Material.TURTLE_SPAWN_EGG);

        eggFromEntity.put(EntityType.BEE, Material.BEE_SPAWN_EGG);
        eggFromEntity.put(EntityType.DOLPHIN, Material.DOLPHIN_SPAWN_EGG);
        eggFromEntity.put(EntityType.LLAMA, Material.LLAMA_SPAWN_EGG);
        eggFromEntity.put(EntityType.PANDA, Material.PANDA_SPAWN_EGG);
        eggFromEntity.put(EntityType.WOLF, Material.WOLF_SPAWN_EGG);
        eggFromEntity.put(EntityType.POLAR_BEAR, Material.POLAR_BEAR_SPAWN_EGG);

        eggFromEntity.put(EntityType.GLOW_SQUID, Material.GLOW_SQUID_SPAWN_EGG);
        eggFromEntity.put(EntityType.AXOLOTL, Material.AXOLOTL_SPAWN_EGG);
        eggFromEntity.put(EntityType.GOAT, Material.GOAT_SPAWN_EGG);
    }

    @Override
    public String getType() {
        return "egg-drop";
    }

    @Override
    public double getBonus(int level) {
        return (double)Math.min(100, level) / 2000.0;
    }

    public void apply(EntityDeathEvent event, int level) {
        if (level < getRequiredLevel()) {
            return;
        }
        double chance = getBonus(level);
        if (random.nextDouble() >= chance) {
            return;
        }

        Material spawnEggType = eggFromEntity.getOrDefault(event.getEntityType(), null);
        if (spawnEggType == null) {
            return;
        }

        event.getDrops().add(new ItemStack(spawnEggType, 1));
    }
}
