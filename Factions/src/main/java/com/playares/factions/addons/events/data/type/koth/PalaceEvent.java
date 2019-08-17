package com.playares.factions.addons.events.data.type.koth;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.events.data.schedule.EventSchedule;
import com.playares.factions.addons.events.loot.palace.PalaceLootChest;
import com.playares.factions.addons.events.loot.palace.PalaceLootTier;
import com.playares.factions.factions.data.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PalaceEvent extends KOTHEvent {
    @Getter @Setter public UUID owningFaction;
    @Getter public final Map<PalaceLootTier, Long> lootTierUnlockTimes;
    @Getter public final List<PalaceLootChest> lootChests;

    public PalaceEvent(EventsAddon addon,
                       UUID ownerId,
                       String name,
                       String displayName,
                       Collection<EventSchedule> schedule,
                       BLocatable captureChestLocation,
                       BLocatable captureCornerA,
                       BLocatable captureCornerB,
                       int defaultTicketsNeededToWin,
                       int defaultTimerDuration) {

        super(addon, ownerId, name, displayName, schedule, captureChestLocation, captureCornerA, captureCornerB, defaultTicketsNeededToWin, defaultTimerDuration);

        this.owningFaction = null;
        this.lootTierUnlockTimes = Maps.newHashMap();
        this.lootChests = Lists.newArrayList();
    }

    @Override
    public void capture(PlayerFaction faction) {
        super.capture(faction);
    }

    public boolean canAccess(UUID uniqueId, PalaceLootChest chest) {
        final PalaceLootTier chestTier = chest.getTier();
        final long unlockTime = lootTierUnlockTimes.getOrDefault(chestTier, Time.now());
        final PlayerFaction owner = (owningFaction != null) ? (PlayerFaction)addon.getPlugin().getFactionManager().getFactionById(owningFaction) : null;

        if (owner != null && owner.getMember(uniqueId) != null) {
            return true;
        }

        return unlockTime <= Time.now();
    }

    public void stock() {
        lootChests.forEach(chest -> addon.getLootManager().fillPalaceChest(chest));
        Bukkit.broadcastMessage(EventsAddon.PREFIX + displayName + ChatColor.GREEN + " has been restocked");
    }
}