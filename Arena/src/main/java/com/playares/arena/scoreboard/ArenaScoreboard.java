package com.playares.arena.scoreboard;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class ArenaScoreboard {
    @Getter
    public final Scoreboard scoreboard;

    @Getter
    public final Team friendlyTeam;

    @Getter
    public final Team enemyTeam;

    public ArenaScoreboard() {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.friendlyTeam = scoreboard.registerNewTeam("friendly");
        this.enemyTeam = scoreboard.registerNewTeam("enemy");

        this.friendlyTeam.setCanSeeFriendlyInvisibles(true);
        this.friendlyTeam.setColor(ChatColor.DARK_GREEN);
        this.friendlyTeam.setPrefix(ChatColor.DARK_GREEN + "");

        this.enemyTeam.setColor(ChatColor.RED);
        this.enemyTeam.setPrefix(ChatColor.RED + "");
    }

    public void clearEnemyTeam() {
        for (String entry : enemyTeam.getEntries()) {
            enemyTeam.removeEntry(entry);
        }
    }
}