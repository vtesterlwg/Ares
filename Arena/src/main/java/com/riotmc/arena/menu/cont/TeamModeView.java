package com.riotmc.arena.menu.cont;

import com.riotmc.arena.team.Team;
import com.riotmc.commons.bukkit.RiotPlugin;
import com.riotmc.commons.bukkit.menu.Menu;
import lombok.Getter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public final class TeamModeView extends Menu {
    @Getter
    public final Team challenged;

    public TeamModeView(@Nonnull RiotPlugin plugin, @Nonnull Player player, @Nonnull String title, int rows, @Nonnull Team challenged) {
        super(plugin, player, title, rows);
        this.challenged = challenged;
    }
}
