package com.playares.factions.claims.subclaims.manager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.claims.data.DefinedClaim;
import com.playares.factions.claims.subclaims.dao.SubclaimDAO;
import com.playares.factions.claims.subclaims.data.Subclaim;
import com.playares.factions.claims.subclaims.handler.SubclaimCreationHandler;
import com.playares.factions.claims.subclaims.handler.SubclaimDeletionHandler;
import com.playares.factions.claims.subclaims.handler.SubclaimUpdateHandler;
import com.playares.factions.claims.subclaims.menu.SubclaimMenu;
import com.playares.factions.factions.data.PlayerFaction;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class SubclaimManager {
    /** Owning Plugin **/
    @Getter public final Factions plugin;
    /** Contains all loaded subclaims **/
    @Getter public final Set<Subclaim> subclaimRepository;
    /** Handles the creation of subclaims **/
    @Getter public final SubclaimCreationHandler creationHandler;
    /** Handles the deletion of subclaims **/
    @Getter public final SubclaimDeletionHandler deletionHandler;
    /** Handles updating subclaims **/
    @Getter public final SubclaimUpdateHandler updateHandler;
    /** Contains active subclaim editors, which are used to update for everyone **/
    @Getter public final Map<Subclaim, List<SubclaimMenu>> activeEditors;

    public SubclaimManager(Factions plugin) {
        this.plugin = plugin;
        this.subclaimRepository = Sets.newConcurrentHashSet();
        this.creationHandler = new SubclaimCreationHandler(this);
        this.deletionHandler = new SubclaimDeletionHandler(this);
        this.updateHandler = new SubclaimUpdateHandler(this);
        this.activeEditors = Maps.newConcurrentMap();
    }

    /**
     * Loads (blocking) all subclaims fro database to memory
     */
    public void loadSubclaims() {
        subclaimRepository.addAll(SubclaimDAO.getSubclaims(plugin, plugin.getMongo()));
    }

    /**
     * Saves all subclaims to database
     * @param blocking Blocking
     */
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

    /**
     * If present, returns a players active menu
     * @param player
     * @return
     */
    public SubclaimMenu getActiveMenu(Player player) {
        for (Subclaim subclaim : getActiveEditors().keySet()) {
            final List<SubclaimMenu> menus = getActiveEditors().get(subclaim);

            for (SubclaimMenu menu : menus) {
                if (menu.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                    return menu;
                }
            }
        }

        return null;
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