package com.playares.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.playares.arena.Arenas;
import com.playares.commons.base.promise.SimplePromise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.UUID;

@AllArgsConstructor
public final class ReportCommand extends BaseCommand {
    @Getter public final Arenas plugin;

    @CommandAlias("ar")
    @Description("Access an after-match report")
    public void onReportLookup(Player player, String uuidName) {
        final UUID uuid;

        try {
            uuid = UUID.fromString(uuidName);
        } catch (IllegalArgumentException ex) {
            player.sendMessage(ChatColor.RED + "Invalid report ID");
            return;
        }

        plugin.getReportManager().getHandler().openReport(player, uuid, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}