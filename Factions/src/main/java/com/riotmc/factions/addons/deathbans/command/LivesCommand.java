package com.riotmc.factions.addons.deathbans.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Values;
import com.riotmc.commons.base.promise.FailablePromise;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.factions.addons.deathbans.DeathbanAddon;
import com.riotmc.factions.addons.deathbans.data.LivesPlayer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public final class LivesCommand extends BaseCommand {
    @Getter public DeathbanAddon addon;

    public LivesCommand(DeathbanAddon addon) {
        this.addon = addon;
    }

    @CommandAlias("lives|live|life|lifes")
    public void onLives(Player player) {
        addon.getLivesManager().getLives(player.getName(), new FailablePromise<LivesPlayer>() {
            @Override
            public void success(@Nonnull LivesPlayer livesPlayer) {
                player.sendMessage(ChatColor.GOLD + "Your Soulbound Lives" + ChatColor.YELLOW + ": " + ChatColor.WHITE + livesPlayer.getSoulboundLives());
                player.sendMessage(ChatColor.GOLD + "Your Standard Lives" + ChatColor.YELLOW + ": " + ChatColor.WHITE + livesPlayer.getStandardLives());
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.GOLD + "Your Soulbound Lives" + ChatColor.YELLOW + ": " + ChatColor.WHITE + 0);
                player.sendMessage(ChatColor.GOLD + "Your Standard Lives" + ChatColor.YELLOW + ": " + ChatColor.WHITE + 0);
            }
        });
    }

    @CommandAlias("lives|live|life|lifes")
    public void onLives(Player player, String username) {
        addon.getLivesManager().getLives(username, new FailablePromise<LivesPlayer>() {
            @Override
            public void success(@Nonnull LivesPlayer livesPlayer) {
                player.sendMessage(ChatColor.GOLD + username + "'s Soulbound Lives" + ChatColor.YELLOW + ": " + ChatColor.WHITE + livesPlayer.getSoulboundLives());
                player.sendMessage(ChatColor.GOLD + username + "'s Standard Lives" + ChatColor.YELLOW + ": " + ChatColor.WHITE + livesPlayer.getStandardLives());
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.GOLD + username + "'s Soulbound Lives" + ChatColor.YELLOW + ": " + ChatColor.WHITE + 0);
                player.sendMessage(ChatColor.GOLD + username + "'s Standard Lives" + ChatColor.YELLOW + ": " + ChatColor.WHITE + 0);
            }
        });
    }

    @CommandAlias("lives|live|life|lifes")
    public void onModify(CommandSender sender, @Values("give|set") String modifier, String username, int amount) {
        if (modifier.equalsIgnoreCase("give")) {
            addon.getLivesManager().getHandler().give(sender, username, amount, new SimplePromise() {
                @Override
                public void success() {
                    sender.sendMessage(ChatColor.GREEN + "You have given " + amount + " lives to " + username);
                }

                @Override
                public void failure(@Nonnull String reason) {
                    sender.sendMessage(ChatColor.RED + reason);
                }
            });
        }

        else if (modifier.equalsIgnoreCase("set")) {
            addon.getLivesManager().getHandler().set(sender, username, amount, new SimplePromise() {
                @Override
                public void success() {
                    sender.sendMessage(ChatColor.GREEN + "You have updated " + username + "'s lives to " + amount);
                }

                @Override
                public void failure(@Nonnull String reason) {
                    sender.sendMessage(ChatColor.RED + reason);
                }
            });
        }
    }
}