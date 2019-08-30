package com.playares.factions.listener;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.playares.commons.bukkit.event.PlayerDamagePlayerEvent;
import com.playares.commons.bukkit.event.PlayerLingeringSplashPlayerEvent;
import com.playares.commons.bukkit.event.PlayerSplashPlayerEvent;
import com.playares.commons.bukkit.location.PLocatable;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Players;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.addons.loggers.data.CombatLogger;
import com.playares.factions.addons.loggers.event.LoggerDeathEvent;
import com.playares.factions.addons.loggers.event.PlayerDamageLoggerEvent;
import com.playares.factions.addons.states.ServerStateAddon;
import com.playares.factions.addons.states.data.ServerState;
import com.playares.factions.claims.data.DefinedClaim;
import com.playares.factions.event.MemberDeathEvent;
import com.playares.factions.factions.data.Faction;
import com.playares.factions.factions.data.PlayerFaction;
import com.playares.factions.factions.data.ServerFaction;
import com.playares.factions.players.dao.PlayerDAO;
import com.playares.factions.players.data.FactionPlayer;
import com.playares.factions.timers.PlayerTimer;
import com.playares.factions.timers.cont.faction.DTRFreezeTimer;
import com.playares.factions.util.FactionUtils;
import com.playares.services.deathban.DeathbanService;
import com.playares.services.playerclasses.PlayerClassService;
import com.playares.services.playerclasses.data.Class;
import com.playares.services.playerclasses.data.ClassConsumable;
import com.playares.services.playerclasses.data.cont.BardClass;
import com.playares.services.playerclasses.event.ConsumeClassItemEvent;
import lombok.Getter;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public final class CombatListener implements Listener {
    @Getter public final Factions plugin;

    public CombatListener(Factions plugin) {
        this.plugin = plugin;
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onClassConsume(ConsumeClassItemEvent event) {
        final PlayerClassService classService = (PlayerClassService)getPlugin().getService(PlayerClassService.class);

        if (classService == null) {
            return;
        }

        final Player player = event.getPlayer();
        final Class playerClass = classService.getClassManager().getCurrentClass(player);
        final FactionPlayer profile = getPlugin().getPlayerManager().getPlayer(player.getUniqueId());

        if (profile.hasTimer(PlayerTimer.PlayerTimerType.PROTECTION)) {
            player.sendMessage(ChatColor.RED + "You can not use consumable effects while you have PvP Protection");
            event.setCancelled(true);
            return;
        }

        // TODO: Eventually fix this but it's expected only bard will give effects for now
        if (!(playerClass instanceof BardClass)) {
            return;
        }

        final BardClass bard = (BardClass)playerClass;

        if (event.getConsumable().getApplicationType().equals(ClassConsumable.ConsumableApplicationType.INDIVIDUAL)) {
            return;
        }

        if (event.getConsumable().getApplicationType().equals(ClassConsumable.ConsumableApplicationType.ALL)) {
            final List<Player> friendlies = FactionUtils.getNearbyFriendlies(plugin, player, bard.getRange());
            final List<Player> enemies = FactionUtils.getNearbyEnemies(plugin, player, bard.getRange());

            friendlies.forEach(friendly -> event.getAffectedPlayers().put(friendly.getUniqueId(), true));
            enemies.forEach(enemy -> event.getAffectedPlayers().put(enemy.getUniqueId(), false));

            return;
        }

        if (event.getConsumable().getApplicationType().equals(ClassConsumable.ConsumableApplicationType.FRIENDLY_ONLY)) {
            final List<Player> friendlies = FactionUtils.getNearbyFriendlies(plugin, player, bard.getRange());
            friendlies.forEach(friendly -> event.getAffectedPlayers().put(friendly.getUniqueId(), true));
            return;
        }

        if (event.getConsumable().getApplicationType().equals(ClassConsumable.ConsumableApplicationType.ENEMY_ONLY)) {
            final List<Player> enemies = FactionUtils.getNearbyEnemies(plugin, player, bard.getRange());
            enemies.forEach(enemy -> event.getAffectedPlayers().put(enemy.getUniqueId(), true));
        }
    }

    @EventHandler
    public void onProjectileCollide(ProjectileCollideEvent event) {
        final Projectile projectile = event.getEntity();
        final Entity entity = event.getCollidedWith();

        if (!(projectile instanceof EnderPearl)) {
            return;
        }

        if (!(projectile.getShooter() instanceof Player)) {
            return;
        }

        if (!(entity instanceof Player)) {
            return;
        }

        final Player shooter = (Player)projectile.getShooter();
        final Player hit = (Player)entity;
        final PlayerFaction shooterFaction = getPlugin().getFactionManager().getFactionByPlayer(shooter.getUniqueId());

        if (shooterFaction == null || shooterFaction.getMember(hit.getUniqueId()) == null) {
            return;
        }

        event.setCancelled(true);
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
            Logger.error("Attacker profile could not be found for " + attacker.getName());
        }

        if (attackedProfile == null) {
            Logger.error("Attacked profile could not be found for " + attacked.getName());
        }

        if (attackerProfile == null || attackedProfile == null) {
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
        final DeathbanService deathbanService = (DeathbanService)getPlugin().getService(DeathbanService.class);
        final ServerStateAddon serverStateAddon = (ServerStateAddon)getPlugin().getAddonManager().getAddon(ServerStateAddon.class);

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
        Bukkit.getOnlinePlayers().forEach(listener -> Players.playSound(listener, Sound.ENTITY_LIGHTNING_THUNDER));

        if (deathbanService != null && serverStateAddon != null && deathbanService.getConfiguration().isDeathbanEnforced()) {
            final boolean permanent = (serverStateAddon.getCurrentState().equals(ServerState.EOTW_PHASE_1) || serverStateAddon.getCurrentState().equals(ServerState.EOTW_PHASE_2));
            deathbanService.deathban(logger.getOwner(), 30, permanent); // TODO: Make dynamic
        }

        new Scheduler(getPlugin()).async(() -> {
            final FactionPlayer factionPlayer = getPlugin().getPlayerManager().loadPlayer(logger.getOwner(), logger.getOwnerUsername());
            factionPlayer.setResetOnJoin(true);
            PlayerDAO.savePlayer(getPlugin().getMongo(), factionPlayer);
        }).run();
    }

    @EventHandler
    public void onMemberDeath(MemberDeathEvent event) {
        final String username = event.getUsername();
        final PlayerFaction faction = event.getFaction();
        final double subtracted = event.getSubtractedDTR();

        faction.sendMessage(ChatColor.DARK_RED + "Member Death" + ChatColor.RED + ": " + ChatColor.RESET + username);
        faction.sendMessage(ChatColor.DARK_RED + "DTR Loss" + ChatColor.RESET + ": " + ChatColor.RESET + "-" + subtracted);
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);

        if (event.getEntity() == null || event.getEntity().getLastDamageCause() == null) {
            return;
        }

        final Player player = event.getEntity();
        final PlayerFaction faction = plugin.getFactionManager().getFactionByPlayer(player.getUniqueId());
        final Player killer = player.getKiller();
        final EntityDamageEvent.DamageCause reason = player.getLastDamageCause().getCause();
        final String rip = ChatColor.RED + "RIP: " + ChatColor.RESET;
        final DeathbanService deathbanService = (DeathbanService)getPlugin().getService(DeathbanService.class);
        final ServerStateAddon serverStateAddon = (ServerStateAddon)getPlugin().getAddonManager().getAddon(ServerStateAddon.class);

        if (deathbanService != null && serverStateAddon != null) {
            final boolean permanent = (serverStateAddon.getCurrentState().equals(ServerState.EOTW_PHASE_1) || serverStateAddon.getCurrentState().equals(ServerState.EOTW_PHASE_2));
            deathbanService.deathban(player.getUniqueId(), 30, permanent);
        }

        if (faction != null) {
            final MemberDeathEvent memberDeathEvent = new MemberDeathEvent(player.getUniqueId(), player.getName(), faction, new PLocatable(player), 1.0, plugin.getFactionConfig().getTimerFreeze());
            Bukkit.getPluginManager().callEvent(memberDeathEvent);
            faction.setDeathsTilRaidable(faction.getDeathsTilRaidable() - memberDeathEvent.getSubtractedDTR());
            faction.addTimer(new DTRFreezeTimer(faction, memberDeathEvent.getFreezeDuration()));
        }

        player.getWorld().strikeLightningEffect(player.getLocation());
        Bukkit.getOnlinePlayers().forEach(listener -> Players.playSound(listener, Sound.ENTITY_LIGHTNING_THUNDER));

        // Fixes 'fell 0 blocks' caused by dying from pearl damage
        if (reason.equals(EntityDamageEvent.DamageCause.FALL)) {
            final double fallDistance = Math.round(player.getFallDistance());

            if (fallDistance <= 1.0) {
                if (killer != null) {
                    Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " slain by " + ChatColor.GOLD + killer.getName());
                } else {
                    Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " died");
                }

                return;
            }
        }

        // This adds death messages to entities through melee and projectile attacks
        if (killer == null && (reason.equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK) || reason.equals(EntityDamageEvent.DamageCause.PROJECTILE))) {
            final EntityDamageEvent originalDamageEvent = player.getLastDamageCause();

            if (originalDamageEvent instanceof EntityDamageByEntityEvent) {
                final EntityDamageByEntityEvent pveEvent = (EntityDamageByEntityEvent)originalDamageEvent;
                final Entity damager = pveEvent.getDamager();

                if (damager != null) {
                    if (damager instanceof LivingEntity) {
                        final LivingEntity entityKiller = (LivingEntity)damager;

                        Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " slain by a " + ChatColor.GOLD + WordUtils.capitalize(entityKiller.getType().name().toLowerCase().replace("_", " ")));

                        return;
                    }

                    else if (damager instanceof Projectile) {
                        final Projectile projectile = (Projectile)damager;

                        if (projectile.getShooter() instanceof LivingEntity) {
                            final LivingEntity entityKiller = (LivingEntity)projectile.getShooter();
                            final String distance = String.format("%.2f", player.getLocation().distance(entityKiller.getLocation()));

                            Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " was shot and killed by a " +
                                    ChatColor.GOLD + WordUtils.capitalize(entityKiller.getType().name().toLowerCase().replace("_", " ")) +
                                    ChatColor.RED + " from a distance of " + ChatColor.BLUE + distance + " blocks");

                            return;
                        }
                    }
                }
            }
        }

        if (killer != null) {
            final String distance = String.format("%.2f", player.getLocation().distance(killer.getLocation()));
            final StringBuilder usingBuilder = new StringBuilder();

            if (killer.getInventory().getItemInMainHand() != null && killer.getInventory().getItemInMainHand().getType().isItem() && !killer.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                usingBuilder.append(ChatColor.RED + " using a " + ChatColor.YELLOW + WordUtils.capitalize(killer.getInventory().getItemInMainHand().getType().name().replace("_", " ").toLowerCase()));

                if (killer.getInventory().getItemInMainHand().hasItemMeta() && killer.getInventory().getItemInMainHand().getItemMeta().getDisplayName() != null) {
                    usingBuilder.append(ChatColor.GOLD + " [" + ChatColor.YELLOW + killer.getInventory().getItemInMainHand().getItemMeta().getDisplayName() + ChatColor.GOLD + "]");
                }
            }

            final String using = usingBuilder.toString();

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
                default: Bukkit.broadcastMessage(rip + ChatColor.GOLD + player.getName() + ChatColor.RED + " slain by " + ChatColor.GOLD + killer.getName() + using);
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