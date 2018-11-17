package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.bukkit.RiotPlugin;
import com.playares.services.ranks.RankService;
import com.playares.services.ranks.data.RiotRank;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public final class ListCommand extends BaseCommand {
    @Getter
    public final RiotPlugin plugin;

    public ListCommand(RiotPlugin plugin) {
        this.plugin = plugin;
    }

    @CommandAlias("list")
    @Description("View a list of all online players")
    public void onList(CommandSender sender) {
        final RankService rankService = (RankService)plugin.getService(RankService.class);

        // Fallback if the service is not found or no ranks to display
        if (rankService == null || rankService.getRanks().isEmpty() || rankService.getDefaultRanks().isEmpty()) {
            final List<String> usernames = Lists.newArrayList();

            Bukkit.getOnlinePlayers().forEach(player -> usernames.add(player.getName()));

            sender.sendMessage(ChatColor.RED + "" + usernames.size() + ChatColor.GOLD + " players online");
            sender.sendMessage(Joiner.on(ChatColor.RESET + ", ").join(usernames));

            return;
        }

        final List<RiotRank> ranks = rankService.getSortedRanks();
        final List<String> rankNames = Lists.newArrayList();
        final List<String> response = Lists.newArrayList();
        final Map<RiotRank, List<String>> players = Maps.newHashMap();

        ranks.forEach(rank -> {
            rankNames.add(rank.getDisplayName());
            players.put(rank, Lists.newArrayList());
        });

        for (Player player : Bukkit.getOnlinePlayers()) {
            final RiotRank rank = rankService.getHighestRank(player);

            if (!players.containsKey(rank)) {
                continue;
            }

            players.get(rank).add(player.getName());
        }

        for (RiotRank rank : ranks) {
            final List<String> names = players.get(rank);
            response.add(Joiner.on(ChatColor.RESET + ", ").join(names));
        }

        sender.sendMessage(ChatColor.RED + "" + Bukkit.getOnlinePlayers().size() + ChatColor.GOLD + " players online");
        sender.sendMessage(Joiner.on(ChatColor.RESET + ", ").join(rankNames));
        sender.sendMessage(Joiner.on(ChatColor.RESET + ", ").join(response));
    }
}