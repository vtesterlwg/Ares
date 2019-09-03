package com.playares.factions.addons.stats.listener;

import com.playares.commons.bukkit.event.PlayerDamagePlayerEvent;
import com.playares.factions.addons.events.data.type.AresEvent;
import com.playares.factions.addons.events.data.type.koth.KOTHEvent;
import com.playares.factions.addons.events.data.type.koth.PalaceEvent;
import com.playares.factions.addons.events.event.EventCaptureEvent;
import com.playares.factions.addons.stats.StatsAddon;
import com.playares.factions.factions.data.PlayerFaction;
import com.playares.factions.players.data.FactionPlayer;
import com.playares.services.playerclasses.PlayerClassService;
import com.playares.services.playerclasses.data.Class;
import com.playares.services.playerclasses.data.ClassConsumable;
import com.playares.services.playerclasses.data.cont.ArcherClass;
import com.playares.services.playerclasses.data.cont.BardClass;
import com.playares.services.playerclasses.event.ConsumeClassItemEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public final class StatisticListener implements Listener {
    @Getter public final StatsAddon addon;

    /**
     * Sets playtime
     * @param event Bukkit PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer factionPlayer = addon.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (factionPlayer == null) {
            return;
        }

        factionPlayer.getStatistics().addTimePlayed();
    }

    /**
     * Sets mined ores
     * @param event Bukkit BlockBreakEvent
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

    }

    /**
     * Sets dragon kills
     * @param event Bukkit EntityDeathEvent
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        final LivingEntity entity = event.getEntity();
        final Player killer = entity.getKiller();

        if (!(entity instanceof EnderDragon)) {
            return;
        }

        if (killer == null) {
            return;
        }

        // TODO: Add dragon kill to faction stats

        final PlayerFaction faction = getAddon().getPlugin().getFactionManager().getFactionByPlayer(killer.getUniqueId());

        if (faction != null) {
            faction.getOnlineMembers().forEach(profile -> {
                final Player player = Bukkit.getPlayer(profile.getUniqueId());
                final FactionPlayer factionPlayer = getAddon().getPlugin().getPlayerManager().getPlayer(profile.getUniqueId());

                if (player != null && factionPlayer != null) {
                    if (player.getLocation().getWorld().getEnvironment().equals(World.Environment.THE_END)) {
                        factionPlayer.getStatistics().addDragonKill();
                    }
                }
            });
        } else {
            final FactionPlayer factionPlayer = getAddon().getPlugin().getPlayerManager().getPlayer(killer.getUniqueId());

            if (factionPlayer != null) {
                factionPlayer.getStatistics().addDragonKill();
            }
        }
    }

    /**
     * Sets player kills/deaths
     * @param event Bukkit PlayerDeathEvent
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player killed = event.getEntity();
        final Player killer = killed.getKiller();
        final FactionPlayer killedProfile = getAddon().getPlugin().getPlayerManager().getPlayer(killed.getUniqueId());

        if (killedProfile != null) {
            killedProfile.getStatistics().addDeath();
        }

        if (killer != null) {
            final FactionPlayer killerProfile = getAddon().getPlugin().getPlayerManager().getPlayer(killer.getUniqueId());

            if (killerProfile != null) {
                killerProfile.getStatistics().addKill();
            }
        }
    }

    /**
     * Sets KOTH/Palace captures
     * @param event Ares EventCaptureEvent
     */
    @EventHandler
    public void onEventCapture(EventCaptureEvent event) {
        final PlayerFaction faction = event.getFaction();
        final AresEvent aresEvent = event.getEvent();

        // TODO: Add to faction stats

        faction.getOnlineMembers().forEach(member -> {
            final FactionPlayer profile = getAddon().getPlugin().getPlayerManager().getPlayer(member.getUniqueId());

            if (aresEvent instanceof KOTHEvent) {
                if (aresEvent instanceof PalaceEvent) {
                    profile.getStatistics().addPalaceCapture();
                } else {
                    profile.getStatistics().addKothCapture();
                }
            }
        });
    }

    /**
     * Sets bard affected
     * @param event Ares ConsumeClassItemEvent
     */
    @EventHandler (priority = EventPriority.MONITOR)
    public void onConsume(ConsumeClassItemEvent event) {
        final PlayerClassService playerClassService = (PlayerClassService)getAddon().getPlugin().getService(PlayerClassService.class);

        if (
                playerClassService == null ||
                event.isCancelled() ||
                event.getAffectedPlayers().isEmpty() ||
                (event.getConsumable().getApplicationType().equals(ClassConsumable.ConsumableApplicationType.INDIVIDUAL) ||
                        event.getConsumable().getApplicationType().equals(ClassConsumable.ConsumableApplicationType.FRIENDLY_ONLY))) {

            return;

        }

        final Player player = event.getPlayer();
        final Class playerClass = playerClassService.getClassManager().getCurrentClass(player);

        if (!(playerClass instanceof BardClass)) {
            return;
        }

        final FactionPlayer profile = getAddon().getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile != null) {
            profile.getStatistics().addBardTotalAffected(event.getAffectedPlayers().size());
        }
    }

    /**
     * Sets max archer distance
     * @param event Ares PlayerDamagePlayerEvent
     */
    @EventHandler
    public void onPlayerDamagePlayer(PlayerDamagePlayerEvent event) {
        if (event.isCancelled() || !event.getType().equals(PlayerDamagePlayerEvent.DamageType.PROJECTILE)) {
            return;
        }

        final PlayerClassService playerClassService = (PlayerClassService)getAddon().getPlugin().getService(PlayerClassService.class);

        if (playerClassService == null) {
            return;
        }

        final Player player = event.getDamager();
        final Class playerClass = playerClassService.getClassManager().getCurrentClass(player);

        if (!(playerClass instanceof ArcherClass)) {
            return;
        }

        final FactionPlayer profile = getAddon().getPlugin().getPlayerManager().getPlayer(player.getUniqueId());
        final double distance = player.getLocation().distance(event.getDamaged().getLocation());

        if (profile != null && profile.getStatistics().isLongestArcherShot(distance)) {
            profile.getStatistics().setArcherLongestShot(distance);
            player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "New Record!" + ChatColor.RESET + " "
            + ChatColor.GRAY + "Your new record for longest Archer shot is " + ChatColor.AQUA + String.format("%.2f", distance) + ChatColor.GRAY + " blocks!");
        }
    }
}
