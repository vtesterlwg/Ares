package com.riotmc.factions.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.factions.Factions;
import com.riotmc.factions.players.FactionPlayer;
import com.riotmc.factions.timers.PlayerTimer;
import com.riotmc.factions.timers.cont.player.ProtectionTimer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class PvPCommand extends BaseCommand {
    @Getter public final Factions plugin;

    public PvPCommand(Factions plugin) {
        this.plugin = plugin;
    }

    @CommandAlias("pvp")
    @Description("Access your PvP Protection")
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
}
