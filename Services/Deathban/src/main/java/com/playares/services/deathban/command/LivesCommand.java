package com.playares.services.deathban.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.services.deathban.DeathbanService;
import com.playares.services.deathban.data.LivesPlayer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

@CommandAlias("lives|lifes|live|life")
public final class LivesCommand extends BaseCommand {
    @Getter public final DeathbanService service;

    public LivesCommand(DeathbanService service) {
        this.service = service;
    }

    @CommandAlias("lives|lifes|live|life")
    @Syntax("[player]")
    @Description("View your lives")
    public void onLives(Player player) {
        service.getLives(player.getUniqueId(), new FailablePromise<LivesPlayer>() {
            @Override
            public void success(@Nonnull LivesPlayer livesPlayer) {
                final int combined = livesPlayer.getStandardLives() + livesPlayer.getSoulboundLives();
                player.sendMessage(ChatColor.GOLD + "You have " + ChatColor.YELLOW + combined + " lives" + ChatColor.RED + " (" + livesPlayer.getSoulboundLives() + " are Soulbound)");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @CommandAlias("lives|lifes|live|life")
    @Syntax("[player]")
    @Description("View a players lives")
    public void onLives(Player player, String username) {
        service.getLives(username, new FailablePromise<LivesPlayer>() {
            @Override
            public void success(@Nonnull LivesPlayer livesPlayer) {
                final int combined = livesPlayer.getStandardLives() + livesPlayer.getSoulboundLives();
                player.sendMessage(ChatColor.GOLD + username + " has " + ChatColor.YELLOW + combined + " lives" + ChatColor.RED + " (" + livesPlayer.getSoulboundLives() + " are Soulbound)");
            }

            @Override
            public void failure(@Nonnull String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("give")
    @Syntax("<player> <amount>")
    @Description("Give a player lives")
    public void onGive(CommandSender sender, String username, int amount, @Optional String soulbound) {
        final boolean sb = (soulbound != null && soulbound.equalsIgnoreCase("-s"));

        service.giveLives(sender, username, amount, sb, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @Subcommand("set")
    @CommandPermission("deathbans.lives.set")
    @Syntax("<player> <amount> [-s soulbound]")
    @Description("Set a players lives")
    public void onSet(CommandSender sender, String username, int amount, @Optional String soulbound) {
        final boolean sb = (soulbound != null && soulbound.equalsIgnoreCase("-s"));

        service.setLives(username, amount, sb, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(@Nonnull String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @HelpCommand
    @Description("View a list of Lives Commands")
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }
}
