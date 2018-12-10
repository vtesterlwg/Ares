package com.riotmc.factions.addons.events.builder;

import com.riotmc.commons.base.promise.FailablePromise;
import com.riotmc.factions.addons.events.builder.type.EventBuilder;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public final class EventBuilderHandler {
    @Getter public final EventBuilderManager manager;

    public EventBuilderHandler(EventBuilderManager manager) {
        this.manager = manager;
    }

    public void setName(EventBuilder builder, Player player, String name) {
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
}
