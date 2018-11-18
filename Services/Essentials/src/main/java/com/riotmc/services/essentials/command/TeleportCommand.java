package com.riotmc.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.riotmc.commons.bukkit.logger.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class TeleportCommand extends BaseCommand {
    @CommandAlias("teleport|tp")
    @CommandPermission("essentials.teleport.basic")
    @CommandCompletion("@players")
    @Syntax("<player>")
    @Description("Teleport to a player")
    public void onTeleport(Player player, String destinationName) {
        final Player destination = Bukkit.getPlayer(destinationName);

        if (destination == null) {
            player.sendMessage(ChatColor.RED + "Player not found");
            return;
        }

        player.teleport(destination);
        player.sendMessage(ChatColor.GREEN + "Teleported to " + destination.getName());
        Logger.print(player.getName() + " teleported to " + destination.getName());
    }

    @CommandAlias("teleportall|tpall")
    @CommandPermission("essentials.teleport.all")
    @Description("Teleport all players to your location")
    public void onTeleportAll(Player player) {
        Bukkit.getOnlinePlayers().stream().filter(players -> !players.getUniqueId().equals(player.getUniqueId())).forEach(toTeleport -> {
            toTeleport.teleport(player);
            toTeleport.sendMessage(ChatColor.GREEN + "Teleported to " + player.getName());
        });

        player.sendMessage(ChatColor.GREEN + "Teleported " + Bukkit.getOnlinePlayers().size() + " players to your location");
        Logger.warn(player.getName() + " teleported " + Bukkit.getOnlinePlayers().size() + " to them");
    }

    @CommandAlias("teleport|tp")
    @CommandPermission("essentials.teleport.others")
    @CommandCompletion("@players @players")
    @Syntax("<player> <player>")
    @Description("Teleport a player to another")
    public void onTeleportOthers(CommandSender sender, String fromName, String toName) {
        final Player from = Bukkit.getPlayer(fromName);
        final Player to = Bukkit.getPlayer(toName);

        if (from == null) {
            sender.sendMessage(ChatColor.RED + fromName + " not found");
            return;
        }

        if (to == null) {
            sender.sendMessage(ChatColor.RED + toName + " not found");
            return;
        }

        from.teleport(to);
        from.sendMessage(ChatColor.GREEN + sender.getName() + " teleported you to " + to.getName());
        Logger.print(sender.getName() + " teleported " + from.getName() + " to " + to.getName());
    }

    @CommandAlias("teleport|tp")
    @Syntax("<x> <y> <z>")
    @Description("Teleport to coordinates in your current world")
    @CommandPermission("essentials.teleport.coords")
    public void onTeleportCoords(Player player, double x, double y, double z) {
        player.teleport(new Location(player.getWorld(), x, y, z));
        player.sendMessage(ChatColor.GREEN + "Teleported to " + x + " " + y + " " + z);
        Logger.print(player.getName() + " teleported to coordinates " + x + " " + y + " " + z);
    }

    @CommandAlias("teleport|tp")
    @CommandPermission("essentials.teleport.coords")
    @CommandCompletion("@worlds")
    @Syntax("<world> <x> <y> <z>")
    @Description("Teleport to coordinates in a specified world")
    public void onTeleportWorldCoords(Player player, String worldName, double x, double y, double z) {
        final World world = Bukkit.getWorld(worldName);

        if (world == null) {
            player.sendMessage(ChatColor.RED + "World not found");
            return;
        }

        player.teleport(new Location(world, x, y, z));
        player.sendMessage(ChatColor.GREEN + "Teleport to " + world.getName() + " " + x + " " + y + " " + z);
        Logger.print(player.getName() + " teleported to " + world.getName() + " " + x + " " + y + " " + z);
    }
}