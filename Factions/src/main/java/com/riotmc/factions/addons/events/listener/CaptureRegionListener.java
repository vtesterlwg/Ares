package com.riotmc.factions.addons.events.listener;

import com.google.common.collect.ImmutableList;
import com.riotmc.commons.bukkit.event.PlayerBigMoveEvent;
import com.riotmc.commons.bukkit.location.PLocatable;
import com.riotmc.factions.addons.events.EventsAddon;
import com.riotmc.factions.addons.events.event.EventContestedEvent;
import com.riotmc.factions.addons.events.event.PlayerEnterCapzoneEvent;
import com.riotmc.factions.addons.events.event.PlayerLeaveCapzoneEvent;
import com.riotmc.factions.addons.events.type.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class CaptureRegionListener implements Listener {
    @Getter public final EventsAddon addon;

    public CaptureRegionListener(EventsAddon addon) {
        this.addon = addon;
    }

    @EventHandler
    public void onEnterCapzone(PlayerEnterCapzoneEvent event) {
        final RiotEvent e = event.getEvent();

        if (e instanceof KOTHEvent) {
            final KOTHEvent koth = (KOTHEvent)e;
            final Contestable contestable = (Contestable)e;
            final boolean contested = addon.getManager().isContested(koth);

            if (contestable.isContested() == contested) {
                return;
            }

            contestable.setContested(contested);

            final EventContestedEvent contestEvent = new EventContestedEvent(contestable, contested);
            Bukkit.getPluginManager().callEvent(contestEvent);
        }
    }

    @EventHandler
    public void onLeaveCapzone(PlayerLeaveCapzoneEvent event) {
        final Player player = event.getPlayer();
        final RiotEvent e = event.getEvent();

        if (e instanceof KOTHEvent) {
            final KOTHEvent koth = (KOTHEvent)e;
            final Contestable contestable = (Contestable)e;
            final boolean contested = addon.getManager().isContested(koth);

            if (contestable.isContested() == contested) {
                return;
            }

            contestable.setContested(contested);

            final EventContestedEvent contestEvent = new EventContestedEvent(contestable, contested);
            Bukkit.getPluginManager().callEvent(contestEvent);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerBigMoveEvent event) {
        final Player player = event.getPlayer();
        final PLocatable to = new PLocatable(event.getTo().getWorld().getName(), event.getTo().getX(), event.getTo().getY(), event.getTo().getZ(), event.getTo().getYaw(), event.getTo().getPitch());

        if (event.isCancelled()) {
            return;
        }

        if (addon.getManager().getActiveEvents().isEmpty()) {
            return;
        }

        final ImmutableList<RiotEvent> registered = addon.getManager().getEventsInsideRegistered(player);
        final ImmutableList<RiotEvent> unregistered = addon.getManager().getEventsInsideUnregistered(player);

        if (!registered.isEmpty()) {
            registered.forEach(e -> {
                if (e instanceof KOTHTimer) {
                    final KOTHTimer kt = (KOTHTimer)e;

                    if (!kt.getCaptureRegion().inside(to)) {
                        final PlayerLeaveCapzoneEvent capEvent = new PlayerLeaveCapzoneEvent(player, kt, kt.getCaptureRegion());
                        Bukkit.getPluginManager().callEvent(capEvent);
                    }
                }

                else if (e instanceof KOTHTicket) {
                    final KOTHTicket kt = (KOTHTicket)e;

                    if (!kt.getCaptureRegion().inside(to)) {
                        final PlayerLeaveCapzoneEvent capEvent = new PlayerLeaveCapzoneEvent(player, kt, kt.getCaptureRegion());
                        Bukkit.getPluginManager().callEvent(capEvent);
                    }
                }
            });
        }

        if (!unregistered.isEmpty()) {
            unregistered.forEach(e -> {
                if (e instanceof KOTHTimer) {
                    final KOTHTimer kt = (KOTHTimer)e;

                    kt.getSession().getInsidePlayers().add(player.getUniqueId());

                    final PlayerEnterCapzoneEvent capEvent = new PlayerEnterCapzoneEvent(player, kt, kt.getCaptureRegion());
                    Bukkit.getPluginManager().callEvent(capEvent);
                }

                else if (e instanceof KOTHTicket) {
                    final KOTHTicket kt = (KOTHTicket)e;

                    kt.getSession().getInsidePlayers().add(player.getUniqueId());

                    final PlayerEnterCapzoneEvent capEvent = new PlayerEnterCapzoneEvent(player, kt, kt.getCaptureRegion());
                    Bukkit.getPluginManager().callEvent(capEvent);
                }
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final ImmutableList<RiotEvent> registered = addon.getManager().getEventsInsideRegistered(player);

        registered.forEach(e -> {
            if (e instanceof KOTHTicket) {
                final KOTHTicket kt = (KOTHTicket)e;
                kt.getSession().getInsidePlayers().remove(player.getUniqueId());
            }

            else if (e instanceof KOTHTimer) {
                final KOTHTimer kt = (KOTHTimer)e;
                kt.getSession().getInsidePlayers().remove(player.getUniqueId());
            }
        });
    }
}