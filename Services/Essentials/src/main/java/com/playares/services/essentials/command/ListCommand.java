package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.services.essentials.EssentialsService;
import com.playares.services.ranks.RankService;
import com.playares.services.ranks.data.Rank;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public final class ListCommand extends BaseCommand {
    @Getter public final AresPlugin plugin;
    @Getter public final EssentialsService service;

    public ListCommand(EssentialsService service) {
        this.plugin = service.getOwner();
        this.service = service;
    }

    @CommandAlias("list")
    @Description("View a list of all online players")
    public void onList(CommandSender sender) {
        final RankService rankService = (RankService)plugin.getService(RankService.class);
        final boolean canSeeVanished = sender.hasPermission("essentials.vanish");

        // Fallback if the service is not found or no ranks to display
        if (rankService == null || (rankService.getRanks().isEmpty() && rankService.getDefaultRanks().isEmpty())) {
            final List<String> usernames = Lists.newArrayList();

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!canSeeVanished && service.getVanishManager().isVanished(player)) {
                    continue;
                }

                usernames.add(player.getName());
            }

            sender.sendMessage(ChatColor.WHITE + "" + usernames.size() + ChatColor.YELLOW + " players are on this server");
            sender.sendMessage(Joiner.on(ChatColor.RESET + ", ").join(usernames));

            return;
        }

        final List<Rank> ranks = rankService.getRanksByAlphabetical();
        final List<String> rankNames = Lists.newArrayList();
        final List<String> response = Lists.newArrayList();
        final Map<Rank, List<String>> players = Maps.newHashMap();
        int onlineCount = 0;

        ranks.forEach(rank -> {
            rankNames.add(rank.getDisplayName());
            players.put(rank, Lists.newArrayList());
        });

        for (Player player : Bukkit.getOnlinePlayers()) {
            final Rank rank = rankService.getHighestRank(player);

            if (!players.containsKey(rank)) {
                continue;
            }

            if (!canSeeVanished && service.getVanishManager().isVanished(player)) {
                continue;
            }

            onlineCount += 1;

            if (rank == null || rank.getPrefix() == null) {
                players.get(rank).add(ChatColor.RESET + player.getName());
            } else {
                players.get(rank).add(rank.getPrefix() + player.getName());
            }
        }

        for (Rank rank : ranks) {
            final List<String> names = players.get(rank);

            if (names.isEmpty()) {
                continue;
            }

            response.add(Joiner.on(ChatColor.RESET + ", ").join(names));
        }

        sender.sendMessage(ChatColor.YELLOW + "There are " + ChatColor.WHITE + onlineCount + ChatColor.YELLOW + " players online");
        sender.sendMessage(Joiner.on(ChatColor.RESET + ", ").join(rankNames));
        sender.sendMessage(Joiner.on(ChatColor.RESET + ", ").join(response));
    }

    @HelpCommand
    @Description("View a list of List Commands")
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }
}