package com.playares.services.profiles;

import com.playares.commons.bukkit.RiotPlugin;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.services.profiles.data.RiotProfile;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

public final class MenuHandler {
    @Getter
    public final RiotPlugin plugin;

    public MenuHandler(RiotPlugin plugin) {
        this.plugin = plugin;
    }

    public Menu createSettingsMenu(Player viewer) {
        return new Menu(plugin, viewer, "Global Settings", 1);
    }

    public void showSettingsMenu(RiotProfile.AresProfileSettings settings, Menu menu) {
        if (menu.isOpen()) {
            menu.clearInventory();
        }

        final ItemBuilder globalChatBuilder = new ItemBuilder().setMaterial(Material.SIGN).setName(ChatColor.YELLOW + "Hide Global Chat");
        final ItemBuilder privateChatBuilder = new ItemBuilder().setMaterial(Material.SIGN).setName(ChatColor.YELLOW + "Hide Private Messages");
        final ItemBuilder tipsBuilder = new ItemBuilder().setMaterial(Material.SIGN).setName(ChatColor.YELLOW + "Hide Tips");

        globalChatBuilder.addLore(Arrays.asList(ChatColor.GRAY + "Enabling this setting will hide",
                ChatColor.GRAY + "all global messages from non-staff members.",
                ChatColor.RESET + " "));

        privateChatBuilder.addLore(Arrays.asList(ChatColor.GRAY + "Enabling this setting will hide",
                ChatColor.GRAY + "all private messages from non-staff members.",
                ChatColor.RESET + " "));

        tipsBuilder.addLore(Arrays.asList(ChatColor.GRAY + "Enabling this setting will hide",
                ChatColor.GRAY + "tip broadcasts sent by the server.",
                ChatColor.RESET + " "));

        globalChatBuilder.addLore(ChatColor.YELLOW + "Status: " + (settings.isHidingGlobalChat() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        privateChatBuilder.addLore(ChatColor.YELLOW + "Status: " + (settings.isHidingPrivateMessages() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        tipsBuilder.addLore(ChatColor.YELLOW + "Status: " + (settings.isHidingTips() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));

        menu.addItem(new ClickableItem(globalChatBuilder.build(), 0, click -> {
            settings.setHidingGlobalChat(!settings.isHidingGlobalChat());
            showSettingsMenu(settings, menu);
        }));

        menu.addItem(new ClickableItem(privateChatBuilder.build(), 2, click -> {
            settings.setHidingPrivateMessages(!settings.isHidingPrivateMessages());
            showSettingsMenu(settings, menu);
        }));

        menu.addItem(new ClickableItem(tipsBuilder.build(), 4, click -> {
            settings.setHidingTips(!settings.isHidingTips());
            showSettingsMenu(settings, menu);
        }));

        if (!menu.isOpen()) {
            menu.open();
        }
    }
}
