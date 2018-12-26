package com.riotmc.factions.addons.deathbans.listener;

import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.addons.deathbans.DeathbanAddon;
import com.riotmc.factions.addons.deathbans.dao.DeathbanDAO;
import com.riotmc.factions.addons.deathbans.data.Deathban;
import com.riotmc.factions.addons.loggers.data.CombatLogger;
import com.riotmc.factions.addons.loggers.event.LoggerDeathEvent;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public final class DeathbanListener implements Listener {
    @Getter public final DeathbanAddon addon;

    public DeathbanListener(DeathbanAddon addon) {
        this.addon = addon;
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        final UUID uniqueId = event.getUniqueId();
        final Deathban deathban = DeathbanDAO.getDeathban(addon.getPlugin().getMongo(), uniqueId);

        if (deathban != null && !deathban.isExpired()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, addon.getDeathbanManager().getDeathbanMessage(deathban));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        addon.getLivesManager().getHandler().createProfile(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();

        if (player.hasPermission("factions.mod") || player.hasPermission("factions.admin") || player.hasPermission("factions.deathbans.bypass")) {
            return;
        }

        new Scheduler(addon.getPlugin()).async(() -> {
            final int deathbanDuration = addon.getDeathbanManager().calculateDeathbanDuration(player.getUniqueId());
            new Scheduler(addon.getPlugin()).sync(() -> addon.getDeathbanManager().getHandler().deathban(player.getUniqueId(), deathbanDuration, false)).run();
        }).run();
    }

    @EventHandler
    public void onLoggerDeath(LoggerDeathEvent event) {
        final CombatLogger logger = event.getLogger();

        new Scheduler(addon.getPlugin()).async(() -> {
            final int deathbanDuration = addon.getDeathbanManager().calculateDeathbanDuration(logger.getUniqueID());
            new Scheduler(addon.getPlugin()).sync(() -> addon.getDeathbanManager().getHandler().deathban(logger.getUniqueID(), deathbanDuration, false)).run();
        }).run();
    }
}