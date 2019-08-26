package com.playares.lobby.selector;

import lombok.Getter;
import org.bukkit.entity.Player;

public final class SelectorHandler {
    @Getter public final SelectorManager manager;

    public SelectorHandler(SelectorManager manager) {
        this.manager = manager;
    }

    public void openMenu(Player viewer) {
        final SelectorMenu menu = new SelectorMenu(getManager().getPlugin(), viewer);
        menu.open();
    }
}
