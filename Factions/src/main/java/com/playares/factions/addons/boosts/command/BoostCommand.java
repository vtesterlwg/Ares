package com.playares.factions.addons.boosts.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.factions.addons.boosts.BoostAddon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@AllArgsConstructor
@CommandAlias("boost|boosts")
public final class BoostCommand extends BaseCommand {
    @Getter public final BoostAddon addon;

    @CommandAlias("boost|boosts")
    @Description("Open & Use your Server Boosts")
    public void onBoost(Player player) {
        addon.getHandler().openMenu(player);
    }

    @Subcommand("give")
    @CommandPermission("factions.boosts.give")
    @Description("Grant players server boosters")
    @CommandCompletion("@players")
    public void onGive(CommandSender sender, String username, @Values("ores|exp|drops") String type, int duration) {
        addon.getHandler().give(sender, username, type, duration, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}