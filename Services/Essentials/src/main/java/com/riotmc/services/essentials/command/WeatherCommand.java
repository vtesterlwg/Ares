package com.riotmc.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.commons.bukkit.logger.Logger;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class WeatherCommand extends BaseCommand {
    @CommandAlias("weather")
    @CommandPermission("essentials.weather")
    @Description("Change the weather")
    @Syntax("<clear/thunder/rain>")
    public void onWeather(Player player, @Values("clear|thunder|rain") String weather) {
        final World world = player.getWorld();

        if (!world.getEnvironment().equals(World.Environment.NORMAL)) {
            player.sendMessage(ChatColor.RED + "You are not in a world with weather");
            return;
        }

        if (weather.equalsIgnoreCase("clear")) {
            world.setThundering(false);
            world.setStorm(false);
            world.setWeatherDuration(Integer.MAX_VALUE);
            player.sendMessage(ChatColor.GREEN + "Weather is now clear");
            Logger.print(player.getName() + " set the weather to clear");
            return;
        }

        if (weather.equalsIgnoreCase("rain")) {
            world.setThundering(false);
            world.setStorm(true);
            world.setWeatherDuration(Integer.MAX_VALUE);
            player.sendMessage(ChatColor.GREEN + "Weather is now rainy");
            Logger.print(player.getName() + " set the weather to rainy");
            return;
        }

        if (weather.equalsIgnoreCase("thunder")) {
            world.setThundering(true);
            world.setStorm(true);
            world.setWeatherDuration(Integer.MAX_VALUE);
            player.sendMessage(ChatColor.GREEN + "Weather is now thundering");
            Logger.print(player.getName() + " set the weather to thundering");
        }
    }
}