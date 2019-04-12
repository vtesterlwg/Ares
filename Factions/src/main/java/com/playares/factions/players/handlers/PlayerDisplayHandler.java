package com.playares.factions.players.handlers;

import com.google.common.collect.Lists;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.addons.stats.StatsAddon;
import com.playares.factions.players.dao.PlayerDAO;
import com.playares.factions.players.data.FactionPlayer;
import com.playares.factions.players.manager.PlayerManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class PlayerDisplayHandler {
    /** Owning Manager **/
    @Getter public final PlayerManager manager;

    public PlayerDisplayHandler(PlayerManager manager) {
        this.manager = manager;
    }

    /**
     * Displays the player leaderboard for the provided category
     * @param viewer Player
     * @param category Category
     * @param promise Promise
     */
    public void displayLeaderboard(Player viewer, String category, SimplePromise promise) {
        new Scheduler(manager.getPlugin()).async(() -> {
            final StatsAddon addon = (StatsAddon)manager.getPlugin().getAddonManager().getAddon(StatsAddon.class);
            final List<FactionPlayer> players = Lists.newArrayList(PlayerDAO.getPlayers(manager.getPlugin(), manager.getPlugin().getMongo()));
            final FactionPlayer personalProfile = manager.getPlayer(viewer.getUniqueId());

            if (addon == null) {
                new Scheduler(manager.getPlugin()).sync(() -> promise.failure("Failed to obtain Stats Addon")).run();
                return;
            }

            if (players.isEmpty()) {
                new Scheduler(manager.getPlugin()).sync(() -> promise.failure("There are no players on the leaderboard")).run();
                return;
            }

            final String fancyCategory;

            if (category.equalsIgnoreCase("elo") || category.equalsIgnoreCase("rating") || category.equalsIgnoreCase("e") || category.equalsIgnoreCase("r")) {
                players.sort(Comparator.comparingInt(p -> p.getStats().calculateELO(addon)));
                fancyCategory = "Rating";
            } else if (category.equalsIgnoreCase("kill") || category.equalsIgnoreCase("kills") || category.equalsIgnoreCase("k")) {
                players.sort(Comparator.comparingInt(p -> p.getStats().getKills()));
                fancyCategory = "Kills";
            } else if (category.equalsIgnoreCase("death") || category.equalsIgnoreCase("deaths") || category.equalsIgnoreCase("d")) {
                players.sort(Comparator.comparingInt(p -> p.getStats().getDeaths()));
                fancyCategory = "Deaths";
            } else if (category.equalsIgnoreCase("minorevent") || category.equalsIgnoreCase("minorevents") || category.equalsIgnoreCase("minor")) {
                players.sort(Comparator.comparingInt(p -> p.getStats().getMinorEventCaptures()));
                fancyCategory = "Minor Event Captures";
            } else if (category.equalsIgnoreCase("majorevent") || category.equalsIgnoreCase("majorevents") || category.equalsIgnoreCase("major")) {
                players.sort(Comparator.comparingInt(p -> p.getStats().getMajorEventCaptures()));
                fancyCategory = "Major Event Captures";
            } else if (category.equalsIgnoreCase("playtime") || category.equalsIgnoreCase("pt")) {
                players.sort(Comparator.comparingLong(p -> p.getStats().getPlaytime()));
                fancyCategory = "Time Played";
            } else if (category.equalsIgnoreCase("diamond") || category.equalsIgnoreCase("diamonds")) {
                players.sort(Comparator.comparingInt(p -> p.getStats().getMinedOres().getOrDefault(Material.DIAMOND_ORE, 0)));
                fancyCategory = "Mined Diamonds";
            } else if (category.equalsIgnoreCase("emerald") || category.equalsIgnoreCase("emeralds")) {
                players.sort(Comparator.comparingInt(p -> p.getStats().getMinedOres().getOrDefault(Material.EMERALD_ORE, 0)));
                fancyCategory = "Mined Emeralds";
            } else if (category.equalsIgnoreCase("gold")) {
                players.sort(Comparator.comparingInt(p -> p.getStats().getMinedOres().getOrDefault(Material.GOLD_ORE, 0)));
                fancyCategory = "Mined Gold";
            } else if (category.equalsIgnoreCase("redstone")) {
                players.sort(Comparator.comparingInt(p -> p.getStats().getMinedOres().getOrDefault(Material.REDSTONE_ORE, 0)));
                fancyCategory = "Mined Redstone";
            } else if (category.equalsIgnoreCase("lapis") || category.equalsIgnoreCase("lazuli")) {
                players.sort(Comparator.comparingInt(p -> p.getStats().getMinedOres().getOrDefault(Material.LAPIS_ORE, 0)));
                fancyCategory = "Mined Lapis";
            } else if (category.equalsIgnoreCase("iron")) {
                players.sort(Comparator.comparingInt(p -> p.getStats().getMinedOres().getOrDefault(Material.IRON_ORE, 0)));
                fancyCategory = "Mined Iron";
            } else if (category.equalsIgnoreCase("coal")) {
                players.sort(Comparator.comparingInt(p -> p.getStats().getMinedOres().getOrDefault(Material.COAL_ORE, 0)));
                fancyCategory = "Mined Coal";
            } else {
                new Scheduler(manager.getPlugin()).sync(() -> promise.failure("Invalid category")).run();
                return;
            }

            Collections.reverse(players);

            new Scheduler(manager.getPlugin()).sync(() -> {
                final Menu menu = new Menu(manager.getPlugin(), viewer, "Players Leaderboard: " + fancyCategory, 6);
                final List<FactionPlayer> top = (players.size() >= 3 ? players.subList(0, 2) : players.subList(0, players.size()));
                final String spacer = ChatColor.RESET + " " + ChatColor.RESET + " " + ChatColor.YELLOW + " - " + ChatColor.RESET;

                int pos = 1;
                int slot = 10;

                for (FactionPlayer player : top) {
                    final ItemBuilder builder = new ItemBuilder().setMaterial(Material.PLAYER_HEAD);
                    ChatColor color = ChatColor.RESET;

                    if (pos == 1) {
                        color = ChatColor.GOLD;
                    } else if (pos == 2) {
                        color = ChatColor.GRAY;
                    } else if (pos == 3) {
                        color = ChatColor.RED;
                    }

                    builder.setName(color + "#" + pos + ChatColor.RESET + " - " + ChatColor.YELLOW + player.getUsername());

                    final List<String> lore = Lists.newArrayList();

                    lore.add(ChatColor.GOLD + "Main" + ChatColor.YELLOW + ": ");
                    lore.add(spacer + ChatColor.GOLD + "Rating: " + ChatColor.YELLOW + player.getStats().calculateELO(addon));
                    lore.add(spacer + ChatColor.GOLD + "Kills: " + ChatColor.YELLOW + player.getStats().getKills());
                    lore.add(spacer + ChatColor.GOLD + "Deaths: " + ChatColor.YELLOW + player.getStats().getDeaths());
                    lore.add(spacer + ChatColor.GOLD + "Time Played: " + ChatColor.YELLOW + Time.convertToInaccurateElapsed(player.getStats().getPlaytime()));
                    lore.add(ChatColor.RESET + " ");
                    lore.add(ChatColor.GOLD + "Events" + ChatColor.YELLOW + ": ");
                    lore.add(spacer + ChatColor.GOLD + "Minor Event Captures: " + ChatColor.YELLOW + player.getStats().getMinorEventCaptures());
                    lore.add(spacer + ChatColor.GOLD + "Major Event Captures: " + ChatColor.YELLOW + player.getStats().getMajorEventCaptures());
                    lore.add(ChatColor.RESET + " ");
                    lore.add(ChatColor.GOLD + "Ores" + ChatColor.YELLOW + ": ");
                    lore.add(spacer + ChatColor.GOLD + "Mined Emeralds: " + ChatColor.YELLOW + player.getStats().getMinedOres().getOrDefault(Material.EMERALD_ORE, 0));
                    lore.add(spacer + ChatColor.GOLD + "Mined Diamonds: " + ChatColor.YELLOW + player.getStats().getMinedOres().getOrDefault(Material.DIAMOND_ORE, 0));
                    lore.add(spacer + ChatColor.GOLD + "Mined Gold: " + ChatColor.YELLOW + player.getStats().getMinedOres().getOrDefault(Material.GOLD_ORE, 0));
                    lore.add(spacer + ChatColor.GOLD + "Mined Redstone: " + ChatColor.YELLOW + player.getStats().getMinedOres().getOrDefault(Material.REDSTONE_ORE, 0));
                    lore.add(spacer + ChatColor.GOLD + "Mined Lapis: " + ChatColor.YELLOW + player.getStats().getMinedOres().getOrDefault(Material.LAPIS_ORE, 0));
                    lore.add(spacer + ChatColor.GOLD + "Mined Iron: " + ChatColor.YELLOW + player.getStats().getMinedOres().getOrDefault(Material.IRON_ORE, 0));
                    lore.add(spacer + ChatColor.GOLD + "Mined Coal: " + ChatColor.YELLOW + player.getStats().getMinedOres().getOrDefault(Material.COAL_ORE, 0));

                    builder.addLore(lore);

                    final ItemStack icon = builder.build();
                    final SkullMeta meta = (SkullMeta)icon.getItemMeta();
                    final int currentSlot = slot;

                    new Scheduler(manager.getPlugin()).async(() -> {
                        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());

                        new Scheduler(manager.getPlugin()).sync(() -> {
                            meta.setOwningPlayer(offlinePlayer);
                            icon.setItemMeta(meta);

                            menu.addItem(new ClickableItem(icon, currentSlot, click -> addon.getStatsHandler().getStats(viewer, player.getUsername(), new SimplePromise() {
                                @Override
                                public void success() {}

                                @Override
                                public void failure(@Nonnull String reason) {
                                    viewer.sendMessage(ChatColor.RED + reason);
                                }
                            })));
                        }).run();
                    }).run();

                    pos++;
                    slot += 3;
                }

                if (personalProfile != null) {
                    final ItemBuilder builder = new ItemBuilder().setMaterial(Material.PLAYER_HEAD);
                    final List<String> lore = Lists.newArrayList();
                    int personalPos = 1;

                    for (FactionPlayer player : players) {
                        if (player.getUniqueId().equals(personalProfile.getUniqueId())) {
                            break;
                        }

                        personalPos++;
                    }

                    builder.setName(ChatColor.GREEN + "(You) " + ChatColor.RESET + "#" + personalPos + " - " + ChatColor.YELLOW + personalProfile.getUsername());

                    lore.add(ChatColor.GOLD + "Main" + ChatColor.YELLOW + ": ");
                    lore.add(spacer + ChatColor.GOLD + "Rating: " + ChatColor.YELLOW + personalProfile.getStats().calculateELO(addon));
                    lore.add(spacer + ChatColor.GOLD + "Kills: " + ChatColor.YELLOW + personalProfile.getStats().getKills());
                    lore.add(spacer + ChatColor.GOLD + "Deaths: " + ChatColor.YELLOW + personalProfile.getStats().getDeaths());
                    lore.add(spacer + ChatColor.GOLD + "Time Played: " + ChatColor.YELLOW + Time.convertToInaccurateElapsed(personalProfile.getStats().getPlaytime()));
                    lore.add(ChatColor.RESET + " ");
                    lore.add(ChatColor.GOLD + "Events" + ChatColor.YELLOW + ": ");
                    lore.add(spacer + ChatColor.GOLD + "Minor Event Captures: " + ChatColor.YELLOW + personalProfile.getStats().getMinorEventCaptures());
                    lore.add(spacer + ChatColor.GOLD + "Major Event Captures: " + ChatColor.YELLOW + personalProfile.getStats().getMajorEventCaptures());
                    lore.add(ChatColor.RESET + " ");
                    lore.add(ChatColor.GOLD + "Ores" + ChatColor.YELLOW + ": ");
                    lore.add(spacer + ChatColor.GOLD + "Mined Emeralds: " + ChatColor.YELLOW + personalProfile.getStats().getMinedOres().getOrDefault(Material.EMERALD_ORE, 0));
                    lore.add(spacer + ChatColor.GOLD + "Mined Diamonds: " + ChatColor.YELLOW + personalProfile.getStats().getMinedOres().getOrDefault(Material.DIAMOND_ORE, 0));
                    lore.add(spacer + ChatColor.GOLD + "Mined Gold: " + ChatColor.YELLOW + personalProfile.getStats().getMinedOres().getOrDefault(Material.GOLD_ORE, 0));
                    lore.add(spacer + ChatColor.GOLD + "Mined Redstone: " + ChatColor.YELLOW + personalProfile.getStats().getMinedOres().getOrDefault(Material.REDSTONE_ORE, 0));
                    lore.add(spacer + ChatColor.GOLD + "Mined Lapis: " + ChatColor.YELLOW + personalProfile.getStats().getMinedOres().getOrDefault(Material.LAPIS_ORE, 0));
                    lore.add(spacer + ChatColor.GOLD + "Mined Iron: " + ChatColor.YELLOW + personalProfile.getStats().getMinedOres().getOrDefault(Material.IRON_ORE, 0));
                    lore.add(spacer + ChatColor.GOLD + "Mined Coal: " + ChatColor.YELLOW + personalProfile.getStats().getMinedOres().getOrDefault(Material.COAL_ORE, 0));

                    builder.addLore(lore);

                    final ItemStack icon = builder.build();
                    final SkullMeta meta = (SkullMeta)icon.getItemMeta();

                    new Scheduler(manager.getPlugin()).async(() -> {
                        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(personalProfile.getUniqueId());

                        new Scheduler(manager.getPlugin()).sync(() -> {
                            meta.setOwningPlayer(offlinePlayer);
                            icon.setItemMeta(meta);

                            menu.addItem(new ClickableItem(icon, 40, click -> addon.getStatsHandler().getStats(viewer, new SimplePromise() {
                                @Override
                                public void success() {}

                                @Override
                                public void failure(@Nonnull String reason) {
                                    viewer.sendMessage(ChatColor.RED + reason);
                                }
                            })));
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
                promise.success();
            }).run();
        }).run();
    }
}
