package com.riotmc.services.essentials.data.invsee;

import com.google.common.collect.Lists;
import com.riotmc.commons.base.util.Time;
import com.riotmc.commons.bukkit.RiotPlugin;
import com.riotmc.commons.bukkit.item.ItemBuilder;
import com.riotmc.commons.bukkit.menu.ClickableItem;
import com.riotmc.commons.bukkit.menu.Menu;
import com.riotmc.commons.bukkit.util.Scheduler;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public final class ViewableInventory extends Menu implements Listener {
    @Getter
    public final Player viewed;

    @Getter
    public BukkitTask updateTask;

    public ViewableInventory(RiotPlugin plugin, Player viewer, Player viewed) {
        super(plugin, viewer, viewed.getName(), 6);
        this.viewed = viewed;
    }

    public void startUpdater() {
        updateTask = new Scheduler(plugin).sync(() -> {
            if (viewed == null || !viewed.isOnline() || viewed.isDead()) {
                getPlayer().closeInventory();
                getPlayer().sendMessage(ChatColor.RED + "Player has died or logged out");
                return;
            }

            final List<String> potionEffects = Lists.newArrayList();

            viewed.getActivePotionEffects().forEach(effect -> potionEffects.add(ChatColor.DARK_AQUA + StringUtils.capitaliseAllWords(
                    effect.getType().getName().toLowerCase().replace("_", " ") + ChatColor.GRAY + ": " +
                            ChatColor.WHITE + Time.convertToHHMMSS((effect.getDuration() / 20) * 1000)
            )));

            final ItemStack health = new ItemBuilder()
                    .setMaterial(Material.GLISTERING_MELON_SLICE)
                    .setName(ChatColor.RED + "Health")
                    .addLore(ChatColor.YELLOW + "" + String.format("%.2f", (viewed.getHealth() / 2)) + ChatColor.GOLD + "/" + ChatColor.YELLOW + "10.0")
                    .build();

            final ItemStack food = new ItemBuilder()
                    .setMaterial(Material.COOKED_BEEF)
                    .setName(ChatColor.GOLD + "Food")
                    .addLore(ChatColor.YELLOW + "" + (viewed.getFoodLevel() / 2) + ChatColor.GOLD + "/" + ChatColor.YELLOW + "10")
                    .build();

            final ItemStack potions = new ItemBuilder()
                    .setMaterial(Material.GLASS_BOTTLE)
                    .setName(ChatColor.AQUA + "Potions")
                    .addLore(potionEffects)
                    .addFlag(ItemFlag.HIDE_POTION_EFFECTS)
                    .build();

            for (int i = 0; i < viewed.getInventory().getSize(); i++) {
                final ItemStack item = viewed.getInventory().getItem(i);

                if (item == null || item.getType().equals(Material.AIR)) {
                    continue;
                }

                addItem(new ClickableItem(item, i, click -> {}));
            }

            for (int i = 45; i < viewed.getInventory().getArmorContents().length; i++) {
                final ItemStack item = viewed.getInventory().getArmorContents()[i];

                if (item == null || item.getType().equals(Material.AIR)) {
                    continue;
                }

                addItem(new ClickableItem(item, i, click -> {}));
            }

            addItem(new ClickableItem(health, 51, click -> {}));
            addItem(new ClickableItem(food, 52, click -> {}));
            addItem(new ClickableItem(potions, 53, click -> {}));
        }).repeat(0L, 20L).run();
    }

    private void stopUpdater() {
        if (updateTask != null) {
            updateTask.cancel();
        }

        InventoryCloseEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getPlayer();

        if (getPlayer().getUniqueId().equals(player.getUniqueId()) || event.getInventory().equals(getInventory())) {
            stopUpdater();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        if (getPlayer().getUniqueId().equals(player.getUniqueId())) {
            stopUpdater();
        }
    }
}
