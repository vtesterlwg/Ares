package com.riotmc.factions.addons.mining;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Worlds;
import com.riotmc.factions.Factions;
import com.riotmc.factions.addons.Addon;
import com.riotmc.factions.claims.DefinedClaim;
import com.riotmc.factions.factions.Faction;
import com.riotmc.factions.factions.PlayerFaction;
import com.riotmc.factions.factions.ServerFaction;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public final class MiningAddon implements Addon, Listener {
    @Getter
    public final Factions plugin;

    @Getter
    public boolean enabled;

    @Getter
    public final Set<Findable> findables;

    private final Random random;

    public MiningAddon(Factions plugin) {
        this.plugin = plugin;
        this.findables = Sets.newHashSet();
        this.random = new Random();
    }

    @Override
    public String getName() {
        return "Mining";
    }

    @Override
    public void prepare() {
        if (!findables.isEmpty()) {
            findables.clear();
        }

        final YamlConfiguration config = plugin.getConfig("config");

        this.enabled = config.getBoolean("mining.enabled");

        for (String matName : config.getConfigurationSection("mining.findables").getKeys(false)) {
            final Material material = Material.matchMaterial(matName);

            if (material == null) {
                Logger.warn("Failed to load findable '" + matName + "', Material not found");
                continue;
            }

            final Material foundIn = Material.matchMaterial(config.getString("mining.findables." + matName + ".found-in"));

            if (foundIn == null) {
                Logger.warn("Failed to load findable '" + matName + "', Found-in Material not found");
                continue;
            }

            final World.Environment environment;

            try {
                environment = World.Environment.valueOf(config.getString("mining.findables." + matName + ".world"));
            } catch (IllegalArgumentException ex) {
                Logger.warn("Failed to load findable '" + matName + "', Environment not found");
                continue;
            }

            final double rate = config.getDouble("mining.findables." + matName + ".rate");
            final int minHeight = config.getInt("mining.findables." + matName + ".height.min");
            final int maxHeight = config.getInt("mining.findables." + matName + ".height.max");
            final int minVeinSize = config.getInt("mining.findables." + matName + ".vein-size.min");
            final int maxVeinSize = config.getInt("mining.findables." + matName + ".vein-size.max");
            final boolean broadcast = config.getBoolean("mining.findables." + matName + ".broadcast");
            final boolean message = config.getBoolean("mining.findables." + matName + ".message");
            final ChatColor color = ChatColor.getByChar(config.getString("mining.findables." + matName + ".color"));

            if (color == null) {
                Logger.warn("Failed to load findable '" + matName + "', Color not found");
                continue;
            }

            final Findable findable = new Findable(
                    material,
                    foundIn,
                    rate,
                    environment,
                    minHeight,
                    maxHeight,
                    minVeinSize,
                    maxVeinSize,
                    broadcast,
                    message,
                    color);

            findables.add(findable);
        }

        Logger.print("Loaded " + findables.size() + " Findables");
    }

    @Override
    public void start() {
        plugin.registerListener(this);
    }

    @Override
    public void stop() {
        BlockBreakEvent.getHandlerList().unregister(this);
    }

    public ImmutableList<Findable> getFindablesAt(Block block) {
        final List<Findable> result = findables
                .stream()
                .filter(findable -> findable.isObtainable(block))
                .sorted(Comparator.comparingDouble(Findable::getRate))
                .collect(Collectors.toList());

        return ImmutableList.copyOf(result);
    }

    public void run(Player player, ItemStack hand, Block block) {
        final List<Findable> possibilities = getFindablesAt(block);

        if (possibilities.isEmpty()) {
            return;
        }

        double pull = random.nextDouble();
        final double luckIncrease = (player.hasPotionEffect(PotionEffectType.LUCK) ? 0.005 * (player.getPotionEffect(PotionEffectType.LUCK).getAmplifier() + 1) : 0.0);
        final boolean isZuergner = (player.hasPotionEffect(PotionEffectType.FAST_DIGGING) &&
                player.getPotionEffect(PotionEffectType.FAST_DIGGING).getAmplifier() > 0 &&
                hand != null && hand.getEnchantmentLevel(Enchantment.DIG_SPEED) >= 5);

        for (Findable possible : possibilities) {
            double rate = possible.getRate();

            rate += luckIncrease;

            if (isZuergner) {
                rate += 0.005;
            }

            if (pull <= rate) {
                if (spawnVein(possible, player, block)) {
                    break;
                }
            }
        }
    }

    public boolean spawnVein(Findable findable, Player player, Block block) {
        int size = random.nextInt(findable.maxVeinSize);

        if (size < findable.minVeinSize) {
            size = findable.minVeinSize;
        }

        final List<Block> blocks = Lists.newArrayList();
        int x = block.getX() + random.nextInt(2);
        int y = block.getY() + random.nextInt(2);
        int z = block.getZ() + random.nextInt(2);
        int attempts = 0;

        while (blocks.size() < size && attempts < 50) {
            attempts++;

            final int direction = random.nextInt(6);

            switch (direction) {
                case 1: x++; break;
                case 2: y++; break;
                case 3: z++; break;
                case 4: x--; break;
                case 5: y--; break;
                case 6: z--; break;
            }

            final Block toAdd = block.getWorld().getBlockAt(x, y, z);
            final DefinedClaim inside = plugin.getClaimManager().getClaimAt(new BLocatable(toAdd));

            if (!toAdd.getType().equals(findable.getFoundType())) {
                continue;
            }

            if (inside != null) {
                final Faction owner = plugin.getFactionManager().getFactionById(inside.getOwnerId());

                if (owner instanceof ServerFaction) {
                    continue;
                }

                if (owner instanceof PlayerFaction) {
                    final PlayerFaction pf = (PlayerFaction)owner;

                    if (pf.getMember(player.getUniqueId()) == null) {
                        continue;
                    }
                }
            }

            blocks.add(toAdd);
        }

        final PlayerFindOreEvent event = new PlayerFindOreEvent(player, findable.getType(), blocks);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        event.getBlocks().forEach(b -> {
            Worlds.playSound(b.getLocation(), Sound.BLOCK_STONE_BREAK);
            Worlds.spawnParticle(b.getLocation(), Particle.VILLAGER_HAPPY, 10, 0.5);
            b.setType(findable.getType());
        });

        if (findable.isBroadcast()) {
            Bukkit.broadcastMessage("[AM] " + findable.getColor() + player.getName() + " found " + event.getBlocks().size() + " " + findable.getName());
        }

        if (findable.isMessage()) {
            player.sendMessage("[AM] " + findable.getColor() + "You found " + event.getBlocks().size() + " " + findable.getName());
        }

        return true;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final ItemStack hand = player.getInventory().getItemInMainHand();

        if (!isEnabled()) {
            return;
        }

        if (block == null || hand == null) {
            return;
        }

        run(player, hand, block);
    }
}