package com.playares.services.serversync.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.playares.services.serversync.ServerSyncService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class LobbyCommand extends BaseCommand {
    @Getter public final ServerSyncService service;

    @CommandAlias("hub|lobby")
    @Description("Return to the lobby")
    public void onLobby(Player player) {
        service.sendToLobby(player);
    }
}
