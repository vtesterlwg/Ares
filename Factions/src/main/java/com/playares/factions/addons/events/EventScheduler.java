package com.playares.factions.addons.events;

import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitTask;

public final class EventScheduler {
    @Getter public final EventsManager manager;
    @Getter @Setter public BukkitTask scheduler;

    public EventScheduler(EventsManager manager) {
        this.manager = manager;


    }

    public void start() {
        if (this.scheduler != null && !this.scheduler.isCancelled()) {
            return;
        }

        this.scheduler = new Scheduler(manager.getAddon().getPlugin()).async(() -> {
            manager.getEventsThatShouldStart().stream().filter(event -> !manager.getActiveEvents().contains(event)).forEach(toStart -> {
                if (toStart instanceof KOTHEvent) {
                    final KOTHEvent koth = (KOTHEvent)toStart;

                    if (!manager.getTicker().isStarted()) {
                        manager.getTicker().start();
                    }

                    koth.start();
                }
            });
        }).repeat(0L, 60 * 20L).run();
    }

    public void stop() {
        if (this.scheduler == null || this.scheduler.isCancelled()) {
            return;
        }

        this.scheduler.cancel();
        this.scheduler = null;
    }
}
