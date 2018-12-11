package com.riotmc.factions.addons.events.builder;

import com.riotmc.commons.bukkit.event.ProcessedChatEvent;
import com.riotmc.factions.addons.events.EventsAddon;
import com.riotmc.factions.addons.events.builder.type.EventBuilder;
import com.riotmc.factions.addons.events.builder.type.KOTHEventBuilder;
import com.riotmc.services.customitems.CustomItemService;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

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
    public void onPlayerChat(ProcessedChatEvent event) {
        final Player player = event.getPlayer();
        final EventBuilder builder = addon.getBuilderManager().getBuilder(player);
        final String message = event.getMessage();

        if (builder instanceof KOTHEventBuilder) {
            final KOTHEventBuilder kothBuilder = (KOTHEventBuilder)builder;

            if (kothBuilder.getCurrentStep().equals(KOTHEventBuilder.KOTHBuilderStep.OWNER)) {
                addon.getBuilderManager().getHandler().setOwner(builder, player, message);
                event.setCancelled(true);
            } else if (kothBuilder.getCurrentStep().equals(KOTHEventBuilder.KOTHBuilderStep.NAME)) {
                addon.getBuilderManager().getHandler().setName(builder, player, message);
                event.setCancelled(true);
            } else if (kothBuilder.getCurrentStep().equals(KOTHEventBuilder.KOTHBuilderStep.DISPLAY_NAME)) {
                addon.getBuilderManager().getHandler().setDisplayName(builder, player, message);
                event.setCancelled(true);
            }
        }

        // Add other builder types here
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final ItemStack hand = player.getInventory().getItemInMainHand();
        final Action action = event.getAction();

        if (hand == null) {
            return;
        }

        if (!action.equals(Action.LEFT_CLICK_BLOCK)) {
            return;
        }

        if (!player.hasPermission("factions.events.builder")) {
            return;
        }

        final EventBuilder builder = addon.getBuilderManager().getBuilder(player);


        if (builder == null) {
            return;
        }

        final CustomItemService itemService = (CustomItemService)addon.getPlugin().getService(CustomItemService.class);

        if (itemService == null) {
            return;
        }

        itemService.getItem(hand).ifPresent(customItem -> {
            if (customItem instanceof EventBuilderWand) {
                if (builder instanceof KOTHEventBuilder) {
                    final KOTHEventBuilder kothBuilder = (KOTHEventBuilder)builder;
                    addon.getBuilderManager().getHandler().setKOTHLocation(kothBuilder, player, block);
                    event.setCancelled(true);
                }
            }
        });
    }
}