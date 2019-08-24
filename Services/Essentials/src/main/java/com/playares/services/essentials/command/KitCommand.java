package com.playares.services.essentials.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.services.essentials.EssentialsService;
import com.playares.services.essentials.data.kit.Kit;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

// TODO: Rewrite
public final class KitCommand extends BaseCommand {
    @Getter public EssentialsService essentials;

    public KitCommand(EssentialsService essentials) {
        this.essentials = essentials;
    }

    @CommandAlias("kits")
    @CommandPermission("essentials.kits")
    @Description("List all kits")
    public void onKitList(CommandSender sender) {
        final List<String> kitNames = Lists.newArrayList();
        essentials.getKitManager().getKits().forEach(kit -> kitNames.add(kit.getName()));
        sender.sendMessage(ChatColor.GOLD + "Kits" + ChatColor.YELLOW + ": " + ChatColor.RESET + Joiner.on(", ").join(kitNames));
    }

    @CommandAlias("kit")
    @CommandPermission("essentials.kits")
    @CommandCompletion("@kits")
    @Description("Load a kit")
    @Syntax("<kit>")
    public void onKitLoad(Player player, String name) {
        final Kit kit = essentials.getKitManager().getKit(name);

        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found");
            return;
        }

        kit.apply(player);
        player.sendMessage(ChatColor.GREEN + "Loaded " + ChatColor.AQUA + kit.getName());
    }

    @CommandAlias("savekit")
    @CommandPermission("essentials.kits")
    @Description("Save a new kit")
    @Syntax("<name>")
    public void onKitSave(Player player, String name) {
        essentials.getKitHandler().createKit(name, player, new SimplePromise() {
            @Override
            public void success() {
                player.sendMessage(ChatColor.GREEN + "Kit created");
            }

            @Override
            public void failure(String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @CommandAlias("delkit")
    @CommandPermission("essentials.kits")
    @CommandCompletion("@kits")
    @Description("Delete a kit")
    @Syntax("<kit>")
    public void onKitDelete(CommandSender sender, String name) {
        essentials.getKitHandler().deleteKit(name, new SimplePromise() {
            @Override
            public void success() {
                sender.sendMessage(ChatColor.GREEN + "Kit deleted");
            }

            @Override
            public void failure(String reason) {
                sender.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @CommandAlias("viewkit")
    @CommandPermission("essentials.kits")
    @CommandCompletion("@kits")
    @Description("View a kits loadout")
    @Syntax("<kit>")
    public void onKitView(Player player, String name) {
        essentials.getKitHandler().viewKit(player, name, new SimplePromise() {
            @Override
            public void success() {}

            @Override
            public void failure(String reason) {
                player.sendMessage(ChatColor.RED + reason);
            }
        });
    }

    @HelpCommand
    @Description("View a list of Kit Commands")
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }
}