package com.playares.services.playerclasses.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.playerclasses.PlayerClassService;
import com.playares.services.playerclasses.event.ConsumeClassItemEvent;
import lombok.Getter;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ClassConsumable {
    @Getter public final PlayerClassService service;
    @Getter public final Material material;
    @Getter public final int duration;
    @Getter public final int cooldown;
    @Getter public final ConsumableApplicationType applicationType;
    @Getter public final PotionEffectType effectType;
    @Getter public final int effectAmplifier;
    @Getter public final Map<UUID, Long> playerCooldowns;

    public ClassConsumable(PlayerClassService service,
                           Material material,
                           int duration,
                           int cooldown,
                           ConsumableApplicationType applicationType,
                           PotionEffectType effectType,
                           int effectAmplifier) {

        this.service = service;
        this.material = material;
        this.duration = duration;
        this.cooldown = cooldown;
        this.applicationType = applicationType;
        this.effectType = effectType;
        this.effectAmplifier = effectAmplifier;
        this.playerCooldowns = Maps.newConcurrentMap();

    }

    public boolean hasCooldown(Player player) {
        return playerCooldowns.containsKey(player.getUniqueId());
    }

    public long getPlayerCooldown(Player player) {
        return playerCooldowns.getOrDefault(player.getUniqueId(), 0L);
    }

    public void consume(Player player) {
        final UUID playerUUID = player.getUniqueId();
        final ConsumeClassItemEvent consumeEvent = new ConsumeClassItemEvent(player, this);

        Bukkit.getPluginManager().callEvent(consumeEvent);

        if (consumeEvent.isCancelled()) {
            return;
        }

        final PotionEffect existing = (player.hasPotionEffect(effectType) ? player.getPotionEffect(effectType) : null);

        if (player.hasPotionEffect(effectType)) {
            player.removePotionEffect(effectType);
        }

        player.addPotionEffect(new PotionEffect(effectType, (duration * 20), effectAmplifier));
        player.sendMessage(ChatColor.LIGHT_PURPLE + "You now have " + ChatColor.AQUA + WordUtils.capitalize(effectType.getName().toLowerCase().replace("_", " ")) + " " +
                (effectAmplifier + 1) + ChatColor.LIGHT_PURPLE + " for " + ChatColor.AQUA + duration + " seconds");

        if (existing != null) {
            new Scheduler(getService().getOwner()).sync(() -> player.addPotionEffect(existing)).delay(duration * 20).run();
        }

        playerCooldowns.put(player.getUniqueId(), (Time.now() + (cooldown * 1000L)));
        new Scheduler(getService().getOwner()).sync(() -> playerCooldowns.remove(playerUUID)).delay(getCooldown() * 20).run();

        if (applicationType.equals(ConsumableApplicationType.INDIVIDUAL)) {
            return;
        }

        final Set<UUID> affected = Sets.newHashSet();

        if (!applicationType.equals(ConsumableApplicationType.ALL)) {
            for (UUID uuid : consumeEvent.getAffectedPlayers().keySet()) {
                final boolean friendly = consumeEvent.getAffectedPlayers().get(uuid);

                if (applicationType.equals(ConsumableApplicationType.FRIENDLY_ONLY) && friendly) {
                    affected.add(uuid);
                    continue;
                }

                if (applicationType.equals(ConsumableApplicationType.ENEMY_ONLY) && !friendly) {
                    affected.add(uuid);
                }
            }
        } else {
            affected.addAll(consumeEvent.affectedPlayers.keySet());
        }

        affected.forEach(uuid -> {
            final Player affectedPlayer = Bukkit.getPlayer(uuid);

            if (affectedPlayer != null) {
                final PotionEffect affectedExisting = (affectedPlayer.hasPotionEffect(effectType) ? affectedPlayer.getPotionEffect(effectType) : null);

                if (affectedPlayer.hasPotionEffect(effectType)) {
                    affectedPlayer.removePotionEffect(effectType);
                }

                affectedPlayer.addPotionEffect(new PotionEffect(effectType, (duration * 20), effectAmplifier));

                switch (applicationType) {
                    case ALL: affectedPlayer.sendMessage(ChatColor.LIGHT_PURPLE + "You now have " + ChatColor.AQUA + WordUtils.capitalize(effectType.getName().toLowerCase().replace("_", " ")) + " " +
                            (effectAmplifier + 1) + ChatColor.LIGHT_PURPLE + " for " + ChatColor.AQUA + duration + " seconds"); break;
                    case FRIENDLY_ONLY: affectedPlayer.sendMessage(ChatColor.LIGHT_PURPLE + "You now have " + ChatColor.AQUA + WordUtils.capitalize(effectType.getName().toLowerCase().replace("_", " ")) + " " +
                            (effectAmplifier + 1) + ChatColor.LIGHT_PURPLE + " for " + ChatColor.AQUA + duration + " seconds " + ChatColor.LIGHT_PURPLE + "thanks to " + ChatColor.AQUA + player.getName()); break;
                    case ENEMY_ONLY: affectedPlayer.sendMessage(ChatColor.RED + "You now have " + ChatColor.BLUE + WordUtils.capitalize(effectType.getName().toLowerCase().replace("_", " ")) + " " +
                            (effectAmplifier + 1) + ChatColor.RED + " for " + ChatColor.BLUE + duration + " seconds"); break;
                }

                if (affectedExisting != null) {
                    new Scheduler(getService().getOwner()).sync(() -> affectedPlayer.addPotionEffect(affectedExisting)).delay(duration * 20).run();
                }
            }
        });
    }

    public enum ConsumableApplicationType {
        INDIVIDUAL, FRIENDLY_ONLY, ENEMY_ONLY, ALL;
    }
}