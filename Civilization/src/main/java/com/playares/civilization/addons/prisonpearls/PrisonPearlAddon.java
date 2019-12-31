package com.playares.civilization.addons.prisonpearls;

import com.playares.civilization.addons.AddonManager;
import com.playares.civilization.addons.CivAddon;
import com.playares.civilization.addons.prisonpearls.listener.PrisonPearlListener;
import lombok.Getter;
import org.bukkit.Bukkit;

public final class PrisonPearlAddon implements CivAddon {
    @Getter public final AddonManager addonManager;
    @Getter public final PrisonPearlManager prisonPearlManager;

    public PrisonPearlAddon(AddonManager addonManager) {
        this.addonManager = addonManager;
        this.prisonPearlManager = new PrisonPearlManager(this);
    }

    @Override
    public String getName() {
        return "Prison Pearl";
    }

    @Override
    public void prepare() {
        Bukkit.getPluginManager().registerEvents(new PrisonPearlListener(this), addonManager.getPlugin());
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
