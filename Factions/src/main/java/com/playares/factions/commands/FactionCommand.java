package com.playares.factions.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.factions.Factions;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@CommandAlias("factions|faction|f|teams|team|t")
public final class FactionCommand extends BaseCommand {
    @Getter public final Factions plugin;

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
    public void onDeposit(Player player, String value) {
        if (value.equalsIgnoreCase("all")) {
            plugin.getFactionManager().getEconomyHandler().depositAll(player, new SimplePromise() {
                @Override
                public void success() {}

                @Override
                public void failure(@Nonnull String reason) {
                    player.sendMessage(ChatColor.RED + reason);
                }
            });

            return;
        }

        final double amount;

        try {
            amount = Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            player.sendMessage(ChatColor.RED + "Value must be a number");
            return;
        }

        plugin.getFactionManager().getEconomyHandler().deposit(player, amount, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("withdraw|w")
    @Description("Withdraw money from your faction balance")
    @Syntax("<amount/all>")
    public void onWithdrawl(Player player, String value) {
        if (value.equalsIgnoreCase("all")) {
            plugin.getFactionManager().getEconomyHandler().withdrawAll(player, new SimplePromise() {
                @Override
                public void success() {}

                @Override
                public void failure(@Nonnull String reason) {
                    player.sendMessage(ChatColor.RED + reason);
                }
            });

            return;
        }

        final double amount;

        try {
            amount = Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            player.sendMessage(ChatColor.RED + "Value must be a number");
            return;
        }

        plugin.getFactionManager().getEconomyHandler().withdraw(player, amount, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
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
        plugin.getSubclaimManager().getCreationHandler().create(player, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
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
    public void onUnclaim(Player player, @Values("all") String all) {
        plugin.getClaimManager().getDeleteHandler().unclaimAll(player, new SimplePromise() {
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

    @Subcommand("unclaimfor")
    @Description("Unclaim land for a faction")
    @Syntax("[all]")
    @CommandPermission("factions.unclaim.others")
    public void onUnclaimFor(Player player, String faction) {
        plugin.getClaimManager().getDeleteHandler().unclaimFor(player, faction, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Faction land unclaimed");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("unclaimfor")
    @Description("Unclaim land for a faction")
    @Syntax("[all]")
    @CommandPermission("factions.unclaim.others")
    public void onUnclaimAllFor(Player player, String faction, @Values("all") String all) {
        plugin.getClaimManager().getDeleteHandler().unclaimAllFor(player, faction, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Faction land unclaimed");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
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

    /*
    /f setbuffer <faction> <claim/build> <buffer>
     */

    @Subcommand("setbuffer|buffer|sb")
    @Description("Update a Server Faction's buffer radius")
    @CommandPermission("factions.buffer.others")
    @Syntax("<faction> <claim/build> <radius>")
    public void onSetBuffer(Player player, String faction, @Values("claim|build") String type, double buffer) {
        if (type.equalsIgnoreCase("claim")) {
            plugin.getFactionManager().getManageHandler().setClaimBuffer(player, faction, buffer, new SimplePromise() {
                @Override
                public void success() {
                    player.sendMessage(ChatColor.GREEN + "Claim buffer has been updated to " + buffer);
                }

                @Override
                public void failure(@Nonnull String reason) {
                    player.sendMessage(ChatColor.RED + reason);
                }
            });

            return;
        }

        plugin.getFactionManager().getManageHandler().setBuildBuffer(player, faction, buffer, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Build buffer has been updated to " + buffer);
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("reload")
    @Description("Reload plugin configuration")
    @CommandPermission("factions.reload")
    public void onReload(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Now reloading Factions configuration");
        getPlugin().getFactionConfig().loadValues();

        sender.sendMessage(ChatColor.YELLOW + "Factions configuration finished reloading. Now reloading addons...");
        getPlugin().getAddonManager().reloadAddons();

        sender.sendMessage(ChatColor.YELLOW + "Now reloading all active services...");
        getPlugin().reloadServices();

        sender.sendMessage(ChatColor.GREEN + "Complete!");
    }

    @HelpCommand
    public void onHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}