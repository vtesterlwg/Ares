package com.playares.factions.factions.handlers;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.factions.Faction;
import com.playares.factions.factions.FactionManager;
import com.playares.factions.factions.PlayerFaction;
import com.playares.factions.factions.ServerFaction;
import com.playares.factions.timers.FactionTimer;
import com.playares.factions.timers.cont.faction.DTRFreezeTimer;
import com.playares.services.profiles.ProfileService;
import com.playares.services.profiles.data.AresProfile;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class FactionDisplayHandler {
    @Getter
    public final FactionManager manager;

    public FactionDisplayHandler(FactionManager manager) {
        this.manager = manager;
    }

    public void prepareFactionInfo(Player viewer, String name, SimplePromise promise) {
        final Player foundPlayer = Bukkit.getPlayer(name);

        manager.getFactionByPlayer(name, faction -> {
            if (faction == null) {
                final Faction byName = manager.getFactionByName(name);

                if (byName == null) {
                    promise.failure("Faction not found");
                    return;
                }

                displayFactionInfo(viewer, byName);
                promise.success();
                return;
            }

            displayFactionInfo(viewer, faction);
            promise.success();
        });
    }

    public void displayFactionInfo(Player viewer, Faction faction) {
        final ProfileService profileService = (ProfileService)manager.getPlugin().getService(ProfileService.class);
        final String spacer = ChatColor.RESET + " " + ChatColor.RESET + " " + ChatColor.YELLOW + " - " + ChatColor.RESET;

        if (faction instanceof ServerFaction) {
            final ServerFaction sf = (ServerFaction)faction;

            viewer.sendMessage(ChatColor.YELLOW + "--------------------" + ChatColor.GOLD + "[ " + ChatColor.WHITE + sf.getDisplayName() + ChatColor.GOLD + " ]" + ChatColor.YELLOW + "--------------------");

            viewer.sendMessage(spacer + ChatColor.DARK_PURPLE + StringUtils.capitaliseAllWords(sf.getFlag().name().toLowerCase().replace("_", " ")));

            viewer.sendMessage(spacer + ChatColor.GOLD + "Located At" + ChatColor.YELLOW + ": " +
                    ChatColor.BLUE + (int)(Math.round(sf.getLocation().getX())) + ChatColor.YELLOW + ", " +
                    ChatColor.BLUE + (int)(Math.round(sf.getLocation().getY())) + ChatColor.YELLOW + ", " +
                    ChatColor.BLUE + (int)(Math.round(sf.getLocation().getZ())) + ChatColor.YELLOW + ", " +
                    ChatColor.BLUE + StringUtils.capitaliseAllWords(sf.getLocation().getBukkit().getWorld().getEnvironment().name().toLowerCase().replace("_", " ")));

            viewer.sendMessage(ChatColor.YELLOW + "------------------------------------------------");

            return;
        }

        final PlayerFaction pf = (PlayerFaction)faction;
        final String dtr = String.format("%.2f", pf.getDeathsTilRaidable());
        String formattedDTR;

        if (pf.getDeathsTilRaidable() >= manager.getPlugin().getFactionConfig().getFactionMaxDTR()) {
            formattedDTR = ChatColor.GREEN + dtr + " (Max)";
        } else if (pf.isRaidable()) {
            formattedDTR = ChatColor.DARK_RED + dtr + " (Raid-able)";
        } else if (pf.isFrozen()) {
            formattedDTR = ChatColor.GRAY + dtr + " (Frozen)";
        } else {
            formattedDTR = ChatColor.YELLOW + dtr + " (Regenerating)";
        }

        viewer.sendMessage(ChatColor.YELLOW + "--------------------" + ChatColor.GOLD + "[ " + ChatColor.WHITE + faction.getName() + ChatColor.GOLD + " ]" + ChatColor.YELLOW + "--------------------");

        if (pf.getHome() != null) {
            viewer.sendMessage(spacer + ChatColor.GOLD + "Home" + ChatColor.YELLOW + ": " +
                    ChatColor.BLUE + (int)(Math.round(pf.getHome().getX())) + ChatColor.YELLOW + ", " +
                    ChatColor.BLUE + (int)(Math.round(pf.getHome().getY())) + ChatColor.YELLOW + ", " +
                    ChatColor.BLUE + (int)(Math.round(pf.getHome().getZ())));
        }

        viewer.sendMessage(spacer + ChatColor.GOLD + "Balance" + ChatColor.YELLOW + ": " + ChatColor.BLUE + "$" + String.format("%.2f", pf.getBalance()));
        viewer.sendMessage(spacer + ChatColor.GOLD + "Deaths Until Raid-able" + ChatColor.YELLOW + ": " + formattedDTR);

        if (pf.isFrozen()) {
            final DTRFreezeTimer timer = (DTRFreezeTimer)pf.getTimer(FactionTimer.FactionTimerType.FREEZE);
            viewer.sendMessage(spacer + ChatColor.GOLD + "Frozen" + ChatColor.YELLOW + ": " + ChatColor.BLUE + Time.convertToRemaining(timer.getRemaining()));
        }

        viewer.sendMessage(spacer + ChatColor.GOLD + "Re-invites" + ChatColor.YELLOW + ": " + ChatColor.BLUE + pf.getReinvites());

        new Scheduler(manager.getPlugin()).async(() -> {
            final Map<PlayerFaction.FactionRank, List<String>> namesByRank = Maps.newHashMap();

            for (PlayerFaction.FactionRank rank : PlayerFaction.FactionRank.values()) {
                final List<String> usernames = Lists.newArrayList();

                for (PlayerFaction.FactionProfile member : pf.getMembersByRank(rank)) {
                    final AresProfile profile = profileService.getProfileBlocking(member.getUniqueId());

                    if (profile != null) {
                        usernames.add(profile.getUsername());
                    }
                }

                namesByRank.put(rank, usernames);
            }

            new Scheduler(manager.getPlugin()).sync(() -> {
                final Map<PlayerFaction.FactionRank, List<String>> formattedNames = Maps.newHashMap();

                for (PlayerFaction.FactionRank rank : namesByRank.keySet()) {
                    final List<String> names = namesByRank.get(rank);
                    final List<String> formatted = Lists.newArrayList();

                    names.sort(Comparator.comparing(name -> Bukkit.getPlayer(name) != null));

                    for (String name : names) {
                        if (Bukkit.getPlayer(name) != null) {
                            formatted.add(ChatColor.GREEN + name);
                        } else {
                            formatted.add(ChatColor.GRAY + name);
                        }
                    }

                    Collections.reverse(formatted);

                    formattedNames.put(rank, formatted);
                }

                final List<String> leaders = formattedNames.get(PlayerFaction.FactionRank.LEADER);
                final List<String> coLeaders = formattedNames.get(PlayerFaction.FactionRank.CO_LEADER);
                final List<String> officers = formattedNames.get(PlayerFaction.FactionRank.OFFICER);
                final List<String> members = formattedNames.get(PlayerFaction.FactionRank.MEMBER);

                if (!leaders.isEmpty()) {
                    viewer.sendMessage(spacer + ChatColor.GOLD + "Leader" + ChatColor.YELLOW + ": " + Joiner.on(ChatColor.YELLOW + ", ").join(leaders));
                }

                if (!coLeaders.isEmpty()) {
                    viewer.sendMessage(spacer + ChatColor.GOLD + "Co-Leader" + ChatColor.YELLOW + ": " + Joiner.on(ChatColor.YELLOW + ", ").join(coLeaders));
                }

                if (!officers.isEmpty()) {
                    viewer.sendMessage(spacer + ChatColor.GOLD + "Officers" + ChatColor.YELLOW + ": " + Joiner.on(ChatColor.YELLOW + ", ").join(officers));
                }

                if (!members.isEmpty()) {
                    viewer.sendMessage(spacer + ChatColor.GOLD + "Members" + ChatColor.YELLOW + ": " + Joiner.on(ChatColor.YELLOW + ", ").join(members));
                }

                viewer.sendMessage(ChatColor.YELLOW + "------------------------------------------------");
            }).run();
        }).run();
    }
}
