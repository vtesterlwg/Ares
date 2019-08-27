package com.playares.lobby.nameplate;

import com.playares.commons.bukkit.logger.Logger;
import com.playares.lobby.Lobby;
import com.playares.services.ranks.RankService;
import com.playares.services.ranks.data.Rank;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class NameplateManager {
    @Getter public final Lobby plugin;
    @Getter public Scoreboard scoreboard;

    public NameplateManager(Lobby plugin) {
        this.plugin = plugin;
    }

    public void build() {
        final RankService rankService = (RankService)getPlugin().getService(RankService.class);

        if (rankService == null) {
            Logger.error("Failed to obtain Rank Service while configuration scoreboard");
            return;
        }

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        if (Bukkit.getOnlinePlayers().size() > 0) {
            Bukkit.getOnlinePlayers().forEach(this::apply);
        }
    }

    private void registerTeams(RankService service) {
        service.getRanks().forEach(rank -> {
            if (scoreboard.getTeam(rank.getName()) == null) {
                final Team team = scoreboard.registerNewTeam(rank.getName());
                team.setPrefix(rank.getPrefix());
                team.setAllowFriendlyFire(true);
                team.setDisplayName(rank.getDisplayName());
            }
        });
    }

    public void apply(Player player) {
        if (scoreboard == null) {
            return;
        }

        final RankService rankService = (RankService)getPlugin().getService(RankService.class);

        if (rankService == null) {
            return;
        }

        final Rank rank = rankService.getHighestRank(player);

        if (rank == null) {
            return;
        }

        final Team team = getScoreboard().getTeam(rank.getName());

        if (team == null) {
            registerTeams(rankService);
        }

        if (team != null) {
            team.addEntry(player.getName());
        }

        player.setScoreboard(scoreboard);
    }

    public void remove(Player player) {
        if (scoreboard == null) {
            return;
        }

        scoreboard.getTeams().stream().filter(team -> team.hasEntry(player.getName())).forEach(team -> team.removeEntry(player.getName()));
    }
}