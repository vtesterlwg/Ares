package com.playares.factions.addons.stats.handler;

import com.google.common.collect.Lists;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.addons.stats.StatsAddon;
import com.playares.factions.players.dao.PlayerDAO;
import com.playares.factions.players.data.FactionPlayer;
import com.playares.services.profiles.ProfileService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@AllArgsConstructor
public final class PlayerStatsHandler {
    @Getter public final StatsAddon addon;

    public void view(Player viewer, String username, FactionPlayer profile, FailablePromise<Menu> promise) {
        final Menu menu = new Menu(getAddon().getPlugin(), viewer, "Player Statistics: " + username, 1);

                    /*
                    ORES - PICKAXE
                    KILLS/DEATHS - SWORD
                    CLASSES - GOLD HELMET
                    EVENTS - DIAMOND HELMET
                    DRAGON HEAD - DRAGONS SLAIN
                    WATCH - TIME PLAYED
                     */

        final List<String> oresLore = Lists.newArrayList();
        final List<String> combatLore = Lists.newArrayList();
        final List<String> eventLore = Lists.newArrayList();
        final List<String> dragonLore = Lists.newArrayList();
        final List<String> timePlayedLore = Lists.newArrayList();

        oresLore.add(ChatColor.DARK_GRAY + "Coal" + ChatColor.RESET + ": " + profile.getStatistics().getMinedCoal());
        oresLore.add(ChatColor.GRAY + "Iron" + ChatColor.RESET + ": " + profile.getStatistics().getMinedIron());
        oresLore.add(ChatColor.RED + "Redstone" + ChatColor.RESET + ": " + profile.getStatistics().getMinedRedstone());
        oresLore.add(ChatColor.BLUE + "Lapis" + ChatColor.RESET + ": " + profile.getStatistics().getMinedLapis());
        oresLore.add(ChatColor.GOLD + "Gold" + ChatColor.RESET + ": " + profile.getStatistics().getMinedGold());
        oresLore.add(ChatColor.AQUA + "Diamond" + ChatColor.RESET + ": " + profile.getStatistics().getMinedDiamond());
        oresLore.add(ChatColor.GREEN + "Emerald" + ChatColor.RESET + ": " + profile.getStatistics().getMinedDiamond());

        final double killDeathRatio = (profile.getStatistics().getDeaths() > 0) ? (double)profile.getStatistics().getKills() / (double)profile.getStatistics().getDeaths() : profile.getStatistics().getKills();
        combatLore.add(ChatColor.DARK_RED + "Kills" + ChatColor.RESET + ": " + profile.getStatistics().getKills());
        combatLore.add(ChatColor.DARK_RED + "Deaths" + ChatColor.RESET + ": " + profile.getStatistics().getDeaths());
        combatLore.add(ChatColor.AQUA + "Kill/Death Ratio" + ChatColor.RESET + ": " + String.format("%.2f", killDeathRatio));
        combatLore.add(ChatColor.RESET + " ");
        combatLore.add(ChatColor.GREEN + "Classes" + ChatColor.RESET + ":");
        combatLore.add(ChatColor.BLUE + "Longest Archer Shot" + ChatColor.RESET + ": " + String.format("%.2f", profile.getStatistics().getArcherLongestShot()) + " blocks");
        combatLore.add(ChatColor.GOLD + "Total Bard Effects Given" + ChatColor.RESET + ": " + profile.getStatistics().getBardTotalAffected());
        combatLore.add(ChatColor.DARK_PURPLE + "Rogue Backstabs" + ChatColor.RESET + ": " + profile.getStatistics().getRogueBackstabs());

        eventLore.add(ChatColor.GOLD + "King of the Hill Captures" + ChatColor.RESET + ": " + profile.getStatistics().getKothCaptures());
        eventLore.add(ChatColor.DARK_GREEN + "Palace Captures" + ChatColor.RESET + ": " + profile.getStatistics().getPalaceCaptures());

        dragonLore.add(ChatColor.DARK_PURPLE + "Slain Dragons" + ChatColor.RESET + ": " + profile.getStatistics().getSlainDragons());

        timePlayedLore.add(ChatColor.WHITE + Time.convertToRemaining(profile.getStatistics().getTimePlayed()));

        final ItemStack oresIcon = new ItemBuilder()
                .setMaterial(Material.DIAMOND_PICKAXE)
                .setName(ChatColor.AQUA + "Mining Statistics")
                .addLore(oresLore)
                .build();

        final ItemStack combatIcon = new ItemBuilder()
                .setMaterial(Material.DIAMOND_SWORD)
                .setName(ChatColor.RED + "Combat Statistics")
                .addLore(combatLore)
                .build();

        final ItemStack eventIcon = new ItemBuilder()
                .setMaterial(Material.DIAMOND_HELMET)
                .setName(ChatColor.GOLD + "Captured Events")
                .addLore(eventLore)
                .build();

        final ItemStack dragonIcon = new ItemBuilder()
                .setMaterial(Material.SKULL_ITEM)
                .setData((short)5)
                .addLore(dragonLore)
                .build();

        final ItemStack timePlayedIcon = new ItemBuilder()
                .setMaterial(Material.WATCH)
                .setName(ChatColor.DARK_GREEN + "Time Played")
                .addLore(timePlayedLore)
                .build();

        menu.addItem(new ClickableItem(oresIcon, 2, null));
        menu.addItem(new ClickableItem(combatIcon, 3, null));
        menu.addItem(new ClickableItem(eventIcon, 4, null));
        menu.addItem(new ClickableItem(dragonIcon, 5, null));
        menu.addItem(new ClickableItem(timePlayedIcon, 6, null));

        promise.success(menu);
    }

    public void view(Player viewer, FailablePromise<Menu> promise) {
        final FactionPlayer profile = getAddon().getPlugin().getPlayerManager().getPlayer(viewer.getUniqueId());

        if (profile == null) {
            promise.failure("Failed to obtain your profile");
            return;
        }

        view(viewer, viewer.getName(), profile, promise);
    }

    public void view(Player viewer, String username, FailablePromise<Menu> promise) {
        final ProfileService profileService = (ProfileService)getAddon().getPlugin().getService(ProfileService.class);

        if (profileService == null) {
            promise.failure("Failed to obtain profile service");
            return;
        }

        profileService.getProfile(username, aresProfile -> {
            if (aresProfile == null) {
                promise.failure("Player not found");
                return;
            }

            new Scheduler(getAddon().getPlugin()).async(() -> {
                final FactionPlayer profile = PlayerDAO.getPlayer(getAddon().getPlugin(), getAddon().getPlugin().getMongo(), Filters.eq("id", aresProfile.getUniqueId()));

                new Scheduler(getAddon().getPlugin()).sync(() -> {
                    if (profile == null) {
                        promise.failure("Player not found");
                        return;
                    }

                    view(viewer, aresProfile.getUsername(), profile, promise);
                }).run();
            }).run();
        });
    }
}
