package com.riotmc.factions.factions.handlers;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.commons.base.util.Time;
import com.riotmc.commons.bukkit.item.ItemBuilder;
import com.riotmc.commons.bukkit.menu.ClickableItem;
import com.riotmc.commons.bukkit.menu.Menu;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.addons.stats.StatsAddon;
import com.riotmc.factions.factions.data.Faction;
import com.riotmc.factions.factions.data.PlayerFaction;
import com.riotmc.factions.factions.data.ServerFaction;
import com.riotmc.factions.factions.manager.FactionManager;
import com.riotmc.factions.timers.FactionTimer;
import com.riotmc.factions.timers.cont.faction.DTRFreezeTimer;
import com.riotmc.services.profiles.ProfileService;
import com.riotmc.services.profiles.data.RiotProfile;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class FactionDisplayHandler {
    /** Owning Manager **/
    @Getter public final FactionManager manager;

    public FactionDisplayHandler(FactionManager manager) {
        this.manager = manager;
    }

    /**
     * Updates the announcement for a faction+
     * @param player Player
     * @param text Message
     * @param promise Promise
     */
    public void updateAnnouncement(Player player, String text, SimplePromise promise) {
        final PlayerFaction faction = manager.getFactionByPlayer(player.getUniqueId());
        final boolean mod = player.hasPermission("factions.mod");

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        if (faction.getMember(player.getUniqueId()).getRank().equals(PlayerFaction.FactionRank.MEMBER) && !mod) {
            promise.failure("Members are not able to perform this action");
            return;
        }

        faction.updateAnnouncement(player, text);

        promise.success();
    }

    /**
     * Updates the rally for a faction
     * @param player Player
     * @param promise Promise
     */
    public void updateRally(Player player, SimplePromise promise) {
        final PlayerFaction faction = manager.getFactionByPlayer(player.getUniqueId());
        final boolean mod = player.hasPermission("factions.mod");

        if (faction == null) {
            promise.failure("You are not in a faction");
            return;
        }

        if (faction.getMember(player.getUniqueId()).getRank().equals(PlayerFaction.FactionRank.MEMBER) && !mod) {
            promise.failure("Members are not able to perform this action");
            return;
        }

        faction.updateRally(player);

        promise.success();
    }

    /**
     * Displays the faction list to the provided viewer
     * @param viewer Player
     * @param page Page
     * @param promise Promise
     */
    public void viewList(Player viewer, int page, SimplePromise promise) {
        final List<PlayerFaction> factions = Lists.newArrayList(manager.getPlayerFactions());

        factions.sort(Comparator.comparingInt(f -> f.getOnlineMembers().size()));

        int finishPos = page * 10;
        int startPos = finishPos - 9;

        if (startPos > factions.size()) {
            promise.failure("Page does not exist");
            return;
        }

        viewer.sendMessage(ChatColor.BLUE + "Faction List " + ChatColor.GOLD + "(" + ChatColor.YELLOW + "Page " + page + ChatColor.GOLD + ")");

        for (int i = startPos; i < finishPos; i++) {
            if (i >= factions.size()) {
                break;
            }

            final PlayerFaction faction = factions.get(i);

            if (faction == null) {
                finishPos++;
                continue;
            }

            viewer.sendMessage(new ComponentBuilder("" + i)
            .color(net.md_5.bungee.api.ChatColor.GOLD)
            .append(".")
            .color(net.md_5.bungee.api.ChatColor.YELLOW)
            .append(" " + faction.getName())
            .color(net.md_5.bungee.api.ChatColor.BLUE)
            .append(" | " + faction.getOnlineMembers().size() + " online | " + String.format("%.2f", faction.getDeathsTilRaidable()) + "DTR")
            .color(net.md_5.bungee.api.ChatColor.GRAY)
            .append(" [MORE INFO]")
            .color(net.md_5.bungee.api.ChatColor.AQUA)
            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f who " + faction.getName()))
            .create());
        }

        if (page > 1) {
            viewer.sendMessage(new ComponentBuilder("[")
            .color(net.md_5.bungee.api.ChatColor.GOLD)
            .append(" << ")
            .color(net.md_5.bungee.api.ChatColor.WHITE)
            .bold(true)
            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f list " + (page - 1)))
            .append("|")
            .color(net.md_5.bungee.api.ChatColor.YELLOW)
            .append(" >> ")
            .color(net.md_5.bungee.api.ChatColor.WHITE)
            .bold(true)
            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f list " + (page + 1)))
            .append("]")
            .color(net.md_5.bungee.api.ChatColor.GOLD)
            .create());
        } else {
            viewer.sendMessage(new ComponentBuilder("[")
            .color(net.md_5.bungee.api.ChatColor.GOLD)
            .append(" >> ")
            .color(net.md_5.bungee.api.ChatColor.WHITE)
            .bold(true)
            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f list " + (page + 1)))
            .append("]")
            .color(net.md_5.bungee.api.ChatColor.GOLD)
            .create());
        }

        promise.success();
    }

    /**
     * Prepares faction information for display
     * @param viewer Player
     * @param name Faction Name
     * @param promise Promise
     */
    public void prepareFactionInfo(Player viewer, String name, SimplePromise promise) {
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

    /**
     * Displays faction information
     * @param viewer Player
     * @param faction Faction
     */
    public void displayFactionInfo(Player viewer, Faction faction) {
        final ProfileService profileService = (ProfileService)manager.getPlugin().getService(ProfileService.class);
        final String spacer = ChatColor.RESET + " " + ChatColor.RESET + " " + ChatColor.YELLOW + " - " + ChatColor.RESET;
        final boolean mod = viewer.hasPermission("factions.mod");

        if (faction instanceof ServerFaction) {
            final ServerFaction sf = (ServerFaction)faction;

            viewer.sendMessage(ChatColor.YELLOW + "--------------------" + ChatColor.GOLD + "[ " + ChatColor.WHITE + sf.getDisplayName() + ChatColor.GOLD + " ]" + ChatColor.YELLOW + "--------------------");

            viewer.sendMessage(spacer + ChatColor.DARK_PURPLE + StringUtils.capitaliseAllWords(sf.getFlag().name().toLowerCase().replace("_", " ")));

            if (sf.getLocation() != null) {
                viewer.sendMessage(spacer + ChatColor.GOLD + "Located At" + ChatColor.YELLOW + ": " +
                        ChatColor.BLUE + (int)(Math.round(sf.getLocation().getX())) + ChatColor.YELLOW + ", " +
                        ChatColor.BLUE + (int)(Math.round(sf.getLocation().getY())) + ChatColor.YELLOW + ", " +
                        ChatColor.BLUE + (int)(Math.round(sf.getLocation().getZ())) + ChatColor.YELLOW + ", " +
                        ChatColor.BLUE + StringUtils.capitaliseAllWords(sf.getLocation().getBukkit().getWorld().getEnvironment().name().toLowerCase().replace("_", " ")));
            }

            viewer.sendMessage(ChatColor.YELLOW + "------------------------------------------------");

            return;
        }

        final StatsAddon addon = (StatsAddon)manager.getPlugin().getAddonManager().getAddon(StatsAddon.class);
        final PlayerFaction pf = (PlayerFaction)faction;
        final String dtr = String.format("%.2f", pf.getDeathsTilRaidable());
        final int elo = (addon != null) ? addon.getStatsManager().getELO(pf) : 0;
        String formattedDTR;

        if (pf.getDeathsTilRaidable() >= pf.getMaxDTR()) {
            formattedDTR = ChatColor.GREEN + dtr + " (Max)";
        } else if (pf.isRaidable()) {
            formattedDTR = ChatColor.DARK_RED + dtr + " (Raid-able)";
        } else if (pf.isFrozen()) {
            formattedDTR = ChatColor.GRAY + dtr + " (Frozen)";
        } else {
            formattedDTR = ChatColor.YELLOW + dtr + " (Regenerating)";
        }

        viewer.sendMessage(ChatColor.YELLOW + "--------------------" + ChatColor.GOLD + "[ " + ChatColor.WHITE + faction.getName() + ChatColor.GOLD + " ]" + ChatColor.YELLOW + "--------------------");

        if (pf.getAnnouncement() != null && (pf.isMember(viewer.getUniqueId()) || mod)) {
            viewer.sendMessage(spacer + ChatColor.GOLD + "Announcement" + ChatColor.YELLOW + ": " + ChatColor.LIGHT_PURPLE + pf.getAnnouncement());
        }

        if (pf.getRally() != null && (pf.isMember(viewer.getUniqueId()) || mod)) {
            viewer.sendMessage(spacer + ChatColor.GOLD + "Rally" + ChatColor.YELLOW + ": " +
                    ChatColor.BLUE + (int)(Math.round(pf.getRally().getX())) + ChatColor.YELLOW + ", " +
                    ChatColor.BLUE + (int)(Math.round(pf.getRally().getY())) + ChatColor.YELLOW + ", " +
                    ChatColor.BLUE + (int)(Math.round(pf.getRally().getZ())) + ChatColor.YELLOW + ", " +
                    ChatColor.BLUE + StringUtils.capitaliseAllWords(pf.getRally().getBukkit().getWorld().getEnvironment().name().toLowerCase().replace("_", " ")));
        }

        if (pf.getHome() != null) {
            viewer.sendMessage(spacer + ChatColor.GOLD + "Home" + ChatColor.YELLOW + ": " +
                    ChatColor.BLUE + (int)(Math.round(pf.getHome().getX())) + ChatColor.YELLOW + ", " +
                    ChatColor.BLUE + (int)(Math.round(pf.getHome().getY())) + ChatColor.YELLOW + ", " +
                    ChatColor.BLUE + (int)(Math.round(pf.getHome().getZ())));
        }

        viewer.sendMessage(new ComponentBuilder("")
                .append(TextComponent.fromLegacyText(spacer + ChatColor.GOLD + "Rating" + ChatColor.YELLOW + ": " + ChatColor.BLUE + elo))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Statistics")
                                .color(net.md_5.bungee.api.ChatColor.DARK_PURPLE)
                                .append("\n").color(net.md_5.bungee.api.ChatColor.RESET)
                                .append(TextComponent.fromLegacyText(ChatColor.GOLD + "Kills" + ChatColor.YELLOW + ": " + ChatColor.BLUE + pf.getStats().getKills()))
                                .append("\n").color(net.md_5.bungee.api.ChatColor.RESET)
                                .append(TextComponent.fromLegacyText(ChatColor.GOLD + "Deaths" + ChatColor.YELLOW + ": " + ChatColor.BLUE + pf.getStats().getDeaths()))
                                .append("\n").color(net.md_5.bungee.api.ChatColor.RESET)
                                .append(TextComponent.fromLegacyText(ChatColor.GOLD + "Minor Event Captures" + ChatColor.YELLOW + ": " + ChatColor.BLUE + pf.getStats().getMinorEventCaptures()))
                                .append("\n").color(net.md_5.bungee.api.ChatColor.RESET)
                                .append(TextComponent.fromLegacyText(ChatColor.GOLD + "Major Event Captures" + ChatColor.YELLOW + ": " + ChatColor.BLUE + pf.getStats().getMajorEventCaptures()))
                                .create()))
                .create());

        viewer.sendMessage(spacer + ChatColor.GOLD + "Balance" + ChatColor.YELLOW + ": " + ChatColor.BLUE + "$" + String.format("%.2f", pf.getBalance()));
        viewer.sendMessage(spacer + ChatColor.GOLD + "Deaths Until Raid-able" + ChatColor.YELLOW + ": " + formattedDTR);

        if (pf.isFrozen()) {
            final DTRFreezeTimer timer = (DTRFreezeTimer)pf.getTimer(FactionTimer.FactionTimerType.FREEZE);
            viewer.sendMessage(spacer + ChatColor.GOLD + "Frozen" + ChatColor.YELLOW + ": " + ChatColor.BLUE + Time.convertToRemaining(timer.getRemaining()));
        }

        viewer.sendMessage(spacer + ChatColor.GOLD + "Re-invites" + ChatColor.YELLOW + ": " + ChatColor.BLUE + pf.getReinvites());
        viewer.sendMessage(spacer + ChatColor.GOLD + "Online" + ChatColor.YELLOW + ": " + ChatColor.BLUE + pf.getOnlineMembers().size() + ChatColor.YELLOW + " / " + ChatColor.BLUE + pf.getMembers().size());

        new Scheduler(manager.getPlugin()).async(() -> {
            final Map<PlayerFaction.FactionRank, List<String>> namesByRank = Maps.newHashMap();

            for (PlayerFaction.FactionRank rank : PlayerFaction.FactionRank.values()) {
                final List<String> usernames = Lists.newArrayList();

                for (PlayerFaction.FactionProfile member : pf.getMembersByRank(rank)) {
                    final RiotProfile profile = profileService.getProfileBlocking(member.getUniqueId());

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

    /**
     * Displays the faction leaderboard for the provided category
     * @param player Player
     * @param category Category
     * @param promise Promise
     */
    public void displayLeaderboard(Player player, String category, SimplePromise promise) {
        final StatsAddon addon = (StatsAddon)manager.getPlugin().getAddonManager().getAddon(StatsAddon.class);
        final List<PlayerFaction> factions = Lists.newArrayList(manager.getPlayerFactions());
        final PlayerFaction personalFaction = manager.getFactionByPlayer(player.getUniqueId());

        if (addon == null) {
            promise.failure("Failed to obtain Stats Addon");
            return;
        }

        if (factions.isEmpty()) {
            promise.failure("There are no factions on the leaderboard");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            final String fancyCategory;

            if (category.equalsIgnoreCase("elo") || category.equalsIgnoreCase("rating") || category.equalsIgnoreCase("e") || category.equalsIgnoreCase("r")) {
                factions.sort(Comparator.comparingInt(f -> f.getStats().calculateELO(addon)));
                fancyCategory = "Rating";
            } else if (category.equalsIgnoreCase("kill") || category.equalsIgnoreCase("kills") || category.equalsIgnoreCase("k")) {
                factions.sort(Comparator.comparingInt(f -> f.getStats().getKills()));
                fancyCategory = "Kills";
            } else if (category.equalsIgnoreCase("death") || category.equalsIgnoreCase("deaths") || category.equalsIgnoreCase("d")) {
                factions.sort(Comparator.comparingInt(f -> f.getStats().getDeaths()));
                fancyCategory = "Deaths";
            } else if (category.equalsIgnoreCase("minorevent") || category.equalsIgnoreCase("minorevents") || category.equalsIgnoreCase("minor")) {
                factions.sort(Comparator.comparingInt(f -> f.getStats().getMinorEventCaptures()));
                fancyCategory = "Minor Event Captures";
            } else if (category.equalsIgnoreCase("majorevent") || category.equalsIgnoreCase("majorevents") || category.equalsIgnoreCase("major")) {
                factions.sort(Comparator.comparingInt(f -> f.getStats().getMajorEventCaptures()));
                fancyCategory = "Major Event Captures";
            } else {
                new Scheduler(manager.getPlugin()).sync(() -> promise.failure("Invalid category")).run();
                return;
            }

            Collections.reverse(factions);

            final Menu menu = new Menu(manager.getPlugin(), player, "Factions Leaderboard: " + fancyCategory, 6);
            final List<PlayerFaction> top = (factions.size() >= 3 ? factions.subList(0, 2) : factions.subList(0, factions.size()));

            new Scheduler(manager.getPlugin()).sync(() -> {
                int pos = 1;
                int slot = 10;

                for (PlayerFaction faction : top) {
                    final ItemBuilder builder = new ItemBuilder().setMaterial(Material.PLAYER_HEAD);
                    ChatColor color = ChatColor.RESET;

                    if (pos == 1) {
                        color = ChatColor.GOLD;
                    } else if (pos == 2) {
                        color = ChatColor.GRAY;
                    } else if (pos == 3) {
                        color = ChatColor.RED;
                    }

                    builder.setName(color + "#" + pos + ChatColor.RESET + " - " + ChatColor.YELLOW + faction.getName());

                    final List<String> lore = Lists.newArrayList();
                    lore.add(ChatColor.GOLD + "Rating: " + ChatColor.YELLOW + faction.getStats().calculateELO(addon));
                    lore.add(ChatColor.GOLD + "Kills: " + ChatColor.YELLOW + faction.getStats().getKills());
                    lore.add(ChatColor.GOLD + "Deaths: " + ChatColor.YELLOW + faction.getStats().getDeaths());
                    lore.add(ChatColor.GOLD + "Minor Event Captures: " + ChatColor.YELLOW + faction.getStats().getMinorEventCaptures());
                    lore.add(ChatColor.GOLD + "Major Event Captures: " + ChatColor.YELLOW + faction.getStats().getMajorEventCaptures());

                    builder.addLore(lore);

                    final ItemStack icon = builder.build();
                    final SkullMeta meta = (SkullMeta)icon.getItemMeta();
                    final int currentSlot = slot;

                    new Scheduler(manager.getPlugin()).async(() -> {
                        final PlayerFaction.FactionProfile leader = faction.getMembersByRank(PlayerFaction.FactionRank.LEADER).get(0);
                        final OfflinePlayer offlinePlayer = (leader != null) ? Bukkit.getOfflinePlayer(leader.getUniqueId()) : null;

                        new Scheduler(manager.getPlugin()).sync(() -> {
                            meta.setOwningPlayer(offlinePlayer);
                            icon.setItemMeta(meta);
                            menu.addItem(new ClickableItem(icon, currentSlot, click -> displayFactionInfo(player, faction)));
                        }).run();
                    }).run();

                    pos++;
                    slot += 3;
                }

                if (personalFaction != null) {
                    final ItemBuilder builder = new ItemBuilder().setMaterial(Material.PLAYER_HEAD);
                    final List<String> lore = Lists.newArrayList();
                    int personalPos = 1;

                    for (PlayerFaction faction : factions) {
                        if (faction.getUniqueId().equals(personalFaction.getUniqueId())) {
                            break;
                        }

                        personalPos++;
                    }

                    builder.setName(ChatColor.GREEN + "(You) " + ChatColor.RESET + "#" + personalPos + " - " + ChatColor.YELLOW + personalFaction.getName());
                    lore.add(ChatColor.GOLD + "Rating: " + ChatColor.YELLOW + personalFaction.getStats().calculateELO(addon));
                    lore.add(ChatColor.GOLD + "Kills: " + ChatColor.YELLOW + personalFaction.getStats().getKills());
                    lore.add(ChatColor.GOLD + "Deaths: " + ChatColor.YELLOW + personalFaction.getStats().getDeaths());
                    lore.add(ChatColor.GOLD + "Minor Event Captures: " + ChatColor.YELLOW + personalFaction.getStats().getMinorEventCaptures());
                    lore.add(ChatColor.GOLD + "Major Event Captures: " + ChatColor.YELLOW + personalFaction.getStats().getMajorEventCaptures());

                    builder.addLore(lore);

                    final ItemStack icon = builder.build();
                    final SkullMeta meta = (SkullMeta)icon.getItemMeta();

                    new Scheduler(manager.getPlugin()).async(() -> {
                        final PlayerFaction.FactionProfile leader = personalFaction.getMembersByRank(PlayerFaction.FactionRank.LEADER).get(0);
                        final OfflinePlayer offlinePlayer = (leader != null) ? Bukkit.getOfflinePlayer(leader.getUniqueId()) : null;

                        new Scheduler(manager.getPlugin()).sync(() -> {
                            meta.setOwningPlayer(offlinePlayer);
                            icon.setItemMeta(meta);
                            menu.addItem(new ClickableItem(icon, 40, click -> displayFactionInfo(player, personalFaction)));
                        }).run();
                    }).run();
                }

                final ItemStack goldBorder = new ItemBuilder().setMaterial(Material.YELLOW_STAINED_GLASS_PANE).setName(ChatColor.GOLD + "1st Place").build();
                final ItemStack silverBorder = new ItemBuilder().setMaterial(Material.WHITE_STAINED_GLASS_PANE).setName(ChatColor.GRAY + "2nd Place").build();
                final ItemStack bronzeBorder = new ItemBuilder().setMaterial(Material.ORANGE_STAINED_GLASS_PANE).setName(ChatColor.RED + "3rd Place").build();

                // Gold Border
                menu.addItem(new ClickableItem(goldBorder, 0, click -> {}));
                menu.addItem(new ClickableItem(goldBorder, 1, click -> {}));
                menu.addItem(new ClickableItem(goldBorder, 2, click -> {}));
                menu.addItem(new ClickableItem(goldBorder, 9, click -> {}));
                menu.addItem(new ClickableItem(goldBorder, 11, click -> {}));
                menu.addItem(new ClickableItem(goldBorder, 18, click -> {}));
                menu.addItem(new ClickableItem(goldBorder, 19, click -> {}));
                menu.addItem(new ClickableItem(goldBorder, 20, click -> {}));

                // Silver Border
                menu.addItem(new ClickableItem(silverBorder, 3, click -> {}));
                menu.addItem(new ClickableItem(silverBorder, 4, click -> {}));
                menu.addItem(new ClickableItem(silverBorder, 5, click -> {}));
                menu.addItem(new ClickableItem(silverBorder, 12, click -> {}));
                menu.addItem(new ClickableItem(silverBorder, 14, click -> {}));
                menu.addItem(new ClickableItem(silverBorder, 21, click -> {}));
                menu.addItem(new ClickableItem(silverBorder, 22, click -> {}));
                menu.addItem(new ClickableItem(silverBorder, 23, click -> {}));

                // Bronze Border
                menu.addItem(new ClickableItem(bronzeBorder, 6, click -> {}));
                menu.addItem(new ClickableItem(bronzeBorder, 7, click -> {}));
                menu.addItem(new ClickableItem(bronzeBorder, 8, click -> {}));
                menu.addItem(new ClickableItem(bronzeBorder, 15, click -> {}));
                menu.addItem(new ClickableItem(bronzeBorder, 17, click -> {}));
                menu.addItem(new ClickableItem(bronzeBorder, 24, click -> {}));
                menu.addItem(new ClickableItem(bronzeBorder, 25, click -> {}));
                menu.addItem(new ClickableItem(bronzeBorder, 26, click -> {}));

                menu.fill(new ItemBuilder().setMaterial(Material.GRAY_STAINED_GLASS_PANE).setName(ChatColor.DARK_GRAY + "made u look").build());
                menu.open();
            }).run();
        }).run();
    }
}