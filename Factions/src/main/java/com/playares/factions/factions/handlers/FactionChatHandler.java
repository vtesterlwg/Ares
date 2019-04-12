package com.playares.factions.factions.handlers;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.factions.factions.data.PlayerFaction;
import com.playares.factions.factions.manager.FactionManager;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class FactionChatHandler {
    /** Owning manager **/
    @Getter public FactionManager manager;

    public FactionChatHandler(FactionManager manager) {
        this.manager = manager;
    }

    /**
     * Cycles the provided player to the next possible chat-channel
     * @param player Player
     * @param promise Promise
     */
    public void cycleChatChannel(Player player, SimplePromise promise) {
        final PlayerFaction faction = manager.getFactionByPlayer(player.getUniqueId());

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        final PlayerFaction.FactionProfile profile = faction.getMember(player.getUniqueId());

        if (profile == null) {
            promise.failure("Failed to obtain your Faction Profile");
            return;
        }

        if (profile.getChannel().equals(PlayerFaction.ChatChannel.PUBLIC)) {
            profile.setChannel(PlayerFaction.ChatChannel.FACTION);
            player.sendMessage(ChatColor.YELLOW + "You are now speaking in " + ChatColor.DARK_GREEN + "Faction Chat");
            promise.success();
            return;
        }

        if (profile.getChannel().equals(PlayerFaction.ChatChannel.FACTION)) {
            if (!profile.getRank().equals(PlayerFaction.FactionRank.MEMBER)) {
                profile.setChannel(PlayerFaction.ChatChannel.OFFICER);
                player.sendMessage(ChatColor.YELLOW + "You are now speaking in " + ChatColor.RED + "Officer Chat");
                promise.success();
                return;
            }

            profile.setChannel(PlayerFaction.ChatChannel.PUBLIC);
            player.sendMessage(ChatColor.YELLOW + "You are now speaking in " + ChatColor.LIGHT_PURPLE + "Global Chat");
            promise.success();
            return;
        }

        if (profile.getChannel().equals(PlayerFaction.ChatChannel.OFFICER)) {
            profile.setChannel(PlayerFaction.ChatChannel.PUBLIC);
            player.sendMessage(ChatColor.YELLOW + "You are now speaking in " + ChatColor.LIGHT_PURPLE + "Global Chat");
            promise.success();
            return;
        }
    }

    /**
     * Sets a players chat-channel
     * @param player Player
     * @param channelName Channel Name
     * @param promise Promise
     */
    public void selectChatChannel(Player player, String channelName, SimplePromise promise) {
        final PlayerFaction faction = manager.getFactionByPlayer(player.getUniqueId());
        final PlayerFaction.ChatChannel channel;

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        final PlayerFaction.FactionProfile profile = faction.getMember(player.getUniqueId());

        if (profile == null) {
            promise.failure("Failed to obtain your Faction Profile");
            return;
        }

        if (channelName.equalsIgnoreCase("p") || channelName.equalsIgnoreCase("pub") || channelName.equalsIgnoreCase("public")) {
            channel = PlayerFaction.ChatChannel.PUBLIC;
        } else if (channelName.equalsIgnoreCase("f") || channelName.equalsIgnoreCase("fac") || channelName.equalsIgnoreCase("faction")) {
            channel = PlayerFaction.ChatChannel.FACTION;
        } else if (channelName.equalsIgnoreCase("o") || channelName.equalsIgnoreCase("off") || channelName.equalsIgnoreCase("officer")) {
            channel = PlayerFaction.ChatChannel.OFFICER;
        } else {
            promise.failure("Invalid chat channel");
            return;
        }

        if (profile.getChannel().equals(channel)) {
            promise.failure("You are already speaking in this channel");
            return;
        }

        if (channel.equals(PlayerFaction.ChatChannel.OFFICER) && profile.getRank().equals(PlayerFaction.FactionRank.MEMBER)) {
            promise.failure("You must be an officer or higher to perform this action");
            return;
        }

        profile.setChannel(channel);

        if (channel.equals(PlayerFaction.ChatChannel.PUBLIC)) {
            player.sendMessage(ChatColor.YELLOW + "You are now speaking in " + ChatColor.LIGHT_PURPLE + "Global Chat");
            promise.success();
            return;
        }

        if (channel.equals(PlayerFaction.ChatChannel.FACTION)) {
            player.sendMessage(ChatColor.YELLOW + "You are now speaking in " + ChatColor.DARK_GREEN + "Faction Chat");
            promise.success();
            return;
        }

        if (channel.equals(PlayerFaction.ChatChannel.OFFICER)) {
            player.sendMessage(ChatColor.YELLOW + "You are now speaking in " + ChatColor.RED + "Officer Chat");
            promise.success();
        }
    }
}