package com.playares.services.humbug;

import com.google.common.collect.Sets;
import com.playares.commons.bukkit.RiotPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.service.RiotService;
import com.playares.services.humbug.command.HumbugCommand;
import com.playares.services.humbug.features.HumbugModule;
import com.playares.services.humbug.features.cont.*;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Set;

public final class HumbugService implements RiotService {
    @Getter
    public final RiotPlugin owner;

    @Getter
    protected Set<HumbugModule> modules;

    @Getter
    protected YamlConfiguration humbugConfig;

    public HumbugService(RiotPlugin owner) {
        this.owner = owner;
        this.modules = Sets.newHashSet();
    }

    public void start() {
        this.humbugConfig = owner.getConfig("humbug");

        registerCommand(new HumbugCommand(this));

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

        this.modules.forEach(module -> {
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

    public void reload() {
        this.humbugConfig = owner.getConfig("humbug");
        this.modules.forEach(HumbugModule::loadValues);
    }

    public String getName() {
        return "Humbug";
    }

    public void registerHumbugModule(HumbugModule module) {
        this.modules.add(module);
    }
}