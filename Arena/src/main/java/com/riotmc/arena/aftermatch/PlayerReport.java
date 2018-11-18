package com.riotmc.arena.aftermatch;

import com.riotmc.arena.player.ArenaPlayer;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public final class PlayerReport implements AftermatchReport {
    @Nonnull @Getter
    public final UUID uniqueId;

    @Nonnull @Getter
    public final String username;

    @Nonnull @Getter
    public final UUID matchId;

    @Nullable @Getter
    public final UUID teamId;

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

    @Nullable @Getter
    public final ItemStack[] contents;

    @Nullable @Getter
    public final ItemStack[] armor;

    public PlayerReport(@Nonnull ArenaPlayer player, @Nonnull UUID matchId, @Nullable UUID teamId, double health) {
        this.uniqueId = player.getUniqueId();
        this.username = player.getUsername();
        this.matchId = matchId;
        this.teamId = teamId;
        this.hits = player.getHits();
        this.damage = player.getDamage();
        this.health = health;
        this.longestShot = player.getLongestShot();
        this.arrowsHit = player.getArrowHits();
        this.arrowsFired = player.getTotalArrowsFired();
        this.accuracy = player.getAccuracy();
        this.contents = (player.getPlayer() != null ? player.getPlayer().getInventory().getContents() : null);
        this.armor = (player.getPlayer() != null ? player.getPlayer().getInventory().getArmorContents() : null);
    }

    public int getRemainingHealthPotions() {
        if (contents == null) {
            return 0;
        }

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