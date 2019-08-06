package com.playares.services.ranks.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.services.ranks.RankService;
import com.playares.services.ranks.data.Rank;
import com.playares.services.ranks.data.RankDataHandler;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandAlias("rank")
public final class RankCommand extends BaseCommand {
    @Getter public RankService service;
    @Getter public RankDataHandler dataHandler;

    public RankCommand(RankService service) {
        this.service = service;
        this.dataHandler = new RankDataHandler(service);
    }

    @Subcommand("create")
    @CommandPermission("rank.configure")
    @Description("Create a new rank")
    @Syntax("<name>")
    public void onCreate(CommandSender sender, String rankName) {
        dataHandler.create(sender, rankName);
    }

    @Subcommand("delete")
    @CommandPermission("rank.configure")
    @Description("Delete a rank")
    @Syntax("<name>")
    public void onDelete(CommandSender sender, String rankName) {
        dataHandler.delete(sender, rankName);
    }

    @Subcommand("give|apply")
    @CommandPermission("rank.give")
    @Description("Give a rank to a player")
    @Syntax("<player> <rank>")
    public void onGive(CommandSender sender, String playerName, String rankName) {
        dataHandler.applyRank(sender, playerName, rankName);
    }

    @Subcommand("revoke|remove")
    @CommandPermission("rank.revoke")
    @Description("Revoke a rank from a player")
    @Syntax("<player> <rank>")
    public void onRevoke(CommandSender sender, String playerName, String rankName) {
        dataHandler.removeRank(sender, playerName, rankName);
    }

    @Subcommand("set")
    @CommandPermission("rank.configure")
    @CommandCompletion("@ranks")
    @Description("Update the value for a rank")
    @Syntax("<rank> <key> <value>")
    public void onSet(CommandSender sender, String rankName, String key, String value) {
        final Rank rank = service.getRankByName(rankName);

        if (rank == null) {
            sender.sendMessage(ChatColor.RED + "Rank not found");
            return;
        }

        if (key.equalsIgnoreCase("name")) {
            dataHandler.setName(sender, rank, value);
            return;
        }

        if (key.equalsIgnoreCase("displayname")) {
            dataHandler.setDisplayName(sender, rank, value);
            return;
        }

        if (key.equalsIgnoreCase("prefix")) {
            dataHandler.setPrefix(sender, rank, value);
            return;
        }

        if (key.equalsIgnoreCase("permission")) {
            dataHandler.setPermission(sender, rank, value);
            return;
        }

        if  (key.equalsIgnoreCase("weight")) {
            dataHandler.setWeight(sender, rank, value);
            return;
        }

        if (key.equalsIgnoreCase("staff")) {
            dataHandler.setStaff(sender, rank, value);
            return;
        }

        if (key.equalsIgnoreCase("default")) {
            dataHandler.setDefault(sender, rank, value);
            return;
        }

        sender.sendMessage(ChatColor.RED + "Invalid key, allowed: <name/displayname/prefix/permission/weight/staff/default>");
    }
}