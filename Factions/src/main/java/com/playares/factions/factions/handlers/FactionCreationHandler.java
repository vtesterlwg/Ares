package com.playares.factions.factions.handlers;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.factions.FactionManager;
import com.playares.factions.factions.PlayerFaction;
import lombok.Getter;
import org.bukkit.entity.Player;

public final class FactionCreationHandler {
    @Getter
    public final FactionManager manager;

    private final String nameRegex = "^[A-Za-z0-9_.]+$";

    public FactionCreationHandler(FactionManager manager) {
        this.manager = manager;
    }

    public void createFaction(Player player, String name, SimplePromise promise) {
        if (!name.matches(nameRegex)) {
            promise.failure("Faction names must only contain characters A-Z, 0-9");
            return;
        }

        if (name.length() < manager.getPlugin().getFactionConfig().getMinFactionNameLength()) {
            promise.failure("Name is too short (Min 3 characters)");
            return;
        }

        if (name.length() > manager.getPlugin().getFactionConfig().getMaxFactionNameLength()) {
            promise.failure("Name is too long (Max 16 characters)");
            return;
        }

        if (manager.getPlugin().getFactionConfig().getBannedFactionNames().contains(name)) {
            promise.failure("This faction name is not allowed");
            return;
        }

        if (manager.getPlayerFaction(player.getUniqueId()) != null) {
            promise.failure("You are already in a faction");
            return;
        }

        if (manager.getFaction(name) != null) {
            promise.failure("Faction name is already in use");
            return;
        }

        final PlayerFaction faction = new PlayerFaction(manager.getPlugin(), name);

        faction.addMember(player.getUniqueId(), PlayerFaction.FactionRank.LEADER);

        manager.getFactionRepository().add(faction);

        Logger.print(player.getName() + " created faction " + name);

        promise.success();
    }

    public void createServerFaction(Player player, String name, SimplePromise promise) {

    }
}
