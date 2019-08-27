package com.playares.services.humbug;

import com.google.common.collect.Sets;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.service.AresService;
import com.playares.services.humbug.features.HumbugModule;
import com.playares.services.humbug.features.cont.*;
import lombok.Getter;

import java.util.Set;

public final class HumbugService implements AresService {
    @Getter public final AresPlugin owner;
    @Getter protected Set<HumbugModule> modules;

    public HumbugService(AresPlugin owner) {
        this.owner = owner;
        this.modules = Sets.newHashSet();
    }

    public void start() {
        registerHumbugModule(new OldPotions(this));
        registerHumbugModule(new OldItemVelocity(this));
        registerHumbugModule(new AntiGrief(this));
        registerHumbugModule(new OldSwordSwing(this));
        registerHumbugModule(new MemeItems(this));
        registerHumbugModule(new AntiGlitch(this));
        registerHumbugModule(new XPBonuses(this));
        registerHumbugModule(new CustomRecipes(this));
        registerHumbugModule(new CustomRecipes(this));
        registerHumbugModule(new OldRegen(this));
        registerHumbugModule(new OldItemValues(this));
        registerHumbugModule(new Elevators(this));
        registerHumbugModule(new MobStacking(this));
        registerHumbugModule(new KitLimits(this));
        registerHumbugModule(new Knockback(this));

        modules.forEach(module -> {
            module.loadValues();

            if (module.isEnabled()) {
                module.start();
                Logger.print("Humbug: Started module '" + module.getName() + "'");
            }
        });
    }

    public void stop() {
        this.modules.forEach(HumbugModule::stop);
    }

    @Override
    public void reload() {
        modules.forEach(module -> {
            module.loadValues();

            if (module.isEnabled()) {
                module.start();
                Logger.print("Humbug: Started module '" + module.getName() + "'");
            }
        });
    }

    public String getName() {
        return "Humbug";
    }

    public void registerHumbugModule(HumbugModule module) {
        this.modules.add(module);
    }
}