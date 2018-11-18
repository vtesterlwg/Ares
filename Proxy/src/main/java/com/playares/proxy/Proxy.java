package com.playares.proxy;

import co.aikar.commands.BungeeCommandManager;
import com.playares.commons.bungee.RiotProxy;
import com.riotmc.services.proxyessentials.ProxyEssentialsService;

public final class Proxy extends RiotProxy {
    @Override
    public void onEnable() {
        registerCommandManager(new BungeeCommandManager(this));

        registerService(new ProxyEssentialsService(this));
        startServices();
    }

    @Override
    public void onDisable() {
        stopServices();
    }
}