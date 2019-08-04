package com.playares.factions.addons.mining;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.util.List;

public final class PlayerFindOreEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Getter public final Material material;
    @Getter public final List<Block> blocks;
    @Getter @Setter public boolean cancelled;

    public PlayerFindOreEvent(Player player, Material material, List<Block> blocks) {
        super(player);
        this.material = material;
        this.blocks = blocks;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}