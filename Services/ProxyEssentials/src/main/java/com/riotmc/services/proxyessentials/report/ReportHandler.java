package com.riotmc.services.proxyessentials.report;

import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.commons.bungee.logging.Logger;
import com.riotmc.services.proxyessentials.ProxyEssentialsService;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class ReportHandler {
    @Getter
    public ProxyEssentialsService service;

    public ReportHandler(ProxyEssentialsService service) {
        this.service = service;
    }


    public void create(ProxiedPlayer player, ProxiedPlayer reported, String message, SimplePromise promise) {
        final UUID uniqueId = player.getUniqueId();

        if (service.getReportManager().hasRecentlyReported(player)) {
            promise.failure("Please wait a minute before sending a new report");
            return;
        }

        service.getProxy().getProxy().getPlayers().forEach(viewer -> {
            if (viewer.hasPermission("proxyessentials.report.view")) {
                viewer.sendMessage(ChatColor.RED + "[" + ChatColor.DARK_RED + "Report" + ChatColor.RED + "] [" +
                        ChatColor.GOLD + player.getServer().getInfo().getName() + ChatColor.DARK_RED + "] " +
                        ChatColor.RED + player.getName() + ChatColor.DARK_RED + " reported " + ChatColor.RED + reported.getName() + " " +
                        ChatColor.DARK_RED + "for: " + ChatColor.YELLOW + message);
            }
        });

        service.getReportManager().getRecentReports().add(player.getUniqueId());
        service.getProxy().getProxy().getScheduler().schedule(service.getProxy(), () -> service.getReportManager().getRecentReports().remove(uniqueId), 60L, TimeUnit.SECONDS);

        Logger.print("[" + player.getServer().getInfo().getName() + "] " + player.getName() + " reported " + reported.getName() + " for: " + message);

        promise.success();
    }
}
