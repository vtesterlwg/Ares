package com.riotmc.factions.addons.events.builder.type;

import com.google.common.collect.Lists;
import com.riotmc.commons.base.promise.FailablePromise;
import com.riotmc.commons.base.promise.SimplePromise;
import com.riotmc.commons.bukkit.location.BLocatable;
import com.riotmc.factions.addons.events.EventsAddon;
import com.riotmc.factions.addons.events.data.type.koth.KOTHEvent;
import com.riotmc.factions.factions.ServerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
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

    public KOTHEventBuilder(EventsAddon addon, Player player) {
        this.addon = addon;
        this.builder = player.getUniqueId();
        this.currentStep = KOTHBuilderStep.OWNER;
    }

    public void setOwningFaction(String name, FailablePromise<String> promise) {
        final ServerFaction faction = (name.equalsIgnoreCase("none") ? null : addon.getPlugin().getFactionManager().getServerFactionByName(name));

        if (faction == null && !name.equalsIgnoreCase("none")) {
            promise.failure("Faction not found");
            return;
        }

        this.owningFaction = faction;

        setCurrentStep(KOTHBuilderStep.NAME);
        promise.success("Owning faction has been set. Now provide the name for this event - This name must be unique as it will be the main identifier. This name will not be used for display.");
    }

    public void setName(String name, SimplePromise promise) {
        if (addon.getManager().getEventByName(name) != null) {
            promise.failure("Event with the name '" + name + "' already exists");
            return;
        }

        this.name = name;

        setCurrentStep(KOTHBuilderStep.DISPLAY_NAME);
        promise.success();
    }

    public void setDisplayName(String name) {
        this.displayName = ChatColor.translateAlternateColorCodes('&', name);
        setCurrentStep(KOTHBuilderStep.CORNER_A);
    }

    public void setCornerA(BLocatable location) {
        this.cornerA = location;
        setCurrentStep(KOTHBuilderStep.CORNER_B);
    }

    public void setCornerB(BLocatable location) {
        this.cornerB = location;
        setCurrentStep(KOTHBuilderStep.FINISHED);
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

        final KOTHEvent event = new KOTHEvent(owningFactionId, name, displayName, Lists.newArrayList(), cornerA, cornerB);
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
        OWNER, NAME, DISPLAY_NAME, CORNER_A, CORNER_B, FINISHED;
    }
}
