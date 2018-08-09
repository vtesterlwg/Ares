package com.playares.services.humbug.features.cont;

import com.playares.services.humbug.HumbugService;
import com.playares.services.humbug.features.HumbugModule;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;

public final class MemeItems implements HumbugModule, Listener {
    @Getter
    public final HumbugService humbug;

    @Getter @Setter
    public boolean enabled;

    @Getter @Setter
    public boolean chorusFruitTeleportDisabled;

    @Getter @Setter
    public boolean enderchestDisabled;

    @Getter @Setter
    public boolean fishingPlayersDisabled;

    @Getter @Setter
    public boolean dolphinsGraceDisabled;

    public MemeItems(HumbugService humbug) {
        this.humbug = humbug;
    }

    @Override
    public void loadValues() {
        this.enabled = humbug.getHumbugConfig().getBoolean("modules.meme-items.enabled");
        this.chorusFruitTeleportDisabled = humbug.getHumbugConfig().getBoolean("modules.meme-items.disable-chorus-fruit-teleportation");
        this.enderchestDisabled = humbug.getHumbugConfig().getBoolean("modules.meme-items.disable-ender-chest");
        this.fishingPlayersDisabled = humbug.getHumbugConfig().getBoolean("modules.meme-items.disable-fishing-players");
        this.dolphinsGraceDisabled = humbug.getHumbugConfig().getBoolean("modules.meme-items.disable-dolphins-grace");
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
                event.getNewEffect().getType().equals(PotionEffectType.DOLPHINS_GRACE)) {

            event.setCancelled(true);

        }
    }
}