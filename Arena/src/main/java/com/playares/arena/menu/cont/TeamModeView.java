package com.playares.arena.menu.cont;

import com.playares.arena.team.Team;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.menu.Menu;
import lombok.Getter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public final class TeamModeView extends Menu {
    @Getter
    public final Team challenged;

    public TeamModeView(@Nonnull AresPlugin plugin, @Nonnull Player player, @Nonnull String title, int rows, @Nonnull Team challenged) {
        super(plugin, player, title, rows);
        this.challenged = challenged;
    }
}
