package com.playares.services.ranks.listener;

import com.playares.commons.bukkit.event.ProcessedChatEvent;
import com.playares.services.ranks.RankService;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class RankListener implements Listener {
    @Getter public final RankService service;

    public RankListener(RankService service) {
        this.service = service;
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void onProcessedChat(ProcessedChatEvent event) {
        event.setDisplayName(service.formatName(event.getPlayer()));
    }
}
