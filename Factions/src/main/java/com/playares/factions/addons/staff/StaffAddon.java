package com.playares.factions.addons.staff;

import com.playares.commons.bukkit.logger.Logger;
import com.playares.factions.Factions;
import com.playares.factions.addons.Addon;
import com.playares.factions.addons.staff.item.*;
import com.playares.factions.addons.staff.listener.StaffListener;
import com.playares.services.customitems.CustomItemService;
import com.playares.services.essentials.EssentialsService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class StaffAddon implements Addon {
    @Getter public final Factions plugin;

    @Override
    public String getName() {
        return "Staff Mode";
    }

    @Override
    public void prepare() {
        registerItems();
    }

    @Override
    public void start() {
        plugin.registerListener(new StaffListener(this));
    }

    @Override
    public void stop() {}

    private void registerItems() {
        final CustomItemService customItemService = (CustomItemService)plugin.getService(CustomItemService.class);

        if (customItemService == null) {
            Logger.error("Failed to obtain Custom Item Service while registering Staff Items");
            return;
        }

        customItemService.registerNewItem(new CombatIndexItem(plugin));
        customItemService.registerNewItem(new EnableVanishItem(this));
        customItemService.registerNewItem(new DisableVanishItem(this));
        customItemService.registerNewItem(new NavigatorItem());
        customItemService.registerNewItem(new InvseeItem());
    }

    public void giveItems(Player player) {
        final CustomItemService customItemService = (CustomItemService)plugin.getService(CustomItemService.class);
        final EssentialsService essentialsService = (EssentialsService)plugin.getService(EssentialsService.class);

        if (customItemService == null) {
            Logger.error("Failed to obtain Custom Item Service while granting " + player.getName() + " staff items");
            return;
        }

        player.getInventory().clear();

        customItemService.getItem(CombatIndexItem.class).ifPresent(item -> player.getInventory().setItem(8, item.getItem()));
        customItemService.getItem(NavigatorItem.class).ifPresent(item -> player.getInventory().setItem(0, item.getItem()));
        customItemService.getItem(InvseeItem.class).ifPresent(item -> player.getInventory().setItem(7, item.getItem()));

        if (essentialsService != null) {
            if (essentialsService.getVanishManager().isVanished(player)) {
                customItemService.getItem(DisableVanishItem.class).ifPresent(item -> player.getInventory().setItem(4, item.getItem()));
            } else {
                customItemService.getItem(EnableVanishItem.class).ifPresent(item -> player.getInventory().setItem(4, item.getItem()));
            }
        }
    }
}