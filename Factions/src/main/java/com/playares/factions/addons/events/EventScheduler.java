package com.playares.factions.addons.events;

import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitTask;

public final class EventScheduler {
    @Getter public final EventsManager manager;
    @Getter @Setter public BukkitTask scheduler;

    EventScheduler(EventsManager manager) {
        this.manager = manager;
    }

    public void start() {
        if (scheduler != null && !scheduler.isCancelled()) {
            return;
        }

        scheduler = new Scheduler(manager.getAddon().getPlugin()).async(() -> {
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
        if (scheduler == null || scheduler.isCancelled()) {
            return;
        }

        scheduler.cancel();
        scheduler = null;
    }
}
