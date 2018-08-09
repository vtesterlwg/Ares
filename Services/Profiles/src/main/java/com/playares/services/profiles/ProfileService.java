package com.playares.services.profiles;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.promise.Promise;
import com.playares.commons.base.util.IPS;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.event.ProcessedChatEvent;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.service.AresService;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.profiles.command.IgnoreCommand;
import com.playares.services.profiles.command.MessageCommand;
import com.playares.services.profiles.command.SettingsCommand;
import com.playares.services.profiles.data.AresProfile;
import com.playares.services.profiles.data.AresProfileDAO;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ProfileService implements AresService, Listener {
    @Getter
    public AresPlugin owner;

    @Getter
    public Set<AresProfile> profiles;

    @Getter
    public final MenuHandler menuHandler;

    @Getter
    public final IgnoreHandler ignoreHandler;

    @Getter
    public final MessageHandler messageHandler;

    public ProfileService(AresPlugin owner) {
        this.owner = owner;
        this.profiles = Sets.newConcurrentHashSet();
        this.menuHandler = new MenuHandler(owner);
        this.ignoreHandler = new IgnoreHandler(this);
        this.messageHandler = new MessageHandler(this);
    }

    public String getName() {
        return "Profiles";
    }

    public void start() {
        registerListener(this);
        registerCommand(new SettingsCommand(this));
        registerCommand(new IgnoreCommand(this));
        registerCommand(new MessageCommand(this));
    }

    public void stop() {
        PlayerQuitEvent.getHandlerList().unregister(messageHandler);

        profiles.forEach(profile -> AresProfileDAO.insertProfile(getOwner().getMongo(), profile));
    }

    public AresProfile getProfile(UUID uniqueId) {
        return profiles.stream().filter(profile -> profile.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public AresProfile getProfile(String username) {
        return profiles.stream().filter(profile -> profile.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    public void getProfile(UUID uniqueId, Promise<AresProfile> promise) {
        if (getProfile(uniqueId) != null) {
            promise.ready(getProfile(uniqueId));
            return;
        }

        new Scheduler(getOwner()).async(() -> {
            final AresProfile profile = AresProfileDAO.getProfile(getOwner().getMongo(), Filters.eq("id", uniqueId));

            new Scheduler(getOwner()).sync(() -> promise.ready(profile)).run();
        }).run();
    }

    public void getProfile(String username, Promise<AresProfile> promise) {
        if (getProfile(username) != null) {
            promise.ready(getProfile(username));
            return;
        }

        new Scheduler(getOwner()).async(() -> {
            final AresProfile profile = AresProfileDAO.getProfile(getOwner().getMongo(), Filters.eq("username", username));

            new Scheduler(getOwner()).sync(() -> promise.ready(profile)).run();
        }).run();
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        final UUID uniqueId = event.getUniqueId();
        final String username = event.getName();
        final String address = event.getAddress().getHostAddress();
        boolean update = false;
        AresProfile profile = AresProfileDAO.getProfile(getOwner().getMongo(), Filters.eq("id", uniqueId));

        if (profile == null) {
            profile = new AresProfile(uniqueId, username);
        }

        if (!profile.getUsername().equals(username)) {
            Logger.print("Updated the username: " + profile.getUsername() + " -> " + username);
            profile.setUsername(username);
            update = true;
        }

        if (profile.getAddress() != IPS.toInt(address)) {
            Logger.print("Updated the IP-Address for " + profile.getUsername() + ": " + profile.getAddress() + " -> " + IPS.toInt(address));
            profile.setAddress(IPS.toInt(address));
            update = true;
        }

        if (update) {
            AresProfileDAO.insertProfile(getOwner().getMongo(), profile);
        }

        profiles.add(profile);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final AresProfile profile = getProfile(player.getUniqueId());

        if (profile == null) {
            return;
        }

        profiles.remove(profile);

        new Scheduler(getOwner()).async(() -> AresProfileDAO.insertProfile(getOwner().getMongo(), profile)).run();
    }

    @EventHandler
    public void onProcessedChat(ProcessedChatEvent event) {
        final Player player = event.getPlayer();
        final AresProfile profile = getProfile(player.getUniqueId());
        final List<UUID> toRemove = Lists.newArrayList();

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            return;
        }

        if (profile.getSettings().isHidingGlobalChat()) {
            player.sendMessage(ChatColor.RED + "You have global chat disabled. You can turn it back on in your global settings.");
            event.setCancelled(true);
        }

        if (player.hasPermission("chat.ignorebypass")) {
            return;
        }

        for (Player recipient : event.getRecipients()) {
            final AresProfile viewerProfile = getProfile(recipient.getUniqueId());

            if (viewerProfile.getSettings().isHidingGlobalChat()) {
                toRemove.add(recipient.getUniqueId());
                continue;
            }

            if (profile.getSettings().isIgnoring(recipient.getUniqueId()) || viewerProfile.getSettings().isIgnoring(player.getUniqueId())) {
                toRemove.add(recipient.getUniqueId());
            }
        }

        event.getRecipients().removeAll(toRemove);
    }
}