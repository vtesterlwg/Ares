package com.playares.services.humbug.features.cont;

import com.google.common.collect.Lists;
import com.playares.services.humbug.HumbugService;
import com.playares.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public final class MemeItems implements HumbugModule, Listener {
    @Getter public final HumbugService humbug;
    @Getter @Setter public boolean enabled;
    @Getter @Setter public boolean chorusFruitTeleportDisabled;
    @Getter @Setter public boolean enderchestDisabled;
    @Getter @Setter public boolean fishingPlayersDisabled;
    @Getter @Setter public boolean disableFireworkElytra;
    @Getter @Setter public boolean lowerTotemDropChances;
    @Getter @Setter public double totemDropChance;
    @Getter @Setter public boolean unbalancedOffhandDisabled;
    @Getter @Setter public boolean craftingEndCrystalDisabled;
    @Getter @Setter public boolean endermiteSpawningDisabled;
    @Getter @Setter public boolean bedBombsDisabled;
    @Getter @Setter public boolean tntDamageNerfed;

    public MemeItems(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Override
    public void loadValues() {
        final YamlConfiguration config = getHumbug().getOwner().getConfig("humbug");

        this.enabled = config.getBoolean("meme-items.enabled");
        this.chorusFruitTeleportDisabled = config.getBoolean("meme-items.disable-chorus-fruit-teleportation");
        this.enderchestDisabled = config.getBoolean("meme-items.disable-ender-chest");
        this.fishingPlayersDisabled = config.getBoolean("meme-items.disable-fishing-players");
        this.disableFireworkElytra = config.getBoolean("meme-items.disable-firework-elytra");
        this.lowerTotemDropChances = config.getBoolean("meme-items.lower-totem-drop-chances.enabled");
        this.totemDropChance = config.getDouble("meme-items.lower-totem-drop-chances.chances");
        this.unbalancedOffhandDisabled = config.getBoolean("meme-items.disable-unbalanced-offhand");
        this.craftingEndCrystalDisabled = config.getBoolean("meme-items.disable-end-crystal-crafting");
        this.endermiteSpawningDisabled = config.getBoolean("meme-items.disable-endermite-spawning");
        this.bedBombsDisabled = config.getBoolean("meme-items.disable-bed-bombs");
        this.tntDamageNerfed = config.getBoolean("meme-items.nerf-tnt-damage");
    }

    @Override
    public String getName() {
        return "Fix Meme Items";
    }

    @Override
    public void start() {
        this.humbug.getOwner().registerListener(this);
    }

    @Override
    public void stop() {
        BlockPlaceEvent.getHandlerList().unregister(this);
        PlayerFishEvent.getHandlerList().unregister(this);
        PlayerTeleportEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerSwapHandItemsEvent.getHandlerList().unregister(this);
        InventoryDragEvent.getHandlerList().unregister(this);
        EntityDamageByEntityEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (!isEnabled() || !isEnderchestDisabled()) {
            return;
        }

        if (!block.getType().equals(Material.ENDER_CHEST)) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "Ender Chests are disabled");
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();

        if (!isEnabled() || !isChorusFruitTeleportDisabled()) {
            return;
        }

        if (player.hasPermission("humbug.bypass")) {
            return;
        }

        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        final Player player = event.getPlayer();

        if (!isEnabled() || !isFishingPlayersDisabled()) {
            return;
        }

        if (!(event.getCaught() instanceof Player)) {
            return;
        }

        if (player.hasPermission("humbug.bypass")) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isEnabled() || !isDisableFireworkElytra()) {
            return;
        }

        final Player player = event.getPlayer();
        final ItemStack hand = event.getItem();
        final Action action = event.getAction();

        if (hand == null || !hand.getType().equals(Material.FIREWORK)) {
            return;
        }

        if (!player.isGliding()) {
            return;
        }

        if (!action.equals(Action.RIGHT_CLICK_AIR) && !action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (player.hasPermission("humbug.bypass")) {
            return;
        }

        player.sendMessage(ChatColor.RED + "This item is disabled while flying");
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isEnabled() || !isLowerTotemDropChances()) {
            return;
        }

        final LivingEntity entity = event.getEntity();

        if (!(entity instanceof Evoker)) {
            return;
        }

        final List<ItemStack> drops = event.getDrops();
        final double roll = Math.abs(new Random().nextDouble());
        double chance = getTotemDropChance();

        if (entity.getKiller() != null) {
            final Player killer = entity.getKiller();

            if (killer.getInventory().getItemInMainHand() != null && killer.getInventory().getItemInMainHand().containsEnchantment(Enchantment.LOOT_BONUS_MOBS)) {
                chance += (killer.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS) * 0.1);
            }
        }

        if (roll > chance) {
            final List<ItemStack> toRemove = Lists.newArrayList();

            for (ItemStack drop : drops) {
                if (drop.getType().equals(Material.TOTEM)) {
                    toRemove.add(drop);
                }
            }

            drops.removeAll(toRemove);
        }
    }

    @EventHandler
    public void onHandSwap(PlayerSwapHandItemsEvent event) {
        if (!isEnabled() || !isUnbalancedOffhandDisabled()) {
            return;
        }

        final Player player = event.getPlayer();
        final ItemStack item = event.getOffHandItem();

        if (item == null || !item.getType().equals(Material.BOW)) {
            return;
        }

        if (player.hasPermission("humbug.bypass")) {
            return;
        }

        player.sendMessage(ChatColor.RED + "This item can not be moved to your off-hand");
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!isEnabled() || !isUnbalancedOffhandDisabled()) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getWhoClicked();

        if (player.hasPermission("humbug.bypass")) {
            return;
        }

        for (Integer i : event.getNewItems().keySet()) {
            final ItemStack item = event.getNewItems().get(i);

            if (i == 45 && (item.getType().equals(Material.BOW) || item.getType().equals(Material.SHIELD))) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "This item can not be moved to your off-hand");
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled() || !isUnbalancedOffhandDisabled()) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getWhoClicked();
        final ItemStack cursor = event.getCursor();

        if (cursor == null ||
                (!cursor.getType().equals(Material.BOW) &&
                !cursor.getType().equals(Material.SHIELD))) {

            return;

        }

        if (event.getRawSlot() != 45) {
            return;
        }

        if (player.hasPermission("humbug.bypass")) {
            return;
        }

        player.sendMessage(ChatColor.RED + "This item can not be moved to your off-hand");
        event.setCancelled(true);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        final Player player = (Player)event.getWhoClicked();
        final ItemStack item = event.getCurrentItem();

        if (!item.getType().equals(Material.END_CRYSTAL)) {
            return;
        }

        if (player.hasPermission("humbug.bypass")) {
            return;
        }

        player.sendMessage(ChatColor.RED + "This item can not be crafted");
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEndermiteSpawn(CreatureSpawnEvent event) {
        final LivingEntity entity = event.getEntity();

        if (event.isCancelled()) {
            return;
        }

        if (!(entity instanceof Endermite)) {
            return;
        }

        if (!isEndermiteSpawningDisabled()) {
            return;
        }

        if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.ENDER_PEARL)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onBedInteract(PlayerInteractEvent event) {
        if (!isEnabled() || !isBedBombsDisabled()) {
            return;
        }

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        if (event.getClickedBlock() == null || !(event.getClickedBlock().getType().equals(Material.BED) || event.getClickedBlock().getType().equals(Material.BED_BLOCK))) {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();

        if (block.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
            return;
        }

        if (player.hasPermission("humbug.bypass")) {
            return;
        }

        player.sendMessage(ChatColor.RED + "Bed bombs have been disabled");
        event.setCancelled(true);
    }

    @EventHandler
    public void onTNTDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled() || !isTntDamageNerfed()) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity) || !(event.getDamager() instanceof TNTPrimed)) {
            return;
        }

        event.setDamage(event.getDamage() / 1.5);
    }

    @EventHandler
    public void onItemCraft(CraftItemEvent event) {
        if (!isEnabled() || !isCraftingEndCrystalDisabled()) {
            return;
        }

        final ItemStack item = event.getCurrentItem();

        if (item == null || !item.getType().equals(Material.END_CRYSTAL)) {
            return;
        }

        event.setCurrentItem(null);
        event.setCancelled(true);
    }
}