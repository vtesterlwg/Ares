package com.playares.services.humbug.features.cont;

import com.playares.commons.bukkit.util.Scheduler;
import com.playares.services.humbug.HumbugService;
import com.playares.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;

public final class Elevators implements HumbugModule, Listener {
    @Getter public final HumbugService humbug;
    @Getter @Setter public boolean enabled;

    public Elevators(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Override
    public void loadValues() {
        final YamlConfiguration config = getHumbug().getOwner().getConfig("humbug");
        this.enabled = config.getBoolean("elevators.enabled");
    }

    @Override
    public String getName() {
        return "Elevators";
    }

    @Override
    public void start() {
        this.humbug.getOwner().registerListener(this);
    }

    @Override
    public void stop() {
        VehicleExitEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (!isEnabled()) {
            return;
        }

        if (isElevator(event.getVehicle())) {
            final Location destination = getDestination(event.getVehicle().getLocation());

            if (destination != null) {
                destination.setYaw(event.getExited().getLocation().getYaw());
                destination.setPitch(event.getExited().getLocation().getPitch());

                new Scheduler(getHumbug().getOwner()).sync(() -> event.getExited().teleport(destination)).delay(1L).run();
            }
        }
    }

    private boolean isElevator(Vehicle vehicle) {
        if (!(vehicle instanceof Minecart)) {
            return false;
        }

        final Block above = vehicle.getLocation().getBlock().getRelative(BlockFace.UP);
        return above != null && above.getType().isBlock();
    }

    private Location getDestination(Location location) {
        for (int y = location.getBlockY(); y < location.getWorld().getMaxHeight(); y++) {
            final Block block = location.getWorld().getBlockAt(location.getBlockX(), y, location.getBlockZ());

            if (block != null && block.getType().equals(Material.AIR)) {
                return block.getLocation();
            }
        }

        return null;
    }
}