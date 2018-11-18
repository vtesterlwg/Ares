package com.riotmc.services.profiles;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.client.model.Filters;
import com.playares.commons.base.promise.Promise;
import com.playares.commons.base.util.IPS;
import com.playares.commons.bukkit.RiotPlugin;
import com.playares.commons.bukkit.event.ProcessedChatEvent;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.service.RiotService;
import com.playares.commons.bukkit.util.Scheduler;
import com.riotmc.services.profiles.command.IgnoreCommand;
import com.riotmc.services.profiles.command.MessageCommand;
import com.riotmc.services.profiles.command.SettingsCommand;
import com.riotmc.services.profiles.data.RiotProfile;
import com.riotmc.services.profiles.data.RiotProfileDAO;
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

public final class ProfileService implements RiotService, Listener {
    @Getter
    public RiotPlugin owner;

    @Getter
    public Set<RiotProfile> profiles;

    @Getter
    public final MenuHandler menuHandler;

    @Getter
    public final IgnoreHandler ignoreHandler;

    @Getter
    public final MessageHandler messageHandler;

    public ProfileService(RiotPlugin owner) {
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

        profiles.forEach(profile -> RiotProfileDAO.insertProfile(getOwner().getMongo(), profile));
    }

    public RiotProfile getProfile(UUID uniqueId) {
        return profiles.stream().filter(profile -> profile.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public RiotProfile getProfile(String username) {
        return profiles.stream().filter(profile -> profile.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    public void getProfile(UUID uniqueId, Promise<RiotProfile> promise) {
        if (getProfile(uniqueId) != null) {
            promise.ready(getProfile(uniqueId));
            return;
        }

        new Scheduler(getOwner()).async(() -> {
            final RiotProfile profile = RiotProfileDAO.getProfile(getOwner().getMongo(), Filters.eq("id", uniqueId));

            new Scheduler(getOwner()).sync(() -> promise.ready(profile)).run();
        }).run();
    }

    public void getProfile(String username, Promise<RiotProfile> promise) {
        if (getProfile(username) != null) {
            promise.ready(getProfile(username));
            return;
        }

        new Scheduler(getOwner()).async(() -> {
            final RiotProfile profile = RiotProfileDAO.getProfile(getOwner().getMongo(), Filters.eq("username", username));

            new Scheduler(getOwner()).sync(() -> promise.ready(profile)).run();
        }).run();
    }

    public RiotProfile getProfileBlocking(UUID uniqueId) {
        if (getProfile(uniqueId) != null) {
            return getProfile(uniqueId);
        }

        return RiotProfileDAO.getProfile(getOwner().getMongo(), Filters.eq("id", uniqueId));
    }

    public RiotProfile getProfileBlocking(String username) {
        if (getProfile(username) != null) {
            return getProfile(username);
        }

        return RiotProfileDAO.getProfile(getOwner().getMongo(), Filters.eq("username", username));
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        final UUID uniqueId = event.getUniqueId();
        final String username = event.getName();
        final String address = event.getAddress().getHostAddress();
        boolean update = false;
        RiotProfile profile = RiotProfileDAO.getProfile(getOwner().getMongo(), Filters.eq("id", uniqueId));

        if (profile == null) {
            profile = new RiotProfile(uniqueId, username);
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
            RiotProfileDAO.insertProfile(getOwner().getMongo(), profile);
        }

        profiles.add(profile);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final RiotProfile profile = getProfile(player.getUniqueId());

        if (profile == null) {
            return;
        }

        profiles.remove(profile);

        new Scheduler(getOwner()).async(() -> RiotProfileDAO.insertProfile(getOwner().getMongo(), profile)).run();
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onProcessedChat(ProcessedChatEvent event) {
        final Player player = event.getPlayer();
        final RiotProfile profile = getProfile(player.getUniqueId());
        final List<Player> toRemove = Lists.newArrayList();

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
            final RiotProfile viewerProfile = getProfile(recipient.getUniqueId());

            if (viewerProfile.getSettings().isHidingGlobalChat()) {
                toRemove.add(recipient);
                continue;
            }

            if (profile.getSettings().isIgnoring(recipient.getUniqueId()) || viewerProfile.getSettings().isIgnoring(player.getUniqueId())) {
                toRemove.add(recipient);
            }
        }

        event.getRecipients().removeAll(toRemove);
    }
}