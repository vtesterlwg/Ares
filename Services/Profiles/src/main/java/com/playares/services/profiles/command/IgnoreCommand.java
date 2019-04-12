package com.playares.services.profiles.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.services.profiles.ProfileService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class IgnoreCommand extends BaseCommand {
    @Getter
    public final ProfileService profileService;

    @CommandAlias("ignore|block")
    @Description("Hide messages from a player")
    public void onIgnore(Player player, String name) {
        profileService.getIgnoreHandler().ignorePlayer(player, name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "You will no longer see messages from this player");
            }

            @Override
            public void failure(String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @CommandAlias("unignore|unblock")
    @Description("Unhide a player's messages")
    public void onUnignore(Player player, String name) {
        profileService.getIgnoreHandler().unignorePlayer(player, name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "You will begin seeing messages from this player again");
            }

            @Override
            public void failure(String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}