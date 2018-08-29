package com.playares.arena.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.arena.Arenas;
import com.playares.commons.base.promise.SimplePromise;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

@CommandAlias("mode")
public final class ModeCommand extends BaseCommand {
    @Nonnull @Getter
    public final Arenas plugin;

    public ModeCommand(@Nonnull Arenas plugin) {
        this.plugin = plugin;
    }

    @Subcommand("create")
    @CommandPermission("arena.mode.create")
    @Syntax("<name>")
    @Description("Create a new mode")
    public void onCreate(Player player, String name) {
        plugin.getModeHandler().createMode(name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Mode created");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("delete|remove")
    @CommandPermission("arena.mode.delete")
    @Syntax("<name>")
    @Description("Delete a mode")
    public void onDelete(Player player, String name) {
        plugin.getModeHandler().deleteMode(name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Mode deleted");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("seticon")
    @CommandPermission("arena.mode.icon")
    @Syntax("<mode>")
    @Description("Update the icon for a mode")
    public void onSetIcon(Player player, String name) {
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand == null || hand.getType().equals(Material.AIR)) {
            player.sendMessage(ChatColor.RED + "You are not holding an item");
            return;
        }

        plugin.getModeHandler().setModeIcon(hand, name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Icon updated");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("addloadout|al")
    @CommandPermission("arena.mode.addkit")
    @Syntax("<mode> <kit>")
    @Description("Add a kit to a mode")
    public void onKitAdd(Player player, String modeName, String kitName) {
        plugin.getModeHandler().addLoadout(modeName, kitName, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Added kit to this mode");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("remloadout|rl")
    @CommandPermission("arena.mode.remkit")
    @Syntax("<mode> <kit>")
    @Description("Remove a kit from a mode")
    public void onKitRemove(Player player, String modeName, String kitName) {
        plugin.getModeHandler().removeLoadout(modeName, kitName, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Removed kit from this mode");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}