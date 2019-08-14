package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.remap.RemappedEffect;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@CommandAlias("effect")
public final class EffectCommand extends BaseCommand {
    @Subcommand("give")
    @CommandPermission("essentials.effect")
    @CommandCompletion("@potions")
    @Description("Apply a potion effect")
    @Syntax("<player> <effect> <seconds> <amplifier>")
    public void onGive(Player player, String playerName, String effectName, int seconds, int amplifier) {
        final Player toApply = Bukkit.getPlayer(playerName);
        final PotionEffectType effect = RemappedEffect.getEffectTypeByName(effectName);

        if (toApply == null) {
            player.sendMessage(ChatColor.RED + "Player not found");
            return;
        }

        if (effect == null) {
            player.sendMessage(ChatColor.RED + "Effect not found");
            return;
        }

        final PotionEffect newEffect = new PotionEffect(effect, (seconds * 20), (amplifier - 1));
        toApply.addPotionEffect(newEffect);
        toApply.sendMessage(ChatColor.YELLOW + "You have been given " + ChatColor.WHITE + effect.getName() + " " + amplifier + ChatColor.YELLOW + " for " + ChatColor.WHITE + seconds + " seconds");

        player.sendMessage(ChatColor.GREEN + "Effect applied");
        Logger.print(player.getName() + " gave potion effect " + effect.getName() + " " + amplifier + " for " + seconds + " seconds to " + toApply.getName());
    }

    @Subcommand("remove")
    @CommandPermission("essentials.effect")
    @CommandCompletion("@potions")
    @Description("Remove a potion effect from yourself")
    @Syntax("<effect>")
    public void onRemove(Player player, String name) {
        if (name.equalsIgnoreCase("all")) {
            player.getActivePotionEffects().clear();
            player.sendMessage(ChatColor.YELLOW + "Your potion effects have been cleared");
            Logger.print(player.getName() + " removed all of their potion effects");
            return;
        }

        final PotionEffectType type = PotionEffectType.getByName(name);

        if (type == null) {
            player.sendMessage(ChatColor.RED + "Effect not found");
            return;
        }

        final String fancyName = StringUtils.capitaliseAllWords(type.getName().toLowerCase().replace("_", " "));

        player.removePotionEffect(type);
        player.sendMessage(ChatColor.YELLOW + "Removed effect " + ChatColor.WHITE + fancyName);
        Logger.print(player.getName() + " removed effect " + fancyName + " from themselves");
    }

    @Subcommand("remove")
    @CommandPermission("essentials.effect")
    @CommandCompletion("@potions @players")
    @Description("Remove a potion effect from a player")
    @Syntax("<effect> <player>")
    public void onRemoveOther(CommandSender sender, String name, String playerName) {
        final Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found");
            return;
        }

        if (name.equalsIgnoreCase("all")) {
            player.getActivePotionEffects().clear();
            player.sendMessage(ChatColor.YELLOW + "Your potion effects have been cleared");
            sender.sendMessage(ChatColor.YELLOW + "You have removed " + ChatColor.WHITE + "all" + ChatColor.YELLOW + " potion effects from " + ChatColor.WHITE + player.getName());
            Logger.print(sender.getName() + " removed all of " + player.getName() + "'s potion effects");
            return;
        }

        final PotionEffectType type = PotionEffectType.getByName(name);

        if (type == null) {
            player.sendMessage(ChatColor.RED + "Effect not found");
            return;
        }

        final String fancyName = StringUtils.capitaliseAllWords(type.getName().toLowerCase().replace("_", " "));

        player.removePotionEffect(type);
        player.sendMessage(ChatColor.YELLOW + "Removed effect " + ChatColor.WHITE + fancyName);
        sender.sendMessage(ChatColor.YELLOW + "Removed effect " + ChatColor.WHITE + fancyName + ChatColor.YELLOW + " from " + ChatColor.WHITE + player.getName());
        Logger.print(sender.getName() + " removed effect " + fancyName + " from " + player.getName());
    }
}