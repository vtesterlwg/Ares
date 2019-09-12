package com.playares.factions.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.playares.factions.Factions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class FactionsHelpCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @CommandAlias("help")
    @Description("View help information for this server")
    public void onHelp(Player player) {
        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Factions Help");

        player.sendMessage(new ComponentBuilder("/f help")
        .color(ChatColor.GOLD).bold(true)
        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f help"))
        .append(" View factions commands").color(ChatColor.GRAY).bold(false).create());

        player.sendMessage(new ComponentBuilder("/map")
        .color(ChatColor.GOLD).bold(true)
        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/map"))
        .append(" View map-specific information").color(ChatColor.GRAY).bold(false).create());

        player.sendMessage(new ComponentBuilder("/events")
        .color(ChatColor.GOLD).bold(true)
        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/events"))
        .append(" View all event locations & active event leaderboards").color(ChatColor.GRAY).bold(false)
        .create());

        player.sendMessage(new ComponentBuilder("/boosts")
        .color(ChatColor.GOLD).bold(true)
        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/boosts"))
        .append(" Access your global server boosts").color(ChatColor.GRAY)
        .create());

        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "/lives" + ChatColor.GRAY + " View your lives");
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "/revive" + ChatColor.BLUE + " [player]" + ChatColor.GRAY + " Revive yourself or a friend");
    }
}