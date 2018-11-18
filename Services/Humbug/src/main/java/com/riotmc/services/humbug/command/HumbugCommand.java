package com.riotmc.services.humbug.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.riotmc.commons.bukkit.logger.Logger;
import com.riotmc.services.humbug.HumbugService;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandAlias("humbug")
public final class HumbugCommand extends BaseCommand {
    @Getter
    public final HumbugService humbug;

    public HumbugCommand(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Subcommand("reload")
    @CommandPermission("humbug.reload")
    @Description("Reload Humbug")
    public void onReload(CommandSender sender) {
        humbug.reload();
        sender.sendMessage(ChatColor.GREEN + "Humbug reloaded");
        Logger.print(sender.getName() + " reloaded Humbug");
    }
}