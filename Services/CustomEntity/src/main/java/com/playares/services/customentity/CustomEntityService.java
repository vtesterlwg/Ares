package com.playares.services.customentity;

import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.service.AresService;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.MinecraftKey;

public final class CustomEntityService implements AresService {
    @Getter public EntityTypes customEntityTypes;
    @Getter public AresPlugin owner;

    public CustomEntityService(AresPlugin owner) {
        this.owner = owner;
    }

    public void start() {}

    public void stop() {}

    public String getName() {
        return "Custom Entities";
    }

    public void register(int entityId, String entityName, Class<? extends Entity> clazz) {
        final MinecraftKey key = new MinecraftKey(entityName);

        EntityTypes.b.a(entityId, key, clazz);
        EntityTypes.d.add(key);

        Logger.print("Registered custom entity: " + entityName);
    }
}