package com.playares.factions.addons.stats.handler;

import com.google.common.collect.Lists;
import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.factions.addons.stats.StatsAddon;
import com.playares.factions.factions.data.PlayerFaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@AllArgsConstructor
public final class FactionStatsHandler {
    @Getter public final StatsAddon addon;

    public void view(Player viewer, PlayerFaction faction, FailablePromise<Menu> promise) {
        final Menu menu = new Menu(getAddon().getPlugin(), viewer, "Faction Statistics: " + faction.getName(), 1);

        final List<String> oresLore = Lists.newArrayList();
        final List<String> combatLore = Lists.newArrayList();
        final List<String> eventLore = Lists.newArrayList();
        final List<String> dragonLore = Lists.newArrayList();
        final List<String> factionLore = Lists.newArrayList();

        oresLore.add(ChatColor.DARK_GRAY + "Coal" + ChatColor.RESET + ": " + faction.getStatistics().getMinedCoal());
        oresLore.add(ChatColor.GRAY + "Iron" + ChatColor.RESET + ": " + faction.getStatistics().getMinedIron());
        oresLore.add(ChatColor.RED + "Redstone" + ChatColor.RESET + ": " + faction.getStatistics().getMinedRedstone());
        oresLore.add(ChatColor.BLUE + "Lapis" + ChatColor.RESET + ": " + faction.getStatistics().getMinedLapis());
        oresLore.add(ChatColor.GOLD + "Gold" + ChatColor.RESET + ": " + faction.getStatistics().getMinedGold());
        oresLore.add(ChatColor.AQUA + "Diamond" + ChatColor.RESET + ": " + faction.getStatistics().getMinedDiamond());
        oresLore.add(ChatColor.GREEN + "Emerald" + ChatColor.RESET + ": " + faction.getStatistics().getMinedEmerald());

        final double killDeathRatio = (faction.getStatistics().getDeaths() > 0) ? (double)faction.getStatistics().getKills() / (double)faction.getStatistics().getDeaths() : faction.getStatistics().getKills();
        combatLore.add(ChatColor.DARK_RED + "Kills" + ChatColor.RESET + ": " + faction.getStatistics().getKills());
        combatLore.add(ChatColor.DARK_RED + "Deaths" + ChatColor.RESET + ": " + faction.getStatistics().getDeaths());
        combatLore.add(ChatColor.AQUA + "Kill/Death Ratio" + ChatColor.RESET + ": " + String.format("%.2f", killDeathRatio));

        eventLore.add(ChatColor.GOLD + "King of the Hill Captures" + ChatColor.RESET + ": " + faction.getStatistics().getKothCaptures());
        eventLore.add(ChatColor.DARK_GREEN + "Palace Captures" + ChatColor.RESET + ": " + faction.getStatistics().getPalaceCaptures());

        dragonLore.add(ChatColor.DARK_PURPLE + "Slain Dragons" + ChatColor.RESET + ": " + faction.getStatistics().getSlainDragons());

        factionLore.add(ChatColor.GOLD + "DTR" + ChatColor.RESET + ": " + String.format("%.2f", faction.getDeathsTilRaidable()) + "/" + faction.getMaxDTR());
        factionLore.add(ChatColor.GOLD + "Members" + ChatColor.RESET + ": " + faction.getOnlineMembers().size() + "/" + faction.getMembers().size());
        factionLore.add(ChatColor.GOLD + "Balance" + ChatColor.RESET + ": $" + String.format("%.2f", faction.getBalance()));
        factionLore.add(ChatColor.RESET + " ");
        factionLore.add(ChatColor.GREEN + "Click for more info!");

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
                .setName(ChatColor.LIGHT_PURPLE + "Slain Bosses")
                .setData((short)5)
                .addLore(dragonLore)
                .build();

        final ItemStack factionIcon = new ItemBuilder()
                .setMaterial(Material.SKULL_ITEM)
                .setName(ChatColor.GREEN + "Faction Info")
                .setData((short)3)
                .addLore(factionLore)
                .build();

        menu.addItem(new ClickableItem(oresIcon, 2, null));
        menu.addItem(new ClickableItem(combatIcon, 3, null));
        menu.addItem(new ClickableItem(eventIcon, 4, null));
        menu.addItem(new ClickableItem(dragonIcon, 5, null));
        menu.addItem(new ClickableItem(factionIcon, 6, click -> {
            viewer.closeInventory();
            getAddon().getPlugin().getFactionManager().getDisplayHandler().displayFactionInfo(viewer, faction);
        }));

        promise.success(menu);
    }

    public void view(Player viewer, FailablePromise<Menu> promise) {
        final PlayerFaction faction = getAddon().getPlugin().getFactionManager().getFactionByPlayer(viewer.getUniqueId());

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        view(viewer, faction, promise);
    }

    public void view(Player viewer, String factionName, FailablePromise<Menu> promise) {
        final PlayerFaction faction = getAddon().getPlugin().getFactionManager().getPlayerFactionByName(factionName);

        if (faction == null) {
            promise.failure("Faction not found");
            return;
        }

        view(viewer, faction, promise);
    }
}
