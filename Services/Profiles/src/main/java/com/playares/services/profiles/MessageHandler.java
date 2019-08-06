package com.playares.services.profiles;

import com.google.common.collect.Maps;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.services.profiles.data.AresProfile;
import com.playares.services.ranks.RankService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;

public final class MessageHandler implements Listener {
    @Getter
    public final ProfileService profileService;

    @Getter
    public final Map<UUID, UUID> recentMessages;

    public MessageHandler(ProfileService profileService) {
        this.profileService = profileService;
        this.recentMessages = Maps.newConcurrentMap();
        profileService.registerListener(this);
    }

    public Player getRecentMessage(Player player) {
        if (recentMessages.containsKey(player.getUniqueId())) {
            return Bukkit.getPlayer(recentMessages.get(player.getUniqueId()));
        }

        return null;
    }

    public void sendReply(Player sender, String message, SimplePromise promise) {
        final Player receiver = getRecentMessage(sender);

        if (receiver == null) {
            promise.failure("Player not found");
            return;
        }

        sendMessage(sender, receiver.getName(), message, promise);
    }

    public void sendMessage(Player sender, String receiverName, String message, SimplePromise promise) {
        final RankService rankService = (RankService)getProfileService().getOwner().getService(RankService.class);
        final Player receiver = Bukkit.getPlayer(receiverName);

        if (receiver == null) {
            promise.failure("Player not found");
            return;
        }

        if (receiver.getUniqueId().equals(sender.getUniqueId())) {
            promise.failure("You can not message yourself");
            return;
        }

        final AresProfile senderProfile = profileService.getProfile(sender.getUniqueId());
        final AresProfile receiverProfile = profileService.getProfile(receiverName);

        if (senderProfile.getSettings().isHidingPrivateMessages()) {
            promise.failure("You have private messages disabled. You can turn them back on in your Global Settings.");
            return;
        }

        if (senderProfile.getSettings().getIgnored().contains(receiverProfile.getUniqueId())) {
            promise.failure("You are ignoring this player. You can unblock this player by typing '/unignore " + receiverProfile.getUsername() + "'.");
            return;
        }

        if (receiverProfile.getSettings().isHidingPrivateMessages() && !sender.hasPermission("chat.ignorebypass")) {
            promise.failure("This player has private messages disabled");
            return;
        }

        if (receiverProfile.getSettings().getIgnored().contains(senderProfile.getUniqueId()) && !sender.hasPermission("chat.ignorebypass")) {
            promise.failure("This player has private messages disabled");
            return;
        }

        if (rankService != null) {
            sender.sendMessage(ChatColor.GRAY + "(To: " + rankService.formatName(receiver) + ChatColor.GRAY + "): " + ChatColor.WHITE + message);
            receiver.sendMessage(ChatColor.GRAY + "(From: " + rankService.formatName(sender) + ChatColor.GRAY + "): " + ChatColor.WHITE + message);
            recentMessages.put(receiver.getUniqueId(), sender.getUniqueId());
            Logger.print("[PM] " + sender.getName() + " -> " + receiver.getName() + ": " + message);
            promise.success();
            return;
        }

        sender.sendMessage(ChatColor.GRAY + "(To: " + ChatColor.WHITE + receiver.getName() + ChatColor.GRAY + "): " + ChatColor.WHITE + message);
        receiver.sendMessage(ChatColor.GRAY + "(From: " + ChatColor.WHITE + sender.getName() + ChatColor.GRAY + "): " + ChatColor.WHITE + message);
        recentMessages.put(receiver.getUniqueId(), sender.getUniqueId());
        Logger.print("[PM] " + sender.getName() + " -> " + receiver.getName() + ": " + message);
        promise.success();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        recentMessages.remove(event.getPlayer().getUniqueId());
    }
}