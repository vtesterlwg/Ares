package com.playares.services.automatedrestarts;

import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.service.AresService;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.serversync.ServerSyncService;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;
import org.spigotmc.RestartCommand;

public final class AutomatedRestartService implements AresService {
    static final String PREFIX = ChatColor.DARK_RED + "[" + ChatColor.RED + "Rebooting" + ChatColor.DARK_RED + "]";
    static int DEFAULT_REBOOT_TIME = 300; // TODO: Make configurable

    @Getter public final AresPlugin owner;
    @Getter public final RebootHandler handler;
    @Getter @Setter public long rebootCommenceTime;
    @Getter @Setter public long rebootTime;
    @Getter @Setter public boolean inProgress;
    @Getter @Setter public BukkitTask checkTask;

    public AutomatedRestartService(AresPlugin owner, int nextRebootTimeSeconds) {
        this.owner = owner;
        this.handler = new RebootHandler(this);
        this.rebootCommenceTime = Time.now() + (nextRebootTimeSeconds * 1000);
        this.rebootTime = 0L;
        this.inProgress = false;
    }

    @Override
    public void start() {
        owner.registerCommand(new RebootCommand(this));

        this.checkTask = new Scheduler(owner).async(() -> {
            if (!isInProgress() && Time.now() >= getRebootCommenceTime()) {
                handler.startCountdown(DEFAULT_REBOOT_TIME);
            }

            if (isInProgress() && Time.now() >= getRebootTime()) {
                final ServerSyncService syncService = (ServerSyncService)getOwner().getService(ServerSyncService.class);

                if (syncService != null) {
                    Bukkit.getOnlinePlayers().forEach(syncService::sendToLobby);
                }

                RestartCommand.restart();
            }
        }).repeat(20L, 20L).run();
    }

    @Override
    public void stop() {
        if (checkTask != null) {
            checkTask.cancel();
        }

        this.inProgress = false;
        this.rebootTime = 0L;
        this.rebootCommenceTime = 0L;
    }

    @Override
    public String getName() {
        return "Automated Restarts";
    }

    public long getTimeUntilReboot() {
        if (isInProgress()) {
            return rebootTime - Time.now();
        }

        return rebootCommenceTime - Time.now();
    }
}