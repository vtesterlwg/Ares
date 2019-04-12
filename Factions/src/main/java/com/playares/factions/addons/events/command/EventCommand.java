package com.playares.factions.addons.events.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Values;
import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.events.menu.EventsMenu;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@CommandAlias("event|events|e")
public final class EventCommand extends BaseCommand {
    @Getter public final EventsAddon addon;

    public EventCommand(EventsAddon addon) {
        this.addon = addon;
    }

    @Subcommand("list")
    public void onList(Player player) {
        addon.getManager().getHandler().list(player, new FailablePromise<EventsMenu>() {
            @Override
            public void success(@Nonnull EventsMenu eventsMenu) {
                eventsMenu.open();
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("create")
    public void onCreate(Player player, @Values("koth|palace") String type) {
        addon.getManager().getHandler().create(player, type, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("delete")
    public void onDelete(Player player, String name) {
        addon.getManager().getHandler().delete(player, name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Event deleted");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("rename")
    public void onRename(Player player, String currentName, String newName) {
        addon.getManager().getHandler().rename(player, currentName, newName, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Event renamed");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("start")
    public void onStart(Player player, String name, int ticketsNeededToWin, int timerDuration) {
        addon.getManager().getHandler().start(name, ticketsNeededToWin, timerDuration, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Event started");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("stop")
    public void onStop(Player player, String name) {
        addon.getManager().getHandler().stop(player, name, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Event stopped");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("set")
    public void onSet(Player player, String name, @Values("timer|wincondition|wc") String type, int value) {
        addon.getManager().getHandler().set(player, name, type, value, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Event value updated");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }
}