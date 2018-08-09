package com.playares.services.humbug;

import com.google.common.collect.Sets;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.service.AresService;
import com.playares.services.humbug.command.HumbugCommand;
import com.playares.services.humbug.features.HumbugModule;
import com.playares.services.humbug.features.cont.*;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Set;

/**
 * TODO:
 * LogBlock - Will be it's own service
 * Disable Enderchests - Done
 * Disable Shulker Boxes - We'll only disable them outside faction claims
 * Disable Entity Block Greifing - Done
 * Disable placing boats on land - Not needed
 * Disable Cobblestone Generators - Done
 * Disable Fire Spread - Done
 * Disable breaking Mob Spawners - Done
 * Disable chorus fruit teleportation - Done
 * Disable attack cooldowns - Done
 * Fix item velocity being random as fuck - Done
 * Disable Fishing Rods attaching to players - Done
 * Toggleable 1.5 Potion Values - Done
 * Toggleable 1.8 Armor/Weapon Values - Done
 * Toggleable 1.8 Enderpearl Velocity - Done
 * Toggleable 1.8 Health Regeneration - Done
 * Toggleable XP Bonus when Mining ores with Fortune and Killing mobs with Looting - Done
 * Set base movement speed for horses
 * Fix players clipping through blocks when logging out on a vehicle - No longer an issue in 1.13
 * Add crafting recipe for XP bottles - Done
 * Add crafting recipe for Glistening Melons - Done
 * Add crafting recipe for Horse Armor - Done
 * Add crafting recipe for Saddles - Done
 * Add minecart elevators - Done
 * Spawn obsidian platform when entering the nether - Done
 * Remove nether portal packets after using a nether portal - Not needed
 * Fix splash potions by removing ticksLived check in EntityProjectile class (need to repull paper) - Done
 */
public final class HumbugService implements AresService {
    @Getter
    public final AresPlugin owner;

    @Getter
    protected Set<HumbugModule> modules;

    @Getter
    protected YamlConfiguration humbugConfig;

    public HumbugService(AresPlugin owner) {
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

        this.modules.forEach(module ->{
            if (module.isEnabled()) {
                module.loadValues();
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
    }

    public String getName() {
        return "Humbug";
    }

    public void registerHumbugModule(HumbugModule module) {
        this.modules.add(module);
    }
}