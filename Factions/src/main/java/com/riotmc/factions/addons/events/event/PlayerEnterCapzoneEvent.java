package com.riotmc.factions.addons.events.event;

import com.riotmc.factions.addons.events.data.CaptureRegion;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class PlayerEnterCapzoneEvent extends PlayerEvent {
    @Getter
    public static final HandlerList handlerList = new HandlerList();

    @Getter
    public final CaptureRegion capRegion;

    public PlayerEnterCapzoneEvent(Player player, CaptureRegion region) {
        super(player);
        this.capRegion = region;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
