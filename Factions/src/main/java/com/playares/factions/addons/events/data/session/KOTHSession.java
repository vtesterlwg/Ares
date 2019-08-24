package com.playares.factions.addons.events.data.session;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.base.util.Time;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.events.data.timer.KOTHCountdownTimer;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import com.playares.factions.factions.data.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.*;

public final class KOTHSession {
    @Getter public final KOTHEvent event;
    @Getter @Setter public boolean active;
    @Getter @Setter public PlayerFaction capturingFaction;
    @Getter @Setter public long captureChestUnlockTime;
    @Getter @Setter public int ticketsNeededToWin;
    @Getter @Setter public int timerDuration;
    @Getter public boolean contested;
    @Getter @Setter public long nextAllowedNotification;
    @Getter @Setter public KOTHCountdownTimer timer;
    @Getter public final Map<PlayerFaction, Integer> leaderboard;

    public KOTHSession(KOTHEvent event, int ticketsNeededToWin, int timerDuration) {
        this.event = event;
        this.active = false;
        this.capturingFaction = null;
        this.captureChestUnlockTime = -1;
        this.leaderboard = Maps.newConcurrentMap();
        this.ticketsNeededToWin = ticketsNeededToWin;
        this.timerDuration = timerDuration;
        this.contested = false;
        this.nextAllowedNotification = Time.now();
        this.timer = new KOTHCountdownTimer(event, timerDuration);
        this.timer.freeze();
    }

    public boolean isCaptured() {
        return capturingFaction != null && !active;
    }

    public long getTimeUntilCaptureChestUnlock() {
        return captureChestUnlockTime - Time.now();
    }

    public void setContested(Set<PlayerFaction> factions) {
        final List<String> factionNames = Lists.newArrayList();
        factions.forEach(faction -> factionNames.add(faction.getName()));

        this.contested = true;
        this.timer.freeze();

        if (nextAllowedNotification <= Time.now()) {
            Bukkit.broadcastMessage(EventsAddon.PREFIX + event.getDisplayName() + ChatColor.GOLD + " is being contested by " +
                    ChatColor.YELLOW + Joiner.on(ChatColor.GOLD + ", " + ChatColor.YELLOW).join(factionNames));

            this.nextAllowedNotification = Time.now() + (3 * 1000L);
        }
    }

    public void setUncontested(boolean reset) {
        if (reset) {
            reset();
            return;
        }

        this.timer.unfreeze();
        this.contested = false;

        if (nextAllowedNotification <= Time.now()) {
            Bukkit.broadcastMessage(EventsAddon.PREFIX + event.getDisplayName() + ChatColor.GOLD + " is being controlled by " + ChatColor.YELLOW + getCapturingFaction().getName());
            this.nextAllowedNotification = Time.now() + (3 * 1000L);
        }
    }

    public void reset() {
        this.capturingFaction = null;
        this.contested = false;
        this.timer.setExpire(Time.now() + (getTimerDuration() * 1000));
        this.timer.freeze();

        if (nextAllowedNotification <= Time.now()) {
            Bukkit.broadcastMessage(EventsAddon.PREFIX + event.getDisplayName() + ChatColor.GOLD + " has been reset");
            this.nextAllowedNotification = Time.now() + (3 * 1000L);
        }
    }

    public int getTickets(PlayerFaction faction) {
        return leaderboard.getOrDefault(faction, 0);
    }

    public ImmutableList<PlayerFaction> getSortedLeaderboard() {
        final List<PlayerFaction> factions = Lists.newArrayList(leaderboard.keySet());
        factions.sort(Comparator.comparingInt(this::getTickets));
        Collections.reverse(factions);
        return ImmutableList.copyOf(factions);
    }

    public void tick(PlayerFaction faction) {
        final int existingTickets = getTickets(faction);
        final int newTickets = existingTickets + 1;

        if (newTickets >= getTicketsNeededToWin()) {
            event.capture(faction);
            return;
        }

        getLeaderboard().put(faction, newTickets);

        Bukkit.broadcastMessage(EventsAddon.PREFIX + ChatColor.YELLOW + faction.getName() + ChatColor.GOLD + " has gained a ticket for controlling " +
                event.getDisplayName() + ChatColor.RED + " (" + newTickets + "/" + getTicketsNeededToWin() + ")");

        for (PlayerFaction otherFaction : getLeaderboard().keySet()) {
            if (otherFaction.getUniqueId().equals(faction.getUniqueId())) {
                continue;
            }

            final int tickets = getTickets(otherFaction) - 2;

            if (tickets <= 0) {
                getLeaderboard().remove(otherFaction);
                otherFaction.sendMessage(EventsAddon.PREFIX + ChatColor.GOLD + "Your faction is no longer on the leaderboard for " + event.getDisplayName());
                continue;
            }

            getLeaderboard().put(otherFaction, tickets);
            otherFaction.sendMessage(EventsAddon.PREFIX + ChatColor.GOLD + "Your faction now has " + ChatColor.YELLOW + tickets + " tickets");
        }

        timer.setExpire(Time.now() + (getTimerDuration() * 1000L));
    }
}