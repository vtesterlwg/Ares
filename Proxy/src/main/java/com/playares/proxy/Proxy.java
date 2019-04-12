package com.playares.proxy;

import co.aikar.commands.BungeeCommandManager;
import com.playares.commons.bungee.AresProxy;
import com.playares.services.proxyessentials.ProxyEssentialsService;

public final class Proxy extends AresProxy {
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