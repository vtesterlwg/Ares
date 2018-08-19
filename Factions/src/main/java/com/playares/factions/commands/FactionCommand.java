package com.playares.factions.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.factions.Factions;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

/*
/f buffer <faction> <buffer>
/f reinvite restock <all/faction>
/f tag <faction> <tag>
*/
@CommandAlias("factions|faction|f|teams|team|t")
public final class FactionCommand extends BaseCommand {
    @Getter
    public final Factions plugin;

    public FactionCommand(Factions plugin) {
        this.plugin = plugin;
    }

    @Subcommand("create")
    @Description("Create a faction")
    @Syntax("<name>")
    public void onCreate(Player player, @Single String name) {
        plugin.getFactionManager().getCreateHandler().createFaction(player, name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Your faction has been created");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("create")
    @Description("Create a faction")
    @Syntax("[server] <name>")
    @CommandPermission("factions.create.server")
    public void onCreate(Player player, @Flags("server") String server, @Single String name) {

    }

    @Subcommand("accept")
    @Description("Accept an invitation")
    @Syntax("<factionName/playerName>")
    public void onAccept(Player player, String name) {
        plugin.getFactionManager().getCreateHandler().acceptInvite(player, name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "You have joined the faction");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
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
    @Description("View your faction about page")
    public void onShow(Player player) {
        plugin.getFactionManager().getDisplayHandler().prepareFactionInfo(player, player.getName(), new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("show|who")
    @Description("View a factions about page")
    @Syntax("<player/faction>")
    @CommandCompletion("@players")
    public void onShow(Player player, String name) {
        plugin.getFactionManager().getDisplayHandler().prepareFactionInfo(player, name, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
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
        plugin.getFactionManager().getCreateHandler().sendInvite(player, username, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Invitation sent");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("uninvite|uninv")
    @Description("Revoke a players invitation to your faction")
    @Syntax("<player>")
    public void onUninvite(Player player, String username) {
        plugin.getFactionManager().getCreateHandler().revokeInvite(player, username, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Invitation has been revoked");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
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

    @Subcommand("claim")
    @Description("Begin the claiming process for your faction")
    @Syntax("[faction]")
    @CommandPermission("factions.claim.others")
    @CommandCompletion("@factions")
    public void onClaim(Player player, String faction) {

    }

    @Subcommand("subclaim")
    @Description("Subclaim the chest you are looking at")
    public void onSubclaim(Player player) {

    }

    @Subcommand("sethome")
    @Description("Update your factions home location to your current location")
    public void onSetHome(Player player) {

    }

    @Subcommand("sethome")
    @Description("Update your factions home location to your current location")
    @Syntax("[faction]")
    @CommandPermission("factions.sethome.others")
    @CommandCompletion("@factions")
    public void onSetHome(Player player, String faction) {

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
        plugin.getFactionManager().getManageHandler().rename(player, name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Faction name updated");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("rename")
    @Description("Rename your faction")
    @Syntax("[faction] <name>")
    @CommandPermission("factions.rename.others")
    @CommandCompletion("@factions")
    public void onRename(Player player, String faction, String name) {
        plugin.getFactionManager().getManageHandler().renameOther(player, faction, name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Faction name updated");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("disband")
    @Description("Disband your faction")
    public void onDisband(Player player) {

    }

    @Subcommand("disband")
    @Description("Disband your faction")
    @Syntax("[faction]")
    @CommandPermission("factions.disband.others")
    @CommandCompletion("@factions")
    public void onDisband(Player player, String faction) {

    }

    @Subcommand("rally|r")
    @Description("Update your factions rallypoint")
    public void onRally(Player player) {

    }

    @Subcommand("freeze")
    @Description("Freeze a factions DTR")
    @Syntax("<faction> <time>")
    @CommandPermission("factions.freeze.others")
    @CommandCompletion("@factions")
    public void onFreeze(Player player, String faction, String time) {
        plugin.getFactionManager().getStaffHandler().freeze(player, faction, time, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Faction regeneration has been frozen");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("unfreeze|thaw")
    @Description("Unfreeze a factions DTR")
    @Syntax("<faction>")
    @CommandPermission("factions.freeze.others")
    @CommandCompletion("@factions")
    public void onUnfreeze(Player player, String faction) {
        plugin.getFactionManager().getStaffHandler().unfreeze(player, faction, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Faction regeneration has been thawed");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("setdtr")
    @Description("Update a factions DTR")
    @Syntax("<faction> <value>")
    @CommandPermission("factions.dtr.others")
    @CommandCompletion("@factions")
    public void onSetDTR(Player player, String name, double dtr) {
        plugin.getFactionManager().getStaffHandler().updateDTR(player, name, dtr, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Faction DTR has been updated");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("help")
    @Description("View a list of commands")
    public void onHelp(Player player) {

    }
}