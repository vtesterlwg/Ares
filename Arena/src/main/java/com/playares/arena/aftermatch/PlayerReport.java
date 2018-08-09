package com.playares.arena.aftermatch;

import com.playares.arena.player.ArenaPlayer;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.UUID;

public final class PlayerReport implements AftermatchReport {
    @Getter
    public final UUID uniqueId;

    @Getter
    public final UUID matchId;

    @Getter
    public final String username;

    @Getter
    public final int hits;

    @Getter
    public final double damage;

    @Getter
    public final double health;

    @Getter
    public final double longestShot;

    @Getter
    public final int arrowsHit;

    @Getter
    public final int arrowsFired;

    @Getter
    public final double accuracy;

    @Getter
    public final ItemStack[] contents;

    @Getter
    public final ItemStack[] armor;

    public PlayerReport(ArenaPlayer player, UUID matchId, double health) {
        this.uniqueId = player.getUniqueId();
        this.matchId = matchId;
        this.username = player.getUsername();
        this.hits = player.getHits();
        this.damage = player.getDamage();
        this.health = health;
        this.longestShot = player.getLongestShot();
        this.arrowsHit = player.getArrowHits();
        this.arrowsFired = player.getTotalArrowsFired();
        this.accuracy = player.getAccuracy();
        this.contents = player.getPlayer().getInventory().getContents();
        this.armor = player.getPlayer().getInventory().getArmorContents();
    }

    public int getRemainingHealthPotions() {
        int count = 0;

        for (ItemStack item : contents) {
            if (item == null || !item.getType().equals(Material.SPLASH_POTION)) {
                continue;
            }

            final PotionMeta meta = (PotionMeta)item.getItemMeta();

            if (meta.getBasePotionData().getType().equals(PotionType.INSTANT_HEAL)) {
                count++;
            }
        }

        return count;
    }
}