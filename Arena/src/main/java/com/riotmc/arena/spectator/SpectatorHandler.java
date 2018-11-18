package com.riotmc.arena.spectator;

import com.playares.commons.base.promise.SimplePromise;
import com.riotmc.arena.Arenas;
import com.riotmc.arena.items.ExitSpectatorItem;
import com.riotmc.arena.match.Match;
import com.riotmc.arena.match.MatchStatus;
import com.riotmc.arena.match.cont.DuelMatch;
import com.riotmc.arena.match.cont.TeamMatch;
import com.riotmc.arena.player.ArenaPlayer;
import com.riotmc.arena.player.PlayerStatus;
import com.riotmc.services.customitems.CustomItemService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import javax.annotation.Nonnull;

public final class SpectatorHandler {
    @Nonnull @Getter
    public Arenas plugin;

    public SpectatorHandler(@Nonnull Arenas plugin) {
        this.plugin = plugin;
    }

    public void startSpectating(@Nonnull ArenaPlayer viewer, @Nonnull ArenaPlayer viewed, @Nonnull SimplePromise promise) {
        final CustomItemService customItemService = (CustomItemService)plugin.getService(CustomItemService.class);

        if (customItemService == null) {
            promise.failure("Could not obtain Custom Item Service");
            return;
        }

        if (viewed.getPlayer() == null) {
            promise.failure("Player is not online");
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

        if (viewer.getPlayer() != null) {
            viewer.getPlayer().teleport(viewed.getPlayer().getLocation());
            viewer.getPlayer().getInventory().clear();
            viewer.getPlayer().getInventory().setArmorContents(null);
        }

        customItemService.getItem(ExitSpectatorItem.class).ifPresent(item -> viewer.getPlayer().getInventory().setItem(8, item.getItem()));

        promise.success();
    }

    public void stopSpectating(@Nonnull ArenaPlayer viewer, @Nonnull SimplePromise promise) {
        if (!viewer.getStatus().equals(PlayerStatus.SPECTATING)) {
            promise.failure("You are not in spectator mode");
            return;
        }

        final Match match = viewer.getMatch();

        viewer.setStatus(PlayerStatus.LOBBY);
        viewer.setMatch(null);

        if (viewer.getPlayer() != null) {
            viewer.getPlayer().teleport(plugin.getPlayerHandler().getLobby().getBukkit());
        }

        if (match != null) {
            match.getSpectators().remove(viewer);
        }

        updateSpectators(viewer);

        plugin.getPlayerHandler().giveLobbyItems(viewer);

        promise.success();
    }

    public void updateSpectators(@Nonnull ArenaPlayer viewer) {
        if (viewer.getPlayer() == null) {
            return;
        }

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
                match.getSpectators()
                        .stream()
                        .filter(s -> s.getPlayer() != null)
                        .forEach(spectator -> viewer.getPlayer().showPlayer(plugin, spectator.getPlayer()));

                if (match instanceof DuelMatch) {
                    final DuelMatch duel = (DuelMatch)match;

                    if (!match.getStatus().equals(MatchStatus.ENDGAME)) {
                        duel.getOpponents()
                                .stream()
                                .filter(o -> o.getPlayer() != null)
                                .forEach(opponent -> opponent.getPlayer().hidePlayer(plugin, viewer.getPlayer()));
                    }
                }

                else if (match instanceof TeamMatch) {
                    final TeamMatch teamfight = (TeamMatch)match;

                    teamfight.getOpponents().forEach(team -> team.getMembers()
                            .stream()
                            .filter(m -> m.getStatus().equals(PlayerStatus.INGAME))
                            .filter(m -> m.getPlayer() != null)
                            .forEach(member -> member.getPlayer().hidePlayer(plugin, viewer.getPlayer())));
                }
            }

            return;
        }

        if (viewer.getStatus().equals(PlayerStatus.INGAME)) {
            final Match match = viewer.getMatch();

            if (match != null && viewer.getPlayer() != null) {
                match.getSpectators()
                        .stream()
                        .filter(s -> s.getPlayer() != null)
                        .forEach(spectator -> viewer.getPlayer().hidePlayer(plugin, spectator.getPlayer()));
            }
        }
    }
}
