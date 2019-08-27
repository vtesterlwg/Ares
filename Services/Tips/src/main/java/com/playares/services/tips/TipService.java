package com.playares.services.tips;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.service.AresService;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.profiles.ProfileService;
import com.playares.services.profiles.data.AresProfile;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Queue;

public final class TipService implements AresService {
    @Getter public final AresPlugin owner;
    @Getter @Setter public int messageInterval;
    @Getter @Setter public String prefix;
    @Getter public final List<String> messages;
    @Getter public final Queue<String> messageQueue;
    @Getter @Setter public BukkitTask messagerTask;

    public TipService(AresPlugin owner) {
        this.owner = owner;
        this.messages = Lists.newArrayList();
        this.messageQueue = Queues.newConcurrentLinkedQueue();
    }

    @Override
    public void start() {
        load();
    }

    @Override
    public void stop() {
        if (messagerTask != null) {
            messagerTask.cancel();
            messagerTask = null;
        }

        messages.clear();
        messageQueue.clear();
    }

    @Override
    public void reload() {
        load();
    }

    @Override
    public String getName() {
        return "Tips";
    }

    private String pullMessage() {
        if (messageQueue.isEmpty()) {
            messageQueue.addAll(messages);
        }

        return messageQueue.remove();
    }

    private void load() {
        if (messagerTask != null) {
            messagerTask.cancel();
            messagerTask = null;
        }

        if (!messages.isEmpty()) {
            messages.clear();
            Logger.warn("Cleared messages while reloading " + getName());
        }

        if (!messageQueue.isEmpty()) {
            messageQueue.clear();
            Logger.warn("Cleared message queue while reloading " + getName());
        }

        final YamlConfiguration config = getOwner().getConfig("tips");

        messageInterval = config.getInt("settings.broadcast-interval");
        prefix = ChatColor.translateAlternateColorCodes('&', config.getString("settings.prefix"));

        for (String message : config.getStringList("messages")) {
            messages.add(ChatColor.translateAlternateColorCodes('&', message));
        }

        Logger.print("Loaded " + messages.size() + " tips");

        messagerTask = new Scheduler(getOwner()).sync(() -> {
            final ProfileService profileService = (ProfileService)getOwner().getService(ProfileService.class);

            if (profileService == null) {
                Logger.error("Failed to obtain Profile Service while issuing a broadcast");
                return;
            }

            final String message = pullMessage();

            Bukkit.getOnlinePlayers().forEach(player -> {
                final AresProfile profile = profileService.getProfile(player.getUniqueId());

                if (!profile.getSettings().isHidingTips()) {
                    player.sendMessage(prefix + ChatColor.RESET + message);
                }
            });

        }).repeat(getMessageInterval() * 20, getMessageInterval() * 20).run();
    }
}
