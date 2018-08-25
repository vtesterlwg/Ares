package com.playares.factions.listener;

import com.google.common.collect.Lists;
import com.playares.commons.bukkit.event.PlayerBigMoveEvent;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.util.Items;
import com.playares.factions.Factions;
import com.playares.factions.claims.DefinedClaim;
import com.playares.factions.event.PlayerChangeClaimEvent;
import com.playares.factions.factions.Faction;
import com.playares.factions.factions.PlayerFaction;
import com.playares.factions.factions.ServerFaction;
import com.playares.factions.players.FactionPlayer;
import com.playares.factions.timers.PlayerTimer;
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
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public final class ClaimListener implements Listener {
    @Getter
    public final Factions plugin;

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
        final List<Block> toRemove = Lists.newArrayList();

        for (Block affected : event.getBlocks()) {
            final DefinedClaim affectedClaim = plugin.getClaimManager().getClaimAt(new BLocatable(affected));

            if (pistonClaim == null && affectedClaim != null) {
                toRemove.add(affected);
            }

            else if (pistonClaim != null && affectedClaim == null) {
                toRemove.add(affected);
            }

            else if (pistonClaim != null && !pistonClaim.getUniqueId().equals(affectedClaim.getUniqueId())) {
                toRemove.add(affected);
            }
        }

        event.getBlocks().removeAll(toRemove);
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        final Block piston = event.getBlock();
        final DefinedClaim pistonClaim = plugin.getClaimManager().getClaimAt(new BLocatable(piston));
        final List<Block> toRemove = Lists.newArrayList();

        for (Block affected : event.getBlocks()) {
            final DefinedClaim affectedClaim = plugin.getClaimManager().getClaimAt(new BLocatable(affected));

            if (pistonClaim == null && affectedClaim != null) {
                toRemove.add(affected);
            }

            else if (pistonClaim != null && affectedClaim == null) {
                toRemove.add(affected);
            }

            else if (pistonClaim != null && !pistonClaim.getUniqueId().equals(affectedClaim.getUniqueId())) {
                toRemove.add(affected);
            }
        }

        event.getBlocks().removeAll(toRemove);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Action action = event.getAction();
        final Block block = event.getClickedBlock();
        final boolean admin = player.hasPermission("factions.admin");

        if (block == null || (block.getType().equals(Material.AIR) || block.getType().equals(Material.CAVE_AIR) || block.getType().equals(Material.VOID_AIR))) {
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

        if (expectedClaim == predictedClaim) {
            return;
        }

        final PlayerChangeClaimEvent changeClaimEvent = new PlayerChangeClaimEvent(player, expectedClaim, predictedClaim);
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
        final DefinedClaim to = event.getTo();

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
        final DefinedClaim from = event.getFrom();
        final DefinedClaim to = event.getTo();

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
                    player.sendMessage(ChatColor.YELLOW + "Now Entering: " + ChatColor.RESET + sf.getDisplayName());
                } else if (owner instanceof PlayerFaction) {
                    final PlayerFaction pf = (PlayerFaction)owner;
                    final ChatColor color = (pf.getMember(player.getUniqueId()) != null ? ChatColor.GREEN : ChatColor.RED);
                    player.sendMessage(ChatColor.YELLOW + "Now Entering: " + color + pf.getName());
                }
            }
        }

        if (to != null) {
            final Faction owner = plugin.getFactionManager().getFactionById(to.getOwnerId());

            if (owner != null) {
                if (owner instanceof ServerFaction) {
                    final ServerFaction sf = (ServerFaction)owner;
                    player.sendMessage(ChatColor.YELLOW + "Now Entering: " + ChatColor.RESET + sf.getDisplayName());
                } else if (owner instanceof PlayerFaction) {
                    final PlayerFaction pf = (PlayerFaction)owner;
                    final ChatColor color = (pf.getMember(player.getUniqueId()) != null ? ChatColor.GREEN : ChatColor.RED);
                    player.sendMessage(ChatColor.YELLOW + "Now Entering: " + color + pf.getName());
                }
            }
        }
    }
}