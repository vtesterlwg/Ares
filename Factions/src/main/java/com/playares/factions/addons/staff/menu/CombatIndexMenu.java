package com.playares.factions.addons.staff.menu;

import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.players.data.FactionPlayer;
import com.playares.factions.timers.PlayerTimer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class CombatIndexMenu extends Menu {
    @Getter public final Factions plugin;
    private BukkitTask updater;

    public CombatIndexMenu(@Nonnull Factions plugin, @Nonnull Player player) {
        super(plugin, player, "Combat Tagged Players", 6);
        this.plugin = plugin;
    }

    @Override
    public void open() {
        super.open();
        updater = new Scheduler(plugin).sync(this::update).repeat(0L, 20L).run();
    }

    private void update() {
        clearInventory();

        final List<FactionPlayer> taggedPlayers = plugin.getPlayerManager().getPlayerRepository().stream().filter(player -> player.hasTimer(PlayerTimer.PlayerTimerType.COMBAT)).collect(Collectors.toList());
        taggedPlayers.sort(Comparator.comparing(FactionPlayer::getUsername));

        int pos = 0;

        for (FactionPlayer tagged : taggedPlayers) {
            if (pos >= 53) {
                break;
            }

            final ItemStack icon = new ItemBuilder()
                    .setMaterial(Material.SKULL_ITEM)
                    .setData((short)3)
                    .setName(ChatColor.RED + tagged.getUsername())
                    .addLore(ChatColor.GRAY + Time.convertToRemaining(tagged.getTimer(PlayerTimer.PlayerTimerType.COMBAT).getRemaining()))
                    .build();

            addItem(new ClickableItem(icon, pos, click -> {
                if (tagged.getPlayer() == null) {
                    player.sendMessage(ChatColor.RED + "Player not found");
                    return;
                }

                player.teleport(tagged.getPlayer());
            }));

            pos++;
        }
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        super.onInventoryClose(event);

        if (updater != null && !updater.isCancelled()) {
            updater.cancel();
            updater = null;
        }
    }
}
