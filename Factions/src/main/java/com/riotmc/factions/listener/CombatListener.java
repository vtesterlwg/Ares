package com.riotmc.factions.listener;

import com.playares.commons.bukkit.event.PlayerDamagePlayerEvent;
import com.playares.commons.bukkit.event.PlayerLingeringSplashPlayerEvent;
import com.playares.commons.bukkit.event.PlayerSplashPlayerEvent;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.util.Players;
import com.riotmc.factions.Factions;
import com.riotmc.factions.addons.loggers.CombatLogger;
import com.riotmc.factions.addons.loggers.event.LoggerDeathEvent;
import com.riotmc.factions.addons.loggers.event.PlayerDamageLoggerEvent;
import com.riotmc.factions.claims.DefinedClaim;
import com.riotmc.factions.event.MemberDeathEvent;
import com.riotmc.factions.factions.Faction;
import com.riotmc.factions.factions.PlayerFaction;
import com.riotmc.factions.factions.ServerFaction;
import com.riotmc.factions.players.FactionPlayer;
import com.riotmc.factions.timers.PlayerTimer;
import com.riotmc.factions.timers.cont.faction.DTRFreezeTimer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class CombatListener implements Listener {
    @Getter
    public final Factions plugin;

    public CombatListener(Factions plugin) {
        this.plugin = plugin;
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerAttackPlayer(PlayerDamagePlayerEvent event) {
        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();

        if (attacker.getUniqueId().equals(attacked.getUniqueId())) {
            return;
        }

        final FactionPlayer attackerProfile = plugin.getPlayerManager().getPlayer(attacker.getUniqueId());
        final FactionPlayer attackedProfile = plugin.getPlayerManager().getPlayer(attacked.getUniqueId());

        if (attackerProfile == null) {
            attacker.sendMessage(ChatColor.RED + "Failed to obtain your profile");
            event.setCancelled(true);
            return;
        }

        if (attackedProfile == null) {
            attacker.sendMessage(ChatColor.RED + "Failed to obtain this players profile");
            event.setCancelled(true);
            return;
        }

        if (attackerProfile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION)) {
            attacker.sendMessage(ChatColor.RED + "You can not attack players while you have PvP Protection");
            event.setCancelled(true);
            return;
        }

        if (attackedProfile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION)) {
            attacker.sendMessage(ChatColor.RED + "This player has PvP Protection");
            event.setCancelled(true);
            return;
        }

        if (attackerProfile.getCurrentClaim() != null) {
            final DefinedClaim claim = attackerProfile.getCurrentClaim();
            final Faction owner = plugin.getFactionManager().getFactionById(claim.getOwnerId());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)owner;

                if (sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                    attacker.sendMessage(ChatColor.RED + "PvP is disabled in " + ChatColor.RESET + sf.getDisplayName());
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (attackedProfile.getCurrentClaim() != null) {
            final DefinedClaim claim = attackedProfile.getCurrentClaim();
            final Faction owner = plugin.getFactionManager().getFactionById(claim.getOwnerId());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)owner;

                if (sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                    attacker.sendMessage(ChatColor.RED + "PvP is disabled in " + ChatColor.RESET + sf.getDisplayName());
                    event.setCancelled(true);
                    return;
                }
            }
        }

        final PlayerFaction attackerFaction = plugin.getFactionManager().getFactionByPlayer(attacker.getUniqueId());

        if (attackerFaction != null && attackerFaction.getMember(attacked.getUniqueId()) != null) {
            attacker.sendMessage(ChatColor.RED + "PvP is disabled between " + ChatColor.RESET + "Faction Members");
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerSplashPlayer(PlayerSplashPlayerEvent event) {
        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();
        final ThrownPotion potion = event.getPotion();
        boolean isDebuff = false;

        if (attacker.getUniqueId().equals(attacked.getUniqueId())) {
            return;
        }

        for (PotionEffect effect : potion.getEffects()) {
            if (effect.getType().equals(PotionEffectType.POISON) ||
            effect.getType().equals(PotionEffectType.SLOW) ||
            effect.getType().equals(PotionEffectType.WEAKNESS) ||
            effect.getType().equals(PotionEffectType.HARM)) {
                isDebuff = true;
                break;
            }
        }

        if (!isDebuff) {
            return;
        }

        final FactionPlayer attackerProfile = plugin.getPlayerManager().getPlayer(attacker.getUniqueId());
        final FactionPlayer attackedProfile = plugin.getPlayerManager().getPlayer(attacked.getUniqueId());

        if (attackerProfile == null || attackedProfile == null) {
            event.setCancelled(true);
            return;
        }

        if (attackerProfile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION) || attackedProfile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION)) {
            event.setCancelled(true);
            return;
        }

        if (attackerProfile.getCurrentClaim() != null) {
            final DefinedClaim claim = attackerProfile.getCurrentClaim();
            final Faction owner = plugin.getFactionManager().getFactionById(claim.getOwnerId());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)owner;

                if (sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (attackedProfile.getCurrentClaim() != null) {
            final DefinedClaim claim = attackedProfile.getCurrentClaim();
            final Faction owner = plugin.getFactionManager().getFactionById(claim.getOwnerId());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)owner;

                if (sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onPlayerLingeringSplash(PlayerLingeringSplashPlayerEvent event) {
        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();
        final AreaEffectCloud cloud = event.getCloud();

        if (attacker.getUniqueId().equals(attacked.getUniqueId())) {
            return;
        }

        if (!cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.HARM) &&
        !cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.WEAKNESS) &&
        !cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.SLOW) &&
        !cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.POISON)) {
            return;
        }

        final FactionPlayer attackerProfile = plugin.getPlayerManager().getPlayer(attacker.getUniqueId());
        final FactionPlayer attackedProfile = plugin.getPlayerManager().getPlayer(attacked.getUniqueId());

        if (attackerProfile == null || attackedProfile == null) {
            event.setCancelled(true);
            return;
        }

        if (attackerProfile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION) || attackedProfile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION)) {
            event.setCancelled(true);
            return;
        }

        if (attackerProfile.getCurrentClaim() != null) {
            final DefinedClaim claim = attackerProfile.getCurrentClaim();
            final Faction owner = plugin.getFactionManager().getFactionById(claim.getOwnerId());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)owner;

                if (sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (attackedProfile.getCurrentClaim() != null) {
            final DefinedClaim claim = attackedProfile.getCurrentClaim();
            final Faction owner = plugin.getFactionManager().getFactionById(claim.getOwnerId());

            if (owner instanceof ServerFaction) {
                final ServerFaction sf = (ServerFaction)owner;

                if (sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onLoggerDamage(PlayerDamageLoggerEvent event) {
        final Player damager = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(damager.getUniqueId());
        final PlayerFaction faction = plugin.getFactionManager().getFactionByPlayer(damager.getUniqueId());
        final CombatLogger logger = event.getLogger();

        if (profile != null && profile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION)) {
            damager.sendMessage(ChatColor.RED + "You can not attack players while you have PvP Protection");
            event.setCancelled(true);
            return;
        }

        if (profile != null && profile.getCurrentClaim() != null) {
            final DefinedClaim inside = profile.getCurrentClaim();
            final ServerFaction sf = plugin.getFactionManager().getServerFactionById(inside.getOwnerId());

            if (sf != null && sf.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                damager.sendMessage(ChatColor.RED + "PvP is disabled in " + ChatColor.RESET + sf.getDisplayName());
                event.setCancelled(true);
                return;
            }
        }

        if (faction != null && faction.getMember(logger.getOwner()) != null) {
            damager.sendMessage(ChatColor.RED + "PvP is disabled between " + ChatColor.RESET + "Faction Members");
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLoggerDeath(LoggerDeathEvent event) {
        final CombatLogger logger = event.getLogger();
        final CraftLivingEntity living = (CraftLivingEntity)logger.getBukkitEntity();

        if (event.isCancelled()) {
            return;
        }

        final PlayerFaction faction = plugin.getFactionManager().getFactionByPlayer(logger.getOwner());

        if (event.getKiller() != null) {
            final Player killer = event.getKiller();

            Bukkit.broadcastMessage(ChatColor.RED + "RIP: " + ChatColor.DARK_RED + "(Combat-Logger) " + ChatColor.GOLD + logger.getOwnerUsername() +
                    ChatColor.RED + " slain by " + ChatColor.GOLD + killer.getName());
        } else {
            Bukkit.broadcastMessage(ChatColor.RED + "RIP: " + ChatColor.DARK_RED + "(Combat-Logger) " + ChatColor.GOLD + logger.getOwnerUsername() +
                    ChatColor.RED + " died");
        }

        if (faction != null) {
            final MemberDeathEvent memberDeathEvent = new MemberDeathEvent(logger.getOwner(), logger.getOwnerUsername(), faction, new PLocatable(living), 1.0, plugin.getFactionConfig().getTimerFreeze());
            Bukkit.getPluginManager().callEvent(memberDeathEvent);
            faction.setDeathsTilRaidable(faction.getDeathsTilRaidable() - memberDeathEvent.getSubtractedDTR());
            faction.addTimer(new DTRFreezeTimer(faction, memberDeathEvent.getFreezeDuration()));
        }

        logger.getBukkitEntity().getWorld().strikeLightningEffect(logger.getBukkitEntity().getLocation());
        Bukkit.getOnlinePlayers().forEach(listener -> Players.playSound(listener, Sound.ENTITY_LIGHTNING_BOLT_THUNDER));
    }

    @EventHandler
    public void onMemberDeath(MemberDeathEvent event) {
        final String username = event.getUsername();
        final PlayerFaction faction = event.getFaction();
        final double subtracted = event.getSubtractedDTR();

        faction.sendMessage(ChatColor.DARK_RED + "Member Death" + ChatColor.RED + ": " + ChatColor.RESET + username);
        faction.sendMessage(ChatColor.DARK_RED + "DTR Loss" + ChatColor.RESET + ": " + ChatColor.RESET + "-" + subtracted);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity() == null || event.getEntity().getLastDamageCause() == null) {
            return;
        }

        final Player player = event.getEntity();
        final PlayerFaction faction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());
        final Player killer = player.getKiller();
        final EntityDamageEvent.DamageCause reason = player.getLastDamageCause().getCause();
        final String rip = ChatColor.RED + "RIP: " + ChatColor.RESET;

        if (faction != null) {
            final MemberDeathEvent memberDeathEvent = new MemberDeathEvent(player.getUniqueId(), player.getName(), faction, new PLocatable(player), 1.0, plugin.getFactionConfig().getTimerFreeze());
            Bukkit.getPluginManager().callEvent(memberDeathEvent);
            faction.setDeathsTilRaidable(faction.getDeathsTilRaidable() - memberDeathEvent.getSubtractedDTR());
            faction.addTimer(new DTRFreezeTimer(faction, memberDeathEvent.getFreezeDuration()));
        }

        player.getWorld().strikeLightningEffect(player.getLocation());
        Bukkit.getOnlinePlayers().forEach(listener -> Players.playSound(listener, Sound.ENTITY_LIGHTNING_BOLT_THUNDER));

        event.setDeathMessage(null);

        if (killer != null) {
            final String distance = String.format("%.2f", player.getLocation().distance(killer.getLocation()));

            switch (reason) {
                case FIRE:
                case FIRE_TICK:
                case LAVA:
                case DRAGON_BREATH:
                case MELTING:
                case HOT_FLOOR: {
                    Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " burned to death thanks to " + ChatColor.GOLD + killer.getName());
                    break;
                }
                case MAGIC:
                case CUSTOM:
                case SUICIDE:
                case POISON:
                case LIGHTNING:
                case THORNS: {
                    Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " died by magic thanks to " + ChatColor.GOLD + killer.getName());
                    break;
                }
                case CRAMMING:
                case DROWNING:
                case SUFFOCATION: {
                    Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " ran out of air thanks to " + ChatColor.GOLD + killer.getName()); break;
                }
                case ENTITY_EXPLOSION:
                case BLOCK_EXPLOSION: {
                    Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " blew up thanks to " + ChatColor.GOLD + killer.getName());
                    break;
                }
                case PROJECTILE: Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " was shot and killed by " + ChatColor.GOLD + killer.getName() + ChatColor.RED + " from a distance of " + ChatColor.BLUE + distance + " blocks"); break;
                case FALL: Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " fell " + ChatColor.BLUE + Math.round(player.getFallDistance()) + " blocks" + ChatColor.RED + " to their death thanks to " + ChatColor.GOLD + killer.getName()); break;
                case VOID: Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + " slipped and fell in to the void thanks to " + ChatColor.GOLD + killer.getName()); break;
                case FLY_INTO_WALL: Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " hit a wall going too fast thanks to " + ChatColor.GOLD + killer.getName()); break;
                case WITHER: Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " withered away thanks to " + ChatColor.GOLD + killer.getName()); break;
                case STARVATION: Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " starved to death thanks to " + ChatColor.GOLD + killer.getName()); break;
                default: Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " slain by " + ChatColor.GOLD + killer.getName());
            }

            return;
        }

        switch (reason) {
            case HOT_FLOOR:
            case LAVA:
            case MELTING:
            case FIRE:
            case DRAGON_BREATH:
            case FIRE_TICK: {
                Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " burned to death");
                break;
            }
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION: {
                Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " blew up");
                break;
            }
            case SUFFOCATION:
            case DROWNING:
            case CRAMMING: {
                Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " ran out of air");
                break;
            }
            case POISON:
            case SUICIDE:
            case THORNS:
            case CUSTOM:
            case MAGIC: {
                Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " died by magic");
                break;
            }
            case FALLING_BLOCK: Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " died from a heavy block falling on their head"); break;
            case STARVATION: Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " starved to death"); break;
            case WITHER: Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " withered away"); break;
            case FLY_INTO_WALL: Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " hit a wall going too fast"); break;
            case VOID: Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " slipped and fell in to the void"); break;
            case FALL: Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " fell " + ChatColor.BLUE + Math.round(player.getFallDistance()) + " blocks" + ChatColor.RED + " to their death"); break;
            case CONTACT: Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " was too close to a cactus"); break;
            case LIGHTNING: Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " was struck by lightning"); break;
            default: Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " died");
        }
    }
}