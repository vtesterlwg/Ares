package com.playares.minez.bukkitz.listener;

import com.playares.minez.bukkitz.MineZ;
import com.playares.minez.bukkitz.data.MZPlayer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public final class ThirstListener implements Listener {
    @Getter public final MineZ plugin;

    public ThirstListener(MineZ plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDrinkPotion(PlayerItemConsumeEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();

        if (!item.getType().equals(Material.POTION)) {
            return;
        }

        final PotionMeta meta = (PotionMeta)item.getItemMeta();

        if (!meta.getBasePotionData().getType().equals(PotionType.WATER)) {
            return;
        }

        final MZPlayer mzPlayer = plugin.getPlayerManager().getLocalPlayer(player.getUniqueId());

        if (mzPlayer != null) {
            mzPlayer.setThirst(20.0);
            player.sendMessage(ChatColor.AQUA + "Ah, much better!");
        }
    }
}
