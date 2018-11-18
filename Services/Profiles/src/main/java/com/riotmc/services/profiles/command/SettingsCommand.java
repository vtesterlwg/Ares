package com.riotmc.services.profiles.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.playares.commons.bukkit.menu.Menu;
import com.riotmc.services.profiles.ProfileService;
import com.riotmc.services.profiles.data.RiotProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class SettingsCommand extends BaseCommand {
    @Getter
    public ProfileService profileService;

    @CommandAlias("settings|config|bukkitsettings")
    @Description("Access your Global Settings")
    public void onCommand(Player player) {
        final Menu menu = profileService.getMenuHandler().createSettingsMenu(player);
        final RiotProfile profile = profileService.getProfile(player.getUniqueId());

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Could not find your Ares Profile");
            return;
        }

        profileService.getMenuHandler().showSettingsMenu(profile.getSettings(), menu);
    }
}
