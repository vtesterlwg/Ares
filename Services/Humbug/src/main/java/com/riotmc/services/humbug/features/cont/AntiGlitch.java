package com.riotmc.services.humbug.features.cont;

import com.riotmc.commons.bukkit.event.PlayerBigMoveEvent;
import com.riotmc.commons.bukkit.util.Players;
import com.riotmc.services.humbug.HumbugService;
import com.riotmc.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public final class AntiGlitch implements HumbugModule, Listener {
    @Getter
    public final HumbugService humbug;

    @Getter @Setter
    public boolean enabled;

    @Getter @Setter
    public boolean disablePearlClipping;

    @Getter @Setter
    public boolean disableElytraClipping;

    @Getter @Setter
    public boolean disableSwimClipping;

    public AntiGlitch(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Override
    public void loadValues() {
        this.enabled = humbug.getHumbugConfig().getBoolean("anti-glitch.enabled");
        this.disablePearlClipping = humbug.getHumbugConfig().getBoolean("anti-glitch.disable-pearl-clipping");
        this.disableElytraClipping = humbug.getHumbugConfig().getBoolean("anti-glitch.disable-elytra-clipping");
        this.disableSwimClipping = humbug.getHumbugConfig().getBoolean("anti-glitch.disable-swim-clipping");
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
        if (!isEnabled() || !isDisableElytraClipping() || event.isCancelled()) {
            return;
        }

        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            return;
        }

        final Location fixed = new Location(event.getTo().getWorld(),
                event.getTo().getBlockX() + 0.5, event.getTo().getBlockY(),
                event.getTo().getBlockZ() + 0.5,
                event.getTo().getYaw(),
                event.getTo().getPitch());

        event.setTo(fixed);
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

    @EventHandler
    public void onSwim(PlayerBigMoveEvent event) {
        /*
            This currently does not work because the swimming animation can be easily overrided by holding the sprint key
            Perhaps this can be corrected in the client if not by the Paper team
         */

        /*if (!isEnabled() || !isDisableSwimClipping() || event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();

        if (
                !player.isSwimming() ||
                !player.getGameMode().equals(GameMode.SURVIVAL) ||
                player.getLocation().getPitch() <= 45.0) {

            return;

        }

        final Block below = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

        if (!below.isLiquid()) {
            Bukkit.broadcastMessage("in block!");
            player.setSprinting(false);
            player.setSwimming(false);
        }*/
    }
}