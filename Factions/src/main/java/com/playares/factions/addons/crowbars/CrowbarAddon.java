package com.playares.factions.addons.crowbars;

import com.google.common.collect.Sets;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Players;
import com.playares.factions.Factions;
import com.playares.factions.addons.Addon;
import com.playares.factions.claims.data.DefinedClaim;
import com.playares.factions.factions.data.Faction;
import com.playares.factions.factions.data.PlayerFaction;
import com.playares.factions.factions.data.ServerFaction;
import com.playares.services.customitems.CustomItemService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

public final class CrowbarAddon implements Addon, Listener {
    @Getter public final Factions plugin;
    @Getter @Setter public boolean enabled;
    @Getter public final Set<EntityType> allowedTypes;

    public CrowbarAddon(Factions plugin) {
        this.plugin = plugin;
        this.allowedTypes = Sets.newHashSet();
    }

    @Override
    public String getName() {
        return "Crowbars";
    }

    @Override
    public void prepare() {
        final YamlConfiguration config = getPlugin().getConfig("config");
        final List<String> names = config.getStringList("crowbars.allowed-spawner-types");

        this.enabled = config.getBoolean("crowbars.enabled");

        for (String name : names) {
            final EntityType type;

            try {
                type = EntityType.valueOf(name);
            } catch (IllegalArgumentException ex) {
                Logger.error("Invalid entity type '" + name + "' for crowbar entity types");
                continue;
            }

            allowedTypes.add(type);
        }
    }

    @Override
    public void start() {
        final CustomItemService customItemService = (CustomItemService)plugin.getService(CustomItemService.class);

        if (customItemService == null) {
            setEnabled(false);
            Logger.error("Failed to obtain the Custom Item Service, Crowbar Addon will not run properly and has been disabled");
            return;
        }

        customItemService.registerNewItem(new CrowbarItem());
        plugin.registerListener(this);
    }

    @Override
    public void stop() {
        PlayerInteractEvent.getHandlerList().unregister(this);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlockPlaced();
        final ItemStack item = event.getItemInHand();

        if (block == null || !block.getType().equals(Material.MOB_SPAWNER)) {
            return;
        }

        final String displayName = (item.getItemMeta() != null) ? item.getItemMeta().getDisplayName() : null;
        final BlockState state = block.getState();
        final CreatureSpawner spawner = (CreatureSpawner)state;

        if (displayName == null) {
            return;
        }

        final String mobEntityType = displayName.toUpperCase().replace(" ", "_").replace("_SPAWNER", "");
        final EntityType type;

        try {
            type = EntityType.valueOf(mobEntityType);
        } catch (IllegalArgumentException ex) {
            return;
        }

        spawner.setSpawnedType(type);
        state.update();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isEnabled()) {
            return;
        }

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (event.getClickedBlock() == null || !event.getClickedBlock().getType().equals(Material.MOB_SPAWNER)) {
            return;
        }

        final Player player = event.getPlayer();
        final ItemStack hand = player.getInventory().getItemInMainHand();
        final Block block = event.getClickedBlock();
        final CreatureSpawner spawner = ((CreatureSpawner)block.getState());
        final World.Environment env = block.getLocation().getWorld().getEnvironment();
        final CustomItemService customItemService = (CustomItemService)getPlugin().getService(CustomItemService.class);

        if (customItemService == null) {
            return;
        }

        if (hand == null) {
            return;
        }

        customItemService.getItem(hand).ifPresent(customItem -> customItemService.getClazz(customItem).ifPresent(clazz -> {
            if (clazz != CrowbarItem.class) {
                return;
            }

            if (!env.equals(World.Environment.NORMAL)) {
                player.sendMessage(ChatColor.RED + "Crowbars can only be used in the Overworld");
                return;
            }

            if (!allowedTypes.contains(spawner.getSpawnedType())) {
                player.sendMessage(ChatColor.RED + StringUtils.capitaliseAllWords(spawner.getSpawnedType().name().toLowerCase().replace("_", " ")) + " Spawners can not be broken by a crowbar");
                return;
            }

            final DefinedClaim inside = getPlugin().getClaimManager().getClaimAt(new BLocatable(block));

            if (inside != null) {
                final Faction owner = getPlugin().getFactionManager().getFactionById(inside.getOwnerId());

                if (owner instanceof ServerFaction) {
                    final ServerFaction serverFaction = (ServerFaction)owner;
                    player.sendMessage(ChatColor.RED + "This land is owned by " + ChatColor.RESET + serverFaction.getDisplayName());
                    return;
                }

                if (owner instanceof PlayerFaction) {
                    final PlayerFaction playerFaction = (PlayerFaction)owner;

                    if (playerFaction.getMember(player.getUniqueId()) == null && !playerFaction.isRaidable()) {
                        player.sendMessage(ChatColor.RED + "This land is owned by " + ChatColor.RESET + playerFaction.getName());
                        return;
                    }
                }
            }

            final ItemStack item = new ItemBuilder()
                    .setMaterial(Material.MOB_SPAWNER)
                    .setName(StringUtils.capitaliseAllWords(spawner.getSpawnedType().name().toLowerCase().replace("_", " ")) + " Spawner")
                    .build();

            block.breakNaturally();
            block.getWorld().dropItem(block.getLocation(), item);

            Players.spawnParticle(player, player.getEyeLocation(), Particle.EXPLOSION_NORMAL, 1);
            Players.playSound(player, Sound.ENTITY_ITEM_BREAK);
            player.getInventory().setItemInMainHand(null);
        }));
    }
}
