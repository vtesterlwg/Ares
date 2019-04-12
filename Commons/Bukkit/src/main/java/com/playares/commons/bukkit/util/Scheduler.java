package com.playares.commons.bukkit.util;

import com.playares.commons.bukkit.AresPlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Scheduler {
    @Nonnull @Getter public AresPlugin owner;
    private boolean async;
    @Nullable private Runnable task;
    @Nonnull private RunCycle cycle;
    private long delay;
    private long interval;

    public Scheduler(@Nonnull AresPlugin owner) {
        this.owner = owner;
        this.cycle = RunCycle.ONCE;
    }

    public Scheduler sync(@Nonnull Runnable task) {
        this.task = task;
        this.async = false;
        return this;
    }

    public Scheduler async(@Nonnull Runnable task) {
        this.task = task;
        this.async = true;
        return this;
    }

    public Scheduler delay(long milliseconds) {
        this.cycle = RunCycle.DELAYED;
        this.delay = milliseconds;
        return this;
    }

    public Scheduler repeat(long delay, long interval) {
        this.cycle = RunCycle.REPEATING;
        this.delay = delay;
        this.interval = interval;
        return this;
    }

    public BukkitTask run() {
        if (task == null) {
            throw new NullPointerException("Task can not be null");
        }

        switch(this.cycle) {
            case ONCE: return (async) ? Bukkit.getScheduler().runTaskAsynchronously(owner, task) : Bukkit.getScheduler().runTask(owner, task);
            case DELAYED: return (async) ? Bukkit.getScheduler().runTaskLaterAsynchronously(owner, task, delay) : Bukkit.getScheduler().runTaskLater(owner, task, delay);
            case REPEATING: return (async) ? Bukkit.getScheduler().runTaskTimerAsynchronously(owner, task, delay, interval) : Bukkit.getScheduler().runTaskTimer(owner, task, delay, interval);
        }

        throw new NullPointerException("Run cycle can not be null");
    }

    public enum RunCycle {
        ONCE,
        DELAYED,
        REPEATING
    }
}
