package com.playares.services.essentials.vanish;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Worlds;
import com.playares.services.essentials.EssentialsService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class VanishHandler implements Listener {
    @Getter public final EssentialsService essentials;

    public VanishHandler(EssentialsService essentials) {
        this.essentials = essentials;
        essentials.registerListener(this);

        essentials.getOwner().getProtocol().addPacketListener(
                new PacketAdapter(essentials.getOwner(), ListenerPriority.LOWEST,
                        PacketType.Play.Server.NAMED_SOUND_EFFECT, PacketType.Play.Server.BLOCK_ACTION) {

                    @Override
                    public void onPacketSending(PacketEvent event) {
                        final Player player = event.getPlayer();

                        if (player == null) {
                            return;
                        }

                        if (essentials.getVanishManager().isVanished(player)) {
                            event.setCancelled(true);
                        }
                    }
                }
        );
    }

    public void hidePlayer(Player player, boolean playEffect) {
        essentials.getVanishManager().getVanished().add(player.getUniqueId());

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!essentials.getVanishManager().shouldSee(viewer, player)) {
                viewer.hidePlayer(player);
            }
        }

        if (playEffect) {
            Worlds.spawnParticle(player.getLocation(), Particle.SPELL_WITCH, 20);
        }

        Logger.print(player.getName() + " vanished");
    }

    public void showPlayer(Player player, boolean playEffect) {
        essentials.getVanishManager().getVanished().remove(player.getUniqueId());

        Bukkit.getOnlinePlayers().forEach(viewer -> {
            if (essentials.getVanishManager().shouldSee(viewer, player)) {
                viewer.showPlayer(player);
            }
        });

        if (playEffect) {
            Worlds.spawnParticle(player.getLocation(), Particle.SPELL_WITCH, 20);
        }

        Logger.print(player.getName() + " unvanished");
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        final LivingEntity target = event.getTarget();

        if (!(target instanceof Player)) {
            return;
        }

        final Player player = (Player)target;

        if (getEssentials().getVanishManager().isVanished(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final boolean vanish = player.hasPermission("essentials.vanish");

        if (vanish) {
            hidePlayer(player, false);
            return;
        }

        essentials.getVanishManager().getVanished().forEach(vanished -> {
            final Player vanishedPlayer = Bukkit.getPlayer(vanished);

            if (vanishedPlayer != null) {
                player.hidePlayer(vanishedPlayer);
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        Bukkit.getOnlinePlayers().forEach(viewer -> {
            viewer.showPlayer(player);
            player.showPlayer(viewer);
        });
    }
}