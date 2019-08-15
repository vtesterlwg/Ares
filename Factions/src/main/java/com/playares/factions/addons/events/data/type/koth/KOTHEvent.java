package com.playares.factions.addons.events.data.type.koth;

import com.google.common.collect.Lists;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.commons.bukkit.util.Players;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.events.data.region.CaptureRegion;
import com.playares.factions.addons.events.data.schedule.EventSchedule;
import com.playares.factions.addons.events.data.session.KOTHSession;
import com.playares.factions.addons.events.data.type.AresEvent;
import com.playares.factions.factions.data.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class KOTHEvent implements AresEvent {
    @Getter public final EventsAddon addon;
    @Getter @Setter public UUID ownerId;
    @Getter @Setter public String name;
    @Getter @Setter public String displayName;
    @Getter public final List<EventSchedule> schedule;
    @Getter public final BLocatable captureChestLocation;
    @Getter @Setter public KOTHSession session;
    @Getter @Setter public CaptureRegion captureRegion;
    @Getter @Setter public int defaultTicketsNeededToWin;
    @Getter @Setter public int defaultTimerDuration;

    public KOTHEvent(EventsAddon addon,
                     UUID ownerId,
                     String name,
                     String displayName,
                     Collection<EventSchedule> schedule,
                     BLocatable captureChestLocation,
                     BLocatable captureCornerA,
                     BLocatable captureCornerB,
                     int defaultTicketsNeededToWin,
                     int defaultTimerDuration) {

        this.addon = addon;
        this.ownerId = ownerId;
        this.name = name;
        this.displayName = displayName;
        this.schedule = Lists.newArrayList(schedule);
        this.captureChestLocation = captureChestLocation;
        this.session = null;
        this.captureRegion = new CaptureRegion(captureCornerA, captureCornerB);
        this.defaultTicketsNeededToWin = defaultTicketsNeededToWin;
        this.defaultTimerDuration = defaultTimerDuration;

    }

    public void start() {
        start(defaultTicketsNeededToWin, defaultTimerDuration);
    }

    public void start(int ticketsNeededToWin, int timerDuration) {
        this.session = new KOTHSession(this, ticketsNeededToWin, timerDuration);
        this.session.setActive(true);

        Bukkit.broadcastMessage(EventsAddon.PREFIX + displayName + ChatColor.GOLD + " can now be contested");
    }

    public void stop() {
        this.session = null;

        Bukkit.broadcastMessage(EventsAddon.PREFIX + displayName + ChatColor.GOLD + " can no longer be contested");
    }

    @Override
    public void capture(PlayerFaction faction) {
        session.setActive(false);
        session.setCaptureChestUnlockTime(Time.now() + (30 * 1000L));
        session.setCapturingFaction(faction);

        Bukkit.broadcastMessage(EventsAddon.PREFIX + displayName + ChatColor.GOLD + " has been captured by " + ChatColor.YELLOW + faction.getName());

        // Play victory sound
        faction.getOnlineMembers().forEach(member -> {
            final Player player = Bukkit.getPlayer(member.getUniqueId());

            if (player != null) {
                Players.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE);
            }
        });

        getAddon().getLootManager().fillCaptureChest(this);
    }
}