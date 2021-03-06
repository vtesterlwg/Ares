package com.playares.services.essentials.data.kit;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.services.essentials.EssentialsService;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class KitHandler {
    @Getter public final EssentialsService essentials;

    public KitHandler(EssentialsService essentials) {
        this.essentials = essentials;
    }

    public void createKit(String name, Player player, SimplePromise promise) {
        if (essentials.getKitManager().getKit(name) != null) {
            promise.failure("Kit name is already in use");
            return;
        }

        final Kit kit = new Kit(name, player.getInventory().getStorageContents(), player.getInventory().getArmorContents());

        essentials.getKitManager().getKits().add(kit);
        essentials.getKitManager().saveKits();

        promise.success();
    }

    public void deleteKit(String name, SimplePromise promise) {
        final Kit kit = essentials.getKitManager().getKit(name);

        if (kit == null) {
            promise.failure("Kit not found");
            return;
        }

        essentials.getKitManager().getKits().remove(kit);
        essentials.getKitManager().deleteKit(kit);

        promise.success();
    }

    public void viewKit(Player player, String name, SimplePromise promise) {
        final Kit kit = essentials.getKitManager().getKit(name);

        if (kit == null) {
            promise.failure("Kit not found");
            return;
        }

        final Menu view = new Menu(essentials.getOwner(), player, "Kit: " + kit.getName(), 6);

        for (int i = 0; i < kit.getContents().length; i++) {
            final ItemStack item = kit.getContents()[i];

            if (item == null) {
                continue;
            }

            view.addItem(new ClickableItem(item, i, click -> {}));
        }

        for (int i = 45; i< kit.getArmor().length; i++) {
            final ItemStack item = kit.getArmor()[i];

            if (item == null) {
                continue;
            }

            view.addItem(new ClickableItem(item, i, click -> {}));
        }

        view.open();
        promise.success();
    }
}
