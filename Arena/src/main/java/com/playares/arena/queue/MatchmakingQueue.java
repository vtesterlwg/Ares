package com.playares.arena.queue;

import com.playares.arena.kit.Kit;
import com.playares.commons.bukkit.item.ItemBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@AllArgsConstructor
public final class MatchmakingQueue {
    @Getter public final QueueType queueType;
    @Getter public final List<Kit> allowedKits;

    public ItemStack getIcon() {
        return new ItemBuilder()
                .setMaterial(getQueueType().getIcon())
                .setName(ChatColor.GOLD + getQueueType().getDisplayName())
                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                .build();
    }

    @AllArgsConstructor
    public enum QueueType {
        NO_DEBUFF("No Debuff", 16, Material.DIAMOND_SWORD, 1),
        DEBUFF("Debuff", 16, Material.DIAMOND_SWORD, 3),
        ARCHER("Archer", 16, Material.BOW, 5),
        HCF("HCF", 16, Material.GOLD_HELMET, 7);

        @Getter public final String displayName;
        @Getter public final int enderpearlCooldown;
        @Getter public final Material icon;
        @Getter public final int iconPosition;
    }
}