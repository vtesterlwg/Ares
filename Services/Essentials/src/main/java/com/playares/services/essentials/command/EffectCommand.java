package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.playares.commons.bukkit.logger.Logger;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@CommandAlias("effect")
public final class EffectCommand extends BaseCommand {
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
    @CommandPermission("essentials.effect.others")
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

    @Subcommand("give")
    @Description("Give an effect to yourself")
    @CommandPermission("essentials.effect")
    @CommandCompletion("@potions")
    @Syntax("<effect> <amplifier> <duration>")
    public void onApply(Player player, String name, int amplifier, int duration) {
        final PotionEffectType type = PotionEffectType.getByName(name);

        if (type == null) {
            player.sendMessage(ChatColor.RED + "Effect not found");
            return;
        }

        final PotionEffect effect = type.createEffect((duration * 20), (amplifier - 1));
        final String fancyName = StringUtils.capitaliseAllWords(type.getName().toLowerCase().replace("_", " "));

        player.addPotionEffect(effect);
        player.sendMessage(ChatColor.YELLOW + "You have been given " + ChatColor.WHITE + fancyName + " " + amplifier + ChatColor.YELLOW + " for " + ChatColor.WHITE + duration + " seconds");
        Logger.print(player.getName() + " applied effect " + fancyName + amplifier + " to themselves for " + duration + " seconds");
    }

    @Subcommand("give")
    @Description("Apply an effect to a player")
    @CommandPermission("essentials.effect.others")
    @CommandCompletion("@potions @players")
    @Syntax("<effect> <amplifier> <duration> <player>")
    public void onApplyOther(CommandSender sender, String name, int amplifier, int duration, String playerName) {
        final Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found");
            return;
        }

        final PotionEffectType type = PotionEffectType.getByName(name);

        if (type == null) {
            sender.sendMessage(ChatColor.RED + "Effect not found");
            return;
        }

        final PotionEffect effect = type.createEffect((duration * 20), (amplifier - 1));
        final String fancyName = StringUtils.capitaliseAllWords(type.getName().toLowerCase().replace("_", " "));

        player.addPotionEffect(effect);
        player.sendMessage(ChatColor.YELLOW + "You have been given " + ChatColor.WHITE + fancyName + " " + amplifier + ChatColor.YELLOW + " for " + ChatColor.WHITE + duration + " seconds" + ChatColor.YELLOW + " by " + ChatColor.WHITE + sender.getName());
        sender.sendMessage(ChatColor.YELLOW + "You have given " + ChatColor.WHITE + fancyName + " " + amplifier + ChatColor.YELLOW + " to " + ChatColor.WHITE + player.getName() + ChatColor.YELLOW + " for " + ChatColor.WHITE + duration + " seconds");
        Logger.print(sender.getName() + " applied effect " + fancyName + amplifier + " to " + player.getName() + " for " + duration + " seconds");
    }
}