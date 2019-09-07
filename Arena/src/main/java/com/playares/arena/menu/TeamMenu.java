package com.playares.arena.menu;

import com.google.common.collect.Lists;
import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.team.Team;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.util.List;

public final class TeamMenu extends Menu {
    @Getter public final Arenas plugin;
    @Getter public BukkitTask updater;

    public TeamMenu(@Nonnull Arenas plugin, @Nonnull Player player) {
        super(plugin, player, "Other Teams", 6);
        this.plugin = plugin;
        this.updater = new Scheduler(plugin).sync(this::update).repeat(0L, 3 * 20L).run();
    }

    private void update() {
        if (!isOpen()) {
            return;
        }

        final List<ClickableItem> elements = Lists.newArrayList();

        new Scheduler(plugin).async(() -> {
            for (Team team : plugin.getTeamManager().getAvailableTeams()) {
                final List<String> lore = Lists.newArrayList();
                final OfflinePlayer offlineLeader = Bukkit.getOfflinePlayer(team.getLeader().getUniqueId());

                for (ArenaPlayer member : team.getMembers()) {
                    lore.add(ChatColor.GOLD + member.getUsername());
                }

                new Scheduler(plugin).sync(() -> {
                    final ItemStack icon = new ItemBuilder()
                            .setMaterial(Material.SKULL_ITEM)
                            .setData((short)3)
                            .setName(ChatColor.GOLD + team.getLeader().getUsername() + ChatColor.DARK_AQUA + " (" + ChatColor.YELLOW + team.getMembers().size() + ChatColor.DARK_AQUA + ")")
                            .addLore(lore)
                            .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                            .build();

                    if (offlineLeader != null) {
                        final SkullMeta skullMeta = (SkullMeta)icon.getItemMeta();
                        skullMeta.setOwningPlayer(offlineLeader);
                        icon.setItemMeta(skullMeta);
                    }

                    elements.add(new ClickableItem(icon, elements.size(), click -> {
                        player.sendMessage("Duel: " + team.getLeader().getUsername() + "'s Team");
                    }));
                }).run();
            }

            new Scheduler(getPlugin()).sync(() -> {
                clearInventory();
                elements.forEach(this::addItem);
            }).run();
        }).run();
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        super.onInventoryClose(event);

        if (updater != null) {
            updater.cancel();
            updater = null;
        }
    }
}