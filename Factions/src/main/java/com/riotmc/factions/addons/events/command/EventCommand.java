package com.riotmc.factions.addons.events.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Values;
import org.bukkit.entity.Player;

@CommandAlias("event|e")
public final class EventCommand extends BaseCommand {
    /*
    /event list
    /event create <name> <displayName> <kothticket/kothtimer/palace>
    /event rename <name> <newName>
    /event delete <name>
    /event start <name>
    /event stop <name>
    /event set <timer/wincondition> <amount>
     */

    @Subcommand("list")
    public void onList(Player player) {

    }

    @Subcommand("create")
    public void onCreate(String name, String displayName, String type) {

    }

    @Subcommand("rename")
    public void onRename(String name, String newName) {

    }

    @Subcommand("delete")
    public void onDelete(String name) {

    }

    @Subcommand("start")
    public void onStart(String name) {

    }

    @Subcommand("stop")
    public void onStop(String name) {

    }

    @Subcommand("set")
    public void onSet(@Values("set") String set, @Values("timer|wincondition") String value, int amount) {

    }
}
