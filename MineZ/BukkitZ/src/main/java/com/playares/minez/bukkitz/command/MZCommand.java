package com.playares.minez.bukkitz.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import com.playares.minez.bukkitz.MineZ;
import com.playares.minez.bukkitz.menu.ServerMenu;
import lombok.Getter;
import org.bukkit.entity.Player;

/*
/mz setpve <true/false>
/mz setpremium <true/false>
/mz spawn
/mz shop
/mz settings
 */

@CommandAlias("mz|minez")
public final class MZCommand extends BaseCommand {
    @Getter public final MineZ plugin;

    public MZCommand(MineZ plugin) {
        this.plugin = plugin;
    }

    @Subcommand("server|servers")
    public void openServerSelector(Player player) {
        new ServerMenu(plugin, player).open();
    }

    @Subcommand("test")
    public void onTest(Player player) {
        // give bandage from item manager
    }
}
