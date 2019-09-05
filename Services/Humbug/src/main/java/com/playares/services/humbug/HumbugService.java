package com.playares.services.humbug;

import com.google.common.collect.Maps;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.service.AresService;
import com.playares.services.humbug.features.HumbugModule;
import com.playares.services.humbug.features.cont.*;
import lombok.Getter;

import java.util.Map;

public final class HumbugService implements AresService {
    @Getter public final AresPlugin owner;
    @Getter protected Map<Class<? extends HumbugModule>, HumbugModule> modules;

    public HumbugService(AresPlugin owner) {
        this.owner = owner;
        this.modules = Maps.newHashMap();
    }

    public void start() {
        registerHumbugModule(OldPotions.class, new OldPotions(this));
        registerHumbugModule(OldItemVelocity.class, new OldItemVelocity(this));
        registerHumbugModule(AntiGrief.class, new AntiGrief(this));
        registerHumbugModule(OldSwordSwing.class, new OldSwordSwing(this));
        registerHumbugModule(MemeItems.class, new MemeItems(this));
        registerHumbugModule(AntiGlitch.class, new AntiGlitch(this));
        registerHumbugModule(XPBonuses.class, new XPBonuses(this));
        registerHumbugModule(CustomRecipes.class, new CustomRecipes(this));
        registerHumbugModule(OldRegen.class, new OldRegen(this));
        registerHumbugModule(OldItemVelocity.class, new OldItemValues(this));
        registerHumbugModule(Elevators.class, new Elevators(this));
        registerHumbugModule(MobStacking.class, new MobStacking(this));
        registerHumbugModule(KitLimits.class, new KitLimits(this));
        registerHumbugModule(Knockback.class, new Knockback(this));
        registerHumbugModule(AntiAttributes.class, new AntiAttributes(this));

        modules.values().forEach(module -> {
            module.loadValues();

            if (module.isEnabled()) {
                module.start();
                Logger.print("Humbug: Started module '" + module.getName() + "'");
            }
        });
    }

    public void stop() {
        modules.values().forEach(HumbugModule::stop);
    }

    @Override
    public void reload() {
        modules.values().forEach(HumbugModule::loadValues);
    }

    public String getName() {
        return "Humbug";
    }

    public void registerHumbugModule(Class<? extends HumbugModule> clazz, HumbugModule module) {
        modules.put(clazz, module);
    }

    public HumbugModule getModule(Class<? extends HumbugModule> clazz) {
        return getModules().get(clazz);
    }
}