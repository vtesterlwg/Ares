package com.playares.arena.listener;

import com.google.common.collect.Lists;
import com.playares.arena.Arenas;
import com.playares.arena.match.Match;
import com.playares.arena.match.cont.DuelMatch;
import com.playares.arena.match.cont.TeamMatch;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.player.PlayerStatus;
import com.playares.arena.team.Team;
import com.playares.commons.base.util.Time;
import com.playares.services.classes.data.effects.ClassEffectable;
import com.playares.services.classes.event.BardEffectEvent;
import com.playares.services.classes.event.ClassConsumeEvent;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public final class ClassListener implements Listener {
    @Getter
    public final Arenas plugin;

    public ClassListener(Arenas plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBardEffect(BardEffectEvent event) {
        final Player player = event.getPlayer();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final ClassEffectable consumable = event.getConsumable();

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            event.setCancelled(true);
            return;
        }

        if (!profile.getStatus().equals(PlayerStatus.INGAME) || profile.getMatch() == null) {
            event.setCancelled(true);
            return;
        }

        if (profile.getRemainingConsumableCooldown(consumable) > 0L) {
            player.sendMessage(ChatColor.RED + "You can not use this consumable for another " + ChatColor.RED + "" + ChatColor.BOLD +
                    Time.convertToDecimal(profile.getRemainingConsumableCooldown(consumable)) + ChatColor.RED + "s");

            event.setCancelled(true);
            return;
        }

        final Match match = profile.getMatch();

        if (match instanceof DuelMatch) {
            final List<Player> toRemove = Lists.newArrayList();

            for (Player affected : event.getAffectedEntities()) {
                if (!affected.getUniqueId().equals(player.getUniqueId())) {
                    toRemove.add(affected);
                }
            }

            event.getAffectedEntities().removeAll(toRemove);
            profile.getClassCooldowns().put(consumable.getMaterial(), (Time.now() + (consumable.getCooldown() * 1000L)));
            return;
        }

        if (match instanceof TeamMatch) {
            final Team team = profile.getTeam();
            final List<Player> toRemove = Lists.newArrayList();

            if (team == null) {
                player.sendMessage(ChatColor.RED + "You are not on a team");
                event.setCancelled(true);
                return;
            }

            for (Player affected : event.getAffectedEntities()) {
                final ArenaPlayer teammate = plugin.getPlayerManager().getPlayer(affected.getUniqueId());

                if (!team.getMembers().contains(teammate) || !teammate.getStatus().equals(PlayerStatus.INGAME)) {
                    toRemove.add(affected);
                }
            }

            event.getAffectedEntities().removeAll(toRemove);
            profile.getClassCooldowns().put(consumable.getMaterial(), (Time.now() + (consumable.getCooldown() * 1000L)));
        }
    }

    @EventHandler
    public void onConsume(ClassConsumeEvent event) {
        final Player player = event.getPlayer();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final ClassEffectable consumable = event.getConsumable();

        if (profile == null) {
            player.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            event.setCancelled(true);
            return;
        }

        if (!profile.getStatus().equals(PlayerStatus.INGAME) || profile.getMatch() == null) {
            event.setCancelled(true);
            return;
        }

        if (profile.getRemainingConsumableCooldown(consumable) > 0L) {
            player.sendMessage(ChatColor.RED + "You can not use this consumable for another " + ChatColor.RED + "" + ChatColor.BOLD +
                    Time.convertToDecimal(profile.getRemainingConsumableCooldown(consumable)) + ChatColor.RED + "s");

            event.setCancelled(true);
            return;
        }

        profile.getClassCooldowns().put(consumable.getMaterial(), (Time.now() + (consumable.getCooldown() * 1000L)));
    }
}