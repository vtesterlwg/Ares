package com.riotmc.arena.menu.cont;

import com.riotmc.arena.player.ArenaPlayer;
import com.riotmc.commons.bukkit.RiotPlugin;
import com.riotmc.commons.bukkit.menu.Menu;
import lombok.Getter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public final class DuelModeView extends Menu {
    @Nonnull @Getter
    public final ArenaPlayer challenged;

    public DuelModeView(@Nonnull RiotPlugin plugin, @Nonnull Player player, @Nonnull String title, int rows, @Nonnull ArenaPlayer challenged) {
        super(plugin, player, title, rows);
        this.challenged = challenged;
    }
}
