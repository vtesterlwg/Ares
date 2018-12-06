package com.riotmc.factions.addons.events.menu;

import com.google.common.collect.Lists;
import com.riotmc.commons.base.util.Time;
import com.riotmc.commons.bukkit.item.ItemBuilder;
import com.riotmc.commons.bukkit.menu.Menu;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.addons.events.EventsAddon;
import com.riotmc.factions.addons.events.type.RiotEvent;
import com.riotmc.factions.addons.events.type.koth.KOTHTicket;
import com.riotmc.factions.addons.events.type.koth.KOTHTimer;
import com.riotmc.factions.addons.events.type.koth.Palace;
import com.riotmc.factions.factions.PlayerFaction;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public final class EventMenu extends Menu {
    @Getter public final EventsAddon addon;
    @Getter public BukkitTask updater;

    public EventMenu(EventsAddon addon, Player player) {
        super(addon.getPlugin(), player, "Events", 1);
        this.addon = addon;
        this.addon.getPlugin().registerListener(this);
    }

    private void update() {
        final List<RiotEvent> events = addon.getManager().getEventsAlphabetical();

        clearInventory();

        events.forEach(event -> {
            final ItemBuilder builder = new ItemBuilder();

            if (event instanceof KOTHTicket) {
                final KOTHTicket koth = (KOTHTicket)event;
                final boolean active = (koth.getSession() != null && koth.getSession().isActive());
                final List<String> lore = Lists.newArrayList();

                builder.setMaterial(active ? Material.GREEN_CONCRETE : Material.RED_CONCRETE);
                builder.setName(koth.getDisplayName() + ChatColor.YELLOW + "[" + ChatColor.GOLD + "Ticket" + ChatColor.YELLOW + "]" + ChatColor.YELLOW + "[" + (active ? ChatColor.GREEN + "Active" : ChatColor.RED + "Inactive") + ChatColor.YELLOW + "]");

                lore.add(ChatColor.GOLD + "Event Located At: " + ChatColor.YELLOW + koth.getCaptureRegion().getCornerA().toString());
                lore.add(ChatColor.RESET + " ");

                if (active) {
                    final List<PlayerFaction> leaderboard = koth.getSession().getTicketLeaderboard();

                    lore.add(ChatColor.GOLD + "Tickets Needed: " + ChatColor.YELLOW + koth.getSession().getWinCondition());
                    lore.add(ChatColor.GOLD + "Ticket Interval: " + ChatColor.YELLOW + koth.getSession().getTimerDuration());
                    lore.add(ChatColor.RESET + " ");

                    if (koth.getSession().getCapturingFaction() != null) {
                        lore.add(ChatColor.BLUE + "Controlled By: " + ChatColor.AQUA + koth.getSession().getCapturingFaction().getName());
                    }

                    lore.add(ChatColor.BLUE + "Status: " + (koth.isContested() ? ChatColor.RED + "Contested" : ChatColor.YELLOW + "Uncontested"));
                    lore.add(ChatColor.BLUE + "Remaining Time: " + ChatColor.YELLOW + Time.convertToHHMMSS(koth.getSession().getTimer().getRemaining()));

                    lore.add(ChatColor.RESET + " ");

                    if (!leaderboard.isEmpty()) {
                        final int cap = leaderboard.size() > 10 ? 9 : leaderboard.size();

                        for (int i = 0; i < cap; i++) {
                            final PlayerFaction faction = leaderboard.get(i);
                            final int tickets = koth.getSession().getTickets(faction);

                            lore.add(ChatColor.DARK_RED + "" + (i + 1) + ChatColor.RED + ". " + ChatColor.GOLD + faction.getName() + ChatColor.YELLOW + ": " + tickets);
                        }
                    } else {
                        lore.add(ChatColor.GRAY + "There aren't any factions with tickets currently");
                    }
                } else {
                    lore.add(ChatColor.GRAY + "This event is scheduled to start in " + ChatColor.WHITE + Time.convertToRemaining(koth.getTimeUntilNextSchedule()));
                }
            }

            else if (event instanceof Palace) {
                final Palace palace = (Palace)event;
            }

            else if (event instanceof KOTHTimer) {
                final KOTHTimer koth = (KOTHTimer)event;
                final boolean active = (koth.getSession() != null && koth.getSession().isActive());
                final List<String> lore = Lists.newArrayList();

                builder.setMaterial(active ? Material.GREEN_CONCRETE : Material.RED_CONCRETE);
                builder.setName(koth.getDisplayName() + ChatColor.YELLOW + "[" + ChatColor.GOLD + "Timer" + ChatColor.YELLOW + "]" + ChatColor.YELLOW + "[" + (active ? ChatColor.GREEN + "Active" : ChatColor.RED + "Inactive") + ChatColor.YELLOW + "]");

                lore.add(ChatColor.GOLD + "Event Located At: " + ChatColor.YELLOW + koth.getCaptureRegion().getCornerA().toString());
                lore.add(ChatColor.RESET + " ");

                if (active) {
                    if (koth.getSession().getCapturingFaction() != null) {
                        lore.add(ChatColor.BLUE + "Controlled By: " + ChatColor.AQUA + koth.getSession().getCapturingFaction().getName());
                    }

                    lore.add(ChatColor.BLUE + "Status: " + (koth.isContested() ? ChatColor.RED + "Contested" : ChatColor.YELLOW + "Uncontested"));
                    lore.add(ChatColor.BLUE + "Remaining Time: " + ChatColor.YELLOW + Time.convertToHHMMSS(koth.getSession().getTimer().getRemaining()));
                } else {
                    lore.add(ChatColor.GRAY + "This event is scheduled to start in " + ChatColor.WHITE + Time.convertToRemaining(koth.getTimeUntilNextSchedule()));
                }
            }
        });
    }

    @Override
    public void open() {
        super.open();
        this.updater = new Scheduler(addon.getPlugin()).repeat(0L, 20L).sync(this::update).run();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        this.updater.cancel();
        InventoryCloseEvent.getHandlerList().unregister(this);
    }
}
