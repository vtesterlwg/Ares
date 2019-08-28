package com.playares.factions.claims.subclaims.handler;

import com.playares.factions.claims.subclaims.data.Subclaim;
import com.playares.factions.claims.subclaims.manager.SubclaimManager;
import com.playares.factions.claims.subclaims.menu.SubclaimMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class SubclaimEditorHandler {
    @Getter public final SubclaimManager manager;

    public void openEditor(Player viewer, Subclaim subclaim) {
        final SubclaimMenu menu = new SubclaimMenu(manager.getPlugin(), viewer, subclaim);
        menu.open();
        menu.update();
    }
}
