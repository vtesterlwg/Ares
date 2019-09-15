package com.playares.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import com.playares.arena.Arenas;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class DuelCommand extends BaseCommand {
    @Getter public final Arenas plugin;

    @CommandAlias("duel")
    public void onDuel(Player player, String username) {

    }
}
