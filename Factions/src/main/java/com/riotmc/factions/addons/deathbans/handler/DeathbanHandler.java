package com.riotmc.factions.addons.deathbans.handler;

import com.mongodb.client.model.Filters;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.commons.base.util.Time;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.addons.deathbans.dao.DeathbanDAO;
import com.riotmc.factions.addons.deathbans.dao.LivesDAO;
import com.riotmc.factions.addons.deathbans.data.Deathban;
import com.riotmc.factions.addons.deathbans.data.LivesPlayer;
import com.riotmc.factions.addons.deathbans.manager.DeathbanManager;
import com.riotmc.services.profiles.ProfileService;
import com.riotmc.services.profiles.data.RiotProfile;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class DeathbanHandler {
    public final DeathbanManager manager;

    public DeathbanHandler(DeathbanManager manager) {
        this.manager = manager;
    }

    public void clear(SimplePromise promise) {
        Logger.warn("Clearing all deathbans");

        new Scheduler(manager.getAddon().getPlugin()).async(() -> {
            DeathbanDAO.clearDeathbans(manager.getAddon().getPlugin().getMongo());

            new Scheduler(manager.getAddon().getPlugin()).sync(() -> {
                Logger.print("Cleared deathbans");
                promise.success();
            }).run();
        }).run();
    }

    public void deathban(UUID uniqueId, int seconds, boolean permanent) {
        final long ms = (seconds * 1000L);
        final Deathban deathban = new Deathban(uniqueId, Time.now() + ms, permanent);

        new Scheduler(manager.getAddon().getPlugin()).async(() -> {
            DeathbanDAO.saveDeathban(manager.getAddon().getPlugin().getMongo(), deathban);

            new Scheduler(manager.getAddon().getPlugin()).sync(() -> {
                final Player player = Bukkit.getPlayer(uniqueId);

                if (player != null) {
                    manager.getRecentlyKicked().add(uniqueId);
                    player.kickPlayer(manager.getDeathbanMessage(deathban));
                    new Scheduler(manager.getAddon().getPlugin()).delay(60L).sync(() -> manager.getRecentlyKicked().remove(uniqueId)).run();
                }
            }).run();
        }).run();
    }

    public void revive(CommandSender commandSender, String username, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)manager.getAddon().getPlugin().getService(ProfileService.class);
        final Player player = (commandSender instanceof Player) ? (Player)commandSender : null;
        final boolean admin = commandSender.hasPermission("factions.admin");

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        new Scheduler(manager.getAddon().getPlugin()).async(() -> {
            final RiotProfile revivedProfile = profileService.getProfileBlocking(username);

            if (revivedProfile == null) {
                new Scheduler(manager.getAddon().getPlugin()).sync(() -> promise.failure("Player not found")).run();
                return;
            }

            final LivesPlayer sender = (player != null) ? LivesDAO.getLivesPlayer(manager.getAddon().getPlugin().getMongo(), Filters.eq("id", player.getUniqueId())) : null;
            final Deathban deathban = DeathbanDAO.getDeathban(manager.getAddon().getPlugin().getMongo(), revivedProfile.getUniqueId());

            new Scheduler(manager.getAddon().getPlugin()).sync(() -> {
                if (deathban == null) {
                    promise.failure("Player is not deathbanned");
                    return;
                }

                if (deathban.isPermanent() && !admin) {
                    promise.failure("This player is banned until deathbans are cleared by staff");
                    return;
                }

                if (sender != null && !admin) {
                    if (sender.getStandardLives() <= 0) {
                        promise.failure("You do not have enough lives");
                        return;
                    }
                }

                new Scheduler(manager.getAddon().getPlugin()).async(() -> {
                    if (sender != null && !admin) {
                        sender.setStandardLives(sender.getStandardLives() - 1);
                        LivesDAO.saveLivesPlayer(manager.getAddon().getPlugin().getMongo(), sender);
                    }

                    DeathbanDAO.deleteDeathban(manager.getAddon().getPlugin().getMongo(), deathban);

                    new Scheduler(manager.getAddon().getPlugin()).sync(() -> {
                        Logger.print(commandSender.getName() + " revived " + revivedProfile.getUsername());
                        promise.success();
                    }).run();
                }).run();
            }).run();
        }).run();
    }
}
