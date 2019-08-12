package com.playares.factions.listener;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.event.PlayerBigMoveEvent;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.util.Items;
import com.playares.factions.Factions;
import com.playares.factions.claims.data.DefinedClaim;
import com.playares.factions.claims.world.WorldLocation;
import com.playares.factions.event.PlayerChangeClaimEvent;
import com.playares.factions.factions.data.Faction;
import com.playares.factions.factions.data.PlayerFaction;
import com.playares.factions.factions.data.ServerFaction;
import com.playares.factions.players.data.FactionPlayer;
import com.playares.factions.timers.PlayerTimer;
import com.playares.factions.timers.cont.player.EnderpearlTimer;
import com.playares.factions.timers.cont.player.ProtectionTimer;
import com.playares.factions.util.FactionUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class ClaimListener implements Listener {
    @Getter public final Factions plugin;

    public ClaimListener(Factions plugin) {
        this.plugin = plugin;
    }

    private void handlePlayerBlockMods(Player player, Block block, Cancellable event) {
        final DefinedClaim claim = plugin.getClaimManager().getClaimAt(new BLocatable(block));
        final boolean admin = player.hasPermission("factions.admin");

        if (claim == null) {
            return;
        }

        final Faction faction = plugin.getFactionManager().getFactionById(claim.getOwnerId());

        if (faction == null) {
            return;
        }

        if (faction instanceof ServerFaction && !admin) {
            final ServerFaction sf = (ServerFaction)faction;

            player.sendMessage(ChatColor.RED + "This land is owned by " + ChatColor.RESET + sf.getDisplayName());
            event.setCancelled(true);
        } else if (faction instanceof PlayerFaction) {
            final PlayerFaction pf = (PlayerFaction)faction;

            if (!pf.isRaidable() && pf.getMember(player.getUniqueId()) == null && !admin) {
                player.sendMessage(ChatColor.RED + "This land is owned by " + ChatColor.RESET + pf.getName());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        handlePlayerBlockMods(event.getPlayer(), event.getBlock(), event);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        handlePlayerBlockMods(event.getPlayer(), event.getBlock(), event);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        handlePlayerBlockMods(event.getPlayer(), event.getBlockClicked(), event);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        handlePlayerBlockMods(event.getPlayer(), event.getBlockClicked(), event);
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        final Block piston = event.getBlock();
        final DefinedClaim pistonClaim = plugin.getClaimManager().getClaimAt(new BLocatable(piston));

        for (Block affected : event.getBlocks()) {
            final DefinedClaim affectedClaim = plugin.getClaimManager().getClaimAt(new BLocatable(affected));

            if (pistonClaim == null && affectedClaim != null) {
                event.setCancelled(true);
                return;
            }

            else if (pistonClaim != null && affectedClaim == null) {
                event.setCancelled(true);
                return;
            }

            else if (pistonClaim != null && !pistonClaim.getUniqueId().equals(affectedClaim.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        final Block piston = event.getBlock();
        final DefinedClaim pistonClaim = plugin.getClaimManager().getClaimAt(new BLocatable(piston));

        for (Block affected : event.getBlocks()) {
            final DefinedClaim affectedClaim = plugin.getClaimManager().getClaimAt(new BLocatable(affected));

            if (pistonClaim == null && affectedClaim != null) {
                event.setCancelled(true);
                return;
            }

            else if (pistonClaim != null && affectedClaim == null) {
                event.setCancelled(true);
                return;
            }

            else if (pistonClaim != null && !pistonClaim.getUniqueId().equals(affectedClaim.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Action action = event.getAction();
        final Block block = event.getClickedBlock();
        final boolean admin = player.hasPermission("factions.admin");

        if (block == null || (block.getType().equals(Material.AIR))) {
            return;
        }

        if (!Items.isInteractable(block.getType())) {
            return;
        }

        final DefinedClaim inside = plugin.getClaimManager().getClaimAt(new BLocatable(block));

        if (inside == null) {
            return;
        }

        final Faction owner = plugin.getFactionManager().getFactionById(inside.getOwnerId());

        if (owner == null) {
            return;
        }

        if (owner instanceof ServerFaction) {
            final ServerFaction sf = (ServerFaction)owner;

            if (sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                if (!action.equals(Action.PHYSICAL)) {
                    player.sendMessage(ChatColor.RED + "This land is owned by " + ChatColor.RESET + sf.getDisplayName());
                }

                event.setCancelled(true);
            }
        } else if (owner instanceof PlayerFaction) {
            final PlayerFaction pf = (PlayerFaction)owner;

            if (!pf.isRaidable() && pf.getMember(player.getUniqueId()) == null && !admin) {
                if (!action.equals(Action.PHYSICAL)) {
                    player.sendMessage(ChatColor.RED + "This land is owned by " + ChatColor.RESET + pf.getName());
                }

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerHungerChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getEntity();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null || profile.getCurrentClaim() == null) {
            return;
        }

        final ServerFaction faction = plugin.getFactionManager().getServerFactionById(profile.getCurrentClaim().getOwnerId());

        if (faction != null && faction.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.setExhaustion(0);

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        final LivingEntity entity = (LivingEntity)event.getEntity();
        final DefinedClaim inside = plugin.getClaimManager().getClaimAt(new PLocatable(entity));

        if (inside != null) {
            final ServerFaction faction = plugin.getFactionManager().getServerFactionById(inside.getOwnerId());

            if (faction != null && faction.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        final DefinedClaim inside = plugin.getClaimManager().getClaimAt(new PLocatable(event.getEntity()));

        if (inside != null) {
            final ServerFaction faction = plugin.getFactionManager().getServerFactionById(inside.getOwnerId());

            if (faction != null && faction.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getTarget();
        final DefinedClaim inside = plugin.getClaimManager().getClaimAt(new PLocatable(player));

        if (inside != null) {
            final ServerFaction faction = plugin.getFactionManager().getServerFactionById(inside.getOwnerId());

            if (faction != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityExlode(EntityExplodeEvent event) {
        final List<Block> toRemove = Lists.newArrayList();

        for (Block block : event.blockList()) {
            final DefinedClaim inside = plugin.getClaimManager().getClaimAt(new BLocatable(block));

            if (inside != null) {
                toRemove.add(block);
            }
        }

        event.blockList().removeAll(toRemove);
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        final DefinedClaim inside = plugin.getClaimManager().getClaimAt(new BLocatable(event.getBlock()));

        if (inside != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            return;
        }

        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        final EnderpearlTimer timer = (EnderpearlTimer)profile.getTimer(PlayerTimer.PlayerTimerType.ENDERPEARL);
        final DefinedClaim inside = plugin.getClaimManager().getClaimAt(new PLocatable(
                event.getTo().getWorld().getName(),
                event.getTo().getX(),
                event.getTo().getY(),
                event.getTo().getZ(),
                event.getTo().getYaw(),
                event.getTo().getPitch()));

        if (timer == null) {
            return;
        }

        if (inside == null) {
            return;
        }

        final Faction owner = plugin.getFactionManager().getFactionById(inside.getOwnerId());

        if (owner == null) {
            return;
        }

        if (owner instanceof PlayerFaction) {
            if (profile.getTimer(PlayerTimer.PlayerTimerType.PROTECTION) != null) {
                player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                player.sendMessage(ChatColor.RED + "Your enderpearl landed in a claim you are not allowed to enter");

                profile.getTimers().remove(timer);

                event.setCancelled(true);
            }
        } else if (owner instanceof ServerFaction) {
            final ServerFaction sf = (ServerFaction)owner;

            if (profile.getTimer(PlayerTimer.PlayerTimerType.COMBAT) != null && sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
                player.sendMessage(ChatColor.RED + "Your enderpearl landed in a claim you are not allowed to enter");

                profile.getTimers().remove(timer);

                event.setCancelled(true);
            }

            // TODO: Cancel enderpearl landing in active Palace event claims
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        final DefinedClaim inside = plugin.getClaimManager().getClaimAt(new PLocatable(player));

        if (inside != null) {
            final Faction owner = plugin.getFactionManager().getFactionById(inside.getOwnerId());
            final PlayerChangeClaimEvent changeClaimEvent = new PlayerChangeClaimEvent(player, null, player.getLocation(), null, inside);
            Bukkit.getPluginManager().callEvent(changeClaimEvent);

            if (changeClaimEvent.isCancelled()) {
                FactionUtils.teleportOutsideClaims(plugin, player);
                player.sendMessage(ChatColor.DARK_PURPLE + "You have been escorted outside of the claim you were logged out in");
                return;
            }

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)owner;

                if (sf.getFlag().equals(ServerFaction.FactionFlag.EVENT)) {
                    FactionUtils.teleportOutsideClaims(plugin, player);
                    player.sendMessage(ChatColor.DARK_PURPLE + "You have been escorted outside of the claim you were logged out in");
                    return;
                }
            }

            profile.setCurrentClaim(inside);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerBigMoveEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        final DefinedClaim expectedClaim = profile.getCurrentClaim();
        final DefinedClaim predictedClaim = plugin.getClaimManager().getClaimAt(new PLocatable(
                event.getTo().getWorld().getName(),
                event.getTo().getX(),
                event.getTo().getY(),
                event.getTo().getZ(),
                event.getTo().getYaw(),
                event.getTo().getPitch()));

        final WorldLocation expectedWorldLocation = getPlugin().getClaimManager().getWorldLocationManager().getWorldLocation(
                new PLocatable(event.getFrom().getWorld().getName(), event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ(), event.getFrom().getYaw(), event.getFrom().getPitch()));

        final WorldLocation predictedWorldLocation = getPlugin().getClaimManager().getWorldLocationManager().getWorldLocation(
                new PLocatable(event.getTo().getWorld().getName(), event.getTo().getX(), event.getTo().getY(), event.getTo().getZ(), event.getTo().getYaw(), event.getTo().getPitch()));

        if (expectedClaim == null && predictedClaim == null && !expectedWorldLocation.equals(predictedWorldLocation)) {
            player.sendMessage(ChatColor.GOLD + "Now Leaving: " + ChatColor.RESET + expectedWorldLocation.getDisplayName() + ChatColor.GOLD + " (" + ChatColor.RED + "Deathban" + ChatColor.GOLD + ")");
            player.sendMessage(ChatColor.GOLD + "Now Entering: " + ChatColor.RESET + predictedWorldLocation.getDisplayName() + ChatColor.GOLD + " (" + ChatColor.RED + "Deathban" + ChatColor.GOLD + ")");
        }

        if (expectedClaim == predictedClaim) {
            return;
        }

        final PlayerChangeClaimEvent changeClaimEvent = new PlayerChangeClaimEvent(player, event.getFrom(), event.getTo(), expectedClaim, predictedClaim);
        Bukkit.getPluginManager().callEvent(changeClaimEvent);

        if (changeClaimEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        profile.setCurrentClaim(predictedClaim);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // We handle Ender Pearl teleportation above
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            return;
        }

        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (profile == null) {
            return;
        }

        final DefinedClaim expectedClaim = profile.getCurrentClaim();
        final DefinedClaim predictedClaim = plugin.getClaimManager().getClaimAt(new PLocatable(
                event.getTo().getWorld().getName(),
                event.getTo().getX(),
                event.getTo().getY(),
                event.getTo().getZ(),
                event.getTo().getYaw(),
                event.getTo().getPitch()
        ));

        final WorldLocation expectedWorldLocation = getPlugin().getClaimManager().getWorldLocationManager().getWorldLocation(
                new PLocatable(event.getFrom().getWorld().getName(), event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ(), event.getFrom().getYaw(), event.getFrom().getPitch()));

        final WorldLocation predictedWorldLocation = getPlugin().getClaimManager().getWorldLocationManager().getWorldLocation(
                new PLocatable(event.getTo().getWorld().getName(), event.getTo().getX(), event.getTo().getY(), event.getTo().getZ(), event.getTo().getYaw(), event.getTo().getPitch()));

        if (expectedClaim == null && predictedClaim == null && !expectedWorldLocation.equals(predictedWorldLocation)) {
            player.sendMessage(ChatColor.GOLD + "Now Leaving: " + ChatColor.RESET + expectedWorldLocation.getDisplayName() + ChatColor.GOLD + " (" + ChatColor.RED + "Deathban" + ChatColor.GOLD + ")");
            player.sendMessage(ChatColor.GOLD + "Now Entering: " + ChatColor.RESET + predictedWorldLocation.getDisplayName() + ChatColor.GOLD + " (" + ChatColor.RED + "Deathban" + ChatColor.GOLD + ")");
        }

        if (expectedClaim == predictedClaim) {
            return;
        }

        final PlayerChangeClaimEvent changeClaimEvent = new PlayerChangeClaimEvent(player, event.getFrom(), event.getTo(), expectedClaim, predictedClaim);
        Bukkit.getPluginManager().callEvent(changeClaimEvent);

        if (changeClaimEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        profile.setCurrentClaim(predictedClaim);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerChangeClaim(PlayerChangeClaimEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final DefinedClaim to = event.getClaimTo();

        if (to != null) {
            final Faction toFaction = plugin.getFactionManager().getFactionById(to.getOwnerId());

            if (toFaction == null) {
                return;
            }

            if (toFaction instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)toFaction;

                if (profile.hasTimer(PlayerTimer.PlayerTimerType.COMBAT) && sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                    player.sendMessage(ChatColor.RED + "You can not enter this claim while combat-tagged");
                    event.setCancelled(true);
                    return;
                }

                if (profile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION) && sf.getFlag().equals(ServerFaction.FactionFlag.EVENT)) {
                    player.sendMessage(ChatColor.RED + "You can not enter this claim while you have PvP Protection");
                    event.setCancelled(true);
                }
            } else if (toFaction instanceof PlayerFaction) {
                if (profile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION)) {
                    player.sendMessage(ChatColor.RED + "You can not enter this claim while you have PvP Protection");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerClaimChangeNotification(PlayerChangeClaimEvent event) {
        final Player player = event.getPlayer();
        final DefinedClaim from = event.getClaimFrom();
        final DefinedClaim to = event.getClaimTo();

        final WorldLocation fromWorldLocation = (event.getLocationFrom() != null) ? getPlugin().getClaimManager().getWorldLocationManager().getWorldLocation(new PLocatable(
                        event.getLocationFrom().getWorld().getName(),
                        event.getLocationFrom().getX(),
                        event.getLocationFrom().getY(),
                        event.getLocationFrom().getZ(),
                        event.getLocationFrom().getYaw(),
                        event.getLocationFrom().getPitch()))
                : null;

        final WorldLocation toWorldLocation = getPlugin().getClaimManager().getWorldLocationManager().getWorldLocation(new PLocatable(
                event.getLocationTo().getWorld().getName(),
                event.getLocationTo().getX(),
                event.getLocationTo().getY(),
                event.getLocationTo().getZ(),
                event.getLocationTo().getYaw(),
                event.getLocationTo().getPitch()));

        if (event.isCancelled()) {
            return;
        }

        if (from != null && to != null && from.getOwnerId().equals(to.getOwnerId())) {
            return;
        }

        if (from != null) {
            final Faction owner = plugin.getFactionManager().getFactionById(from.getOwnerId());

            if (owner != null) {
                if (owner instanceof ServerFaction) {
                    final ServerFaction sf = (ServerFaction)owner;
                    player.sendMessage(ChatColor.GOLD + "Now Leaving: " + ChatColor.RESET + sf.getDisplayName() + ChatColor.GOLD + " (" + sf.getFlag().getDisplayName() + ChatColor.GOLD + ")");
                } else if (owner instanceof PlayerFaction) {
                    final PlayerFaction pf = (PlayerFaction)owner;
                    final ChatColor color = (pf.getMember(player.getUniqueId()) != null ? ChatColor.GREEN : ChatColor.RED);
                    player.sendMessage(ChatColor.GOLD + "Now Leaving: " + color + pf.getName() + ChatColor.GOLD + " (" + ChatColor.RED + "Deathban" + ChatColor.GOLD + ")");
                }
            }
        } else if (fromWorldLocation != null) {
            player.sendMessage(ChatColor.GOLD + "Now Leaving: " + ChatColor.RESET + fromWorldLocation.getDisplayName() + ChatColor.GOLD + " (" + ChatColor.RED + "Deathban" + ChatColor.GOLD + ")");
        }

        if (to != null) {
            final Faction owner = plugin.getFactionManager().getFactionById(to.getOwnerId());

            if (owner != null) {
                if (owner instanceof ServerFaction) {
                    final ServerFaction sf = (ServerFaction)owner;
                    player.sendMessage(ChatColor.GOLD + "Now Entering: " + ChatColor.RESET + sf.getDisplayName() + ChatColor.GOLD + " (" + sf.getFlag().getDisplayName() + ChatColor.GOLD + ")");
                } else if (owner instanceof PlayerFaction) {
                    final PlayerFaction pf = (PlayerFaction)owner;
                    final ChatColor color = (pf.getMember(player.getUniqueId()) != null ? ChatColor.GREEN : ChatColor.RED);
                    player.sendMessage(ChatColor.GOLD + "Now Entering: " + color + pf.getName() + ChatColor.GOLD + " (" + ChatColor.RED + "Deathban" + ChatColor.GOLD + ")");
                }
            }
        } else {
            player.sendMessage(ChatColor.GOLD + "Now Entering: " + ChatColor.RESET + toWorldLocation.getDisplayName() + ChatColor.GOLD + " (" + ChatColor.RED + "Deathban" + ChatColor.GOLD + ")");
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerChangeClaimTimerFreeze(PlayerChangeClaimEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());
        final DefinedClaim to = event.getClaimTo();
        boolean toSafezone = false;

        if (profile == null) {
            return;
        }

        final ProtectionTimer existing = (ProtectionTimer)profile.getTimer(PlayerTimer.PlayerTimerType.PROTECTION);

        if (existing == null) {
            return;
        }

        if (to != null) {
            final Faction insideFaction = plugin.getFactionManager().getFactionById(to.getOwnerId());

            if (insideFaction instanceof ServerFaction) {
                final ServerFaction insideServerFaction = (ServerFaction)insideFaction;

                if (insideServerFaction.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                    toSafezone = true;
                }
            }
        }

        if (toSafezone && !existing.isFrozen()) {
            existing.freeze();
        } else if (!toSafezone && existing.isFrozen()) {
            existing.unfreeze();
        }
    }
}