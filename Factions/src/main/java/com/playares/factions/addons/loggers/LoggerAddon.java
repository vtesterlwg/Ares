package com.playares.factions.addons.loggers;

import com.google.common.collect.Maps;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.event.PlayerDamagePlayerEvent;
import com.playares.commons.bukkit.event.PlayerLingeringSplashPlayerEvent;
import com.playares.commons.bukkit.event.PlayerSplashPlayerEvent;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.addons.Addon;
import com.playares.factions.addons.loggers.event.CombatLogEvent;
import com.playares.factions.addons.loggers.event.LoggerDeathEvent;
import com.playares.factions.addons.loggers.event.PlayerDamageLoggerEvent;
import com.playares.factions.claims.DefinedClaim;
import com.playares.factions.factions.ServerFaction;
import com.playares.factions.players.FactionPlayer;
import com.playares.factions.timers.PlayerTimer;
import com.playares.factions.timers.cont.player.CombatTagTimer;
import com.playares.factions.util.FactionUtils;
import com.playares.services.customentity.CustomEntityService;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_13_R2.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public final class LoggerAddon implements Addon, Listener {
    @Getter
    public final Factions plugin;

    @Getter @Setter
    public boolean enabled;

    @Getter @Setter
    public int loggerDuration;

    @Getter @Setter
    public int enemyCheckRadius;

    @Getter
    public final Map<UUID, CombatLogger> loggers;

    public LoggerAddon(Factions plugin) {
        this.plugin = plugin;
        this.loggers = Maps.newConcurrentMap();
    }

    @Override
    public String getName() {
        return "Loggers";
    }

    @Override
    public void prepare() {
        final YamlConfiguration config = plugin.getConfig("config");
        this.enabled = config.getBoolean("loggers.enabled");
        this.loggerDuration = config.getInt("loggers.logger-duration");
        this.enemyCheckRadius = config.getInt("loggers.enemy-radius");
    }

    @Override
    public void start() {
        final CustomEntityService service = (CustomEntityService)plugin.getService(CustomEntityService.class);

        if (service != null) {
            service.register("combat_logger", "villager", CombatLogger.class, CombatLogger::new);
        } else {
            Logger.error("Could not find Custom Entity Service, Combat Loggers will not spawn!");
        }

        plugin.registerListener(this);
    }

    @Override
    public void stop() {
        PlayerDamagePlayerEvent.getHandlerList().unregister(this);
        PlayerSplashPlayerEvent.getHandlerList().unregister(this);
        PlayerLingeringSplashPlayerEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
        EntityDeathEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
        PlayerJoinEvent.getHandlerList().unregister(this);
        PlayerDamageLoggerEvent.getHandlerList().unregister(this);
        CombatLogEvent.getHandlerList().unregister(this);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLoggerSpawn(CombatLogEvent event) {
        final Player player = event.getPlayer();
        Bukkit.broadcastMessage(ChatColor.RED + "Combat-Logger: " + ChatColor.YELLOW + player.getName());
        Logger.print(player.getName() + " combat-logged");
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onLoggerDamage(PlayerDamageLoggerEvent event) {
        final Player attacker = event.getPlayer();

        if (!isEnabled()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        performAttack(attacker, null);
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerAttackPlayer(PlayerDamagePlayerEvent event) {
        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();

        if (!isEnabled()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if (attacker.getUniqueId().equals(attacked.getUniqueId())) {
            return;
        }

        performAttack(attacker, attacked);
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerSplashPlayer(PlayerSplashPlayerEvent event) {
        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();
        final ThrownPotion potion = event.getPotion();
        boolean isDebuff = false;

        if (!isEnabled()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

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

        performAttack(attacker, attacked);
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerLingeringSplashPlayer(PlayerLingeringSplashPlayerEvent event) {
        final Player attacker = event.getDamager();
        final Player attacked = event.getDamaged();
        final AreaEffectCloud cloud = event.getCloud();

        if (!isEnabled()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if (attacker.getUniqueId().equals(attacked.getUniqueId())) {
            return;
        }

        if (!cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.HARM) &&
                !cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.WEAKNESS) &&
                !cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.SLOW) &&
                !cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.POISON)) {
            return;
        }

        performAttack(attacker, attacked);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final CombatLogger logger = loggers.get(player.getUniqueId());

        if (!isEnabled()) {
            return;
        }

        if (logger == null) {
            return;
        }

        logger.reapply(player);
        logger.getBukkitEntity().remove();
        loggers.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer profile = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (!isEnabled()) {
            return;
        }

        if (profile == null) {
            return;
        }

        if (profile.isSafelogging()) {
            return;
        }

        if (profile.getCurrentClaim() != null) {
            final DefinedClaim claim = profile.getCurrentClaim();
            final ServerFaction faction = plugin.getFactionManager().getServerFactionById(claim.getOwnerId());

            if (faction != null && faction.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                return;
            }
        }

        if (profile.hasTimer(PlayerTimer.PlayerTimerType.COMBAT)) {
            spawnLogger(player);
            return;
        }

        if (player.getFallDistance() >= 4.0) {
            spawnLogger(player);
            return;
        }

        if (player.getNoDamageTicks() > 0) {
            spawnLogger(player);
            return;
        }

        if (!FactionUtils.getNearbyEnemies(plugin, player, enemyCheckRadius).isEmpty()) {
            spawnLogger(player);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        final LivingEntity entity = event.getEntity();
        final Player killer = entity.getKiller();
        final EntityLiving nmsEntity = ((CraftLivingEntity)entity).getHandle();

        if (!isEnabled()) {
            return;
        }

        if (!(nmsEntity instanceof CombatLogger)) {
            return;
        }

        final CombatLogger logger = (CombatLogger)nmsEntity;
        final LoggerDeathEvent deathEvent = new LoggerDeathEvent(logger, killer);

        Bukkit.getPluginManager().callEvent(deathEvent);

        if (deathEvent.isCancelled()) {
            return;
        }

        logger.dropItems(entity.getLocation());
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isEnabled()) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        final LivingEntity damaged = (LivingEntity)event.getEntity();
        final Entity damager = event.getDamager();
        final EntityLiving nmsDamaged = ((CraftLivingEntity)damaged).getHandle();

        if (!(nmsDamaged instanceof CombatLogger)) {
            return;
        }

        final CombatLogger logger = (CombatLogger)nmsDamaged;

        if (damager instanceof Player) {
            final Player playerDamager = (Player)damager;
            final PlayerDamageLoggerEvent loggerEvent = new PlayerDamageLoggerEvent(playerDamager, logger);

            Bukkit.getPluginManager().callEvent(loggerEvent);

            if (loggerEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }
        }

        if (damager instanceof Projectile) {
            final Projectile projectile = (Projectile)damager;
            final ProjectileSource source = projectile.getShooter();

            if (source instanceof Player) {
                final Player playerDamager = (Player)source;
                final PlayerDamageLoggerEvent loggerEvent = new PlayerDamageLoggerEvent(playerDamager, logger);

                Bukkit.getPluginManager().callEvent(loggerEvent);

                if (loggerEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private void spawnLogger(Player player) {
        if (!isEnabled()) {
            return;
        }

        final CombatLogEvent event = new CombatLogEvent(player);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        final CombatLogger logger = new CombatLogger(((CraftWorld)player.getWorld()).getHandle(), player);
        logger.spawn();
        loggers.put(player.getUniqueId(), logger);

        new Scheduler(plugin).sync(() -> {
            logger.getBukkitEntity().remove();
            loggers.remove(player.getUniqueId());
        }).delay(loggerDuration * 20L).run();
    }

    private void performAttack(@Nonnull Player attacker, @Nullable Player attacked) {
        final FactionPlayer attackerProfile = plugin.getPlayerManager().getPlayer(attacker.getUniqueId());

        if (attackerProfile.hasTimer(PlayerTimer.PlayerTimerType.COMBAT)) {
            final PlayerTimer existing = attackerProfile.getTimer(PlayerTimer.PlayerTimerType.COMBAT);

            if (existing == null) {
                attackerProfile.addTimer(new CombatTagTimer(attacker.getUniqueId(), plugin.getFactionConfig().getTimerCombatTagAttacker()));
                attacker.sendMessage(ChatColor.RED + "Combat Tag: " + ChatColor.BLUE + Time.convertToHHMMSS(plugin.getFactionConfig().getTimerCombatTagAttacker() * 1000L));
            } else if (existing.getExpire() < ((plugin.getFactionConfig().getTimerCombatTagAttacker() * 1000L) + Time.now())) {
                existing.setExpire((plugin.getFactionConfig().getTimerCombatTagAttacker() * 1000L) + Time.now());
            }
        } else {
            attackerProfile.addTimer(new CombatTagTimer(attacker.getUniqueId(), plugin.getFactionConfig().getTimerCombatTagAttacker()));
            attacker.sendMessage(ChatColor.RED + "Combat Tag: " + ChatColor.BLUE + Time.convertToHHMMSS(plugin.getFactionConfig().getTimerCombatTagAttacker() * 1000L));
        }

        if (attacked != null) {
            final FactionPlayer attackedProfile = plugin.getPlayerManager().getPlayer(attacked.getUniqueId());

            if (attackedProfile.hasTimer(PlayerTimer.PlayerTimerType.COMBAT)) {
                final PlayerTimer existing = attackedProfile.getTimer(PlayerTimer.PlayerTimerType.COMBAT);

                if (existing == null) {
                    attackedProfile.addTimer(new CombatTagTimer(attacked.getUniqueId(), plugin.getFactionConfig().getTimerCombatTagAttacked()));
                    attacked.sendMessage(ChatColor.RED + "Combat Tag: " + ChatColor.BLUE + Time.convertToHHMMSS(plugin.getFactionConfig().getTimerCombatTagAttacked() * 1000L));
                } else if (existing.getExpire() < ((plugin.getFactionConfig().getTimerCombatTagAttacked() * 1000L) + Time.now())) {
                    existing.setExpire((plugin.getFactionConfig().getTimerCombatTagAttacked() * 1000L) + Time.now());
                }
            } else {
                attackedProfile.addTimer(new CombatTagTimer(attacked.getUniqueId(), plugin.getFactionConfig().getTimerCombatTagAttacked()));
                attacked.sendMessage(ChatColor.RED + "Combat Tag: " + ChatColor.BLUE + Time.convertToHHMMSS(plugin.getFactionConfig().getTimerCombatTagAttacked() * 1000L));
            }
        }
    }
}