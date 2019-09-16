package com.playares.arena.listener;

import com.playares.arena.Arenas;
import com.playares.arena.player.ArenaPlayer;
import com.playares.arena.util.ArenaUtils;
import com.playares.services.playerclasses.PlayerClassService;
import com.playares.services.playerclasses.data.Class;
import com.playares.services.playerclasses.data.ClassConsumable;
import com.playares.services.playerclasses.data.cont.BardClass;
import com.playares.services.playerclasses.event.ConsumeClassItemEvent;
import com.playares.services.playerclasses.event.PlayerClassDeactivateEvent;
import com.playares.services.playerclasses.event.PlayerClassReadyEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

@AllArgsConstructor
public final class ClassListener implements Listener {
    @Getter public final Arenas plugin;

    @EventHandler
    public void onClassReady(PlayerClassReadyEvent event) {
        final Player player = event.getPlayer();
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player);

        if (profile == null) {
            return;
        }

        if (profile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)) {
            event.getPlayerClass().activate(player, false);
        }
    }

    @EventHandler
    public void onClassDeactivate(PlayerClassDeactivateEvent event) {
        event.setMessage(false);
    }

    @EventHandler
    public void onClassConsume(ConsumeClassItemEvent event) {
        final PlayerClassService classService = (PlayerClassService)plugin.getService(PlayerClassService.class);

        if (classService == null) {
            return;
        }

        final Player player = event.getPlayer();
        final Class playerClass = classService.getClassManager().getCurrentClass(player);
        final ArenaPlayer profile = plugin.getPlayerManager().getPlayer(player);

        if (!profile.getStatus().equals(ArenaPlayer.PlayerStatus.INGAME)) {
            player.sendMessage(ChatColor.RED + "You are not in a match");
            event.setCancelled(true);
            return;
        }

        if (!(playerClass instanceof BardClass)) {
            return;
        }

        final BardClass bard = (BardClass)playerClass;

        if (event.getConsumable().getApplicationType().equals(ClassConsumable.ConsumableApplicationType.INDIVIDUAL)) {
            return;
        }

        if (event.getConsumable().getApplicationType().equals(ClassConsumable.ConsumableApplicationType.ALL)) {
            final List<Player> friendlies = ArenaUtils.getNearbyFriendlies(plugin, player, bard.getRange());
            final List<Player> enemies = ArenaUtils.getNearbyEnemies(plugin, player, bard.getRange());

            friendlies.forEach(friendly -> event.getAffectedPlayers().put(friendly.getUniqueId(), true));
            enemies.forEach(enemy -> event.getAffectedPlayers().put(enemy.getUniqueId(), false));

            return;
        }

        if (event.getConsumable().getApplicationType().equals(ClassConsumable.ConsumableApplicationType.FRIENDLY_ONLY)) {
            final List<Player> friendlies = ArenaUtils.getNearbyFriendlies(plugin, player, bard.getRange());
            friendlies.forEach(friendly -> event.getAffectedPlayers().put(friendly.getUniqueId(), true));
            return;
        }

        if (event.getConsumable().getApplicationType().equals(ClassConsumable.ConsumableApplicationType.ENEMY_ONLY)) {
            final List<Player> enemies = ArenaUtils.getNearbyEnemies(plugin, player, bard.getRange());
            enemies.forEach(enemy -> event.getAffectedPlayers().put(enemy.getUniqueId(), false));
        }
    }
}