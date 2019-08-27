package com.playares.services.chatrestrictions;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.event.ProcessedChatEvent;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.service.AresService;
import com.playares.commons.bukkit.util.Scheduler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.UUID;

/**
 * Adds chat restrictions for players who do not meet the proper permission levels
 */
public final class ChatRestrictionService implements AresService, Listener {
    /** Owning Plugin **/
    @Getter public final AresPlugin owner;
    /** If true, chat cooldowns are enforced **/
    @Getter @Setter public boolean chatCooldownsEnabled;
    /** If true, whitelisted links are enforced **/
    @Getter @Setter public boolean whitelistedLinksEnabled;
    /** Stores Player UUIDs that have recently chatted **/
    @Getter public final List<UUID> recentChatters;
    /** Stores a list of all whitelisted links **/
    @Getter public final List<String> whitelistedLinks;
    /** Cooldown for chat messages **/
    @Getter @Setter public int chatCooldown;

    public ChatRestrictionService(AresPlugin owner) {
        this.owner = owner;
        this.recentChatters = Lists.newArrayList();
        this.whitelistedLinks = Lists.newArrayList();
        this.chatCooldown = 5;
    }

    private void load() {
        final YamlConfiguration config = getOwner().getConfig("chat-restrictions");

        if (config == null) {
            Logger.error("Failed to find chat-restrictions.yml");
            return;
        }

        whitelistedLinksEnabled = config.getBoolean("whitelisted-links.enabled");
        whitelistedLinks.addAll(config.getStringList("whitelisted-links.allowed"));
        chatCooldownsEnabled = config.getBoolean("chat-cooldown.enabled");
        chatCooldown = config.getInt("chat-cooldown.duration");
    }

    public void start() {
        load();
        registerListener(this);
    }

    public void stop() {
        recentChatters.clear();
        ProcessedChatEvent.getHandlerList().unregister(this);
    }

    @Override
    public void reload() {
        load();
    }

    public String getName() {
        return "Chat Restrictions";
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onProcessedChat(ProcessedChatEvent event) {
        final Player player = event.getPlayer();
        final UUID uniqueId = player.getUniqueId();
        final String message = event.getMessage();
        final String[] split = message.split(" ");

        if (player.hasPermission("chatrestrict.bypass.cooldown") && player.hasPermission("chatrestrict.bypass.links")) {
            return;
        }

        if (!player.hasPermission("chatrestrict.bypass.cooldown") && recentChatters.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Non-premium players can only talk in chat every " + ChatColor.YELLOW + chatCooldown + " seconds" + ChatColor.RED + ". Purchase a rank at " + ChatColor.AQUA +
                    "https://store.playares.com" + ChatColor.RED + " to bypass this restriction");

            event.setCancelled(true);
            return;
        }

        if (!player.hasPermission("chatrestrict.bypass.links")) {
            for (String str : split) {
                if (isBlacklistedLink(str)) {

                    player.sendMessage(ChatColor.RED + "This type of link is blacklisted for non-premium users. Purchase a rank at " + ChatColor.AQUA +
                            "https://store.playares.com" + ChatColor.RED + " to bypass this restriction");

                    event.setCancelled(true);
                    return;
                }
            }
        }

        recentChatters.add(uniqueId);

        new Scheduler(getOwner()).sync(() -> recentChatters.remove(uniqueId)).delay(20 * chatCooldown).run();
    }

    /**
     * Returns true if the provided string is a blacklisted link
     * @param message Message
     * @return True if blacklisted
     */
    private boolean isBlacklistedLink(String message) {
        final boolean match = message.matches("^(http://www\\.|https://www\\.|http://|https://)?[a-z0-9]+([\\-.][a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(/.*)?$");

        for (String whitelisted : whitelistedLinks) {
            if (message.contains(whitelisted)) {
                return false;
            }
        }

        return match;
    }
}