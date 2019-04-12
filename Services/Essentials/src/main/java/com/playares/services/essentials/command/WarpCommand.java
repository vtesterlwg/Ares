package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.services.essentials.EssentialsService;
import com.playares.services.essentials.data.warp.Warp;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

// TODO: Rewrite
public final class WarpCommand extends BaseCommand {
    @Getter public EssentialsService essentials;

    public WarpCommand(EssentialsService essentials) {
        this.essentials = essentials;
    }

    @CommandAlias("warp")
    @CommandPermission("essentials.warp")
    @CommandCompletion("@warps")
    @Description("Warp to a location")
    @Syntax("<list/name>")
    public void onWarp(Player player, String name) {
        if (name.equalsIgnoreCase("list")) {
            final List<String> names = Lists.newArrayList();

            essentials.getWarpManager().getWarps().forEach(warp -> names.add(warp.getName()));

            player.sendMessage(ChatColor.GOLD + "Warps" + ChatColor.YELLOW + ": " + ChatColor.RESET + Joiner.on(", ").join(names));

            return;
        }

        final Warp warp = essentials.getWarpManager().getWarp(name);

        if (warp == null) {
            player.sendMessage(ChatColor.RED + "Warp not found");
            return;
        }

        warp.teleport(player);
        player.sendMessage(ChatColor.GREEN + "Teleported to " + ChatColor.AQUA + warp.getName());
    }

    @CommandAlias("setwarp")
    @CommandPermission("essentials.warp.create")
    @Description("Create a new warp at your location")
    @Syntax("<name>")
    public void onSetWarp(Player player, String name) {
        essentials.getWarpHandler().createWarp(name, player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Warp created");
            }

            @Override
            public void failure(String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @CommandAlias("delwarp")
    @CommandPermission("essentials.warp.delete")
    @CommandCompletion("@warps")
    @Description("Delete a warp by name")
    @Syntax("<name>")
    public void onDelWarp(Player player, String name) {
        essentials.getWarpHandler().deleteWarp(name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Warp deleted");
            }

            @Override
            public void failure(String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}