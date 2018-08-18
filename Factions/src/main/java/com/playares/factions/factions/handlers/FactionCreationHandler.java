package com.playares.factions.factions.handlers;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.factions.FactionManager;
import com.playares.factions.factions.PlayerFaction;
import lombok.Getter;
import org.bukkit.ChatColor;
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

        if (manager.getFactionByPlayer(player.getUniqueId()) != null) {
            promise.failure("You are already in a faction");
            return;
        }

        if (manager.getFactionByName(name) != null) {
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

    public void sendInvite(Player player, String username, SimplePromise promise) {

    }

    public void revokeInvite(Player player, String username, SimplePromise promise) {

    }

    public void acceptInvite(Player player, String factionName, SimplePromise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByName(factionName);
        final boolean admin = player.hasPermission("factions.admin");

        if (manager.getFactionByPlayer(player.getUniqueId()) != null) {
            promise.failure("You are already in a faction");
            return;
        }

        if (faction == null) {
            promise.failure("Faction not found");
            return;
        }

        if (!faction.isInvited(player.getUniqueId()) && !admin) {
            promise.failure("You have not been invited to this faction");
            return;
        }

        if (faction.isFrozen() && !admin) {
            promise.failure("You can not join this faction while their power is frozen");
            return;
        }

        if (faction.isRaidable() && !admin) {
            promise.failure("You can not join this faction while they are raidable");
            return;
        }

        if (faction.getMembers().size() >= manager.getPlugin().getFactionConfig().getFactionMemberCap() && !admin) {
            promise.failure("Faction is full");
            return;
        }

        if (faction.isReinvited(player.getUniqueId()) && faction.getReinvites() <= 0 && !admin) {
            promise.failure("You have left this faction recently and they are out of re-invites");
            return;
        }

        faction.addMember(player.getUniqueId(), PlayerFaction.FactionRank.MEMBER);
        faction.getScoreboard().getTeam("friendly").addEntry(player.getName());
        faction.getPendingInvites().remove(player.getUniqueId());

        if (faction.isReinvited(player.getUniqueId())) {
            faction.setReinvites(faction.getReinvites() - 1);

            faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD + " consumed a re-invite");
            faction.sendMessage(ChatColor.GOLD + "Remaining" + ChatColor.YELLOW + ": " + faction.getReinvites());
        } else {
            faction.getMemberHistory().add(player.getUniqueId());
        }

        faction.sendMessage(ChatColor.DARK_GREEN + player.getName() + ChatColor.GOLD + " has " + ChatColor.GREEN + "joined" + ChatColor.GOLD + " the faction");

        Logger.print(player.getName() + " has joined " + faction.getName());

        promise.success();
    }
}
