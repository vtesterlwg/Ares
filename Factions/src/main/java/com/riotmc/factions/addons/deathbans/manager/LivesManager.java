package com.riotmc.factions.addons.deathbans.manager;

import com.mongodb.client.model.Filters;
import com.riotmc.commons.base.promise.FailablePromise;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.addons.deathbans.DeathbanAddon;
import com.riotmc.factions.addons.deathbans.dao.LivesDAO;
import com.riotmc.factions.addons.deathbans.data.LivesPlayer;
import com.riotmc.factions.addons.deathbans.handler.LivesHandler;
import com.riotmc.services.profiles.ProfileService;
import com.riotmc.services.profiles.data.RiotProfile;
import lombok.Getter;

import java.util.UUID;

public final class LivesManager {
    @Getter public final DeathbanAddon addon;
    @Getter public final LivesHandler handler;

    public LivesManager(DeathbanAddon addon) {
        this.addon = addon;
        this.handler = new LivesHandler(this);
    }

    public void getLives(UUID uniqueId, FailablePromise<LivesPlayer> promise) {
        new Scheduler(addon.getPlugin()).async(() -> {
            final LivesPlayer player = LivesDAO.getLivesPlayer(addon.getPlugin().getMongo(), Filters.eq("id", uniqueId));

            new Scheduler(addon.getPlugin()).sync(() -> {
                if (player == null) {
                    promise.failure("Player not found");
                    return;
                }

                promise.success(player);
            }).run();
        }).run();
    }

    public void getLives(String username, FailablePromise<LivesPlayer> promise) {
        final ProfileService profileService = (ProfileService)addon.getPlugin().getService(ProfileService.class);

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        new Scheduler(addon.getPlugin()).async(() -> {
            final RiotProfile profile = profileService.getProfileBlocking(username);

            if (profile == null) {
                new Scheduler(addon.getPlugin()).sync(() -> promise.failure("Player not found")).run();
                return;
            }

            final LivesPlayer player = LivesDAO.getLivesPlayer(addon.getPlugin().getMongo(), Filters.eq("id", profile.getUniqueId()));

            new Scheduler(addon.getPlugin()).sync(() -> {
                if (player == null) {
                    promise.failure("Player not found");
                    return;
                }

                promise.success(player);
            }).run();
        }).run();
    }
}