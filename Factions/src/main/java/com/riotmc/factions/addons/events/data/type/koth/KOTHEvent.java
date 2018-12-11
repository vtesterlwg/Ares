package com.riotmc.factions.addons.events.data.type.koth;

import com.google.common.collect.Lists;
import com.riotmc.commons.bukkit.location.BLocatable;
import com.riotmc.factions.addons.events.EventsAddon;
import com.riotmc.factions.addons.events.data.region.CaptureRegion;
import com.riotmc.factions.addons.events.data.schedule.EventSchedule;
import com.riotmc.factions.addons.events.data.session.KOTHSession;
import com.riotmc.factions.addons.events.data.type.RiotEvent;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class KOTHEvent implements RiotEvent {
    @Getter @Setter public UUID ownerId;
    @Getter @Setter public String name;
    @Getter @Setter public String displayName;
    @Getter public final List<EventSchedule> schedule;
    @Getter @Setter public KOTHSession session;
    @Getter @Setter public CaptureRegion captureRegion;

    public KOTHEvent(UUID ownerId, String name, String displayName, Collection<EventSchedule> schedule, BLocatable captureCornerA, BLocatable captureCornerB) {
        this.ownerId = ownerId;
        this.name = name;
        this.displayName = displayName;
        this.schedule = Lists.newArrayList(schedule);
        this.session = null;
        this.captureRegion = new CaptureRegion(captureCornerA, captureCornerB);
    }

    public void start(int ticketsNeededToWin, int timerDuration) {
        this.session = new KOTHSession(this, ticketsNeededToWin, timerDuration);
        this.session.setActive(true);

        Bukkit.broadcastMessage(EventsAddon.PREFIX + displayName + ChatColor.GOLD + " can now be contested");
    }

    @Override
    public void capture(PlayerFaction faction) {

    }

    public void tick(PlayerFaction faction) {

    }
}