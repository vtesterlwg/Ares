package com.playares.lobby.spawn;

import com.playares.commons.bukkit.location.PLocatable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class SpawnHandler {
    @Getter public final SpawnManager manager;

    public void setSpawn(Player player) {
        manager.setSpawn(new PLocatable(player));
        manager.save();
        player.sendMessage(ChatColor.GREEN + "Spawn has been updated");
    }
}
