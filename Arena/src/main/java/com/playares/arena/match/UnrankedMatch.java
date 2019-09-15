package com.playares.arena.match;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.playares.arena.Arenas;
import com.playares.arena.arena.data.Arena;
import com.playares.arena.kit.Kit;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.queue.MatchmakingQueue;
import com.playares.arena.report.PlayerReport;
import com.playares.arena.timer.cont.MatchStartingTimer;
import com.playares.commons.bukkit.util.Players;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class UnrankedMatch extends Match {
    @Getter public final ArenaPlayer playerA;
    @Getter public final ArenaPlayer playerB;

    public UnrankedMatch(Arenas plugin, MatchmakingQueue queue, Arena arena, ArenaPlayer playerA, ArenaPlayer playerB) {
        super(plugin, queue, arena, false);
        this.playerA = playerA;
        this.playerB = playerB;
    }

    @Override
    public ImmutableList<ArenaPlayer> getPlayers() {
        final List<ArenaPlayer> result = Lists.newArrayList();

        result.add(playerA);
        result.add(playerB);
        result.addAll(spectators);

        return ImmutableList.copyOf(result);
    }

    @Override
    public void addSpectator(Player player) {
        super.addSpectator(player);

        playerA.getPlayer().hidePlayer(plugin, player);
        playerB.getPlayer().hidePlayer(plugin, player);

        player.sendMessage(ChatColor.YELLOW + "You are now spectating " + ChatColor.AQUA + playerA.getUsername() + ChatColor.YELLOW + " vs. " + ChatColor.AQUA + playerB.getUsername());
    }

    public void start() {
        getPlayers().forEach(player -> {
            player.addTimer(new MatchStartingTimer(player.getUniqueId(), 5));

            Players.resetHealth(player.getPlayer());
            player.getPlayer().getInventory().clear();
            player.setStatus(ArenaPlayer.PlayerStatus.INGAME);
            player.setActiveReport(new PlayerReport(player));
        });

        arena.teleportToSpawnpointA(playerA.getPlayer());
        arena.teleportToSpawnpointB(playerB.getPlayer());

        addToScoreboardA(playerA.getPlayer());
        addToScoreboardB(playerB.getPlayer());

        if (queue.getAllowedKits().isEmpty()) {
            sendMessage(ChatColor.RED + "Failed to find any kits for this queue type");
        } else {
            for (Kit kit : queue.getAllowedKits()) {
                playerA.getPlayer().getInventory().addItem(kit.getBook());
                playerB.getPlayer().getInventory().addItem(kit.getBook());
            }
        }

        if (!ranked) {
            sendMessage(ChatColor.YELLOW + "Unranked " + ChatColor.GOLD + getQueue().getQueueType().getDisplayName() + ChatColor.YELLOW + ": " + ChatColor.AQUA + playerA.getUsername() + ChatColor.YELLOW + " vs. " + ChatColor.AQUA + playerB.getUsername());
        }
    }

    public ArenaPlayer getWinner() {
        final ArenaPlayer.PlayerStatus sA = playerA.getStatus();
        final ArenaPlayer.PlayerStatus sB = playerB.getStatus();

        if (sA.equals(ArenaPlayer.PlayerStatus.INGAME) && !sB.equals(ArenaPlayer.PlayerStatus.INGAME)) {
            return playerA;
        }

        if (!sA.equals(ArenaPlayer.PlayerStatus.INGAME) && sB.equals(ArenaPlayer.PlayerStatus.INGAME)) {
            return playerB;
        }

        return null;
    }
}