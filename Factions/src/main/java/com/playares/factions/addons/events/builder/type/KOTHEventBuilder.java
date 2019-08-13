package com.playares.factions.addons.events.builder.type;

import com.google.common.collect.Lists;
import com.playares.commons.base.promise.FailablePromise;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.factions.addons.events.EventsAddon;
import com.playares.factions.addons.events.builder.EventBuilderWand;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import com.playares.factions.factions.data.ServerFaction;
import com.playares.services.customitems.CustomItemService;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;

public class KOTHEventBuilder implements EventBuilder {
    @Getter public final EventsAddon addon;
    @Getter @Setter public KOTHBuilderStep currentStep;
    @Getter public final UUID builder;
    @Getter public ServerFaction owningFaction;
    @Getter public String name;
    @Getter public String displayName;
    @Getter public BLocatable cornerA;
    @Getter public BLocatable cornerB;
    @Getter public BLocatable lootChest;

    public KOTHEventBuilder(EventsAddon addon, Player player) {
        this.addon = addon;
        this.builder = player.getUniqueId();
        this.currentStep = KOTHBuilderStep.OWNER;

        player.sendMessage(ChatColor.BLUE + "You are now building a King of the Hill Event");
        player.sendMessage(ChatColor.YELLOW + "Start by providing the name of the Server Faction connected to this event.");
        player.sendMessage(ChatColor.GRAY + "If there is no Server Faction connected to this event just provide 'none'");
    }

    public void setOwningFaction(String name, FailablePromise<String> promise) {
        final ServerFaction faction = (name.equalsIgnoreCase("none") ? null : addon.getPlugin().getFactionManager().getServerFactionByName(name));

        if (faction == null && !name.equalsIgnoreCase("none")) {
            promise.failure("Faction not found");
            return;
        }

        this.owningFaction = faction;
        setCurrentStep(KOTHBuilderStep.NAME);
        promise.success("Provide a unique name for this event");
    }

    public void setName(String name, FailablePromise<String> promise) {
        if (addon.getManager().getEventByName(name) != null) {
            promise.failure("Event with the name '" + name + "' already exists");
            return;
        }

        this.name = name;
        setCurrentStep(KOTHBuilderStep.DISPLAY_NAME);
        promise.success("Provide a display name for this event");
    }

    public void setDisplayName(String name) {
        final Player player = Bukkit.getPlayer(builder);
        final CustomItemService customItemService = (CustomItemService)addon.getPlugin().getService(CustomItemService.class);

        if (player != null && customItemService != null) {
            customItemService.getItem(EventBuilderWand.class).ifPresent(item -> player.getInventory().addItem(item.getItem()));
        }

        this.displayName = ChatColor.translateAlternateColorCodes('&', name);

        setCurrentStep(KOTHBuilderStep.CORNER_A);
    }

    public void setCornerA(BLocatable location) {
        this.cornerA = location;
        setCurrentStep(KOTHBuilderStep.CORNER_B);
    }

    public void setCornerB(BLocatable location) {
        this.cornerB = location;
        setCurrentStep(KOTHBuilderStep.LOOT_CHEST);
    }

    public void setLootChest(BLocatable location, FailablePromise<String> promise) {
        final Player player = Bukkit.getPlayer(builder);
        final Block block = location.getBukkit();
        final CustomItemService customItemService = (CustomItemService)addon.getPlugin().getService(CustomItemService.class);

        if (block == null || !(block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST))) {
            promise.failure("This block is not a chest");
            return;
        }

        if (player != null && customItemService != null) {
            customItemService.getItem(EventBuilderWand.class).ifPresent(item -> player.getInventory().remove(item.getItem()));
        }

        this.lootChest = location;
        setCurrentStep(KOTHBuilderStep.FINISHED);
        promise.success("Event loot chest location has been set");
    }

    public void build(FailablePromise<KOTHEvent> promise) {
        final UUID owningFactionId = (owningFaction != null) ? owningFaction.getUniqueId() : null;

        if (name == null) {
            promise.failure("Name is not set");
            return;
        }

        if (displayName == null) {
            promise.failure("Display name is not set");
            return;
        }

        if (cornerA == null) {
            promise.failure("Corner A is not set");
            return;
        }

        if (cornerB == null) {
            promise.failure("Corner B is not set");
            return;
        }

        if (lootChest == null) {
            promise.failure("Loot chest is not set");
            return;
        }

        final KOTHEvent event = new KOTHEvent(addon, owningFactionId, name, displayName, Lists.newArrayList(), lootChest, cornerA, cornerB);
        promise.success(event);
    }

    /*
    Is this a Palace event?
    Is this event connected to a faction? If so provide a name here otherwise type 'None'
    Provide the name for this event. This name must be unique and is used to identify it throughout the plugin. This name will not be used in displays.
    Provide the display name for this event. This name supports color codes and will be used for all displays.
    Punch a corner of the capture region for this event
    Punch the opposite corner of the capture region for this event
     */

    public enum KOTHBuilderStep {
        OWNER, NAME, DISPLAY_NAME, CORNER_A, CORNER_B, LOOT_CHEST, FINISHED;
    }
}
