package com.playares.factions.addons.stats;

import com.mongodb.client.model.Filters;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.addons.stats.holder.PlayerStatisticHolder;
import com.playares.factions.players.FactionPlayer;
import com.playares.factions.players.PlayerDAO;
import com.playares.services.profiles.ProfileService;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class StatsHandler {
    @Getter
    public final Factions plugin;

    public StatsHandler(Factions plugin) {
        this.plugin = plugin;
    }

    public void printStats(Player viewer, String username, PlayerStatisticHolder stats) {
        final String spacer = ChatColor.RESET + " " + ChatColor.RESET + " " + ChatColor.YELLOW + " - " + ChatColor.RESET;
        final int kills = stats.getKills();
        final int deaths = stats.getDeaths();
        final int minorEvents = stats.getMinorEventCaptures();
        final int majorEvents = stats.getMajorEventCaptures();
        final int coal = stats.getMinedOres().getOrDefault(Material.COAL_ORE, 0);
        final int iron = stats.getMinedOres().getOrDefault(Material.IRON_ORE, 0);
        final int redstone = stats.getMinedOres().getOrDefault(Material.REDSTONE_ORE, 0);
        final int lapis = stats.getMinedOres().getOrDefault(Material.LAPIS_ORE, 0);
        final int gold = stats.getMinedOres().getOrDefault(Material.GOLD_ORE, 0);
        final int diamond = stats.getMinedOres().getOrDefault(Material.DIAMOND_ORE, 0);
        final int emerald = stats.getMinedOres().getOrDefault(Material.EMERALD_ORE, 0);
        final long playtime = stats.getPlaytime();

        viewer.sendMessage(ChatColor.RESET + " ");
        viewer.sendMessage(ChatColor.BLUE + "Player Statistics: " + ChatColor.RESET + username);
        viewer.sendMessage(ChatColor.GREEN + "K" + ChatColor.RESET + ": " + kills + " " + ChatColor.RED + "D" + ChatColor.RESET + ": " + deaths);
        viewer.sendMessage(ChatColor.GOLD + "Minor Events" + ChatColor.RESET + ": " + minorEvents + " " + ChatColor.GOLD + "Major Events" + ChatColor.RESET + ": " + majorEvents);
        viewer.sendMessage(ChatColor.BLUE + "Playtime" + ChatColor.RESET + ": " + Time.convertToRemaining(playtime));
        viewer.sendMessage(ChatColor.GOLD + "Ores" + ChatColor.RESET + ": ");
        viewer.sendMessage(spacer + ChatColor.GOLD + "Coal" + ChatColor.RESET + coal);
        viewer.sendMessage(spacer + ChatColor.GOLD + "Iron" + ChatColor.RESET + iron);
        viewer.sendMessage(spacer + ChatColor.GOLD + "Redstone" + ChatColor.RESET + redstone);
        viewer.sendMessage(spacer + ChatColor.GOLD + "Lapis" + ChatColor.RESET + lapis);
        viewer.sendMessage(spacer + ChatColor.GOLD + "Gold" + ChatColor.RESET + gold);
        viewer.sendMessage(spacer + ChatColor.GOLD + "Diamond" + ChatColor.RESET + diamond);
        viewer.sendMessage(spacer + ChatColor.GOLD + "Emerald" + ChatColor.RESET + emerald);
        viewer.sendMessage(ChatColor.RESET + " ");
    }

    public void getStats(Player viewer, SimplePromise promise) {
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(viewer.getUniqueId());

        if (profile == null) {
            promise.failure("Failed to obtain your profile");
            return;
        }

        final PlayerStatisticHolder stats = profile.getStats();
        printStats(viewer, viewer.getName(), stats);
        promise.success();
    }

    public void getStats(Player viewer, String name, SimplePromise promise) {
        final ProfileService profileService = (ProfileService)plugin.getService(ProfileService.class);

        if (profileService == null) {
            promise.failure("Failed to obtain Profile Service");
            return;
        }

        profileService.getProfile(name, aresProfile -> {
            if (aresProfile == null) {
                promise.failure("Player not found");
                return;
            }

            new Scheduler(plugin).async(() -> {
                final FactionPlayer profile = (plugin.getPlayerManager().getPlayer(aresProfile.getUniqueId()) != null ?
                        plugin.getPlayerManager().getPlayer(aresProfile.getUniqueId()) :
                        PlayerDAO.getPlayer(plugin.getMongo(), Filters.eq("id", aresProfile.getUniqueId())));

                new Scheduler(plugin).sync(() -> {
                    if (profile == null) {
                        promise.failure("Player not found");
                        return;
                    }

                    final PlayerStatisticHolder stats = profile.getStats();
                    printStats(viewer, aresProfile.getUsername(), stats);
                    promise.success();
                }).run();
            }).run();
        });
    }
}