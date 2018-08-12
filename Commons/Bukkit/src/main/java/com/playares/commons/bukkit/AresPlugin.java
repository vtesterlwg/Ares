package com.playares.commons.bukkit;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.collect.Maps;
import com.playares.commons.base.connect.mongodb.MongoDB;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.service.AresService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public abstract class AresPlugin extends JavaPlugin {
    @Getter
    public Map<Class<? extends AresService>, AresService> services;

    @Getter
    public MongoDB mongo;

    @Getter
    public PaperCommandManager commandManager;

    @Getter
    public ProtocolManager protocol;

    public void registerService(AresService service) {
        if (services == null) {
            services = Maps.newHashMap();
        }

        services.put(service.getClass(), service);
        Logger.print("Registered new Ares Service: " + service.getName());
    }

    public void registerMongo(MongoDB mongo) {
        this.mongo = mongo;
        Logger.print("Registered new Mongo Database Instance");
    }

    public void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    public void registerCommandManager(PaperCommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public void registerProtocol(ProtocolManager protocol) {
        this.protocol = protocol;
    }

    public void registerCommand(BaseCommand command) {
        if (this.commandManager == null) {
            throw new NullPointerException("Command Manager was not initialized");
        }

        this.commandManager.registerCommand(command);
    }

    public void startServices() {
        services.values().forEach(service -> {
            service.start();
            Logger.print("Service Started: " + service.getName());
        });
    }

    public void stopServices() {
        services.values().forEach(service -> {
            service.stop();
            Logger.print("Service Stopped: " + service.getName());
        });
    }

    public AresService getService(Class<? extends AresService> clazz) {
        if (!services.containsKey(clazz)) {
            return null;
        }

        return services.get(clazz);
    }

    public YamlConfiguration getConfig(String name) {
        final File file = new File(getDataFolder() + "/" + name + ".yml");

        if (!file.exists()) {
            createConfig(name);
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    public void createConfig(String name) {
        final File file = new File(getDataFolder() + "/" + name + ".yml");

        if (file.exists()) {
            return;
        }

        if (getDataFolder().mkdirs()) {
            Logger.print("Created directory");
        }

        try {
            if (file.createNewFile()) {
                Logger.print("Created file '" + name + ".yml'");
            }
        } catch (IOException ex) {
            Logger.error("Failed to create '" + name + ".yml'");
            return;
        }

        saveResource(name + ".yml", true);
    }

    public void saveConfig(String name, YamlConfiguration config) {
        final File file = new File(getDataFolder() + "/" + name + ".yml");

        if (!file.exists()) {
            Logger.warn("Couldn't find file '" + name + ".yml'");
            createConfig(name);
        }

        try {
            config.save(file);
        } catch (IOException ex) {
            Logger.error("Failed to save file '" + name + ".yml'", ex);
        }
    }
}