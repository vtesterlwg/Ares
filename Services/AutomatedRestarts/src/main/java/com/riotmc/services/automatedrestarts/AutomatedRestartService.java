package com.riotmc.services.automatedrestarts;

import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.RiotPlugin;
import com.playares.commons.bukkit.service.RiotService;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;
import org.spigotmc.RestartCommand;

public final class AutomatedRestartService implements RiotService {
    static final String PREFIX = ChatColor.DARK_RED + "[" + ChatColor.RED + "Rebooting" + ChatColor.DARK_RED + "]";
    static int DEFAULT_REBOOT_TIME = 300; // TODO: Make configurable

    @Getter
    public final RiotPlugin owner;

    @Getter
    public final RebootHandler handler;

    @Getter @Setter
    public long rebootCommenceTime;

    @Getter @Setter
    public long rebootTime;

    @Getter @Setter
    public boolean inProgress;

    @Getter @Setter
    public BukkitTask checkTask;

    public AutomatedRestartService(RiotPlugin owner, int nextRebootTimeSeconds) {
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