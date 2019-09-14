package com.playares.arena.report;

import com.playares.arena.player.ArenaPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public final class PlayerReport {
    @Getter public final ArenaPlayer player;
    @Getter public ItemStack[] contents;
    @Getter public ItemStack[] armor;
    @Getter @Setter public double health;
    @Getter @Setter public int food;
    @Getter public int swordHits;
    @Getter public double damage;
    @Getter public int arrowsFired;
    @Getter public int arrowsHit;

    public PlayerReport(ArenaPlayer player) {
        this.player = player;
        this.contents = player.getPlayer().getInventory().getContents();
        this.armor = player.getPlayer().getInventory().getArmorContents();
        this.health = player.getPlayer().getHealth();
        this.food = player.getPlayer().getFoodLevel();
        this.swordHits = 0;
        this.damage = 0;
        this.arrowsFired = 0;
        this.arrowsHit = 0;
    }

    public void addSwordHit() {
        swordHits += 1;
    }

    public void addArrowFired() {
        arrowsFired += 1;
    }

    public void addArrowHit() {
        arrowsHit += 1;
    }

    public void addDamage(double damage) {
        this.damage += damage;
    }

    public int getRemainingHealthPotions() {
        int total = 0;

        for (ItemStack item : contents) {
            if (item == null || !item.getType().equals(Material.SPLASH_POTION)) {
                continue;
            }

            final PotionMeta meta = (PotionMeta)item.getItemMeta();

            if (meta.getBasePotionData().getType().equals(PotionType.INSTANT_HEAL)) {
                total++;
            }
        }

        return total;
    }

    public double getBowAccuracy() {
        if (arrowsHit == 0 || arrowsFired == 0) {
            return 0.0;
        }

        return (double)((arrowsHit * 100.0F) / arrowsFired);
    }

    public void pullInventory() {
        this.contents = player.getPlayer().getInventory().getContents();
        this.armor = player.getPlayer().getInventory().getArmorContents();
    }
}
