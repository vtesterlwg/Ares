package com.riotmc.commons.bukkit.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.riotmc.commons.bukkit.RiotPlugin;
import com.riotmc.commons.bukkit.logger.Logger;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@ParametersAreNonnullByDefault
public final class Players {
    public static void playSound(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
    }

    public static void playSound(Player player, Location location, Sound sound) {
        player.playSound(location, sound, 1.0F, 1.0F);
    }

    public static void sendBlockChange(Player player, Location location, Material material) {
        player.sendBlockChange(location, material.createBlockData());
    }

    public static void spawnEffect(Player player, Location location, Effect effect) {
        player.playEffect(location, effect, null);
    }

    public static void spawnEffect(Player player, Location location, Effect effect, int amount) {
        Preconditions.checkArgument(amount > 0, "Amount must be greater than 0");
        player.playEffect(location, effect, amount);
    }

    public static void spawnParticle(Player player, Location location, Particle particle, int amount) {
        Preconditions.checkArgument(amount > 0, "Amount must be greater than 0");
        player.spawnParticle(particle, location, amount);
    }

    public static void sendSignChange(Player player, Location location, String[] lines) {
        Preconditions.checkArgument(lines.length > 4, "Signs can only hold 4 lines");
        player.sendSignChange(location, lines);
    }

    public static void sendActionBar(Player player, String text) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
    }

    public static void teleportWithVehicle(RiotPlugin plugin, Player player, Location location) {
        final Entity vehicle = player.getVehicle();

        if (vehicle == null) {
            player.teleport(location);
            return;
        }

        player.teleport(location);

        new Scheduler(plugin).sync(() -> {
            vehicle.teleport(location);

            new Scheduler(plugin).sync(() -> vehicle.addPassenger(player)).delay(5L).run();
        }).delay(5L).run();
    }

    public static void sendSignChange(RiotPlugin plugin, Player player, Location location, String[] lines, long duration) {
        final Block block = location.getBlock();

        if (!(block.getState() instanceof Sign)) {
            throw new IllegalArgumentException("Block must be a sign");
        }

        final Sign sign = (Sign)block.getState();
        final String[] stored = sign.getLines();

        sendSignChange(player, location, lines);

        new Scheduler(plugin).sync(() -> {
            if (!player.isOnline() || !(location.getBlock().getState() instanceof Sign)) {
                return;
            }

            sendSignChange(player, location, stored);
        }).delay(duration).run();
    }

    public static void resetWalkSpeed(Player player) {
        player.setWalkSpeed(0.2F);
    }

    public static void resetFlySpeed(Player player) {
        player.setFlySpeed(0.2F);
    }

    public static void resetHealth(Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setExhaustion(0);
        player.setFallDistance(0.0F);
        player.setFireTicks(0);
        player.setNoDamageTicks(20);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    }

    public static void sendTablist(ProtocolManager manager, Player player, String header, String footer) {
        final PacketContainer packet = manager.createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);

        packet.getChatComponents()
                .write(0, WrappedChatComponent.fromText(header))
                .write(1, WrappedChatComponent.fromText(footer));

        try {
            manager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            Logger.error("Failed to send tab", e);
        }
    }

    public static Block getBlockBelow(Player player, int maxDistance) {
        final int max = player.getLocation().getBlockY() - maxDistance;

        for (int y = player.getLocation().getBlockY(); y > max; y--) {
            final Block block = player.getLocation().getWorld().getBlockAt(player.getLocation().getBlockX(), y, player.getLocation().getBlockZ());

            if (block == null || !block.getType().isSolid()) {
                continue;
            }

            return block;
        }

        return null;
    }

    public static List<Block> getNearbyBlocks(Player player, int radius) {
        final List<Block> result = Lists.newArrayList();
        final Location loc = player.getLocation();
        final int minX = loc.getBlockX() - radius;
        final int minY = loc.getBlockY() - radius;
        final int minZ = loc.getBlockZ() - radius;
        final int maxX = loc.getBlockX() + radius;
        final int maxY = loc.getBlockY() + radius;
        final int maxZ = loc.getBlockZ() + radius;

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    final Block block = loc.getWorld().getBlockAt(x, y, z);
                    result.add(block);
                }
            }
        }

        return result;
    }

    private Players() {
        throw new UnsupportedOperationException("This class can not be instantiated");
    }
}
