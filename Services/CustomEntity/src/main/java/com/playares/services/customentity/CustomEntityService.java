package com.playares.services.customentity;

import com.mojang.datafixers.types.Type;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.service.AresService;
import lombok.Getter;
import net.minecraft.server.v1_13_R2.*;

import java.util.Map;
import java.util.function.Function;

public final class CustomEntityService implements AresService {
    @Getter
    public AresPlugin owner;

    public CustomEntityService(AresPlugin owner) {
        this.owner = owner;
    }

    public void start() {}

    public void stop() {}

    public String getName() {
        return "Custom Entities";
    }

    @SuppressWarnings("unchecked")
    public void register(String name, String extendedFrom, Class<? extends Entity> clazz, Function<? super World, ? extends Entity> function) {
        Map<Object, Type<?>> dataTypes = (Map<Object, Type<?>>) DataConverterRegistry.a().getSchema(15190).findChoiceType(DataConverterTypes.n).types();
        dataTypes.put("minecraft:" + name, dataTypes.get("minecraft:" + extendedFrom));
        EntityTypes.a(name, EntityTypes.a.a(clazz, function));
        Logger.print("Registered custom entity: " + name + ", extends from " + extendedFrom);
    }
}