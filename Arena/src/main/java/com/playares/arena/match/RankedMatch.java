package com.playares.arena.match;

import com.playares.arena.Arenas;
import com.playares.arena.arena.data.Arena;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.queue.MatchmakingQueue;
import lombok.Getter;
import org.bukkit.ChatColor;

public final class RankedMatch extends UnrankedMatch {
    @Getter public final ArenaPlayer playerA;
    @Getter public final ArenaPlayer playerB;
    @Getter public final int ratingA;
    @Getter public final int ratingB;

    public RankedMatch(Arenas plugin, MatchmakingQueue queue, Arena arena, ArenaPlayer playerA, ArenaPlayer playerB) {
        super(plugin, queue, arena, playerA, playerB);
        this.playerA = playerA;
        this.playerB = playerB;
        this.ratingA = playerA.getRankedData().getRating(queue.getQueueType());
        this.ratingB = playerB.getRankedData().getRating(queue.getQueueType());

        setRanked(true);
    }

    @Override
    public void start() {
        super.start();

        sendMessage(
                ChatColor.YELLOW + "Ranked " + ChatColor.GOLD + getQueue().getQueueType().getDisplayName() + ChatColor.YELLOW + ": " +
                ChatColor.AQUA + playerA.getUsername() + ChatColor.GOLD + " (" + ChatColor.GREEN + ratingA + ChatColor.GOLD + ") " + ChatColor.YELLOW + "vs. " +
                ChatColor.AQUA + playerB.getUsername() + ChatColor.GOLD + " (" + ChatColor.GREEN + ratingB + ChatColor.GOLD + ")");
    }
}
