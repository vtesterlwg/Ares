package com.playares.arena.spectator;

import com.playares.arena.Arenas;
import com.playares.arena.items.ExitSpectatorItem;
import com.playares.arena.match.Match;
import com.playares.arena.match.MatchStatus;
import com.playares.arena.match.cont.DuelMatch;
import com.playares.arena.match.cont.TeamMatch;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.player.PlayerStatus;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.services.customitems.CustomItemService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public final class SpectatorHandler {
    @Getter
    public Arenas plugin;

    public SpectatorHandler(Arenas plugin) {
        this.plugin = plugin;
    }

    public void startSpectating(ArenaPlayer viewer, ArenaPlayer viewed, SimplePromise promise) {
        final CustomItemService customItemService = (CustomItemService)plugin.getService(CustomItemService.class);

        if (customItemService == null) {
            promise.failure("Could not obtain Custom Item Service");
            return;
        }

        if (viewed.getMatch() == null) {
            promise.failure(viewed.getUsername() + " is not in a match");
            return;
        }

        if (viewed.getMatch().getStatus().equals(MatchStatus.ENDGAME) || viewed.getMatch().getStatus().equals(MatchStatus.FINISHED)) {
            promise.failure(viewed.getUsername() + "'s match is ending");
            return;
        }

        if (!viewer.getStatus().equals(PlayerStatus.LOBBY)) {
            promise.failure("You must be in the lobby to perform this action");
            return;
        }

        viewer.setStatus(PlayerStatus.SPECTATING);
        viewer.setMatch(viewed.getMatch());
        viewed.getMatch().getSpectators().add(viewer);

        if (viewed.getMatch() instanceof DuelMatch) {
            final DuelMatch duel = (DuelMatch)viewed.getMatch();
            duel.sendMessage(duel.getViewers(), ChatColor.LIGHT_PURPLE + viewer.getUsername() + ChatColor.YELLOW + " has started spectating");
        }

        else if (viewed.getMatch() instanceof TeamMatch) {
            final TeamMatch teamfight = (TeamMatch)viewed.getMatch();
            teamfight.sendMessage(teamfight.getViewers(), ChatColor.LIGHT_PURPLE + viewer.getUsername() + ChatColor.YELLOW + " has started spectating");
        }

        updateSpectators(viewer);

        viewer.getPlayer().teleport(viewed.getPlayer().getLocation());
        viewer.getPlayer().getInventory().clear();
        viewer.getPlayer().getInventory().setArmorContents(null);

        customItemService.getItem(ExitSpectatorItem.class).ifPresent(item -> viewer.getPlayer().getInventory().setItem(8, item.getItem()));

        promise.success();
    }

    public void stopSpectating(ArenaPlayer viewer, SimplePromise promise) {
        if (!viewer.getStatus().equals(PlayerStatus.SPECTATING)) {
            promise.failure("You are not in spectator mode");
            return;
        }

        final Match match = viewer.getMatch();

        viewer.setStatus(PlayerStatus.LOBBY);
        viewer.setMatch(null);

        match.getSpectators().remove(viewer);

        updateSpectators(viewer);

        plugin.getPlayerHandler().giveLobbyItems(viewer);

        viewer.getPlayer().teleport(plugin.getPlayerHandler().getLobby().getBukkit());

        promise.success();
    }

    public void updateSpectators(ArenaPlayer viewer) {
        if (viewer.getStatus().equals(PlayerStatus.LOBBY)) {
            Bukkit.getOnlinePlayers().forEach(online -> {
                online.showPlayer(plugin, viewer.getPlayer());
                viewer.getPlayer().showPlayer(plugin, online);
            });

            return;
        }

        if (viewer.getStatus().equals(PlayerStatus.SPECTATING) || viewer.getStatus().equals(PlayerStatus.INGAME_DEAD)) {
            final Match match = viewer.getMatch();

            if (match != null) {
                match.getSpectators().forEach(spectator -> {
                    if (spectator.getPlayer() != null) {
                        viewer.getPlayer().showPlayer(plugin, spectator.getPlayer());
                    }
                });

                if (match instanceof DuelMatch) {
                    final DuelMatch duel = (DuelMatch)match;

                    duel.getOpponents().forEach(opponent -> {
                        if (opponent.getPlayer() != null && !match.getStatus().equals(MatchStatus.ENDGAME)) {
                            opponent.getPlayer().hidePlayer(plugin, viewer.getPlayer());
                        }
                    });
                }

                else if (match instanceof TeamMatch) {
                    final TeamMatch teamfight = (TeamMatch)match;

                    teamfight.getOpponents().forEach(team -> team.getMembers().forEach(member -> {
                        if (member.getStatus().equals(PlayerStatus.INGAME)) {
                            if (member.getPlayer() != null && !match.getStatus().equals(MatchStatus.ENDGAME)) {
                                member.getPlayer().hidePlayer(plugin, viewer.getPlayer());
                            }
                        }
                    }));
                }
            }

            return;
        }

        if (viewer.getStatus().equals(PlayerStatus.INGAME)) {
            final Match match = viewer.getMatch();

            if (match != null) {
                match.getSpectators().forEach(spectator -> {
                    if (spectator.getPlayer() != null) {
                        viewer.getPlayer().hidePlayer(plugin, spectator.getPlayer());
                    }
                });
            }
        }
    }
}
