package com.playares.services.profiles;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.services.profiles.data.RiotProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class IgnoreHandler {
    @Getter
    public final ProfileService profileService;

    public void unignorePlayer(Player player, String name, SimplePromise promise) {
        final RiotProfile profile = profileService.getProfile(player.getUniqueId());

        profileService.getProfile(name, ignoredProfile -> {
            if (ignoredProfile == null) {
                promise.failure("Player not found");
                return;
            }

            if (!profile.getSettings().isIgnoring(ignoredProfile.getUniqueId())) {
                promise.failure("You are not ignoring this player");
                return;
            }

            Logger.print(player.getName() + " is no longer ignoring " + ignoredProfile.getUsername());
            profile.getSettings().getIgnored().remove(ignoredProfile.getUniqueId());
            promise.success();
        });
    }

    public void ignorePlayer(Player player, String name, SimplePromise promise) {
        final RiotProfile profile = profileService.getProfile(player.getUniqueId());

        profileService.getProfile(name, ignoredProfile -> {
            if (ignoredProfile == null) {
                promise.failure("Player not found");
                return;
            }

            if (profile.getSettings().isIgnoring(ignoredProfile.getUniqueId())) {
                promise.failure("You are already ignoring this player");
                return;
            }

            Logger.print(player.getName() + " is now ignoring " + ignoredProfile.getUsername());
            profile.getSettings().getIgnored().add(ignoredProfile.getUniqueId());
            promise.success();
        });
    }
}
