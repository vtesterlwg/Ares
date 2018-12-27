package com.riotmc.factions.addons.deathbans.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
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
import javax.annotation.Syntax;

@CommandAlias("lives|live|life|lifes")
public final class LivesCommand extends BaseCommand {
    @Getter public DeathbanAddon addon;

    public LivesCommand(DeathbanAddon addon) {
        this.addon = addon;
    }

    @Description("View lives profiles")
    @Syntax("<name>")
    public void onLives(Player player) {
        addon.getLivesManager().getLives(player.getUniqueId(), new FailablePromise<LivesPlayer>() {
            @Override
            public void success(@Nonnull LivesPlayer livesPlayer) {
                player.sendMessage(ChatColor.GOLD + "Your Lives" + ChatColor.YELLOW + ": " +
                        ChatColor.WHITE + (livesPlayer.getSoulboundLives() + livesPlayer.getStandardLives()) + " " +
                        ChatColor.RED + "(" + livesPlayer.getSoulboundLives() + " soulbound)");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Description("View lives profiles")
    @Syntax("<name>")
    public void onLives(Player player, String username) {
        addon.getLivesManager().getLives(username, new FailablePromise<LivesPlayer>() {
            @Override
            public void success(@Nonnull LivesPlayer livesPlayer) {
                player.sendMessage(ChatColor.GOLD + username + "'s Lives" + ChatColor.YELLOW + ": " +
                        ChatColor.WHITE + (livesPlayer.getSoulboundLives() + livesPlayer.getStandardLives()) + " " +
                        ChatColor.RED + "(" + livesPlayer.getSoulboundLives() + " soulbound)");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Description("Give a player lives")
    @Syntax("give <name> <amount>")
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