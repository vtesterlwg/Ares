package com.playares.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.player.PlayerStatus;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class DuelCommand extends BaseCommand {
    @Getter
    public Arenas plugin;

    public DuelCommand(Arenas plugin) {
        this.plugin = plugin;
    }

    @CommandAlias("duel")
    @Description("Challenge a player to a duel")
    @CommandCompletion("@players")
    public void onDuel(Player player, String name) {
        final ArenaPlayer challenger = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final ArenaPlayer challenged = plugin.getPlayerManager().getPlayer(name);

        if (challenger == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            return;
        }

        if (challenged == null) {
            player.sendMessage(ChatColor.RED + "Player not found");
            return;
        }

        if (challenger.getUniqueId().equals(challenged.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can not duel yourself");
            return;
        }

        if (!challenged.getStatus().equals(PlayerStatus.LOBBY)) {
            player.sendMessage(ChatColor.RED + "This player is not in the lobby");
            return;
        }

        if (challenged.getTeam() != null) {
            player.sendMessage(ChatColor.RED + "This player is in a team");
            return;
        }

        plugin.getMenuHandler().openDuelChallengeMenu(player, challenged);
    }
}
