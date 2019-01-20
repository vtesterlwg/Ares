package com.riotmc.services.humbug.features.cont;

import com.google.common.collect.Lists;
import com.riotmc.services.humbug.HumbugService;
import com.riotmc.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

public final class MemeItems implements HumbugModule, Listener {
    @Getter public final HumbugService humbug;
    @Getter @Setter public boolean enabled;
    @Getter @Setter public boolean chorusFruitTeleportDisabled;
    @Getter @Setter public boolean enderchestDisabled;
    @Getter @Setter public boolean fishingPlayersDisabled;
    @Getter @Setter public boolean dolphinsGraceDisabled;
    @Getter @Setter public boolean naturalPhantomsDisabled;
    @Getter @Setter public boolean disableFireworkElytra;
    @Getter @Setter public boolean lowerTotemDropChances;
    @Getter @Setter public double totemDropChance;
    @Getter @Setter public boolean unbalancedOffhandDisabled;
    @Getter @Setter public boolean craftingEndCrystalDisabled;

    public MemeItems(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Override
    public void loadValues() {
        this.enabled = humbug.getHumbugConfig().getBoolean("meme-items.enabled");
        this.chorusFruitTeleportDisabled = humbug.getHumbugConfig().getBoolean("meme-items.disable-chorus-fruit-teleportation");
        this.enderchestDisabled = humbug.getHumbugConfig().getBoolean("meme-items.disable-ender-chest");
        this.fishingPlayersDisabled = humbug.getHumbugConfig().getBoolean("meme-items.disable-fishing-players");
        this.dolphinsGraceDisabled = humbug.getHumbugConfig().getBoolean("meme-items.disable-dolphins-grace");
        this.naturalPhantomsDisabled = humbug.getHumbugConfig().getBoolean("meme-items.disable-natural-phantom-spawning");
        this.disableFireworkElytra = humbug.getHumbugConfig().getBoolean("meme-items.disable-firework-elytra");
        this.lowerTotemDropChances = humbug.getHumbugConfig().getBoolean("meme-items.lower-totem-drop-chances.enabled");
        this.totemDropChance = humbug.getHumbugConfig().getDouble("meme-items.lower-totem-drop-chances.chances");
        this.unbalancedOffhandDisabled = humbug.getHumbugConfig().getBoolean("meme-items.disable-unbalanced-offhand");
        this.craftingEndCrystalDisabled = humbug.getHumbugConfig().getBoolean("meme-items.disable-end-crystal-crafting");
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
        EntityPotionEffectEvent.getHandlerList().unregister(this);
        PlayerFishEvent.getHandlerList().unregister(this);
        PlayerTeleportEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerSwapHandItemsEvent.getHandlerList().unregister(this);
        InventoryDragEvent.getHandlerList().unregister(this);
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
        if (!isEnabled() || !isChorusFruitTeleportDisabled()) {
            return;
        }

        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (!isEnabled() || !isFishingPlayersDisabled()) {
            return;
        }

        if (!(event.getCaught() instanceof Player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityEffect(EntityPotionEffectEvent event) {
        if (!isEnabled() || !isDolphinsGraceDisabled()) {
            return;
        }

        if (event.getAction().equals(EntityPotionEffectEvent.Action.ADDED) &&
                event.getCause().equals(EntityPotionEffectEvent.Cause.DOLPHIN) &&
                event.getNewEffect().getType().equals(PotionEffectType.DOLPHINS_GRACE)) {

            event.setCancelled(true);

        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!isEnabled() || !isNaturalPhantomsDisabled()) {
            return;
        }

        if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL) && !event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.DEFAULT)) {
            return;
        }

        if (!event.getEntityType().equals(EntityType.PHANTOM)) {
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

        if (hand == null || !hand.getType().equals(Material.FIREWORK_ROCKET)) {
            return;
        }

        if (!player.isGliding()) {
            return;
        }

        if (!action.equals(Action.RIGHT_CLICK_AIR) && !action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

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

            if (killer.getItemInHand() != null && killer.getItemInHand().containsEnchantment(Enchantment.LOOT_BONUS_MOBS)) {
                chance += (killer.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS) * 0.1);
            }
        }

        if (roll > chance) {
            final List<ItemStack> toRemove = Lists.newArrayList();

            for (ItemStack drop : drops) {
                if (drop.getType().equals(Material.TOTEM_OF_UNDYING)) {
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

        if (item == null || (!item.getType().equals(Material.BOW) && !item.getType().equals(Material.TRIDENT))) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "This item can not be moved to your off-hand");
        return;
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

        for (Integer i : event.getNewItems().keySet()) {
            final ItemStack item = event.getNewItems().get(i);

            if (i == 45 && (item.getType().equals(Material.BOW) || item.getType().equals(Material.TRIDENT) || item.getType().equals(Material.SHIELD))) {
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
                !cursor.getType().equals(Material.TRIDENT) &&
                !cursor.getType().equals(Material.SHIELD))) {

            return;

        }

        if (event.getRawSlot() != 45) {
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

        if (player.hasPermission("humbug.bypass")) {
            return;
        }

        if (!item.getType().equals(Material.END_CRYSTAL)) {
            return;
        }

        player.sendMessage(ChatColor.RED + "This item can not be crafted");
        event.setCancelled(true);
    }
}