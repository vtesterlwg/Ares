package com.riotmc.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Syntax;
import com.playares.commons.bukkit.RiotPlugin;
import com.riotmc.services.essentials.data.invsee.ViewableInventory;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class InvSeeCommand extends BaseCommand {
    @Getter
    public final RiotPlugin plugin;

    public InvSeeCommand(RiotPlugin plugin) {
        this.plugin = plugin;
    }

    @CommandAlias("invsee|inv|viewinventory|viewinv")
    @CommandPermission("essentials.invsee")
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void onCommand(Player player, String viewing) {
        final Player viewingPlayer = Bukkit.getPlayer(viewing);

        final ViewableInventory viewable = new ViewableInventory(plugin, player, viewingPlayer);

        viewable.open();
        viewable.startUpdater();
    }
}