package com.playares.factions.addons.events.menu;

import com.google.common.collect.Lists;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.AresPlugin;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.menu.ClickableItem;
import com.playares.commons.bukkit.menu.Menu;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.events.data.type.AresEvent;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import com.playares.factions.factions.data.Faction;
import com.playares.factions.factions.data.PlayerFaction;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.util.List;

public final class EventsMenu extends Menu {
    @Getter public final EventsAddon addon;
    @Getter public BukkitTask updater;

    public EventsMenu(EventsAddon addon, @Nonnull AresPlugin plugin, @Nonnull Player player, @Nonnull String title, int rows) {
        super(plugin, player, title, rows);
        this.addon = addon;
    }

    private void update() {
        final List<AresEvent> events = addon.getManager().getEventsAlphabetical();
        int pos = 0;

        clearInventory();

        for (AresEvent event : events) {
            final ItemBuilder builder = new ItemBuilder().setName(event.getDisplayName());
            final List<String> lore = Lists.newArrayList();

            if (event instanceof KOTHEvent) {
                final KOTHEvent koth = (KOTHEvent)event;
                final boolean active = koth.getSession() != null && koth.getSession().isActive();

                builder.setMaterial(Material.CONCRETE);
                builder.setData(active ? (short)5 : (short)14);

                lore.add(ChatColor.GOLD + "Type" + ChatColor.YELLOW + ": " + ChatColor.BLUE + "King of the Hill");
                lore.add(ChatColor.GOLD + "Status" + ChatColor.YELLOW + ": " + (active ? ChatColor.GREEN + "Active" : ChatColor.RED + "Inactive"));
                lore.add(ChatColor.GOLD + "Located At" + ChatColor.YELLOW + ": " + koth.getCaptureRegion().getCornerA().toString());

                if (active) {
                    lore.add(ChatColor.GOLD + "Tickets Needed To Win" + ChatColor.YELLOW + ": " + koth.getSession().getTicketsNeededToWin());
                    lore.add(ChatColor.GOLD + "Timer Duration" + ChatColor.YELLOW + ": " + Time.convertToRemaining((koth.getSession().getTimerDuration() * 1000L)));
                }

                lore.add(ChatColor.RESET + " ");

                if (active) {
                    if (koth.getSession().getCapturingFaction() != null) {
                        lore.add(ChatColor.GOLD + "Controlled By" + ChatColor.YELLOW + ": " + koth.getSession().getCapturingFaction().getName());
                    }

                    // TODO: Add status here

                    lore.add(ChatColor.GOLD + "Remaining Time" + ChatColor.YELLOW + ": " + Time.convertToHHMMSS(koth.getSession().getTimer().getRemaining()));

                    if (koth.getSession().getTicketsNeededToWin() > 1) {
                        final List<PlayerFaction> leaderboard = koth.getSession().getSortedLeaderboard();
                        int position = 1;

                        lore.add(ChatColor.RESET + " ");
                        lore.add(ChatColor.GOLD + "Leaderboard");

                        if (!leaderboard.isEmpty()) {
                            for (PlayerFaction faction : leaderboard) {
                                final int tickets = koth.getSession().getTickets(faction);

                                lore.add(ChatColor.GOLD + "" + position + ". " + ChatColor.YELLOW + faction.getName() + ChatColor.BLUE + " (" + tickets + ")");

                                position += 1;
                            }
                        } else {
                            lore.add(ChatColor.YELLOW + "No factions have a ticket yet");
                        }
                    }
                } else {
                    lore.add(ChatColor.GRAY + "This event will activate:");
                    lore.add(ChatColor.WHITE + ((koth.getTimeToNextSchedule() != -1) ? Time.convertToRemaining(koth.getTimeToNextSchedule()) : "Unscheduled"));
                }
            }

            builder.addLore(lore);

            addItem(new ClickableItem(builder.build(), pos, click -> {
                if (event.getOwnerId() != null) {
                    final Faction faction = addon.getPlugin().getFactionManager().getFactionById(event.getOwnerId());

                    if (faction != null) {
                        addon.getPlugin().getFactionManager().getDisplayHandler().displayFactionInfo(player, faction);
                    }
                }
            }));

            pos++;
        }
    }

    @Override
    public void open() {
        super.open();
        this.updater = new Scheduler(plugin).repeat(0L, 20L).sync(this::update).run();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        if (this.updater != null) {
            this.updater.cancel();
            this.updater = null;
        }

        InventoryCloseEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
    }
}