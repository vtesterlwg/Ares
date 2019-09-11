package com.playares.services.punishments;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.profiles.ProfileService;
import com.playares.services.profiles.data.AresProfile;
import com.playares.services.profiles.data.AresProfileDAO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

@AllArgsConstructor
public final class AltsHandler {
    @Getter public final PunishmentService service;

    public void lookup(CommandSender sender, String username, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)service.getOwner().getService(ProfileService.class);

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        profileService.getProfile(username, aresProfile -> {
            if (aresProfile == null) {
                promise.failure("Player not found");
                return;
            }

            new Scheduler(getService().getOwner()).async(() -> {
                final List<AresProfile> matches = AresProfileDAO.getProfiles(getService().getOwner().getMongo(), Filters.eq("address", aresProfile.getAddress()));

                new Scheduler(getService().getOwner()).sync(() -> {
                    // Accounts connected to <username>
                    final List<String> usernames = Lists.newArrayList();

                    matches.forEach(match -> usernames.add(match.getUsername()));

                    sender.sendMessage(ChatColor.GOLD + "Accounts connected to " + ChatColor.YELLOW + aresProfile.getUsername());
                    sender.sendMessage(ChatColor.AQUA + Joiner.on(ChatColor.YELLOW + ", " + ChatColor.AQUA).join(usernames));
                }).run();
            }).run();
        });
    }
}
