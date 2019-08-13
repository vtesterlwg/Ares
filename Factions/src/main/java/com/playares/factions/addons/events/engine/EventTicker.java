package com.playares.factions.addons.events.engine;

import com.google.common.collect.Sets;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.events.data.type.AresEvent;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import com.playares.factions.factions.data.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;

public final class EventTicker {
    @Getter public final EventsAddon addon;
    @Getter @Setter public BukkitTask tickerTask;

    public EventTicker(EventsAddon addon) {
        this.addon = addon;
    }

    public void start() {
        this.tickerTask = new Scheduler(addon.getPlugin()).sync(() -> {
            for (AresEvent event : addon.getManager().getActiveEvents()) {
                if (event instanceof KOTHEvent) {
                    final KOTHEvent koth = (KOTHEvent)event;
                    final Set<UUID> playersInCaptureRegion = Sets.newHashSet();
                    final Set<PlayerFaction> factionsInCaptureRegion = Sets.newHashSet();

                    if (koth.getSession().getTimer().isExpired()) {
                        koth.getSession().getTimer().onFinish();
                        continue;
                    }

                    Bukkit.getOnlinePlayers().forEach(player -> {
                        final PLocatable location = new PLocatable(player);

                        if (koth.getCaptureRegion().inside(location)) {
                            playersInCaptureRegion.add(player.getUniqueId());
                        }
                    });

                    if (playersInCaptureRegion.isEmpty() && koth.getSession().getCapturingFaction() != null) {
                        koth.getSession().reset();
                        continue;
                    }

                    playersInCaptureRegion.forEach(id -> {
                        final PlayerFaction faction = getAddon().getPlugin().getFactionManager().getFactionByPlayer(id);

                        if (faction != null) {
                            factionsInCaptureRegion.add(faction);
                        }
                    });

                    if (factionsInCaptureRegion.isEmpty() && koth.getSession().getCapturingFaction() != null) {
                        koth.getSession().reset();
                        continue;
                    }

                    // Capturing faction is no longer in the region
                    if (koth.getSession().getCapturingFaction() != null && !factionsInCaptureRegion.contains(koth.getSession().getCapturingFaction())) {
                        koth.getSession().reset();
                        continue;
                    }

                    // Event has multiple factions and one of them is the capturing faction
                    if (factionsInCaptureRegion.size() >= 2) {
                        if (koth.getSession().isContested()) {
                            continue;
                        }

                        koth.getSession().setContested(factionsInCaptureRegion);

                        continue;
                    }

                    if (factionsInCaptureRegion.size() == 1 && factionsInCaptureRegion.contains(koth.getSession().getCapturingFaction())) {
                        if (koth.getSession().isContested()) {
                            koth.getSession().setUncontested(false);
                        }

                        continue;
                    }

                    if (koth.getSession().getCapturingFaction() == null) {
                        factionsInCaptureRegion.stream().findFirst().ifPresent(faction -> {
                            koth.getSession().getTimer().unfreeze(); // Unfreeze has to be first because it sets the expire time
                            koth.getSession().getTimer().setExpire(Time.now() + (koth.getSession().getTimerDuration() * 1000L));
                            koth.getSession().setCapturingFaction(faction);
                            Bukkit.broadcastMessage(EventsAddon.PREFIX + koth.getDisplayName() + ChatColor.GOLD + " is now being controlled by " + ChatColor.YELLOW + faction.getName());
                        });
                    }
                }
            }
        }).repeat(0L, 10L).run();
    }

    public void stop() {
        if (this.tickerTask != null && !this.tickerTask.isCancelled()) {
            this.tickerTask.cancel();
            this.tickerTask = null;
        }
    }

    public boolean isStarted() {
        return this.tickerTask != null && !this.tickerTask.isCancelled();
    }
}