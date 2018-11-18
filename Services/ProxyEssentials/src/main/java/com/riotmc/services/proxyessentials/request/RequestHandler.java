package com.riotmc.services.proxyessentials.request;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bungee.logging.Logger;
import com.riotmc.services.proxyessentials.ProxyEssentialsService;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class RequestHandler {
    @Getter
    public ProxyEssentialsService service;

    public RequestHandler(ProxyEssentialsService service) {
        this.service = service;
    }

    public void create(ProxiedPlayer player, String message, SimplePromise promise) {
        final UUID uniqueId = player.getUniqueId();

        if (service.getRequestManager().hasRecentlyRequested(player)) {
            promise.failure(ChatColor.RED + "Please wait a minute before sending a new request");
            return;
        }

        service.getProxy().getProxy().getPlayers().forEach(viewer -> {
            if (viewer.hasPermission("proxyessentials.request.view")) {
                viewer.sendMessage(ChatColor.GOLD + "[" + ChatColor.RED + "Request" + ChatColor.GOLD + "] [" +
                        ChatColor.RED + player.getServer().getInfo().getName() + ChatColor.GOLD + "] " +
                        ChatColor.DARK_AQUA + player.getName() + ChatColor.BLUE + " requested: " + ChatColor.YELLOW + message);
            }
        });

        service.getRequestManager().getRecentRequests().add(player.getUniqueId());
        service.getProxy().getProxy().getScheduler().schedule(service.getProxy(), () -> service.getRequestManager().getRecentRequests().remove(uniqueId), 60L, TimeUnit.SECONDS);

        Logger.print("[" + player.getServer().getInfo().getName() + "] " + player.getName() + " requested: " + message);

        promise.success();
    }
}
