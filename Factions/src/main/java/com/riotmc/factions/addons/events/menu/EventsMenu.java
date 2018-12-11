package com.riotmc.factions.addons.events.menu;

import com.google.common.collect.Lists;
import com.riotmc.commons.base.util.Time;
import com.riotmc.commons.bukkit.RiotPlugin;
import com.riotmc.commons.bukkit.item.ItemBuilder;
import com.riotmc.commons.bukkit.menu.ClickableItem;
import com.riotmc.commons.bukkit.menu.Menu;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.addons.events.EventsAddon;
import com.riotmc.factions.addons.events.data.type.RiotEvent;
import com.riotmc.factions.addons.events.data.type.koth.KOTHEvent;
import com.riotmc.factions.factions.Faction;
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

    public EventsMenu(EventsAddon addon, @Nonnull RiotPlugin plugin, @Nonnull Player player, @Nonnull String title, int rows) {
        super(plugin, player, title, rows);
        this.addon = addon;

    }

    public void update() {
        final List<RiotEvent> events = addon.getManager().getEventsAlphabetical();
        int pos = 0;

        clearInventory();

        for (RiotEvent event : events) {
            final ItemBuilder builder = new ItemBuilder().setName(event.getDisplayName());
            final List<String> lore = Lists.newArrayList();

            if (event instanceof KOTHEvent) {
                final KOTHEvent koth = (KOTHEvent)event;
                final boolean active = koth.getSession() != null && koth.getSession().isActive();

                builder.setMaterial(active ? Material.GREEN_CONCRETE : Material.RED_CONCRETE);

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

                    lore.add(ChatColor.GOLD + "Remaining Time" + ChatColor.YELLOW + ": " + koth.getSession().getTimer().getRemaining());

                    if (koth.getSession().getTicketsNeededToWin() > 1) {
                        // TODO: Ticket Leaderboard
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
