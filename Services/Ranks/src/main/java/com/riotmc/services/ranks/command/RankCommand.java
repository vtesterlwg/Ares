package com.riotmc.services.ranks.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.playares.commons.base.promise.SimplePromise;
import com.riotmc.services.ranks.RankService;
import com.riotmc.services.ranks.data.RiotRank;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;

@CommandAlias("rank")
public final class RankCommand extends BaseCommand {
    @Getter
    public RankService rankService;

    public RankCommand(RankService service) {
        this.rankService = service;
    }

    @Subcommand("setdisplay|sd")
    @CommandPermission("rank.configure")
    @CommandCompletion("@ranks")
    @Description("Set the display name of a Rank")
    @Syntax("<rank> <display>")
    public void onSetDisplay(CommandSender sender, String rankName, String displayName) {
        rankService.getRankHandler().setDisplay(rankName, displayName, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Updated display name");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("setprefix|spre")
    @CommandPermission("rank.configure")
    @CommandCompletion("@ranks")
    @Description("Set the prefix of a Rank")
    @Syntax("<rank> <prefix>")
    public void onSetPrefix(CommandSender sender, String rankName, String rankPrefix) {
        rankService.getRankHandler().setPrefix(rankName, rankPrefix, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Updated prefix");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("setperm|sperm")
    @CommandPermission("rank.configure")
    @CommandCompletion("@ranks")
    @Description("Set the permission for a Rank")
    @Syntax("<rank> <permission>")
    public void onSetPermission(CommandSender sender, String rankName, String permission) {
        rankService.getRankHandler().setPermission(rankName, permission, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Updated permission");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("setweight|sw")
    @CommandPermission("rank.configure")
    @CommandCompletion("@ranks")
    @Description("Set the display name of a Rank")
    @Syntax("<rank> <display>")
    public void onSetWeight(CommandSender sender, String rankName, int weight) {
        rankService.getRankHandler().setWeight(rankName, weight, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Updated weight");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("setstaff|ss")
    @CommandPermission("rank.configure")
    @CommandCompletion("@ranks")
    @Description("Set the rank to be staff only")
    @Syntax("<rank> <true/false>")
    public void onSetStaff(CommandSender sender, String rankName, boolean staff) {
        rankService.getRankHandler().setStaff(rankName, staff, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Rank status updated");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("seteveryone|se")
    @CommandPermission("rank.configure")
    @CommandCompletion("@ranks")
    @Description("Set the rank to be a default")
    @Syntax("<rank> <true/false>")
    public void onSetEveryone(CommandSender sender, String rankName, boolean everyone) {
        rankService.getRankHandler().setEveryone(rankName, everyone, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Rank status updated");
            }

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("list")
    @Description("List all ranks")
    public void onListCommand(Player player) {
        final List<String> names = Lists.newArrayList();
        rankService.getSortedRanks().forEach(rank -> names.add(rank.getDisplayName()));
        player.sendMessage(ChatColor.GOLD + "Ranks" + ChatColor.YELLOW + ": " + ChatColor.RESET + Joiner.on(ChatColor.WHITE + ", ").join(names));
    }

    @Subcommand("create")
    @CommandPermission("rank.create")
    @Description("Create a new rank")
    public void onCreateRank(Player player, String name) {
        rankService.getRankHandler().createRank(name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Rank created, requires additional setup");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("delete")
    @CommandPermission("rank.delete")
    @CommandCompletion("@ranks")
    @Description("Delete a rank")
    public void onDeleteRank(Player player, String name) {
        final RiotRank rank = rankService.getRank(name);

        if (rank == null) {
            player.sendMessage(ChatColor.RED + "Rank not found");
            return;
        }

        rankService.getRankHandler().deleteRank(rank, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Rank has been deleted");
            }

            @Override
            public void failure(String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}