package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class ShopCommand extends BaseCommand {
    @CommandAlias("shop|donate|store")
    @Description("Retrieve the link to our online store")
    public void onShop(Player player) {
        player.sendMessage(ChatColor.AQUA + "You can visit the Ares Store at " + ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + "https://www.playares.com/shop");
    }
}
