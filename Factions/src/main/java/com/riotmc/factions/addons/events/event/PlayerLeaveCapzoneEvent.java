package com.riotmc.factions.addons.events.event;

import com.riotmc.factions.addons.events.data.CaptureRegion;
import com.riotmc.factions.addons.events.type.RiotEvent;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public final class PlayerLeaveCapzoneEvent extends PlayerEvent {
    @Getter
    public static final HandlerList handlerList = new HandlerList();

    @Getter
    public final RiotEvent event;

    @Getter
    public final CaptureRegion capRegion;

    public PlayerLeaveCapzoneEvent(Player player, RiotEvent event, CaptureRegion region) {
        super(player);
        this.event = event;
        this.capRegion = region;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
