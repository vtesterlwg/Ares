package com.playares.factions.addons.loggers;

import com.google.common.collect.Maps;
import com.playares.commons.base.promise.SimplePromise;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.event.PlayerDamagePlayerEvent;
import com.playares.commons.bukkit.event.PlayerLingeringSplashPlayerEvent;
import com.playares.commons.bukkit.event.PlayerSplashPlayerEvent;
import com.playares.commons.bukkit.logger.Logger;
import com.playares.commons.bukkit.util.Scheduler;
import com.playares.factions.Factions;
import com.playares.factions.addons.Addon;
import com.playares.factions.addons.loggers.command.LogoutCommand;
import com.playares.factions.addons.loggers.data.CombatLogger;
import com.playares.factions.addons.loggers.event.CombatLogEvent;
import com.playares.factions.addons.loggers.event.LoggerDeathEvent;
import com.playares.factions.addons.loggers.event.PlayerDamageLoggerEvent;
import com.playares.factions.claims.data.DefinedClaim;
import com.playares.factions.factions.data.ServerFaction;
import com.playares.factions.players.data.FactionPlayer;
import com.playares.factions.timers.PlayerTimer;
import com.playares.factions.timers.cont.player.CombatTagTimer;
import com.playares.factions.timers.cont.player.LogoutTimer;
import com.playares.factions.util.FactionUtils;
import com.playares.services.customentity.CustomEntityService;
import com.playares.services.deathban.DeathbanService;
import com.playares.services.deathban.dao.DeathbanDAO;
import com.playares.services.deathban.data.Deathban;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_12_R1.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public final class LoggerAddon implements Addon, Listener {
    @Getter public final Factions plugin;
    @Getter @Setter public boolean enabled;
    /* Duration combat loggers should be alive for */
    @Getter @Setter public int loggerDuration;
    /* Radius to check for enemy players when performing a logger check */
    @Getter @Setter public int enemyCheckRadius;
    /* Time (in seconds) the logout timer delays before safely removing the player from the server */
    @Getter @Setter public int logoutTimerDuration;
    /* Map containing currently active combat-loggers */
    @Getter public final Map<UUID, CombatLogger> loggers;

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
        this.logoutTimerDuration = config.getInt("timers.player.logout");
    }

    @Override
    public void start() {
        final CustomEntityService service = (CustomEntityService)plugin.getService(CustomEntityService.class);

        if (service != null) {
            service.register(120, "combat_logger", CombatLogger.class);
        } else {
            Logger.error("Could not find Custom Entity Service, Combat Loggers will not spawn!");
        }

        plugin.registerListener(this);
        plugin.registerCommand(new LogoutCommand(this));
    }

    public void attemptSafeLogout(Player player, SimplePromise promise) {
        final FactionPlayer factionPlayer = plugin.getPlayerManager().getPlayer(player.getUniqueId());

        if (factionPlayer == null) {
            promise.failure("Failed to obtain faction profile");
            return;
        }

        if (factionPlayer.hasTimer(PlayerTimer.PlayerTimerType.LOGOUT)) {
            promise.failure("Logout timer has already started");
            return;
        }

        if (factionPlayer.hasTimer(PlayerTimer.PlayerTimerType.COMBAT)) {
            promise.failure("You can not start a safe logout while combat-tagged");
            return;
        }

        final LogoutTimer timer = new LogoutTimer(plugin, player.getUniqueId(), logoutTimerDuration, factionPlayer);
        factionPlayer.getTimers().add(timer);
        promise.success();
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
        final DeathbanService deathbanService = (DeathbanService)getPlugin().getService(DeathbanService.class);

        if (deathbanService != null && deathbanService.getRecentlyKicked().contains(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

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

        // Fix for custom potions
        if (cloud.getBasePotionData() == null || cloud.getBasePotionData().getType() == null || cloud.getBasePotionData().getType().getEffectType() == null) {
            return;
        }

        if (
                !cloud.getBasePotionData().getType().getEffectType().equals(PotionEffectType.HARM) &&
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
        final DeathbanService deathbanService = (DeathbanService)getPlugin().getService(DeathbanService.class);

        if (!isEnabled()) {
            return;
        }

        if (profile == null) {
            return;
        }

        if (profile.isSafelogging()) {
            return;
        }

        if (player.hasPermission("factions.mod") || player.hasPermission("factions.admin")) {
            return;
        }

        if (!player.getGameMode().equals(GameMode.SURVIVAL)) {
            return;
        }

        if (player.getHealth() <= 0.0 || player.isDead()) {
            return;
        }

        if (profile.getCurrentClaim() != null) {
            final DefinedClaim claim = profile.getCurrentClaim();
            final ServerFaction faction = plugin.getFactionManager().getServerFactionById(claim.getOwnerId());

            if (faction != null && faction.getFlag().equals(ServerFaction.FactionFlag.SAFEZONE)) {
                return;
            }
        }

        if (deathbanService != null) {
            new Scheduler(getPlugin()).async(() -> {
                final Deathban deathban = DeathbanDAO.getDeathban(getPlugin().getMongo(), profile.getUniqueId());

                new Scheduler(getPlugin()).sync(() -> {
                    if (deathban != null) {
                        return;
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
                }).run();
            }).run();

            return;
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
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        final Entity entity = event.getRightClicked();

        if (event.isCancelled()) {
            return;
        }

        if (!(entity instanceof LivingEntity)) {
            return;
        }

        final LivingEntity livingEntity = (LivingEntity)entity;
        final EntityLiving asNms = ((CraftLivingEntity)livingEntity).getHandle();

        if (!(asNms instanceof CombatLogger)) {
            return;
        }

        event.setCancelled(true);
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

    @EventHandler (priority = EventPriority.HIGH)
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (!isEnabled()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        final Chunk chunk = event.getChunk();

        if (getLoggers().isEmpty()) {
            return;
        }

        getLoggers().values().stream().filter(logger ->
                logger.getBukkitEntity().getChunk().getChunkKey() == chunk.getChunkKey()).findAny().ifPresent(found -> event.setCancelled(true));
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
                attackerProfile.addTimer(new CombatTagTimer(plugin, attacker.getUniqueId(), plugin.getFactionConfig().getTimerCombatTagAttacker()));
                attacker.sendMessage(ChatColor.RED + "Combat Tag: " + ChatColor.BLUE + Time.convertToHHMMSS(plugin.getFactionConfig().getTimerCombatTagAttacker() * 1000L));
            } else if (existing.getExpire() < ((plugin.getFactionConfig().getTimerCombatTagAttacker() * 1000L) + Time.now())) {
                existing.setExpire((plugin.getFactionConfig().getTimerCombatTagAttacker() * 1000L) + Time.now());
            }
        } else {
            attackerProfile.addTimer(new CombatTagTimer(plugin, attacker.getUniqueId(), plugin.getFactionConfig().getTimerCombatTagAttacker()));
            attacker.sendMessage(ChatColor.RED + "Combat Tag: " + ChatColor.BLUE + Time.convertToHHMMSS(plugin.getFactionConfig().getTimerCombatTagAttacker() * 1000L));
        }

        if (attacked != null) {
            final FactionPlayer attackedProfile = plugin.getPlayerManager().getPlayer(attacked.getUniqueId());

            if (attackedProfile.hasTimer(PlayerTimer.PlayerTimerType.COMBAT)) {
                final PlayerTimer existing = attackedProfile.getTimer(PlayerTimer.PlayerTimerType.COMBAT);

                if (existing == null) {
                    attackedProfile.addTimer(new CombatTagTimer(plugin, attacked.getUniqueId(), plugin.getFactionConfig().getTimerCombatTagAttacked()));
                    attacked.sendMessage(ChatColor.RED + "Combat Tag: " + ChatColor.BLUE + Time.convertToHHMMSS(plugin.getFactionConfig().getTimerCombatTagAttacked() * 1000L));
                } else if (existing.getExpire() < ((plugin.getFactionConfig().getTimerCombatTagAttacked() * 1000L) + Time.now())) {
                    existing.setExpire((plugin.getFactionConfig().getTimerCombatTagAttacked() * 1000L) + Time.now());
                }
            } else {
                attackedProfile.addTimer(new CombatTagTimer(plugin, attacked.getUniqueId(), plugin.getFactionConfig().getTimerCombatTagAttacked()));
                attacked.sendMessage(ChatColor.RED + "Combat Tag: " + ChatColor.BLUE + Time.convertToHHMMSS(plugin.getFactionConfig().getTimerCombatTagAttacked() * 1000L));
            }
        }
    }
}