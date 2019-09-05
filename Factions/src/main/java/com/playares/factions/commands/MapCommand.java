package com.playares.factions.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.factions.Factions;
import com.playares.factions.addons.states.ServerStateAddon;
import com.playares.services.humbug.HumbugService;
import com.playares.services.humbug.features.cont.KitLimits;
import com.playares.services.playerclasses.PlayerClassService;
import com.playares.services.playerclasses.data.Class;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@AllArgsConstructor
public final class MapCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @CommandAlias("map")
    @Description("View map-specific information")
    public void onMap(Player player) {
        final Menu menu = new Menu(plugin, player, "Map Info", 1);
        final Menu classMenu = new Menu(plugin, player, "Map Info: Classes", 1);
        final List<String> mapLore = Lists.newArrayList();
        final List<String> enchantLore = Lists.newArrayList();
        final List<String> potionLore = Lists.newArrayList();
        final List<String> classLore = Lists.newArrayList();
        final List<String> storeLore = Lists.newArrayList();
        final HumbugService humbugService = (HumbugService)getPlugin().getService(HumbugService.class);
        final ServerStateAddon serverStateAddon = (ServerStateAddon)getPlugin().getAddonManager().getAddon(ServerStateAddon.class);

        for (World world : Bukkit.getWorlds()) {
            final double distance = world.getWorldBorder().getSize() / 2;

            if (world.getEnvironment().equals(World.Environment.NORMAL)) {
                mapLore.add(ChatColor.GREEN + "Overworld" + ChatColor.RESET + ": " + distance + "x" + distance);
                continue;
            }

            if (world.getEnvironment().equals(World.Environment.NETHER)) {
                mapLore.add(ChatColor.DARK_RED + "Nether" + ChatColor.RESET + ": " + distance + "x" + distance);
                continue;
            }

            mapLore.add(ChatColor.DARK_PURPLE + "End" + ChatColor.RESET + ": " + distance + "x" + distance);
        }

        classLore.add(ChatColor.AQUA + "View information about our" + ChatColor.YELLOW + " PvP Classes");
        classLore.add(ChatColor.RESET + " ");
        classLore.add(ChatColor.GREEN + "Click to view more!");

        storeLore.add(ChatColor.AQUA + "Browse our store and purchase");
        storeLore.add(ChatColor.YELLOW + "Ranks, Lives & Boosters" + ChatColor.AQUA + "!");
        storeLore.add(ChatColor.RESET + " ");
        storeLore.add(ChatColor.GREEN + "Click to receive a link!");

        if (serverStateAddon != null) {
            mapLore.add(ChatColor.RESET + " ");
            mapLore.add(ChatColor.GOLD + "Current State" + ChatColor.RESET + ": " + serverStateAddon.getCurrentState().getDisplayName());
        }

        if (humbugService != null) {
            final KitLimits kitLimits = (KitLimits)humbugService.getModule(KitLimits.class);

            if (kitLimits != null) {
                for (KitLimits.EnchantLimit enchantLimit : kitLimits.getEnchantLimits()) {
                    enchantLore.add(ChatColor.GOLD + WordUtils.capitalize(enchantLimit.getType().getName().replace("_", " ").toLowerCase()));
                    enchantLore.add(ChatColor.YELLOW + "Max Level" + ChatColor.RESET + ": " + enchantLimit.getMaxLevel() + ChatColor.YELLOW + ", " + ChatColor.YELLOW + "Status" + ChatColor.RESET + ": " + (enchantLimit.isDisabled() ? ChatColor.RED + "Disabled" : ChatColor.GREEN + "Enabled"));
                    enchantLore.add(ChatColor.RESET + " ");
                }

                for (KitLimits.PotionLimit potionLimit : kitLimits.getPotionLimits()) {
                    potionLore.add(ChatColor.GOLD + WordUtils.capitalize(potionLimit.getType().getName().replace("_", " ").toLowerCase()));

                    potionLore.add(
                            ChatColor.YELLOW + "Disabled" + ChatColor.RED + ": " + (potionLimit.isDisabled() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No") +
                            ChatColor.YELLOW + ", " + ChatColor.YELLOW + "Amplifiable" + ChatColor.RESET + ": " + (potionLimit.isAmplifiable() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No") +
                            ChatColor.YELLOW + ", " + ChatColor.YELLOW + "Extendable" + ChatColor.RESET + ": " + (potionLimit.isExtendable() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));

                    potionLore.add(ChatColor.RESET + " ");
                }
            }
        }

        final ItemStack mapIcon = new ItemBuilder()
                .setMaterial(Material.MAP)
                .setName(ChatColor.AQUA + "Map Information")
                .addLore(mapLore)
                .build();

        final ItemStack enchantIcon = new ItemBuilder()
                .setMaterial(Material.DIAMOND_HELMET)
                .setName(ChatColor.AQUA + "Enchantment Limits")
                .addLore(enchantLore)
                .addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
                .addFlag(ItemFlag.HIDE_ENCHANTS)
                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                .build();

        final ItemStack potionIcon = new ItemBuilder()
                .setMaterial(Material.POTION)
                .setName(ChatColor.AQUA + "Potion Limits")
                .addLore(potionLore)
                .addFlag(ItemFlag.HIDE_POTION_EFFECTS)
                .build();

        final ItemStack classIcon = new ItemBuilder()
                .setMaterial(Material.LEATHER_CHESTPLATE)
                .setName(ChatColor.AQUA + "Class Info")
                .addLore(classLore)
                .build();

        final ItemStack storeIcon = new ItemBuilder()
                .setMaterial(Material.EMERALD)
                .setName(ChatColor.AQUA + "Visit Store")
                .addLore(storeLore)
                .build();

        menu.addItem(new ClickableItem(mapIcon, 2, null));
        menu.addItem(new ClickableItem(enchantIcon, 3, null));
        menu.addItem(new ClickableItem(potionIcon, 4, null));
        menu.addItem(new ClickableItem(storeIcon, 6, click -> {
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Visit our store at " + ChatColor.YELLOW + "shop.playares.net");
        }));

        menu.addItem(new ClickableItem(classIcon, 5, click -> {
            final PlayerClassService classService = (PlayerClassService)getPlugin().getService(PlayerClassService.class);

            if (classService == null) {
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "Failed to obtain Class Service");
                return;
            }

            int pos = 0;

            for (Class playerClass : classService.getClassManager().getClasses()) {
                final List<String> lore = Lists.newArrayList();
                final List<String> passives = Lists.newArrayList();

                playerClass.getPassiveEffects().keySet().forEach(effectType -> {
                    final int amplifier = playerClass.getPassiveEffects().get(effectType) + 1;
                    passives.add(ChatColor.BLUE + WordUtils.capitalize(effectType.getName().toLowerCase().replace("_", " ")) + ChatColor.GRAY + " " + amplifier);
                });

                lore.add(ChatColor.BLUE + "Passives" + ChatColor.RESET + ": ");
                lore.add(ChatColor.GRAY + Joiner.on(ChatColor.GRAY + ", ").join(passives));

                lore.add(ChatColor.RESET + " ");
                lore.add(ChatColor.RED + "Consumables" + ChatColor.RESET + ": ");

                playerClass.getConsumables().forEach(classConsumable -> {
                    lore.add(ChatColor.GOLD + WordUtils.capitalize(classConsumable.getEffectType().getName().toLowerCase().replace("_", " ")) + ChatColor.GOLD + " (" + ChatColor.YELLOW + WordUtils.capitalize(classConsumable.getMaterial().name().toLowerCase().replace("_", " ")) + ChatColor.GOLD + ")");

                    lore.add(
                            ChatColor.YELLOW + "Affects" + ChatColor.RESET + ": " + WordUtils.capitalize(classConsumable.getApplicationType().name().toLowerCase().replace("_", " ")) +
                            ChatColor.YELLOW + ", " + ChatColor.YELLOW + "Amplifier" + ChatColor.YELLOW + ": " + ChatColor.RESET + (classConsumable.getEffectAmplifier() + 1) +
                            ChatColor.YELLOW + ", " + ChatColor.YELLOW + "Duration" + ChatColor.YELLOW + ": " + ChatColor.RESET + classConsumable.getDuration() + "s" +
                            ChatColor.YELLOW + ", " + ChatColor.YELLOW + "Cooldown" + ChatColor.YELLOW + ": " + ChatColor.RESET + classConsumable.getCooldown() + "s");

                    lore.add(ChatColor.RESET + " ");
                });

                final ItemStack icon = new ItemBuilder()
                        .setMaterial(playerClass.getRequiredHelmet())
                        .setName(ChatColor.GOLD + playerClass.getName())
                        .addLore(lore)
                        .build();

                classMenu.addItem(new ClickableItem(icon, pos, null));

                pos++;
            }

            classMenu.open();
        }));

        menu.open();
    }

    @HelpCommand
    public void onHelp(Player player, CommandHelp help) {
        help.showHelp();
    }
}
