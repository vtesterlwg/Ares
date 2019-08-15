package com.playares.factions.addons.events.builder;

import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.factions.addons.events.builder.type.EventBuilder;
import com.playares.factions.addons.events.builder.type.KOTHEventBuilder;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public final class EventBuilderHandler {
    @Getter public final EventBuilderManager manager;

    EventBuilderHandler(EventBuilderManager manager) {
        this.manager = manager;
    }

    void setOwner(EventBuilder builder, Player player, String name) {
        builder.setOwningFaction(name, new FailablePromise<String>() {
            @Override
            public void success(@Nonnull String s) {
                player.sendMessage(ChatColor.GREEN + s);
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    void setName(EventBuilder builder, Player player, String name) {
        builder.setName(name, new FailablePromise<String>() {
            @Override
            public void success(@Nonnull String s) {
                player.sendMessage(ChatColor.GREEN + s);
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    void setDisplayName(EventBuilder builder, Player player, String name) {
        builder.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        player.sendMessage(ChatColor.GREEN + "Left-click a corner of the event capture region while holding the Event Builder Wand");
    }

    void setKOTHLocation(KOTHEventBuilder builder, Player player, Block block) {
        final KOTHEventBuilder.KOTHBuilderStep currentStep = builder.getCurrentStep();

        if (currentStep.equals(KOTHEventBuilder.KOTHBuilderStep.CORNER_A)) {
            builder.setCornerA(new BLocatable(block));
            player.sendMessage(ChatColor.GREEN + "Left-click the opposite corner of the event region while holding the Event Builder Wand");
            return;
        }

        if (currentStep.equals(KOTHEventBuilder.KOTHBuilderStep.CORNER_B)) {
            builder.setCornerB(new BLocatable(block));
            player.sendMessage(ChatColor.GREEN + "Left-click the loot chest for this event while holding the Event Builder Wand");
            return;
        }

        if (currentStep.equals(KOTHEventBuilder.KOTHBuilderStep.LOOT_CHEST)) {
            builder.setLootChest(new BLocatable(block), new FailablePromise<String>() {
                @Override
                public void success(@Nonnull String s) {
                    player.sendMessage(ChatColor.GREEN + s);

                    builder.build(new FailablePromise<KOTHEvent>() {
                        @Override
                        public void success(@Nonnull KOTHEvent kothEvent) {
                            manager.getBuilders().remove(builder);
                            manager.getAddon().getManager().getEventRepository().add(kothEvent);
                            player.sendMessage(ChatColor.GREEN + kothEvent.getName() + " is now ready to be scheduled in the events.yml file");
                        }

                        @Override
                        public void failure(@Nonnull String reason) {
                            player.sendMessage(ChatColor.RED + "Failed to create event " + builder.getName() + ", reason: " + reason);
                        }
                    });
                }

                @Override
                public void failure(@Nonnull String reason) {
                    player.sendMessage(ChatColor.RED + reason);
                }
            });
        }
    }
}