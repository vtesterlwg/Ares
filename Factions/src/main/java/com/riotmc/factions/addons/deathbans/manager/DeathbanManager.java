package com.riotmc.factions.addons.deathbans.manager;

import com.google.common.collect.Sets;
import com.mongodb.client.model.Filters;
import com.riotmc.commons.base.promise.FailablePromise;
import com.riotmc.commons.base.util.Time;
import com.riotmc.commons.bukkit.util.Scheduler;
import com.riotmc.factions.addons.deathbans.DeathbanAddon;
import com.riotmc.factions.addons.deathbans.dao.DeathbanDAO;
import com.riotmc.factions.addons.deathbans.data.Deathban;
import com.riotmc.factions.addons.deathbans.handler.DeathbanHandler;
import com.riotmc.factions.players.dao.PlayerDAO;
import com.riotmc.factions.players.data.FactionPlayer;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.Set;
import java.util.UUID;

public final class DeathbanManager {
    @Getter public final DeathbanAddon addon;
    @Getter public final DeathbanHandler handler;
    @Getter public final Set<UUID> recentlyKicked;

    public DeathbanManager(DeathbanAddon addon) {
        this.addon = addon;
        this.handler = new DeathbanHandler(this);
        this.recentlyKicked = Sets.newConcurrentHashSet();
    }

    public void getDeathban(UUID uniqueId, FailablePromise<Deathban> promise) {
        new Scheduler(addon.getPlugin()).async(() -> {
            final Deathban deathban = DeathbanDAO.getDeathban(addon.getPlugin().getMongo(), uniqueId);

            new Scheduler(addon.getPlugin()).sync(() -> {
                if (deathban == null) {
                    promise.failure("Player is not deathbanned");
                    return;
                }

                promise.success(deathban);
            }).run();
        }).run();
    }

    public int calculateDeathbanDuration(UUID uniqueId) {
        // TODO: Change duration based on server state, right now we're just always using NORMAL
        FactionPlayer factionPlayer = addon.getPlugin().getPlayerManager().getPlayer(uniqueId);

        if (factionPlayer == null) {
            factionPlayer = PlayerDAO.getPlayer(addon.getPlugin(), addon.getPlugin().getMongo(), Filters.eq("id", uniqueId));

            if (factionPlayer == null) {
                return addon.getConfiguration().getNormalMinDeathban();
            }
        }

        final int playtimeToSec = (int)(factionPlayer.getStats().getPlaytime() / 1000L);
        int duration = addon.getConfiguration().getNormalMinDeathban();

        if (playtimeToSec > addon.getConfiguration().getNormalMinDeathban()) {
            if (playtimeToSec > addon.getConfiguration().getNormalMaxDeathban()) {
                duration = addon.getConfiguration().getNormalMaxDeathban();
            } else {
                duration = playtimeToSec;
            }
        }

        return duration;
    }

    public String getDeathbanMessage(Deathban deathban) {
        if (deathban.isPermanent()) {
            return ChatColor.RED + "You are deathbanned" + ChatColor.RESET + "\n" +
                    ChatColor.YELLOW + "You will be able to join again when the next map begins" + ChatColor.RESET + "\n" +
                    ChatColor.GREEN + "Thank you for playing, see you next map!";
        } else {
            return ChatColor.RED + "You are deathbanned" + ChatColor.RESET + "\n" +
                    ChatColor.YELLOW + "Expires in " + ChatColor.RESET + Time.convertToRemaining(deathban.getTimeUntilUndeathban()) + ChatColor.RESET + "\n" +
                    ChatColor.AQUA + "Bypass this wait by reviving yourself using " + ChatColor.GOLD + "/revive";
        }
    }
}
