package com.playares.factions.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.Factions;
import com.playares.factions.players.data.FactionPlayer;
import com.playares.factions.timers.PlayerTimer;
import com.playares.factions.timers.cont.player.ProtectionTimer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class PvPCommand extends BaseCommand {
    @Getter public final Factions plugin;

    public PvPCommand(Factions plugin) {
        this.plugin = plugin;
    }

    @CommandAlias("pvp")
    @Description("Remove your PvP Protection")
    @Syntax("<enable>")
    public void onPvP(Player player, @Values("enable") String enable) {
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            return;
        }

        final ProtectionTimer timer = (ProtectionTimer)profile.getTimer(PlayerTimer.PlayerTimerType.PROTECTION);

        if (timer == null) {
            player.sendMessage(ChatColor.RED + "You do not have PvP Protection");
            return;
        }

        profile.getTimers().remove(timer);
        timer.onFinish();

        Logger.print(player.getName() + " removed their PvP Protection");
    }

    @HelpCommand
    @Description("View a list of PvP Commands")
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }
}
