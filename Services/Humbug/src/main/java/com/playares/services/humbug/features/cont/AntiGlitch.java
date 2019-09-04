package com.playares.services.humbug.features.cont;

import com.playares.commons.bukkit.event.PlayerBigMoveEvent;
import com.playares.commons.bukkit.util.Players;
import com.playares.services.humbug.HumbugService;
import com.playares.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.potion.PotionEffectType;

public final class AntiGlitch implements HumbugModule, Listener {
    @Getter public final HumbugService humbug;
    @Getter @Setter public boolean enabled;
    @Getter @Setter public boolean disablePearlClipping;
    @Getter @Setter public boolean disableElytraClipping;

    public AntiGlitch(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Override
    public void loadValues() {
        final YamlConfiguration config = getHumbug().getOwner().getConfig("humbug");

        this.enabled = config.getBoolean("anti-glitch.enabled");
        this.disablePearlClipping = config.getBoolean("anti-glitch.disable-pearl-clipping");
        this.disableElytraClipping = config.getBoolean("anti-glitch.disable-elytra-clipping");
    }

    @Override
    public String getName() {
        return "Anti Glitch";
    }

    @Override
    public void start() {
        this.humbug.getOwner().registerListener(this);
    }

    @Override
    public void stop() {
        PlayerTeleportEvent.getHandlerList().unregister(this);
        PlayerBigMoveEvent.getHandlerList().unregister(this);
        PlayerToggleFlightEvent.getHandlerList().unregister(this);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!isEnabled() || !isDisablePearlClipping() || event.isCancelled()) {
            return;
        }

        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            return;
        }

        if (event.getTo().getBlock() != null &&
                (event.getTo().getBlock().getType().name().contains("FENCE") || event.getTo().getBlock().getType().name().contains("PANE"))) {

            return;

        }

        final Player player = event.getPlayer();
        final double x = event.getTo().getBlockX() + 0.5;
        final double z = event.getTo().getBlockZ() + 0.5;
        final float yaw = event.getTo().getYaw();
        final float pitch = event.getTo().getPitch();
        final double y = (player.hasPotionEffect(PotionEffectType.LEVITATION) && event.getTo().getY() >= event.getFrom().getY()) ? event.getTo().getBlockY() - 1.0 : event.getTo().getBlockY();
        final World world = event.getTo().getWorld();

        event.setTo(new Location(world, x, y, z, yaw, pitch));
    }

    @EventHandler
    public void onPlayerToggleElytra(EntityToggleGlideEvent event) {
        if (!isEnabled() || !isDisableElytraClipping() || event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getEntity();

        if (!event.isGliding()) {
            return;
        }

        final Block ground = Players.getBlockBelow(player, 3);

        if (ground != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onElytraFly(PlayerBigMoveEvent event) {
        if (!isEnabled() || !isDisableElytraClipping() || event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();

        if (
                !player.isGliding() ||
                !player.getGameMode().equals(GameMode.SURVIVAL) ||
                player.getInventory().getChestplate() == null ||
                !player.getInventory().getChestplate().getType().equals(Material.ELYTRA)) {

            return;

        }

        final Block ground = Players.getBlockBelow(player, 3);

        if (ground != null) {
            player.setGliding(false);
        }
    }
}