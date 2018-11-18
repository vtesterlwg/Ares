package com.riotmc.factions.listener;

import com.riotmc.commons.bukkit.event.ProcessedChatEvent;
import com.riotmc.factions.Factions;
import com.riotmc.factions.addons.stats.StatsAddon;
import com.riotmc.factions.factions.PlayerFaction;
import com.riotmc.factions.players.FactionPlayer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ChatListener implements Listener {
    @Getter
    public final Factions plugin;

    public ChatListener(Factions plugin) {
        this.plugin = plugin;
    }

    private String getPublicFormat(PlayerFaction faction, int elo, String displayName, String message, Player receiver) {
        if (faction == null) {
            return ChatColor.BLUE + "[" + elo + "]" + ChatColor.RESET + displayName + ChatColor.RESET + ": " + message;
        }

        if (faction.getMember(receiver.getUniqueId()) != null) {
            return ChatColor.DARK_GREEN + "[" + faction.getName() + "]" + ChatColor.BLUE + "[" + elo + "]" + ChatColor.RESET +
                    displayName + ChatColor.RESET + ": " + message;
        }

        return ChatColor.GOLD + "[" + ChatColor.YELLOW + faction.getName() + ChatColor.GOLD + "]" + ChatColor.BLUE + "[" + elo + "]" + ChatColor.RESET +
                displayName + ChatColor.RESET + ": " + message;
    }

    private String getFactionFormat(String displayName, String message) {
        return ChatColor.DARK_GREEN + "(" + ChatColor.GOLD + "Faction Chat" + ChatColor.DARK_GREEN + ") " + ChatColor.RESET + displayName + ChatColor.DARK_GREEN + ": " + message;
    }

    private String getOfficerFormat(String displayName, String message) {
        return ChatColor.RED + "(" + ChatColor.GOLD + "Officer Chat" + ChatColor.RED + ") " + ChatColor.RESET + displayName + ChatColor.YELLOW + ": " + message;
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onChat(ProcessedChatEvent event) {
        final StatsAddon statsAddon = (StatsAddon)plugin.getAddonManager().getAddon(StatsAddon.class);
        final FactionPlayer factionPlayer = plugin.getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        final PlayerFaction faction = plugin.getFactionManager().getFactionByPlayer(event.getPlayer().getUniqueId());
        final boolean admin = event.getPlayer().hasPermission("factions.admin");
        final int elo = (factionPlayer != null && statsAddon != null) ? statsAddon.getStatsManager().getELO(factionPlayer) : 0;

        event.setCancelled(true);

        if (faction != null) {
            final PlayerFaction.FactionProfile profile = faction.getMember(event.getPlayer().getUniqueId());

            if (profile != null) {
                if (profile.getChannel().equals(PlayerFaction.ChatChannel.PUBLIC) && !event.getMessage().startsWith("@")) {
                    event.getRecipients().forEach(p -> p.sendMessage(getPublicFormat(faction, elo, event.getDisplayName(), event.getMessage(), p)));
                    return;
                }

                if (event.getMessage().startsWith("!") && !profile.getChannel().equals(PlayerFaction.ChatChannel.PUBLIC)) {
                    event.getRecipients().forEach(p -> p.sendMessage(getPublicFormat(faction, elo, event.getDisplayName(), event.getMessage().replaceFirst("!", ""), p)));
                    return;
                }

                if (profile.getChannel().equals(PlayerFaction.ChatChannel.FACTION) && !event.getMessage().startsWith("!")) {
                    event.getRecipients()
                            .stream()
                            .filter(p -> faction.getMember(p.getUniqueId()) != null)
                            .forEach(p -> p.sendMessage(getFactionFormat(event.getDisplayName(), event.getMessage())));

                    return;
                }

                if (event.getMessage().startsWith("@") && !profile.getChannel().equals(PlayerFaction.ChatChannel.FACTION)) {
                    event.getRecipients()
                            .stream()
                            .filter(p -> faction.getMember(p.getUniqueId()) != null)
                            .forEach(p -> p.sendMessage(getFactionFormat(event.getDisplayName(), event.getMessage().replaceFirst("@", ""))));

                    return;
                }

                if (profile.getChannel().equals(PlayerFaction.ChatChannel.OFFICER) && !event.getMessage().startsWith("!") && (!profile.getRank().equals(PlayerFaction.FactionRank.MEMBER) || admin)) {
                    event.getRecipients()
                            .stream()
                            .filter(p -> faction.getMember(p.getUniqueId()) != null && (!faction.getMember(p.getUniqueId()).getRank().equals(PlayerFaction.FactionRank.MEMBER) || admin))
                            .forEach(p -> p.sendMessage(getOfficerFormat(event.getDisplayName(), event.getMessage())));

                    return;
                }

                return;
            }
        }

        event.getRecipients().forEach(p -> p.sendMessage(getPublicFormat(null, elo, event.getDisplayName(), event.getMessage(), p)));
    }
}
