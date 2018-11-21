package com.riotmc.factions.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.factions.Factions;
import lombok.Getter;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

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
    public void onCreate(Player player, @Values("server") String server, @Single String name) {
        plugin.getFactionManager().getCreateHandler().createServerFaction(player, name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Faction has been created");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
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
        plugin.getFactionManager().getDisbandHandler().leave(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "You have left the faction");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("home")
    @Description("Return to your faction home")
    public void onHome(Player player) {
        plugin.getPlayerManager().getTimerHandler().attemptHome(player, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("stuck")
    @Description("Teleport outside of the claim you are standing in")
    public void onStuck(Player player) {
        plugin.getPlayerManager().getTimerHandler().attemptStuck(player, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
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
        plugin.getClaimManager().getMapHandler().renderMap(player, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("list")
    @Description("Retrieve a list of all factions")
    @Syntax("[page]")
    public void onList(Player player) {
        plugin.getFactionManager().getDisplayHandler().viewList(player, 1, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("list")
    @Description("Retrieve a list of all factions")
    @Syntax("[page]")
    public void onList(Player player, int page) {
        final int cleanPage = (page > 0) ? page : 1;

        plugin.getFactionManager().getDisplayHandler().viewList(player, cleanPage, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("chat|c")
    @Description("Change chat channels")
    @Syntax("[channel]")
    public void onChat(Player player) {
        plugin.getFactionManager().getChatHandler().cycleChatChannel(player, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(reason);
            }
        });
    }

    @Subcommand("chat|c")
    @Description("Change chat channels")
    @Syntax("[channel]")
    public void onChat(Player player, @Flags("p|pub|public|f|fac|faction|of|officer") String channel) {
        plugin.getFactionManager().getChatHandler().selectChatChannel(player, channel, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
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
        plugin.getFactionManager().getDisbandHandler().kick(player, username, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Player has been kicked");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("claim")
    @Description("Begin the claiming process for your faction")
    public void onClaim(Player player) {
        plugin.getClaimManager().getCreationHandler().startClaiming(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GOLD + "You have been given a Claiming Stick");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("claim")
    @Description("Begin the claiming process for your faction")
    @Syntax("[faction]")
    @CommandPermission("factions.claim.others")
    public void onClaim(Player player, String faction) {
        plugin.getClaimManager().getCreationHandler().startClaiming(player, faction, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GOLD + "You have been given a Claiming Stick");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("subclaim")
    @Description("Subclaim the chest you are looking at")
    public void onSubclaim(Player player) {

    }

    @Subcommand("sethome")
    @Description("Update your factions home location to your current location")
    public void onSetHome(Player player) {
        plugin.getFactionManager().getManageHandler().setHome(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Faction home location updated");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("sethome")
    @Description("Update your factions home location to your current location")
    @Syntax("[faction]")
    @CommandPermission("factions.sethome.others")
    public void onSetHome(Player player, String faction) {
        plugin.getFactionManager().getManageHandler().setHome(player, faction, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Faction home location updated");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("announcement|a")
    @Description("Send an announcement to all members in the faction")
    @Syntax("<message>")
    public void onAnnouncement(Player player, String message) {
        plugin.getFactionManager().getDisplayHandler().updateAnnouncement(player, message, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Faction announcement has been updated");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("promote")
    @Description("Promote a player to the next highest role")
    @Syntax("<player>")
    public void onPromote(Player player, String username) {
        plugin.getFactionManager().getManageHandler().promote(player, username, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Player has been promoted");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("demote")
    @Description("Demote a player to the next lowest role")
    @Syntax("<player>")
    public void onDemote(Player player, String username) {
        plugin.getFactionManager().getManageHandler().demote(player, username, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Player has been demoted");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("unclaim")
    @Description("Unclaim your land")
    @Syntax("[all]")
    public void onUnclaim(Player player) {
        plugin.getClaimManager().getDeleteHandler().unclaim(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Land unclaimed");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
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
    public void onRename(Player player, String faction, String name) {
        plugin.getFactionManager().getManageHandler().rename(player, faction, name, new SimplePromise() {
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
        plugin.getFactionManager().getDisbandHandler().disband(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Faction has been disbanded");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("disband")
    @Description("Disband your faction")
    @Syntax("[faction]")
    @CommandPermission("factions.disband.others")
    public void onDisband(Player player, String faction) {
        plugin.getFactionManager().getDisbandHandler().disband(player, faction, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Faction has been disbanded");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("rally|r")
    @Description("Update your factions rallypoint")
    public void onRally(Player player) {
        plugin.getFactionManager().getDisplayHandler().updateRally(player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Rally updated to your location");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("freeze")
    @Description("Freeze a factions DTR")
    @Syntax("<faction> <time>")
    @CommandPermission("factions.freeze.others")
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

    @Subcommand("setflag")
    @Description("Update a factions flag")
    @CommandPermission("factions.flag.others")
    @Syntax("[faction] [flag]")
    public void onSetFlag(Player player, String faction, @Values("SAFEZONE|LANDMARK|EVENT") String flag) {
        plugin.getFactionManager().getManageHandler().setFlag(player, faction, flag, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Faction flag has been updated");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("setname|setdisplayname|sdn")
    @Description("Update a factions display name")
    @CommandPermission("factions.displayname.others")
    @Syntax("[faction] [displayName]")
    public void onSetDisplayName(Player player, String faction, @Single String displayName) {
        plugin.getFactionManager().getManageHandler().setDisplayName(player, faction, displayName, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Faction display name has been updated");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("setbuffer|sb")
    @Description("Update a factions buffer radius")
    @CommandPermission("factions.buffer.others")
    @Syntax("[faction] [buffer]")
    public void onSetBuffer(Player player, String faction, double buffer) {
        plugin.getFactionManager().getManageHandler().setBuffer(player, faction, buffer, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Faction buffer has been updated");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("leaderboard|lb|top")
    @Description("View the faction leaderboards")
    @Syntax("[category]")
    public void onLeaderboard(Player player) {
        plugin.getFactionManager().getDisplayHandler().displayLeaderboard(player, "elo", new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("leaderboard|lb|top")
    @Description("View the faction leaderboards")
    @Syntax("[category]")
    public void onLeaderboard(Player player, @Flags("rating|elo|e|kill|kills|k|death|deaths|d|minorevent|minorevents|minor|majorevent|majorevents|major") String category) {
        plugin.getFactionManager().getDisplayHandler().displayLeaderboard(player, category, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("help")
    @Description("View a list of commands")
    public void onHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "Factions Help");
        player.sendMessage(" ");
        player.sendMessage(ChatColor.GOLD + "Create");

        player.sendMessage(new ComponentBuilder("/f create <name>")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Creates a new faction under your ownership")
                        .create()))
                .create());

        player.sendMessage(new ComponentBuilder("/f invite <player>")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Invite a player to join your faction")
                        .create()))
                .create());

        player.sendMessage(new ComponentBuilder("/f accept <faction>")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Accepts an invitation received from a faction")
                        .create()))
                .create());

        player.sendMessage(" ");
        player.sendMessage(ChatColor.GOLD + "Claiming");

        player.sendMessage(new ComponentBuilder("/f claim")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Begin the claiming process for your faction")
                        .append("\nHover over the item you receive for more information")
                        .create()))
                .create());

        player.sendMessage(new ComponentBuilder("/f unclaim [all]")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Unclaim the land you are currently standing in for your faction")
                        .append("\nWarning: If you provide the [all] flag, all of your claims will be removed")
                        .color(net.md_5.bungee.api.ChatColor.RED)
                        .append("\nYour faction will only be refunded 75% of the original claim value")
                        .color(net.md_5.bungee.api.ChatColor.RED)
                        .create()))
                .create());

        player.sendMessage(" ");
        player.sendMessage(ChatColor.GOLD + "Subclaiming");

        player.sendMessage(new ComponentBuilder("/f subclaim")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Creates a subclaim for the chest you are looking at")
                        .append("\nIf the chest is already subclaimed you will open the existing configuration for that chest")
                        .create()))
                .create());

        player.sendMessage(" ");
        player.sendMessage(ChatColor.GOLD + "Management");

        player.sendMessage(new ComponentBuilder("/f disband <name>")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Disband your faction")
                        .append("\nWarning: All claims will be removed and will be refunded to the unclaiming player's balance")
                        .color(net.md_5.bungee.api.ChatColor.RED)
                        .create()))
                .create());

        player.sendMessage(new ComponentBuilder("/f rename <name>")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Rename your faction to a new name")
                        .create()))
                .create());

        player.sendMessage(new ComponentBuilder("/f uninvite <player>")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Revoke a players existing invitation to join your faction")
                        .create()))
                .create());

        player.sendMessage(new ComponentBuilder("/f kick <player>")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Kicks a player from your faction")
                        .append("\nWarning: Re-inviting this player will consume a re-invite if they join before the next Palace event is captured")
                        .color(net.md_5.bungee.api.ChatColor.RED)
                        .create()))
                .create());

        player.sendMessage(new ComponentBuilder("/f leave")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Leave your faction")
                        .create()))
                .create());

        player.sendMessage(new ComponentBuilder("/f promote <player>")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Promote a player to the next highest rank in your faction")
                        .create()))
                .create());

        player.sendMessage(new ComponentBuilder("/f demote <player>")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Demote a player to the next lowest rank in your faction")
                        .create()))
                .create());

        player.sendMessage(new ComponentBuilder("/f sethome")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Set your faction home to your current location")
                        .create()))
                .create());

        player.sendMessage(" ");
        player.sendMessage(ChatColor.GOLD + "Economy");

        player.sendMessage(new ComponentBuilder("/f deposit <amount/all>")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Transfer money from your personal balance to your faction balance")
                        .create()))
                .create());

        player.sendMessage(new ComponentBuilder("/f withdraw <amount/all>")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Transfer money from your factions balance to your personal balance")
                        .create()))
                .create());

        player.sendMessage(" ");
        player.sendMessage(ChatColor.GOLD + "Warps");

        player.sendMessage(new ComponentBuilder("/f home")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Return to your faction home")
                        .create()))
                .create());

        player.sendMessage(new ComponentBuilder("/f stuck")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Warp out of the claim you are inside")
                        .create()))
                .create());

        player.sendMessage(" ");
        player.sendMessage(ChatColor.GOLD + "Display");

        player.sendMessage(new ComponentBuilder("/f show [faction/player]")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Displays faction information")
                        .create()))
                .create());

        player.sendMessage(new ComponentBuilder("/f map")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Draws pillars around nearby faction claims")
                        .create()))
                .create());

        player.sendMessage(new ComponentBuilder("/f list [page]")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Displays a list of all factions")
                        .create()))
                .create());

        player.sendMessage(new ComponentBuilder("/f leaderboard [category]")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Displays the faction leaderboard")
                        .append("\n ")
                        .append("\nValid Categories:")
                        .color(net.md_5.bungee.api.ChatColor.BLUE)
                        .append("\n - Rating")
                        .color(net.md_5.bungee.api.ChatColor.RESET)
                        .append("\n - Kills")
                        .append("\n - Deaths")
                        .append("\n - MinorEvents")
                        .append("\n - MajorEvents")
                        .create()))
                .create());

        player.sendMessage(" ");
        player.sendMessage(ChatColor.GOLD + "Misc");

        player.sendMessage(new ComponentBuilder("/f announcement <message>")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Set your factions announcement to a new message")
                        .create()))
                .create());

        player.sendMessage(new ComponentBuilder("/f rally")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Notify all of your faction members of your current location")
                        .create()))
                .create());

        player.sendMessage(new ComponentBuilder("/f chat [channel]")
                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Change your primary chat channel")
                        .append("\n ")
                        .append("\nValid Categories:")
                        .color(net.md_5.bungee.api.ChatColor.BLUE)
                        .append("\n - Global")
                        .color(net.md_5.bungee.api.ChatColor.RESET)
                        .append("\n - Faction")
                        .append("\n - Officer")
                        .create()))
                .create());

        if (player.hasPermission("factions.mod") || player.hasPermission("factions.admin") || player.isOp()) {
            player.sendMessage(" ");
            player.sendMessage(ChatColor.GOLD + "Staff");

            player.sendMessage(new ComponentBuilder("/f create server <name>")
                    .color(net.md_5.bungee.api.ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Creates a new server faction")
                            .create()))
                    .create());

            player.sendMessage(new ComponentBuilder("/f disband <faction>")
                    .color(net.md_5.bungee.api.ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Disband a named faction")
                            .create()))
                    .create());

            player.sendMessage(new ComponentBuilder("/f rename <faction> <name>")
                    .color(net.md_5.bungee.api.ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Rename a named faction")
                            .create()))
                    .create());

            player.sendMessage(new ComponentBuilder("/f claim <faction>")
                    .color(net.md_5.bungee.api.ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Begin claiming for a named faction")
                            .create()))
                    .create());

            player.sendMessage(new ComponentBuilder("/f unclaim <faction> [all]")
                    .color(net.md_5.bungee.api.ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Creates a new server faction")
                            .create()))
                    .create());

            player.sendMessage(new ComponentBuilder("/f freeze <faction> <time>")
                    .color(net.md_5.bungee.api.ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Freeze a factions power regeneration for a set amount of time")
                            .create()))
                    .create());

            player.sendMessage(new ComponentBuilder("/f unfreeze <faction>")
                    .color(net.md_5.bungee.api.ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Thaw a factions power regeneration")
                            .create()))
                    .create());

            player.sendMessage(new ComponentBuilder("/f setdtr <faction> <dtr>")
                    .color(net.md_5.bungee.api.ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Set a factions DTR to the specified value")
                            .create()))
                    .create());

            player.sendMessage(new ComponentBuilder("/f unclaim <faction> [all]")
                    .color(net.md_5.bungee.api.ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Creates a new server faction")
                            .create()))
                    .create());

            player.sendMessage(new ComponentBuilder("/f setflag <faction> <flag>")
                    .color(net.md_5.bungee.api.ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Set a Server Factions flag type")
                            .append("\n ")
                            .append("\nValid Flag Types:")
                            .color(net.md_5.bungee.api.ChatColor.BLUE)
                            .append("\n - Safezone")
                            .color(net.md_5.bungee.api.ChatColor.RESET)
                            .append("\n - Event")
                            .append("\n - Landmark")
                            .create()))
                    .create());

            player.sendMessage(new ComponentBuilder("/f setname <faction> <name>")
                    .color(net.md_5.bungee.api.ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Updates only the display name for Server Factions")
                            .create()))
                    .create());

            player.sendMessage(new ComponentBuilder("/f setbuffer <faction> <radius>")
                    .color(net.md_5.bungee.api.ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Set the buffer radius around a faction claim")
                            .create()))
                    .create());
        }

        player.sendMessage(ChatColor.RESET + " ");
        player.sendMessage(ChatColor.YELLOW + "Hover over commands for more information");
    }
}