package com.playares.arena.kit;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.services.playerclasses.PlayerClassService;
import com.playares.services.playerclasses.data.Class;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class KitHandler {
    @Getter public final KitManager manager;

    public void create(Player player, String name, SimplePromise promise) {
        final PlayerClassService classService = (PlayerClassService)manager.getPlugin().getService(PlayerClassService.class);
        final Kit existing = manager.getKit(name);
        Class playerClass = null;

        if (existing != null) {
            promise.failure("Kit with this name is already in use");
            return;
        }

        if (classService != null) {
            playerClass = classService.getClassManager().getClassByArmor(player);
        }

        if (playerClass != null) {
            final ClassKit kit = new ClassKit(manager.getPlugin(), name, player.getInventory().getContents(), player.getInventory().getArmorContents(), playerClass);

            manager.getKits().add(kit);
            kit.save();

            Logger.print(player.getName() + " created kit " + name);
            promise.success();
            return;
        }

        final Kit kit = new Kit(manager.getPlugin(), name, player.getInventory().getContents(), player.getInventory().getArmorContents());

        manager.getKits().add(kit);
        kit.save();

        Logger.print(player.getName() + " created kit " + name);
        promise.success();
    }
}