package com.riotmc.commons.bungee;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BungeeCommandManager;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.riotmc.commons.base.connect.mongodb.MongoDB;
import com.riotmc.commons.bungee.logging.Logger;
import com.riotmc.commons.bungee.service.RiotService;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.Map;

public abstract class RiotProxy extends Plugin {
    @Getter
    public Map<Class<? extends RiotService>, RiotService> services;

    @Getter
    public MongoDB mongo;

    @Getter
    public BungeeCommandManager commandManager;

    public void registerService(RiotService service) {
        if (services == null) {
            services = Maps.newHashMap();
        }

        services.put(service.getClass(), service);
        Logger.print("Registered new Riot Service: " + service.getName());
    }

    public void registerListener(Listener listener) {
        getProxy().getPluginManager().registerListener(this, listener);
    }

    public void registerCommandManager(BungeeCommandManager commandManager) {
        this.commandManager = commandManager;
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

    public Configuration getConfig(String name) {
        final File file = new File(getDataFolder() + "/" + name + ".yml");

        if (!file.exists()) {
            createConfig(name);
        }

        try {
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @SuppressWarnings("UnstableApiUsage")
    public void createConfig(String name) {
        final File file = new File(getDataFolder() + "/" + name + ".yml");

        if (file.exists()) {
            return;
        }

        if (file.mkdirs()) {
            Logger.print("Created directory");
        }

        try {
            if (file.createNewFile()) {
                Logger.print("Created file '" + name + ".yml'");
            }

            try (InputStream input = getResourceAsStream(name + ".yml")) {
                final OutputStream output = new FileOutputStream(file);

                ByteStreams.copy(input, output);
            }
        } catch (IOException ex) {
            Logger.error("Failed to create '" + name + ".yml'");
        }
    }

    public void saveConfig(String name, Configuration config) {
        final File file = new File(getDataFolder() + "/" + name + ".yml");

        if (!file.exists()) {
            Logger.warn("Couldn't find file '" + name + ".yml'");
            createConfig(name);
        }


        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
        } catch (IOException ex) {
            Logger.error("Failed to save file '" + name + ".yml'", ex);
        }
    }
}