package com.riotmc.services.profiles.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.playares.commons.base.promise.SimplePromise;
import com.riotmc.services.profiles.ProfileService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class MessageCommand extends BaseCommand {
    @Getter
    public final ProfileService profileService;

    @CommandAlias("message|msg|tell|t|pm")
    @Description("Send a player a message")
    public void onMessage(Player player, String receiverName, String message) {
        profileService.getMessageHandler().sendMessage(player, receiverName, message, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @CommandAlias("reply|r")
    @Description("Reply to your most recent message")
    public void onReply(Player player, String message) {
        profileService.getMessageHandler().sendReply(player, message, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}