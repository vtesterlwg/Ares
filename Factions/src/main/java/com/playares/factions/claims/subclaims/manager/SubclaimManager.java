package com.playares.factions.claims.subclaims.manager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.claims.data.DefinedClaim;
import com.playares.factions.claims.subclaims.dao.SubclaimDAO;
import com.playares.factions.claims.subclaims.data.Subclaim;
import com.playares.factions.claims.subclaims.handler.SubclaimCreationHandler;
import com.playares.factions.claims.subclaims.handler.SubclaimEditorHandler;
import com.playares.factions.factions.data.PlayerFaction;
import lombok.Getter;
import org.bukkit.block.Block;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class SubclaimManager {
    @Getter public final Factions plugin;
    @Getter public final Set<Subclaim> subclaimRepository;
    @Getter public final SubclaimCreationHandler creationHandler;
    @Getter public final SubclaimEditorHandler editorHandler;

    public SubclaimManager(Factions plugin) {
        this.plugin = plugin;
        this.subclaimRepository = Sets.newConcurrentHashSet();
        this.creationHandler = new SubclaimCreationHandler(this);
        this.editorHandler = new SubclaimEditorHandler(this);
    }

    public void loadSubclaims() {
        subclaimRepository.addAll(SubclaimDAO.getSubclaims(plugin, plugin.getMongo()));
    }

    public void saveSubclaims(boolean blocking) {
        Logger.print("Saving " + subclaimRepository.size() + " Subclaims, Blocking = " + blocking);

        if (blocking) {
            SubclaimDAO.saveSubclaims(plugin.getMongo(), subclaimRepository);
            Logger.print("Finished saving subclaims");
            return;
        }

        new Scheduler(plugin).async(() -> {
            SubclaimDAO.saveSubclaims(plugin.getMongo(), subclaimRepository);
            Logger.print("Finished saving subclaims");
        }).run();
    }

    public Subclaim getSubclaimAt(Block block) {
        return subclaimRepository.stream().filter(subclaim -> subclaim.match(block)).findFirst().orElse(null);
    }

    public ImmutableList<Subclaim> getSubclaimsByOwner(PlayerFaction faction) {
        return ImmutableList.copyOf(subclaimRepository.stream().filter(subclaim -> subclaim.getFaction() != null && subclaim.getFaction().getUniqueId().equals(faction.getUniqueId())).collect(Collectors.toList()));
    }

    public ImmutableList<Subclaim> getSubclaimsInside(DefinedClaim claim) {
        final List<Subclaim> result = Lists.newArrayList();

        for (Subclaim subclaim : subclaimRepository) {
            for (BLocatable block : subclaim.getBlocks()) {
                if (claim.inside(block) && !result.contains(subclaim)) {
                    result.add(subclaim);
                }
            }
        }

        return ImmutableList.copyOf(result);
    }
}