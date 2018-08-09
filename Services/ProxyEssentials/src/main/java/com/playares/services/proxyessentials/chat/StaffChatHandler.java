package com.playares.services.proxyessentials.chat;

import com.playares.services.proxyessentials.ProxyEssentialsService;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public final class StaffChatHandler implements Listener {
    @Getter
    public ProxyEssentialsService service;

    public StaffChatHandler(ProxyEssentialsService service) {
        this.service = service;
        service.registerListener(this);
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        final ProxiedPlayer sender = (ProxiedPlayer)event.getSender();
        final String message = event.getMessage();

        if (service.getStaffChatManager().isInStaffChat(sender)) {
            event.setCancelled(true);

            for (ProxiedPlayer viewer : service.getProxy().getProxy().getPlayers()) {
                if (viewer.hasPermission("proxyessentials.staffchat")) {
                    viewer.sendMessage(ChatColor.BLUE + "[" + ChatColor.DARK_AQUA + "Staff" + ChatColor.BLUE + "] " +
                            ChatColor.AQUA + sender.getName() + ChatColor.WHITE + ": " + message);
                }
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        final ProxiedPlayer player = event.getPlayer();

        if (service.getStaffChatManager().isInStaffChat(player)) {
            service.getStaffChatManager().getStaffChat().remove(player.getUniqueId());
        }
    }
}