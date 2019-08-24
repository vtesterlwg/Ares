package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.services.essentials.data.invsee.ViewableInventory;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class InvSeeCommand extends BaseCommand {
    @Getter public final AresPlugin plugin;

    public InvSeeCommand(AresPlugin plugin) {
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

    @HelpCommand
    @Description("View a list of Invsee Commands")
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }
}