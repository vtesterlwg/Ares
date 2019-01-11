package com.riotmc.services.humbug.features.cont;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.riotmc.commons.base.util.Time;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.commons.bukkit.util.Worlds;
import com.riotmc.services.humbug.HumbugService;
import com.riotmc.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MobStacking implements HumbugModule, Listener {
    @Getter public final HumbugService humbug;
    @Getter @Setter public boolean enabled;
    @Getter @Setter public String tagPrefix;
    @Getter @Setter public int stackInterval;
    @Getter @Setter public int maxStackSize;
    @Getter @Setter public int breedCooldown;
    @Getter public List<String> stackTypes;
    @Getter public final List<UUID> stackSkip;
    @Getter public final Map<UUID, Long> breedCooldowns;
    private BukkitTask stackTask;

    private final ImmutableMap<EntityType, List<Material>> breedMaterials = ImmutableMap.<EntityType, List<Material>>builder()
            .put(EntityType.COW, ImmutableList.of(Material.WHEAT))
            .put(EntityType.SHEEP, ImmutableList.of(Material.WHEAT))
            .put(EntityType.MUSHROOM_COW, ImmutableList.of(Material.WHEAT))
            .put(EntityType.HORSE, ImmutableList.of(Material.GOLDEN_APPLE, Material.GOLDEN_CARROT))
            .put(EntityType.CHICKEN, ImmutableList.of(Material.BEETROOT_SEEDS, Material.MELON_SEEDS, Material.PUMPKIN_SEEDS, Material.WHEAT_SEEDS))
            .put(EntityType.PIG, ImmutableList.of(Material.CARROT, Material.CARROTS, Material.POTATOES, Material.POTATO, Material.BEETROOT))
            .put(EntityType.WOLF, ImmutableList.of(Material.PORKCHOP, Material.COOKED_PORKCHOP, Material.BEEF, Material.COOKED_BEEF, Material.CHICKEN, Material.COOKED_CHICKEN, Material.MUTTON, Material.COOKED_MUTTON, Material.ROTTEN_FLESH))
            .put(EntityType.OCELOT, ImmutableList.of(Material.SALMON, Material.TROPICAL_FISH, Material.PUFFERFISH, Material.COD))
            .put(EntityType.RABBIT, ImmutableList.of(Material.DANDELION, Material.CARROTS, Material.CARROT, Material.GOLDEN_CARROT))
            .put(EntityType.LLAMA, ImmutableList.of(Material.HAY_BLOCK))
            .put(EntityType.TURTLE, ImmutableList.of(Material.SEAGRASS)).build();

    public MobStacking(HumbugService humbug) {
        this.humbug = humbug;
        this.stackSkip = Collections.synchronizedList(Lists.newArrayList());
        this.breedCooldowns = Maps.newConcurrentMap();
    }

    @Override
    public void loadValues() {
        this.enabled = humbug.getHumbugConfig().getBoolean("mob-stacking.enabled");
        this.tagPrefix = ChatColor.translateAlternateColorCodes('&', humbug.getHumbugConfig().getString("mob-stacking.tag-prefix"));
        this.stackInterval = humbug.getHumbugConfig().getInt("mob-stacking.stack-interval");
        this.maxStackSize = humbug.getHumbugConfig().getInt("mob-stacking.max-stack-size");
        this.breedCooldown = humbug.getHumbugConfig().getInt("mob-stacking.breed-cooldown");
        this.stackTypes = humbug.getHumbugConfig().getStringList("mob-stacking.stack-types");
    }

    @Override
    public String getName() {
        return "Mobstacking";
    }

    @Override
    public void start() {
        this.humbug.getOwner().registerListener(this);

        this.stackTask = new Scheduler(getHumbug().getOwner()).sync(() -> {
            for (World world : Bukkit.getWorlds()) {
                for (LivingEntity entity : world.getLivingEntities()) {
                    if (!stackTypes.contains(entity.getType().name())) {
                        continue;
                    }

                    if (stackSkip.contains(entity.getUniqueId())) {
                        continue;
                    }

                    if (entity.isLeashed()) {
                        continue;
                    }

                    if (entity.getCustomName() != null && !entity.getCustomName().startsWith(tagPrefix)) {
                        continue;
                    }

                    final List<LivingEntity> merge = Lists.newArrayList();
                    merge.add(entity);

                    for (Entity nearby : entity.getNearbyEntities(3, 3, 3)) {
                        if (!(nearby instanceof LivingEntity)) {
                            continue;
                        }

                        final LivingEntity nearbyLivingEntity = (LivingEntity)nearby;

                        if (!nearby.getType().equals(entity.getType())) {
                            continue;
                        }

                        if (stackSkip.contains(nearby.getUniqueId())) {
                            continue;
                        }

                        if (entity instanceof Ageable) {
                            final Ageable entityA = (Ageable)entity;
                            final Ageable entityB = (Ageable)nearbyLivingEntity;

                            if (entityA.isAdult() != entityB.isAdult()) {
                                continue;
                            }
                        }

                        if (entity instanceof Colorable) {
                            final Colorable entityA = (Colorable)entity;
                            final Colorable entityB = (Colorable)nearbyLivingEntity;

                            if (entityA.getColor() != entityB.getColor()) {
                                continue;
                            }
                        }

                        if (nearbyLivingEntity.isLeashed()) {
                            continue;
                        }

                        if (nearbyLivingEntity.getCustomName() != null && !nearbyLivingEntity.getCustomName().startsWith(tagPrefix))

                        if (merge.contains(nearbyLivingEntity)) {
                            continue;
                        }

                        merge.add(nearbyLivingEntity);
                    }

                    if (merge.size() > 1) {
                        stack(merge);
                    }
                }
            }

            stackSkip.clear();
        }).repeat(stackInterval * 20L, stackInterval * 20L).run();
    }

    @Override
    public void stop() {
        EntityDeathEvent.getHandlerList().unregister(this);

        if (stackTask != null) {
            stackTask.cancel();
        }

        stackSkip.clear();
        breedCooldowns.clear();
    }

    private void stack(List<LivingEntity> entities) {
        if (entities.size() <= 1) {
            return;
        }

        entities.sort((o1, o2) -> {
            final int stackA = getStackSize(o1);
            final int stackB = getStackSize(o2);
            return stackA - stackB;
        });

        Collections.reverse(entities);

        final LivingEntity host = entities.get(0);
        int size = getStackSize(host);

        for (LivingEntity merged : entities) {
            if (merged.getUniqueId().equals(host.getUniqueId())) {
                continue;
            }

            final int mergedSize = getStackSize(merged);

            if ((mergedSize + size) > maxStackSize) {
                continue;
            }

            size += getStackSize(merged);
            stackSkip.add(merged.getUniqueId());
            merged.remove();
        }

        host.setCustomName(tagPrefix + size);
        stackSkip.add(host.getUniqueId());
    }

    private int getStackSize(LivingEntity entity) {
        if (entity.getCustomName() == null || !entity.getCustomName().startsWith(tagPrefix)) {
            return 1;
        }

        try {
            return Integer.parseInt(entity.getCustomName().replace(tagPrefix, ""));
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    public boolean isStacked(LivingEntity entity) {
        return entity.getCustomName() != null && entity.getCustomName().startsWith(tagPrefix);
    }

    public long getBreedingCooldown(Player player) {
        return breedCooldowns.getOrDefault(player.getUniqueId(), 0L);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        final LivingEntity entity = event.getEntity();
        final int stackSize = getStackSize(entity);

        if (!isStacked(entity)) {
            return;
        }

        final LivingEntity clone = (LivingEntity)entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());

        if ((stackSize - 1) > 1) {
            clone.setCustomName(tagPrefix + (stackSize - 1));
        }

        clone.setNoDamageTicks(2);
        clone.setFireTicks(entity.getFireTicks());
        clone.setRemainingAir(entity.getRemainingAir());
        clone.setVelocity(entity.getVelocity());
        clone.setTicksLived(entity.getTicksLived());

        entity.getActivePotionEffects().forEach(clone::addPotionEffect);

        if (entity instanceof Colorable) {
            final Colorable entityA = (Colorable)entity;
            final Colorable entityB = (Colorable)clone;

            entityA.setColor(entityB.getColor());
        }

        if (entity instanceof Ageable) {
            final Ageable entityA = (Ageable)entity;
            final Ageable entityB = (Ageable)clone;

            entityA.setAge(entityB.getAge());
        }
    }

    @EventHandler
    public void onBreed(PlayerInteractEntityEvent event) {
        if (!isEnabled()) {
            return;
        }

        if (!(event.getRightClicked() instanceof LivingEntity)) {
            return;
        }

        final Player player = event.getPlayer();
        final UUID uniqueId = player.getUniqueId();
        final LivingEntity entity = (LivingEntity)event.getRightClicked();
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (!breedMaterials.containsKey(entity.getType())) {
            return;
        }

        if (!breedMaterials.get(entity.getType()).contains(hand.getType())) {
            return;
        }

        if (!isStacked(entity)) {
            return;
        }

        event.setCancelled(true);

        final int stackSize = getStackSize(entity);

        if (stackSize <= 1) {
            player.sendMessage(ChatColor.RED + "Stack size must be at least 2 to begin breeding");
            return;
        }

        if (getBreedingCooldown(player) > Time.now()) {
            player.sendMessage(
                    ChatColor.RED + "You can not breed animals for another " +
                    ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(getBreedingCooldown(player) - Time.now()) +
                    ChatColor.RED + "s");

            return;
        }

        final LivingEntity baby = (LivingEntity)entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());

        if (baby instanceof Ageable) {
            ((Ageable)baby).setBaby();
        }

        if (baby instanceof Colorable) {
            final Colorable parent = (Colorable)entity;
            final Colorable colorable = (Colorable)baby;

            colorable.setColor(parent.getColor());
        }

        if (hand.getAmount() == 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            hand.setAmount(hand.getAmount() - 1);
        }

        Worlds.spawnParticle(entity.getEyeLocation(), Particle.HEART, 10, 1.0);

        breedCooldowns.put(player.getUniqueId(), (Time.now() + (breedCooldown * 1000L)));
        new Scheduler(getHumbug().getOwner()).sync(() -> breedCooldowns.remove(uniqueId)).delay(breedCooldown * 20L).run();
    }
}