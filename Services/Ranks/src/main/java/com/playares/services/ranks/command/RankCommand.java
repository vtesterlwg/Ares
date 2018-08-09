package com.playares.services.ranks.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.services.ranks.RankService;
import com.playares.services.ranks.data.AresRank;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

@CommandAlias("rank")
public final class RankCommand extends BaseCommand {
    @Getter
    public RankService rankService;

    public RankCommand(RankService service) {
        this.rankService = service;
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
        // TODO: Rank creation
    }

    @Subcommand("delete")
    @CommandPermission("rank.delete")
    @Description("Delete a rank")
    public void onDeleteRank(Player player, String name) {
        final AresRank rank = rankService.getRank(name);

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