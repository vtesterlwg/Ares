package com.playares.arena.menu.cont;

import com.playares.arena.player.ArenaPlayer;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.menu.Menu;
import lombok.Getter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public final class DuelModeView extends Menu {
    @Nonnull @Getter
    public final ArenaPlayer challenged;

    public DuelModeView(@Nonnull AresPlugin plugin, @Nonnull Player player, @Nonnull String title, int rows, @Nonnull ArenaPlayer challenged) {
        super(plugin, player, title, rows);
        this.challenged = challenged;
    }
}
