package com.riotmc.factions.addons.deathbans.handler;

import com.mongodb.client.model.Filters;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.addons.deathbans.dao.LivesDAO;
import com.riotmc.factions.addons.deathbans.data.LivesPlayer;
import com.riotmc.factions.addons.deathbans.manager.LivesManager;
import com.riotmc.services.profiles.ProfileService;
import com.riotmc.services.profiles.data.RiotProfile;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class LivesHandler {
    @Getter public final LivesManager manager;

    public LivesHandler(LivesManager manager) {
        this.manager = manager;
    }

    public void createProfile(Player player) {
        new Scheduler(manager.getAddon().getPlugin()).async(() -> {
            if (LivesDAO.getLivesPlayer(manager.getAddon().getPlugin().getMongo(), Filters.eq("id", player.getUniqueId())) == null) {
                final LivesPlayer newProfile = new LivesPlayer(player.getUniqueId(), 0, 0);
                LivesDAO.saveLivesPlayer(manager.getAddon().getPlugin().getMongo(), newProfile);
            }
        }).run();
    }

    public void give(CommandSender commandSender, String username, int amount, SimplePromise promise) {
        if (amount <= 0) {
            promise.failure("Amount must be greater than 0");
            return;
        }

        final ProfileService profileService = (ProfileService)manager.getAddon().getPlugin().getService(ProfileService.class);
        final Player player = (commandSender instanceof Player) ? (Player)commandSender : null;
        final boolean admin = commandSender.hasPermission("factions.admin");

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        new Scheduler(manager.getAddon().getPlugin()).async(() -> {
            final RiotProfile receiverProfile = profileService.getProfileBlocking(username);

            if (receiverProfile == null) {
                new Scheduler(manager.getAddon().getPlugin()).sync(() -> promise.failure("Player not found")).run();
                return;
            }

            final LivesPlayer sender = (player != null) ? LivesDAO.getLivesPlayer(manager.getAddon().getPlugin().getMongo(), Filters.eq("id", player.getUniqueId())) : null;
            final LivesPlayer receiver = LivesDAO.getLivesPlayer(manager.getAddon().getPlugin().getMongo(), Filters.eq("id", receiverProfile.getUniqueId()));

            new Scheduler(manager.getAddon().getPlugin()).sync(() -> {
                if (sender != null && !admin) {
                    if (sender.getStandardLives() < amount) {
                        promise.failure("You do not have enough lives");
                        return;
                    }
                }

                if (receiver == null) {
                    promise.failure("Player not found");
                    return;
                }

                new Scheduler(manager.getAddon().getPlugin()).async(() -> {
                    if (sender != null && !admin) {
                        sender.setStandardLives(sender.getStandardLives() - amount);
                        LivesDAO.saveLivesPlayer(manager.getAddon().getPlugin().getMongo(), sender);
                    }

                    receiver.setStandardLives(receiver.getStandardLives() + amount);
                    LivesDAO.saveLivesPlayer(manager.getAddon().getPlugin().getMongo(), receiver);

                    new Scheduler(manager.getAddon().getPlugin()).sync(() -> {
                        final Player bukkitReceiver = Bukkit.getPlayer(username);

                        if (bukkitReceiver != null) {
                            bukkitReceiver.sendMessage(ChatColor.GREEN + "You have received " + amount + " lives from " + commandSender.getName());
                        }

                        Logger.print(commandSender.getName() + " gave " + amount + " lives to " + receiverProfile.getUsername());
                        promise.success();
                    }).run();
                }).run();
            }).run();
        }).run();
    }

    public void set(CommandSender commandSender, String username, int amount, SimplePromise promise) {
        // TODO: Handle setting lives
    }
}