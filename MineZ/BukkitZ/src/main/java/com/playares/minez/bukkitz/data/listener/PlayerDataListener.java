package com.playares.minez.bukkitz.data.listener;

import com.playares.minez.bukkitz.MineZ;
import com.playares.minez.bukkitz.data.MZPlayer;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerDataListener implements Listener {
    @Getter public MineZ plugin;

    public PlayerDataListener(MineZ plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final MZPlayer test = new MZPlayer(plugin, player);

        plugin.getPlayerManager().getPlayers().add(test);
    }
}
