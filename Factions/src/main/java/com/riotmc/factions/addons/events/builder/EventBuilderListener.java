package com.riotmc.factions.addons.events.builder;

import com.riotmc.factions.addons.events.EventsAddon;
import com.riotmc.factions.addons.events.builder.type.EventBuilder;
import com.riotmc.factions.addons.events.builder.type.KOTHEventBuilder;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class EventBuilderListener implements Listener {
    @Getter public final EventsAddon addon;

    public EventBuilderListener(EventsAddon addon) {
        this.addon = addon;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final EventBuilder existing = addon.getBuilderManager().getBuilder(player);

        if (existing == null) {
            return;
        }

        addon.getBuilderManager().getBuilders().remove(existing);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final EventBuilder builder = addon.getBuilderManager().getBuilder(player);
        final String message = event.getMessage();

        if (builder instanceof KOTHEventBuilder) {
            final KOTHEventBuilder kothBuilder = (KOTHEventBuilder)builder;

            if (kothBuilder.getCurrentStep().equals(KOTHEventBuilder.KOTHBuilderStep.OWNER)) {

            } else if (kothBuilder.getCurrentStep().equals(KOTHEventBuilder.KOTHBuilderStep.NAME)) {

            } else if (kothBuilder.getCurrentStep().equals(KOTHEventBuilder.KOTHBuilderStep.DISPLAY_NAME)) {

            }
        }
    }
}