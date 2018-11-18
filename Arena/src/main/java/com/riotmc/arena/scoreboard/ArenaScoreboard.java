package com.riotmc.arena.scoreboard;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nonnull;

public final class ArenaScoreboard {
    @Nonnull @Getter
    public final Scoreboard scoreboard;

    @Nonnull @Getter
    public final Team friendlyTeam;

    @Nonnull @Getter
    public final Team enemyTeam;

    public ArenaScoreboard() {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.friendlyTeam = scoreboard.registerNewTeam("friendly");
        this.enemyTeam = scoreboard.registerNewTeam("enemy");

        this.friendlyTeam.setCanSeeFriendlyInvisibles(true);
        this.friendlyTeam.setColor(ChatColor.DARK_GREEN);

        this.enemyTeam.setColor(ChatColor.RED);
    }

    public void clearEnemyTeam() {
        for (String entry : enemyTeam.getEntries()) {
            enemyTeam.removeEntry(entry);
        }
    }
}