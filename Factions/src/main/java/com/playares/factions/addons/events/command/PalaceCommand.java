package com.playares.factions.addons.events.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.events.menu.LootMenu;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@CommandAlias("palace")
public final class PalaceCommand extends BaseCommand {
    @Getter public final EventsAddon addon;

    public PalaceCommand(EventsAddon addon) {
        this.addon = addon;
    }

    /*
    /palace restock <palace> Restocks all palace chests
    /palace chest create <palace> <tier> - Creates palace chest for event currently looking at
    /palace chest delete - Deletes palace chest currently looking at
     */

    @Subcommand("restock|rs")
    @CommandPermission("factions.events.palace.restock")
    @CommandCompletion("@events")
    public void onRestock(Player player, String eventName) {
        addon.getManager().getHandler().restock(player, eventName, new SimplePromise() {
            @Override
            public void success() {
                Bukkit.broadcastMessage(EventsAddon.PREFIX + ChatColor.GREEN + "Palace chests have been respawned");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("chest")
    @CommandCompletion("@events")
    @CommandPermission("factions.events.palace.chests")
    public void onChest(Player player, @Values("create") String create, @Values("tier1|tier2|tier3") String tier, String eventName) {
        addon.getManager().getHandler().createChest(player, eventName, tier, new SimplePromise() {
            @Override
            public void success() {

            }

            @Override
            public void failure(@Nonnull String reason) {

            }
        });
    }

    @Subcommand("loot")
    public void onLoot(Player player, @Values("tier1|tier2|tier3") String tier) {
        getAddon().getManager().getHandler().showPalaceLoot(player, tier, new FailablePromise<LootMenu>() {
            @Override
            public void success(@Nonnull LootMenu lootMenu) {
                lootMenu.open();
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @HelpCommand
    @Description("View a list of Palace commands")
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }
}