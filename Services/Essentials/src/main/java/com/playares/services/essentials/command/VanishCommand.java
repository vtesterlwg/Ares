package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.playares.services.essentials.EssentialsService;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class VanishCommand extends BaseCommand {
    @Getter
    public final EssentialsService essentials;

    public VanishCommand(EssentialsService essentials) {
        this.essentials = essentials;
    }

    @CommandAlias("vanish|v|togglevanish")
    @CommandPermission("essentials.vanish")
    @Description("Hide from other players")
    public void onVanish(Player player) {
        if (essentials.getVanishManager().isVanished(player)) {
            essentials.getVanishHandler().showPlayer(player, true);
            player.sendMessage(ChatColor.YELLOW + "You are now " + ChatColor.WHITE + "visible");
            return;
        }

        essentials.getVanishHandler().hidePlayer(player, true);
        player.sendMessage(ChatColor.YELLOW + "You are now " + ChatColor.WHITE + "invisible");
    }
}
