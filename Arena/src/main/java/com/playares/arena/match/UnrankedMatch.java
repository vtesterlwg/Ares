package com.playares.arena.match;

import com.playares.arena.Arenas;
import com.playares.arena.arena.data.Arena;
import com.playares.arena.kit.Kit;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.queue.MatchmakingQueue;
import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class UnrankedMatch extends Match {
    @Getter public final ArenaPlayer playerA;
    @Getter public final ArenaPlayer playerB;

    public UnrankedMatch(Arenas plugin, MatchmakingQueue queue, Arena arena, ArenaPlayer playerA, ArenaPlayer playerB) {
        super(plugin, queue, arena, false);
        this.playerA = playerA;
        this.playerB = playerB;
    }

    @Override
    public void addSpectator(Player player) {
        super.addSpectator(player);

        playerA.getPlayer().hidePlayer(plugin, player);
        playerB.getPlayer().hidePlayer(plugin, player);

        player.sendMessage(ChatColor.YELLOW + "You are now spectating " + ChatColor.AQUA + playerA.getUsername() + ChatColor.YELLOW + " vs. " + ChatColor.AQUA + playerB.getUsername());
    }

    public void sendMessage(BaseComponent message) {
        playerA.getPlayer().sendMessage(message);
        playerB.getPlayer().sendMessage(message);

        spectators.forEach(spectator -> spectator.getPlayer().sendMessage(message));
    }

    public void sendMessage(String message) {
        playerA.getPlayer().sendMessage(message);
        playerB.getPlayer().sendMessage(message);

        spectators.forEach(spectator -> spectator.getPlayer().sendMessage(message));
    }

    public void start() {
        arena.teleportToSpawnpointA(playerA.getPlayer());
        arena.teleportToSpawnpointB(playerB.getPlayer());

        addToScoreboardA(playerA.getPlayer());
        addToScoreboardB(playerB.getPlayer());

        sendMessage(ChatColor.YELLOW + "Unranked " + ChatColor.GOLD + getQueue().getQueueType().getDisplayName() + ChatColor.YELLOW + ": " + ChatColor.AQUA + playerA.getUsername() + ChatColor.YELLOW + " vs. " + ChatColor.AQUA + playerB.getUsername());

        if (queue.getAllowedKits().isEmpty()) {
            sendMessage(ChatColor.RED + "Failed to find any kits for this queue type");
            return;
        }

        for (Kit kit : queue.getAllowedKits()) {
            playerA.getPlayer().getInventory().addItem(kit.getBook());
            playerB.getPlayer().getInventory().addItem(kit.getBook());
        }
    }
}