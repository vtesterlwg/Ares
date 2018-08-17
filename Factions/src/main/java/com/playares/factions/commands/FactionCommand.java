package com.playares.factions.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.entity.Player;

@CommandAlias("factions|faction|f|teams|team|t")
public final class FactionCommand extends BaseCommand {
    @Subcommand("create")
    public void onCreate(Player player, @Single String name) {

    }

    @Subcommand("accept")
    public void onAccept(Player player, String name) {

    }

    @Subcommand("leave")
    public void onLeave(Player player) {

    }

    @Subcommand("home")
    public void onHome(Player player) {

    }

    @Subcommand("stuck")
    public void onStuck(Player player) {

    }

    @Subcommand("deposit|d")
    public void onDeposit(Player player, double amount) {

    }

    @Subcommand("deposit|d")
    public void onDeposit(Player player, @Flags("all") String all) {

    }

    @Subcommand("withdraw|w")
    public void onWithdrawl(Player player, double amount) {

    }

    @Subcommand("withdraw|w")
    public void onWithdraw(Player player, @Flags("all") String all) {

    }

    @Subcommand("show|who")
    public void onShow(Player player, String name) {

    }

    @Subcommand("map")
    public void onMap(Player player) {

    }

    @Subcommand("list")
    public void onList(Player player) {

    }

    @Subcommand("list")
    public void onList(Player player, int page) {

    }

    @Subcommand("invite|inv")
    public void onInvite(Player player, String username) {

    }

    @Subcommand("uninvite|uninv")
    public void onUninvite(Player player, String username) {

    }

    @Subcommand("kick")
    public void onKick(Player player, String username) {

    }

    @Subcommand("claim")
    public void onClaim(Player player, String username) {

    }

    @Subcommand("sethome")
    public void onSetHome(Player player) {

    }

    @Subcommand("announcement|a")
    public void onAnnouncement(Player player, String message) {

    }

    @Subcommand("promote")
    public void onPromote(Player player, String username) {

    }

    @Subcommand("demote")
    public void onDemote(Player player, String username) {

    }

    @Subcommand("unclaim")
    public void onUnclaim(Player player) {

    }

    @Subcommand("unclaim")
    public void onUnclaim(Player player, @Flags("all") String all) {

    }

    @Subcommand("rename")
    public void onRename(Player player, @Single String name) {

    }

    @Subcommand("disband")
    public void onDisband(Player player) {

    }
}
