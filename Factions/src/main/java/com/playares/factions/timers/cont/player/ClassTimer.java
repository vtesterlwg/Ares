package com.playares.factions.timers.cont.player;

import com.playares.commons.bukkit.util.Players;
import com.playares.factions.timers.PlayerTimer;
import com.playares.services.playerclasses.data.Class;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class ClassTimer extends PlayerTimer {
    final Class playerClass;

    public ClassTimer(UUID uniqueId, Class playerClass, int seconds) {
        super(uniqueId, PlayerTimerType.CLASS, seconds);
        this.playerClass = playerClass;
    }

    @Override
    public void onFinish() {
        final Player player = Bukkit.getPlayer(getOwner());

        if (player == null) {
            return;
        }

        playerClass.activate(player);
        Players.playSound(player, Sound.ITEM_ARMOR_EQUIP_GENERIC);
    }
}
