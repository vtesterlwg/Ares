package com.playares.factions.addons.stats;

import com.mongodb.client.model.Filters;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.addons.stats.holder.PlayerStatisticHolder;
import com.playares.factions.players.FactionPlayer;
import com.playares.factions.players.PlayerDAO;
import com.riotmc.services.profiles.ProfileService;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class StatsHandler {
    @Getter
    public final Factions plugin;

    @Getter
    public final StatsAddon addon;

    public StatsHandler(Factions plugin, StatsAddon addon) {
        this.plugin = plugin;
        this.addon = addon;
    }

    public void printStats(Player viewer, String username, PlayerStatisticHolder stats) {
        final String spacer = ChatColor.RESET + " " + ChatColor.RESET + " " + ChatColor.YELLOW + " - " + ChatColor.RESET;
        final int elo = stats.calculateELO(addon);
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

        viewer.sendMessage(ChatColor.DARK_PURPLE + "--------------------" + ChatColor.GOLD + "[ " + ChatColor.WHITE + username + ChatColor.GOLD + " ]" + ChatColor.DARK_PURPLE + "--------------------");
        viewer.sendMessage(ChatColor.GOLD + "Main" + ChatColor.YELLOW + ": ");
        viewer.sendMessage(spacer + ChatColor.GOLD + "Rating" + ChatColor.YELLOW + ": " + ChatColor.WHITE + elo);
        viewer.sendMessage(spacer + ChatColor.GOLD + "Kills" + ChatColor.YELLOW + ": " + ChatColor.WHITE + kills);
        viewer.sendMessage(spacer + ChatColor.GOLD + "Deaths" + ChatColor.YELLOW + ": " + ChatColor.WHITE + deaths);
        viewer.sendMessage(spacer + ChatColor.GOLD + "Playtime" + ChatColor.YELLOW + ": " + ChatColor.BLUE + Time.convertToRemaining(playtime));
        viewer.sendMessage(ChatColor.RESET + " ");
        viewer.sendMessage(ChatColor.GOLD + "Events" + ChatColor.YELLOW + ": ");
        viewer.sendMessage(spacer + ChatColor.GOLD + "Minor Events" + ChatColor.YELLOW + ": " + ChatColor.WHITE + minorEvents);
        viewer.sendMessage(spacer + ChatColor.GOLD + "Major Events" + ChatColor.YELLOW + ": " + ChatColor.WHITE + majorEvents);
        viewer.sendMessage(ChatColor.RESET + " ");
        viewer.sendMessage(ChatColor.GOLD + "Ores" + ChatColor.YELLOW + ":");
        viewer.sendMessage(spacer + ChatColor.DARK_GRAY + "Coal" + ChatColor.YELLOW + ": " + ChatColor.WHITE + coal);
        viewer.sendMessage(spacer + ChatColor.GRAY + "Iron" + ChatColor.YELLOW + ": " + ChatColor.WHITE + iron);
        viewer.sendMessage(spacer + ChatColor.RED + "Redstone" + ChatColor.YELLOW + ": " + ChatColor.WHITE + redstone);
        viewer.sendMessage(spacer + ChatColor.BLUE + "Lapis" + ChatColor.YELLOW + ": " + ChatColor.WHITE + lapis);
        viewer.sendMessage(spacer + ChatColor.GOLD + "Gold" + ChatColor.YELLOW + ": " + ChatColor.WHITE + gold);
        viewer.sendMessage(spacer + ChatColor.AQUA + "Diamond" + ChatColor.YELLOW + ": " + ChatColor.WHITE + diamond);
        viewer.sendMessage(spacer + ChatColor.GREEN + "Emerald" + ChatColor.YELLOW + ": " + ChatColor.WHITE + emerald);
        viewer.sendMessage(ChatColor.DARK_PURPLE + "------------------------------------------------");
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
                        PlayerDAO.getPlayer(plugin, plugin.getMongo(), Filters.eq("id", aresProfile.getUniqueId())));

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