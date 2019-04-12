package com.playares.commons.bukkit.item.custom.event;

import com.playares.commons.bukkit.item.custom.CustomItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fires when a custom item is interacted with
 */
public final class CustomItemInteractEvent extends PlayerEvent implements Cancellable {
    @Getter public static final HandlerList handlerList = new HandlerList();
    @Nonnull @Getter public final CustomItem item;
    @Nonnull @Getter public final Action action;
    @Nullable @Getter public final Block clickedBlock;
    @Getter @Setter public boolean cancelled;

    public CustomItemInteractEvent(Player who, @Nonnull CustomItem item, @Nonnull Action action, @Nullable Block clickedBlock) {
        super(who);
        this.item = item;
        this.action = action;
        this.clickedBlock = clickedBlock;
    }

    public boolean isLeftClick() {
        return action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK);
    }

    public boolean isRightClick() {
        return action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
