package com.riotmc.services.proxyessentials;

import co.aikar.commands.BungeeCommandManager;
import com.riotmc.commons.bungee.RiotProxy;
import com.riotmc.commons.bungee.service.RiotService;
import com.riotmc.services.proxyessentials.chat.StaffChatHandler;
import com.riotmc.services.proxyessentials.chat.StaffChatManager;
import com.riotmc.services.proxyessentials.command.HelpopCommand;
import com.riotmc.services.proxyessentials.command.ReportCommand;
import com.riotmc.services.proxyessentials.command.StaffChatCommand;
import com.riotmc.services.proxyessentials.report.ReportHandler;
import com.riotmc.services.proxyessentials.report.ReportManager;
import com.riotmc.services.proxyessentials.request.RequestHandler;
import com.riotmc.services.proxyessentials.request.RequestManager;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public final class ProxyEssentialsService implements RiotService, Listener {
    @Getter
    public final RiotProxy proxy;

    @Getter
    protected ReportManager reportManager;

    @Getter
    protected ReportHandler reportHandler;

    @Getter
    protected RequestManager requestManager;

    @Getter
    protected RequestHandler requestHandler;

    @Getter
    protected StaffChatManager staffChatManager;

    @Getter
    protected StaffChatHandler staffChatHandler;

    public ProxyEssentialsService(RiotProxy proxy) {
        this.proxy = proxy;
    }

    public void start() {
        getProxy().registerCommandManager(new BungeeCommandManager(getProxy()));

        registerListener(this);

        registerCommand(new HelpopCommand(this));
        registerCommand(new ReportCommand(this));
        registerCommand(new StaffChatCommand(this));

        this.reportManager = new ReportManager();
        this.reportHandler = new ReportHandler(this);
        this.requestManager = new RequestManager();
        this.requestHandler = new RequestHandler(this);
        this.staffChatManager = new StaffChatManager();
        this.staffChatHandler = new StaffChatHandler(this);
    }

    public void stop() {
        this.reportManager.getRecentReports().clear();
        this.requestManager.getRecentRequests().clear();
    }

    public String getName() {
        return "Proxy Essentials";
    }

    @EventHandler
    public void onProxyJoin(PostLoginEvent event) {
        final ProxiedPlayer player = event.getPlayer();

        if (player.hasPermission("proxyessentials.notifications")) {
            getProxy().getProxy().getPlayers().forEach(viewer -> {
                if (viewer.hasPermission("proxyessentials.notifications")) {
                    viewer.sendMessage(ChatColor.BLUE + "[" + ChatColor.DARK_AQUA + "Staff" + ChatColor.BLUE + "] " + ChatColor.AQUA +
                            player.getName() + ChatColor.GREEN + " joined " + ChatColor.AQUA + "the network");
                }
            });
        }
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent event) {
        final ProxiedPlayer player = event.getPlayer();

        if (player.hasPermission("proxyessentials.notifications")) {
            getProxy().getProxy().getPlayers().forEach(viewer -> {
                if (viewer.hasPermission("proxyessentials.notifications")) {
                    viewer.sendMessage(ChatColor.BLUE + "[" + ChatColor.DARK_AQUA + "Staff" + ChatColor.BLUE + "] " + ChatColor.AQUA +
                            player.getName() + ChatColor.RED + " left " + ChatColor.AQUA + "the network");
                }
            });
        }
    }
}