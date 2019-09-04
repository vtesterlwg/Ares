package com.playares.services.playerclasses.listener;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.google.common.collect.Sets;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.event.PlayerDamagePlayerEvent;
import com.playares.commons.bukkit.util.Players;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.playerclasses.PlayerClassService;
import com.playares.services.playerclasses.data.Class;
import com.playares.services.playerclasses.data.ClassConsumable;
import com.playares.services.playerclasses.data.cont.ArcherClass;
import com.playares.services.playerclasses.data.cont.RogueClass;
import com.playares.services.playerclasses.event.*;
import lombok.Getter;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.UUID;

public final class ClassListener implements Listener {
    @Getter public final PlayerClassService service;
    @Getter public final Set<UUID> recentlyLoggedIn;

    public ClassListener(PlayerClassService service) {
        this.service = service;
        this.recentlyLoggedIn = Sets.newConcurrentHashSet();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final UUID uniqueId = player.getUniqueId();

        player.getActivePotionEffects().stream().filter(effect -> effect.getDuration() > 25000).forEach(infiniteEffect -> player.removePotionEffect(infiniteEffect.getType()));
        recentlyLoggedIn.add(player.getUniqueId());

        new Scheduler(service.getOwner()).sync(() -> {
            recentlyLoggedIn.remove(uniqueId);

            final Class playerClass = service.getClassManager().getClassByArmor(player);

            if (playerClass != null) {
                final PlayerClassReadyEvent readyEvent = new PlayerClassReadyEvent(player, playerClass);
                Bukkit.getPluginManager().callEvent(readyEvent);
            }
        }).delay(3L).run();
    }

    @EventHandler
    public void onArmorChange(PlayerArmorChangeEvent event) {
        if (event.getOldItem() != null && event.getNewItem() != null && event.getOldItem().getType().equals(event.getNewItem().getType())) {
            return;
        }

        final Player player = event.getPlayer();

        if (recentlyLoggedIn.contains(player.getUniqueId())) {
            return;
        }

        final Class actualClass = service.getClassManager().getCurrentClass(player);
        final Class expectedClass = service.getClassManager().getClassByArmor(player);

        if (expectedClass != null) {
            if (actualClass != null) {
                actualClass.deactivate(player);
            }

            final PlayerClassReadyEvent readyEvent = new PlayerClassReadyEvent(player, expectedClass);
            Bukkit.getPluginManager().callEvent(readyEvent);

            return;
        }

        if (actualClass != null) {
            final PlayerClassDeactivateEvent deactivateEvent = new PlayerClassDeactivateEvent(player, actualClass);
            Bukkit.getPluginManager().callEvent(deactivateEvent);
            actualClass.deactivate(player, actualClass.getActivePlayers().contains(player.getUniqueId()));
        } else {
            final PlayerClassUnreadyEvent unreadyEvent = new PlayerClassUnreadyEvent(player);
            Bukkit.getPluginManager().callEvent(unreadyEvent);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        final Entity damager = event.getDamager();
        final Entity damaged = event.getEntity();
        double damage = event.getFinalDamage();

        if (event.isCancelled()) {
            return;
        }

        if (!(damager instanceof Arrow)) {
            return;
        }

        if (!(damaged instanceof LivingEntity)) {
            return;
        }

        final Projectile arrow = (Projectile)damager;

        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        final Player player = (Player)arrow.getShooter();
        final Class playerClass = getService().getClassManager().getCurrentClass(player);

        if (!(playerClass instanceof ArcherClass)) {
            return;
        }

        final ArcherClass archerClass = (ArcherClass)playerClass;
        final double maxDamage = archerClass.getMaxDealtDamage();
        final double damagePerBlock = archerClass.getDamagePerBlock();
        final Location locA = player.getLocation().clone();
        final Location locB = damaged.getLocation().clone();

        locA.setY(64.0);
        locB.setY(64.0);

        final double distance = locA.distance(locB);
        final double finalDamage = (((damagePerBlock * distance) + damage) > maxDamage) ? maxDamage : (damagePerBlock * distance) + damage;

        event.setDamage(finalDamage);

        final double healthPre = ((LivingEntity) damaged).getHealth();

        if (damaged instanceof Player) {
            final Player playerDamaged = (Player)damaged;
            playerDamaged.sendMessage(ChatColor.RED + "You have been shot by an " + ChatColor.DARK_RED + "" + ChatColor.BOLD + "ARCHER!");
        }

        new Scheduler(getService().getOwner()).sync(() -> {
            final double healthPost = ((LivingEntity) damaged).getHealth();
            final double diff = (healthPre - healthPost) / 2;
            final String name = ((LivingEntity)damaged).hasPotionEffect(PotionEffectType.INVISIBILITY) ? ChatColor.GRAY + "? ? ?" : ChatColor.GOLD + damaged.getName();

            player.sendMessage(ChatColor.YELLOW + "Your arrow has" + ChatColor.RED + " pierced " + name +
                    ChatColor.YELLOW + " from a distance of " + ChatColor.BLUE + String.format("%.2f", distance) + " blocks " +
                    ChatColor.YELLOW + "(" + ChatColor.RED + String.format("%.2f", diff) + " ❤" + ChatColor.YELLOW + ")");
        }).delay(1L).run();

        // Old HCF Format
        /* player.sendMessage(ChatColor.YELLOW + "[" + ChatColor.BLUE + "Archer" + ChatColor.YELLOW + " w/ " + ChatColor.BLUE + "Range" + ChatColor.YELLOW +
                "(" + ChatColor.RED + String.format("%.2f", distance) + ChatColor.YELLOW + ")]: Damage Increase (" + ChatColor.RED + String.format("%.2f", damage) + ChatColor.YELLOW + " => " +
                ChatColor.BLUE + String.format("%.2f", finalDamage) + ChatColor.YELLOW + ")"); */
    }

    @EventHandler
    public void onConsume(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (event.getHand() == null) {
            return;
        }

        final ItemStack hand = player.getInventory().getItem(event.getHand());
        final Action action = event.getAction();

        if (!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        // Prevents ASSERTION ERROR: TRAP
        if (!player.getGameMode().equals(GameMode.SURVIVAL)) {
            return;
        }

        final Class playerClass = service.getClassManager().getCurrentClass(player);

        if (hand == null || hand.getType().equals(Material.AIR)) {
            return;
        }

        if (playerClass == null || playerClass.getConsumables().isEmpty()) {
            return;
        }

        final ClassConsumable consumable = playerClass.getConsumableByMaterial(hand.getType());

        if (consumable == null) {
            return;
        }

        // Prevents the physical item from being used
        if (consumable.getMaterial().equals(Material.EYE_OF_ENDER)) {
            event.setCancelled(true);
        }

        if (consumable.hasCooldown(player)) {
            player.sendMessage(ChatColor.RED + WordUtils.capitalize(consumable.getEffectType().getName().toLowerCase().replace("_", " ")) + " is locked for " +
                    ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(consumable.getPlayerCooldown(player) - Time.now()) + ChatColor.RED + "s");

            return;
        }

        final ConsumeClassItemEvent consumeClassItemEvent = new ConsumeClassItemEvent(player, consumable);
        Bukkit.getPluginManager().callEvent(consumeClassItemEvent);

        if (consumeClassItemEvent.isCancelled()) {
            return;
        }

        consumable.consume(player, event.getHand());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerDamagePlayer(PlayerDamagePlayerEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();
        final Vector attackerDirection = event.getDamager().getLocation().getDirection();
        final Vector attackedDirection = event.getDamaged().getLocation().getDirection();
        final double dot = attackerDirection.dot(attackedDirection);
        final UUID attackerUUID = attacker.getUniqueId();

        if (attacked.getHealth() <= 0.0 || attacked.isDead() || (attacked.getHealth() - event.getDamage()) <= 0.0) {
            return;
        }

        if (attacker.getInventory().getItemInMainHand() == null || !attacker.getInventory().getItemInMainHand().getType().equals(Material.GOLD_SWORD)) {
            return;
        }

        final Class playerClass = getService().getClassManager().getCurrentClass(attacker);

        if (!(playerClass instanceof RogueClass)) {
            return;
        }

        final RogueClass rogue = (RogueClass)playerClass;

        if (rogue.hasBackstabCooldown(attacker)) {
            final long timeUntilNextAttack = (rogue.getBackstabCooldowns().getOrDefault(attacker.getUniqueId(), 0L) - Time.now());
            attacker.sendMessage(ChatColor.RED + "Backstab is locked for " + ChatColor.RED + "" + ChatColor.BOLD + Time.convertToDecimal(timeUntilNextAttack) + ChatColor.RED + "s");
            return;
        }

        if (dot >= 0.825 && dot <= 1.0) {
            final RogueBackstabEvent backstabEvent = new RogueBackstabEvent(attacker, attacked);
            Bukkit.getPluginManager().callEvent(backstabEvent);

            if (backstabEvent.isCancelled()) {
                return;
            }

            new Scheduler(getService().getOwner()).sync(() -> {
                if (attacked.isDead() || attacked.getHealth() <= 0.0) {
                    return;
                }

                final double newHealth = ((attacked.getHealth() - rogue.getBackstabDamage()) > 0.0) ? attacked.getHealth() - rogue.getBackstabDamage() : 0.0;
                attacked.setHealth(newHealth);
                attacked.sendMessage(ChatColor.RED + "You have been " + ChatColor.DARK_RED + "" + ChatColor.BOLD + "BACKSTABBED!");

                attacker.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                Players.playSound(attacker, Sound.ENTITY_ITEM_BREAK);
                attacked.getWorld().spawnParticle(Particle.HEART, attacked.getLocation().add(0, 1, 0), 5, 0.8, 0.8, 0.8, 0.8);

                if (!attacked.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    attacker.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.RED + "backstabbed" + ChatColor.GOLD + " " + attacked.getName());
                } else {
                    attacker.sendMessage(ChatColor.YELLOW + "You have " + ChatColor.RED + "backstabbed" + ChatColor.GRAY + " ? ? ?");
                }

                final long nextBackstab = Time.now() + (rogue.getBackstabCooldown() * 1000L);
                rogue.getBackstabCooldowns().put(attackerUUID, nextBackstab);

                new Scheduler(getService().getOwner()).sync(() -> {
                    rogue.getBackstabCooldowns().remove(attackerUUID);

                    if (Bukkit.getPlayer(attackerUUID) != null) {
                        Bukkit.getPlayer(attackerUUID).sendMessage(ChatColor.GREEN + rogue.getName() + " backstab is ready");
                        Players.playSound(Bukkit.getPlayer(attackerUUID), Sound.BLOCK_NOTE_CHIME);
                    }
                }).delay(rogue.getBackstabCooldown() * 20).run();
            }).run();
        }
    }
}