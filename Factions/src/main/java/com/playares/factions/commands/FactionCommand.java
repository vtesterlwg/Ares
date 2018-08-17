package com.playares.factions.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;

@CommandAlias("factions|faction|f|teams|team|t")
public final class FactionCommand extends BaseCommand {
    @Subcommand("create")
    @Description("Create a faction")
    @Syntax("<name>")
    public void onCreate(Player player, @Single String name) {

    }

    @Subcommand("accept")
    @Description("Accept an invitation")
    @Syntax("<factionName/playerName>")
    public void onAccept(Player player, String name) {

    }

    @Subcommand("leave")
    @Description("Leave your faction")
    public void onLeave(Player player) {

    }

    @Subcommand("home")
    @Description("Return to your faction home")
    public void onHome(Player player) {

    }

    @Subcommand("stuck")
    @Description("Teleport outside of the claim you are standing in")
    public void onStuck(Player player) {

    }

    @Subcommand("deposit|d")
    @Description("Deposit money in to your faction balance")
    @Syntax("<amount/all>")
    public void onDeposit(Player player, double amount) {

    }

    @Subcommand("deposit|d")
    @Description("Deposit money in to your faction balance")
    @Syntax("<amount/all>")
    public void onDeposit(Player player, @Flags("all") String all) {

    }

    @Subcommand("withdraw|w")
    @Description("Withdraw money from your faction balance")
    @Syntax("<amount/all>")
    public void onWithdrawl(Player player, double amount) {

    }

    @Subcommand("withdraw|w")
    @Description("Withdraw money from your faction balance")
    @Syntax("<amount/all>")
    public void onWithdraw(Player player, @Flags("all") String all) {

    }

    @Subcommand("show|who")
    @Description("View a factions about page")
    @Syntax("<player/faction>")
    @CommandCompletion("@players")
    public void onShow(Player player, String name) {

    }

    @Subcommand("map")
    @Description("View a map of all nearby factions")
    public void onMap(Player player) {

    }

    @Subcommand("list")
    @Description("Retrieve a list of all factions")
    @Syntax("[page]")
    public void onList(Player player) {

    }

    @Subcommand("list")
    @Description("Retrieve a list of all factions")
    @Syntax("[page]")
    public void onList(Player player, int page) {

    }

    @Subcommand("invite|inv")
    @Description("Invite a player to your faction")
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onInvite(Player player, String username) {

    }

    @Subcommand("uninvite|uninv")
    @Description("Revoke a players invitation to your faction")
    @Syntax("<player>")
    public void onUninvite(Player player, String username) {

    }

    @Subcommand("kick")
    @Description("Kick a player from your faction")
    @Syntax("<player>")
    public void onKick(Player player, String username) {

    }

    @Subcommand("claim")
    @Description("Begin the claiming process for your faction")
    public void onClaim(Player player) {

    }

    @Subcommand("subclaim")
    @Description("Subclaim the chest you are looking at")
    public void onSubclaim(Player player) {

    }

    @Subcommand("sethome")
    @Description("Update your factions home location to your current location")
    public void onSetHome(Player player) {

    }

    @Subcommand("announcement|a")
    @Description("Send an announcement to all members in the faction")
    @Syntax("<message>")
    public void onAnnouncement(Player player, String message) {

    }

    @Subcommand("promote")
    @Description("Promote a player to the next highest role")
    @Syntax("<player>")
    public void onPromote(Player player, String username) {

    }

    @Subcommand("demote")
    @Description("Demote a player to the next lowest role")
    @Syntax("<player>")
    public void onDemote(Player player, String username) {

    }

    @Subcommand("unclaim")
    @Description("Unclaim your land")
    @Syntax("[all]")
    public void onUnclaim(Player player) {

    }

    @Subcommand("unclaim")
    @Description("Unclaim your land")
    @Syntax("[all]")
    public void onUnclaim(Player player, @Flags("all") String all) {

    }

    @Subcommand("rename")
    @Description("Rename your faction")
    @Syntax("<name>")
    public void onRename(Player player, @Single String name) {

    }

    @Subcommand("disband")
    @Description("Disband your faction")
    public void onDisband(Player player) {

    }
}