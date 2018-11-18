package com.riotmc.services.essentials.data.warp;

import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.bukkit.location.PLocatable;
import com.riotmc.services.essentials.EssentialsService;
import lombok.Getter;
import org.bukkit.entity.Player;

public final class WarpHandler {
    @Getter
    public final EssentialsService essentials;

    public WarpHandler(EssentialsService essentials) {
        this.essentials = essentials;
    }

    public void createWarp(String name, Player player, SimplePromise promise) {
        if (essentials.getWarpManager().getWarp(name) != null) {
            promise.failure("Warp name is already in use");
            return;
        }

        final PLocatable location = new PLocatable(player);
        final Warp warp = new Warp(name, location.getWorldName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        essentials.getWarpManager().getWarps().add(warp);
        essentials.getWarpManager().saveWarps();

        promise.success();
    }

    public void deleteWarp(String name, SimplePromise promise) {
        final Warp warp = essentials.getWarpManager().getWarp(name);

        if (warp == null) {
            promise.failure("Warp not found");
            return;
        }

        essentials.getWarpManager().getWarps().remove(warp);
        essentials.getWarpManager().deleteWarp(warp);

        promise.success();
    }
}