package com.riotmc.factions.players.handlers;

import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.commons.base.util.Time;
import com.riotmc.commons.bukkit.location.PLocatable;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.commons.bukkit.util.Players;
import com.riotmc.factions.claims.DefinedClaim;
import com.riotmc.factions.factions.Faction;
import com.riotmc.factions.factions.PlayerFaction;
import com.riotmc.factions.factions.ServerFaction;
import com.riotmc.factions.players.FactionPlayer;
import com.riotmc.factions.players.PlayerManager;
import com.riotmc.factions.timers.PlayerTimer;
import com.riotmc.factions.timers.cont.player.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class PlayerTimerHandler {
    /** Player Manager that owns this handler **/
    @Getter public final PlayerManager manager;

    public PlayerTimerHandler(PlayerManager manager) {
        this.manager = manager;
    }

    /**
     * Prints a list of all valid player timer types
     * @param viewer Viewer
     */
    public void list(CommandSender viewer) {
        viewer.sendMessage(ChatColor.GOLD + "Valid Player Timer Types: ");

        for (PlayerTimer.PlayerTimerType type : PlayerTimer.PlayerTimerType.values()) {
            viewer.sendMessage(type.getDisplayName());
        }
    }

    /**
     * Forcefully removes a timer from a players profile
     * @param sender Command Sender
     * @param username Modified Username
     * @param timerName Modified Timer
     * @param promise Promise
     */
    public void remove(CommandSender sender, String username, String timerName, SimplePromise promise) {
        final Player player = Bukkit.getPlayer(username);
        final PlayerTimer.PlayerTimerType type = PlayerTimer.PlayerTimerType.match(timerName.toUpperCase());

        if (player == null) {
            promise.failure("Player not found");
            return;
        }

        if (type == null) {
            promise.failure("Invalid timer type");
            return;
        }

        final FactionPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            promise.failure("Failed to obtain " + player.getName() + "'s profile");
            return;
        }

        final PlayerTimer existing = profile.getTimer(type);

        if (existing == null) {
            promise.failure("Player does not have this timer type");
            return;
        }

        existing.onFinish();
        profile.getTimers().remove(existing);

        Logger.print(sender.getName() + " removed " + ChatColor.RESET + type.getDisplayName() + " timer from " + player.getName());
        promise.success();
    }

    /**
     * Applies a new timer to a player
     * @param sender Command Sender
     * @param username Modified Username
     * @param timerName Timer Name
     * @param time Time
     * @param promise Promise
     */
    public void apply(CommandSender sender, String username, String timerName, String time, SimplePromise promise) {
        final Player player = Bukkit.getPlayer(username);
        final long duration = Time.parseTime(time);

        if (player == null) {
            promise.failure("Player not found");
            return;
        }

        if (duration <= 0L) {
            promise.failure("Invalid timer duration");
            return;
        }

        final int toSeconds = (int)(duration / 1000L);
        final PlayerTimer.PlayerTimerType type = PlayerTimer.PlayerTimerType.match(timerName.toUpperCase());

        if (type == null) {
            promise.failure("Invalid timer type");
            return;
        }

        final FactionPlayer profile = manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            promise.failure("Failed to obtain " + username + "'s profile");
            return;
        }

        final PlayerTimer existing = profile.getTimer(type);

        if (existing != null) {
            existing.setExpire(Time.now() + duration);
            player.sendMessage(type.getDisplayName() + ChatColor.GOLD + " has been applied to your account for " + ChatColor.YELLOW + Time.convertToRemaining(duration));
            Logger.print(sender.getName() + " applied a " + ChatColor.RESET + type.getDisplayName() + " timer to " + player.getName() + " for " + Time.convertToRemaining(duration));
            promise.success();
            return;
        }

        if (type.equals(PlayerTimer.PlayerTimerType.COMBAT)) {
            final DefinedClaim inside = profile.getCurrentClaim();

            if (inside != null) {
                final Faction insideFaction = manager.getPlugin().getFactionManager().getFactionById(inside.getOwnerId());

                if (insideFaction instanceof ServerFaction) {
                    final ServerFaction insideServerFaction = (ServerFaction)insideFaction;

                    if (insideServerFaction.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                        promise.failure(player.getName() + " is inside a safezone claim");
                        return;
                    }
                }
            }

            final CombatTagTimer timer = new CombatTagTimer(manager.getPlugin(), player.getUniqueId(), toSeconds);
            profile.addTimer(timer);
            promise.success();
        }

        else if (type.equals(PlayerTimer.PlayerTimerType.CRAPPLE)) {
            final CrappleTimer timer = new CrappleTimer(player.getUniqueId(), toSeconds);
            profile.addTimer(timer);
            promise.success();
        }

        else if (type.equals(PlayerTimer.PlayerTimerType.ENDERPEARL)) {
            final EnderpearlTimer timer = new EnderpearlTimer(player.getUniqueId(), toSeconds);
            profile.addTimer(timer);
            promise.success();
        }

        else if (type.equals(PlayerTimer.PlayerTimerType.GAPPLE)) {
            final GappleTimer timer = new GappleTimer(player.getUniqueId(), toSeconds);
            profile.addTimer(timer);
            promise.success();
        }

        else if (type.equals(PlayerTimer.PlayerTimerType.HOME)) {
            final PlayerFaction faction = profile.getFaction();

            if (faction == null) {
                promise.failure(player.getName() + " is not in a faction");
                return;
            }

            final HomeTimer timer = new HomeTimer(manager.getPlugin(), player.getUniqueId(), faction, toSeconds);
            profile.addTimer(timer);
            promise.success();
        }

        else if (type.equals(PlayerTimer.PlayerTimerType.PROTECTION)) {
            final DefinedClaim inside = profile.getCurrentClaim();

            if (inside != null) {
                final Faction insideFaction = manager.getPlugin().getFactionManager().getFactionById(inside.getOwnerId());

                if (insideFaction instanceof PlayerFaction) {
                    promise.failure(player.getName() + " is inside a Player Faction Claim");
                    return;
                }

                else if (insideFaction instanceof ServerFaction) {
                    final ServerFaction insideServerFaction = (ServerFaction)insideFaction;

                    if (insideServerFaction.getFlag().equals(ServerFaction.FactionFlag.EVENT)) {
                        promise.failure(player.getName() + " is inside an Event Claim");
                        return;
                    }
                }
            }

            final ProtectionTimer timer = new ProtectionTimer(manager.getPlugin(), player.getUniqueId(), toSeconds);
            profile.addTimer(timer);
            promise.success();
        }

        else if (type.equals(PlayerTimer.PlayerTimerType.STUCK)) {
            final DefinedClaim inside = profile.getCurrentClaim();

            if (inside == null) {
                promise.failure(player.getName() + " is not inside a claim");
                return;
            }

            final StuckTimer timer = new StuckTimer(manager.getPlugin(), player.getUniqueId(), toSeconds);
            profile.addTimer(timer);
            promise.success();
        }

        else if (type.equals(PlayerTimer.PlayerTimerType.TOTEM)) {
            final TotemTimer timer = new TotemTimer(player.getUniqueId(), toSeconds);
            profile.addTimer(timer);
            promise.success();
        }

        player.sendMessage(type.getDisplayName() + ChatColor.GOLD + " has been applied to your account for " + ChatColor.YELLOW + Time.convertToRemaining(duration));
        Logger.print(sender.getName() + " applied a " + ChatColor.RESET + type.getDisplayName() + " timer to " + player.getName() + " for " + Time.convertToRemaining(duration));
    }

    /**
     * Attempts to unstuck a player
     * @param player Player
     * @param promise Promise
     */
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

    /**
     * Attempts to warp a player home
     * @param player Player
     * @param promise Promise
     */
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