package com.playares.factions.players.handlers;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Players;
import com.playares.factions.claims.DefinedClaim;
import com.playares.factions.factions.Faction;
import com.playares.factions.factions.PlayerFaction;
import com.playares.factions.factions.ServerFaction;
import com.playares.factions.players.FactionPlayer;
import com.playares.factions.players.PlayerManager;
import com.playares.factions.timers.PlayerTimer;
import com.playares.factions.timers.cont.player.HomeTimer;
import com.playares.factions.timers.cont.player.StuckTimer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class PlayerTimerHandler {
    @Getter
    public final PlayerManager manager;

    public PlayerTimerHandler(PlayerManager manager) {
        this.manager = manager;
    }

    public void attemptStuck(Player player, SimplePromise promise) {
        final FactionPlayer profile = manager.getPlayer(player.getUniqueId());
        final DefinedClaim inside = manager.getPlugin().getClaimManager().getClaimAt(new PLocatable(player));

        if (profile == null) {
            promise.failure("Failed to obtain your profile");
            return;
        }

        if (inside == null) {
            promise.failure("You are not standing in a claim");
            return;
        }

        if (profile.getTimer(PlayerTimer.PlayerTimerType.STUCK) != null) {
            promise.failure("This action is already in progress");
            return;
        }

        final StuckTimer timer = new StuckTimer(manager.getPlugin(), player.getUniqueId(), manager.getPlugin().getFactionConfig().getTimerStuck());

        profile.getTimers().add(timer);

        player.sendMessage(ChatColor.YELLOW + "You will teleport outside this claim in " + ChatColor.AQUA + manager.getPlugin().getFactionConfig().getTimerStuck() + " seconds" + ChatColor.YELLOW + "." +
                " Moving or taking damage will cancel this timer");

        Logger.print(player.getName() + " is attempting to unstuck themselves");

        promise.success();
    }

    public void attemptHome(Player player, SimplePromise promise) {
        final FactionPlayer profile = manager.getPlayer(player.getUniqueId());
        final PlayerFaction faction = manager.getPlugin().getFactionManager().getFactionByPlayer(player.getUniqueId());
        final DefinedClaim inside = manager.getPlugin().getClaimManager().getClaimAt(new PLocatable(player));

        if (profile == null) {
            promise.failure("Failed to obtain your profile");
            return;
        }

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        if (faction.getHome() == null) {
            promise.failure("Your faction does not have its home location set");
            return;
        }

        if (profile.hasTimer(PlayerTimer.PlayerTimerType.HOME)) {
            promise.failure("You are already in the process of warping home");
            return;
        }

        if (profile.hasTimer(PlayerTimer.PlayerTimerType.COMBAT)) {
            promise.failure("You can not warp home while in combat");
            return;
        }

        if (profile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION)) {
            promise.failure("You can not warp home while you have PvP Protection");
            return;
        }

        if (!player.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
            promise.failure("You can only warp home in the Overworld");
            return;
        }

        if (inside != null) {
            final Faction insideFaction = manager.getPlugin().getFactionManager().getFactionById(inside.getUniqueId());

            if (insideFaction != null) {
                if (insideFaction instanceof ServerFaction) {
                    final ServerFaction sf = (ServerFaction)insideFaction;

                    if (sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                        Players.teleportWithVehicle(manager.getPlugin(), player, faction.getHome().getBukkit());
                        player.sendMessage(ChatColor.GREEN + "Returned to faction home");
                    } else {
                        promise.failure("You can not warp home from inside this claim");
                        return;
                    }
                } else {
                    final PlayerFaction pf = (PlayerFaction)insideFaction;

                    if (!pf.getUniqueId().equals(faction.getUniqueId())) {
                        promise.failure("You can not warp home from inside this claim");
                        return;
                    }
                }
            }
        }

        profile.getTimers().add(new HomeTimer(manager.getPlugin(), player.getUniqueId(), faction, manager.getPlugin().getFactionConfig().getTimerHome()));

        player.sendMessage(ChatColor.YELLOW + "You will return home in " + ChatColor.AQUA + manager.getPlugin().getFactionConfig().getTimerHome() + " seconds" + ChatColor.YELLOW + "." +
                " Moving or taking damage will cancel this timer");

        promise.success();
    }
}