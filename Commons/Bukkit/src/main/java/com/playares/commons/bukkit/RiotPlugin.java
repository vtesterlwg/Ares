package com.playares.commons.bukkit;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.collect.Maps;
import com.playares.commons.base.connect.mongodb.MongoDB;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.service.RiotService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public abstract class RiotPlugin extends JavaPlugin {
    @Getter
    public Map<Class<? extends RiotService>, RiotService> services;

    @Getter
    public MongoDB mongo;

    @Getter
    public PaperCommandManager commandManager;

    @Getter
    public ProtocolManager protocol;

    /**
     * Registers a new Riot Service
     * @param service Riot Service
     */
    public void registerService(RiotService service) {
        if (services == null) {
            services = Maps.newHashMap();
        }

        services.put(service.getClass(), service);
        Logger.print("Registered new Riot Service: " + service.getName());
    }

    /**
     * Registers a new MongoDB instance
     * @param mongo MongoDB Instance
     */
    public void registerMongo(MongoDB mongo) {
        this.mongo = mongo;
        Logger.print("Registered new Mongo Database Instance");
    }

    /**
     * Register a new listener
     * @param listener Listener
     */
    public void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    /**
     * Register a new command manager
     * @param commandManager CommandManager
     */
    public void registerCommandManager(PaperCommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * Register a new Protocol manager
     * @param protocol ProtocolManager
     */
    public void registerProtocol(ProtocolManager protocol) {
        this.protocol = protocol;
    }

    /**
     * Register a new command
     * @param command Command
     */
    public void registerCommand(BaseCommand command) {
        if (this.commandManager == null) {
            throw new NullPointerException("Command Manager was not initialized");
        }

        this.commandManager.registerCommand(command);
    }

    /**
     * Start all services
     */
    protected void startServices() {
        services.values().forEach(service -> {
            service.start();
            Logger.print("Service Started: " + service.getName());
        });
    }

    /**
     * Stop all services
     */
    public void stopServices() {
        services.values().forEach(service -> {
            service.stop();
            Logger.print("Service Stopped: " + service.getName());
        });
    }

    /**
     * Obtain a registered service
     * @param clazz Class Type
     * @return Riot Service
     */
    public RiotService getService(Class<? extends RiotService> clazz) {
        if (!services.containsKey(clazz)) {
            return null;
        }

        return services.get(clazz);
    }

    /**
     * Return a Yaml Configuration
     * @param name File Name
     * @return YamlConfiguration
     */
    public YamlConfiguration getConfig(String name) {
        final File file = new File(getDataFolder() + "/" + name + ".yml");

        if (!file.exists()) {
            createConfig(name);
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Create a new Yaml Configuration
     * @param name File Name
     */
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

    /**
     * Save a Yaml Configuration
     * @param name File Name
     * @param config Config File
     */
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