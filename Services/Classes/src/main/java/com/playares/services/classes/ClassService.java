package com.playares.services.classes;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.google.common.collect.Maps;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.service.AresService;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.classes.data.classes.*;
import com.playares.services.classes.data.effects.ClassEffectable;
import com.playares.services.classes.event.*;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Map;
import java.util.UUID;

public final class ClassService implements AresService, Listener {
    @Getter
    public final AresPlugin owner;

    @Getter
    public final Map<UUID, AresClass> activeClasses;

    @Getter
    public final Map<Class <? extends AresClass>, AresClass> classes;

    public ClassService(AresPlugin owner) {
        this.owner = owner;
        this.activeClasses = Maps.newConcurrentMap();
        this.classes = Maps.newHashMap();
    }

    @Override
    public String getName() {
        return "Classes";
    }

    @Override
    public void start() {
        registerListener(this);

        classes.put(ArcherClass.class, new ArcherClass());
        classes.put(BardClass.class, new BardClass());
        classes.put(RogueClass.class, new RogueClass());
        classes.put(DiverClass.class, new DiverClass());
    }

    @Override
    public void stop() {
        this.activeClasses.keySet().forEach(id -> {
            final Player player = Bukkit.getPlayer(id);

            if (player != null) {
                removeFromClass(player);
            }
        });

        this.activeClasses.clear();
        this.classes.clear();

        PlayerInteractEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
    }

    public AresClass getClass(Class<? extends AresClass> clazz) {
        return classes.get(clazz);
    }

    public AresClass getPlayerClass(Player player) {
        return activeClasses.get(player.getUniqueId());
    }

    public AresClass getClassByArmor(Player player) {
        return classes.values().stream().filter(c -> c.match(player)).findFirst().orElse(null);
    }

    public void addToClass(Player player, AresClass aClass) {
        if (getClassByArmor(player) == null || !getClassByArmor(player).equals(aClass)) {
            return;
        }

        final PlayerEnterClassEvent event = new PlayerEnterClassEvent(player, aClass);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        activeClasses.put(player.getUniqueId(), aClass);

        aClass.getPassiveEffects().forEach(passive -> {
            player.removePotionEffect(passive.getType());
            player.addPotionEffect(passive);
        });

        player.sendMessage(ChatColor.GREEN + aClass.getName() + " activated");
        aClass.sendIntro(player);
    }

    public void removeFromClass(Player player) {
        final AresClass found = getPlayerClass(player);

        if (found == null) {
            return;
        }

        found.getPassiveEffects().forEach(passive -> player.removePotionEffect(passive.getType()));

        activeClasses.remove(player.getUniqueId());

        player.sendMessage(ChatColor.RED + found.getName() + " deactivated");
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        final Player player = event.getPlayer();
        final AresClass foundClass = getPlayerClass(player);
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand == null || hand.getType().equals(Material.AIR)) {
            return;
        }

        if (foundClass == null) {
            return;
        }

        final ClassEffectable consumable = foundClass.getConsumable(hand.getType());

        if (consumable == null) {
            return;
        }

        if (!(foundClass instanceof BardClass)) {
            final ClassConsumeEvent consumeEvent = new ClassConsumeEvent(player, consumable);
            Bukkit.getPluginManager().callEvent(consumeEvent);

            if (consumeEvent.isCancelled()) {
                return;
            }

            if (player.hasPotionEffect(consumable.getEffect().getType())) {
                final PotionEffect storedEffect = player.getPotionEffect(consumable.getEffect().getType());
                final AresClass storedClass = getPlayerClass(player);

                player.removePotionEffect(consumable.getEffect().getType());

                new Scheduler(getOwner()).sync(() -> {
                    final AresClass currentClass = getPlayerClass(player);

                    if (storedClass != null && !storedClass.equals(currentClass)) {
                        return;
                    }

                    player.addPotionEffect(storedEffect);
                }).delay(consumable.getEffect().getDuration() + 1L).run();
            }

            player.addPotionEffect(consumable.getEffect());

            if (hand.getAmount() > 1) {
                hand.setAmount(hand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
        } else {
            final BardEffectEvent bardEvent = new BardEffectEvent(player, consumable);
            Bukkit.getPluginManager().callEvent(bardEvent);

            if (bardEvent.isCancelled() || bardEvent.getAffectedEntities().isEmpty()) {
                return;
            }

            for (Player affected : bardEvent.getAffectedEntities()) {
                if (affected.hasPotionEffect(consumable.getEffect().getType())) {
                    final PotionEffect storedEffect = player.getPotionEffect(consumable.getEffect().getType());
                    final AresClass storedClass = getPlayerClass(affected);

                    affected.removePotionEffect(consumable.getEffect().getType());

                    new Scheduler(getOwner()).sync(() -> {
                        final AresClass currentClass = getPlayerClass(player);

                        if (storedClass != null && !storedClass.equals(currentClass)) {
                            return;
                        }

                        player.addPotionEffect(storedEffect);
                    }).delay(consumable.getEffect().getDuration() + 1L).run();
                }

                affected.addPotionEffect(consumable.getEffect());

                if (affected.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(ChatColor.AQUA + StringUtils.capitaliseAllWords(consumable.getEffect().getType().getName().toLowerCase().replace("_", " ")) +
                            " " + (consumable.getEffect().getAmplifier() + 1) + ChatColor.YELLOW + " applied to " + ChatColor.AQUA + bardEvent.getAffectedEntities().size() + ChatColor.YELLOW +
                            " players");
                } else {
                    player.sendMessage(
                            ChatColor.YELLOW + "Received " + ChatColor.AQUA +
                            StringUtils.capitaliseAllWords(consumable.getEffect().getType().getName().toLowerCase().replace("_", " ")) +
                            " " + (consumable.getEffect().getAmplifier() + 1) + ChatColor.YELLOW + " for " + ChatColor.AQUA + (consumable.getEffect().getDuration() / 20) +
                            ChatColor.YELLOW + " seconds");
                }
            }

            if (hand.getAmount() > 1) {
                hand.setAmount(hand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onProjectileHit(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getDamager() instanceof Arrow)) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        final Projectile projectile = (Projectile)event.getDamager();
        final LivingEntity damaged = (LivingEntity)event.getEntity();

        if (!(projectile.getShooter() instanceof Player)) {
            return;
        }

        final Player player = (Player)projectile.getShooter();
        final AresClass foundClass = getPlayerClass(player);
        final double damage = event.getDamage();

        if (!(foundClass instanceof ArcherClass)) {
            return;
        }

        final ArcherHitEvent archerEvent = new ArcherHitEvent(player, damaged, damage);
        Bukkit.getPluginManager().callEvent(archerEvent);

        if (archerEvent.isCancelled()) {
            return;
        }

        final double archerDamage = archerEvent.getDamage();

        event.setDamage(archerDamage);

        player.sendMessage(
                ChatColor.BLUE + "" + ChatColor.BOLD + "Archer Multiplier" + ChatColor.RESET + " " + ChatColor.YELLOW +
                Math.round(damage) + ChatColor.RED + " -> " + ChatColor.YELLOW + Math.round(archerDamage) + ChatColor.RESET + " " +
                ChatColor.GOLD + "[" + ChatColor.YELLOW + Math.round(archerEvent.getDistance()) + " blocks" + ChatColor.GOLD + "]");
    }

    @EventHandler
    public void onPlayerArmor(PlayerArmorChangeEvent event) {
        final Player player = event.getPlayer();

        new Scheduler(getOwner()).sync(() -> {
            final AresClass current = getClassByArmor(player);
            final AresClass stored = getPlayerClass(player);

            if (getPlayerClass(player) != null) {
                if (current == null || !current.equals(stored)) {
                    removeFromClass(player);
                    return;
                }
            }

            if (current != null && getPlayerClass(player) == null) {
                final PlayerClassUpdateEvent updateEvent = new PlayerClassUpdateEvent(player, current);
                Bukkit.getPluginManager().callEvent(updateEvent);
            }
        }).delay(1L).run();
    }
}