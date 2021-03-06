package com.playares.civilization.addons.prisonpearls.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.item.ItemBuilder;
import com.playares.commons.bukkit.item.custom.CustomItem;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PrisonPearl implements CustomItem, MongoDocument<PrisonPearl>{
    @Getter public UUID uniqueId;
    @Getter public UUID playerUniqueId;
    @Getter public String playerUsername;
    @Getter public long createdTime;
    @Getter public long expireTime;
    @Getter public String reason;
    @Setter public boolean expired;
    @Getter public PrisonPearlLocation location;

    public PrisonPearl() {
        this.uniqueId = null;
        this.playerUniqueId = null;
        this.playerUsername = null;
        this.createdTime = Time.now();
        this.expireTime = Time.now();
        this.reason = null;
        this.expired = true;
        this.location = new PrisonPearlLocation();
    }

    public PrisonPearl(Player killed, String reason, int duration) {
        this.uniqueId = UUID.randomUUID();
        this.playerUniqueId = killed.getUniqueId();
        this.playerUsername = killed.getName();
        this.createdTime = Time.now();
        this.expireTime = Time.now() + (duration * 1000L);
        this.reason = reason;
        this.expired = false;
        this.location = new PrisonPearlLocation();
    }

    public void updateLocation(Block block) {
        location.setLocationType(PrisonPearlLocation.PrisonPearlLocationType.CONTAINER);
        location.setX(block.getX());
        location.setY(block.getY());
        location.setZ(location.getZ());
        location.setWorldName(block.getWorld().getName());
        location.setPlayerHolderUniqueId(null);
    }

    public void updateLocation(Player player) {
        location.setLocationType(PrisonPearlLocation.PrisonPearlLocationType.PLAYER_INVENTORY);
        location.setX(player.getLocation().getBlockX());
        location.setY(player.getLocation().getBlockY());
        location.setZ(player.getLocation().getBlockZ());
        location.setWorldName(player.getLocation().getWorld().getName());
        location.setPlayerHolderUniqueId(player.getUniqueId());
    }

    public void updateLocation(Location loc) {
        location.setLocationType(PrisonPearlLocation.PrisonPearlLocationType.GROUND);
        location.setX(loc.getBlockX());
        location.setY(loc.getBlockY());
        location.setZ(loc.getBlockZ());
        location.setWorldName(loc.getWorld().getName());
        location.setPlayerHolderUniqueId(null);
    }

    public boolean isExpired() {
        return expired || expireTime <= Time.now();
    }

    @Override
    public Material getMaterial() {
        return Material.ENDER_PEARL;
    }

    @Override
    public String getName() {
        return ChatColor.AQUA + playerUsername;
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();

        lore.add(ChatColor.GOLD + "Imprisoned" + ChatColor.YELLOW + ": " + ChatColor.GRAY + Time.convertToDate("MMM d, hh:mm:ss a z", new Date(createdTime)));
        lore.add(ChatColor.GOLD + "Expires" + ChatColor.YELLOW + ": " + ChatColor.GRAY + Time.convertToDate("MMM d, hh:mm:ss a z", new Date(expireTime)));
        lore.add(ChatColor.GOLD + "Reason" + ChatColor.YELLOW + ": " + ChatColor.GRAY + ChatColor.stripColor(reason));
        lore.add(ChatColor.RESET + " ");
        lore.add(ChatColor.AQUA + "Throw" + ChatColor.YELLOW + " this pearl to free this player");
        lore.add(ChatColor.RESET + " ");
        lore.add(ChatColor.GRAY + "ID: " + uniqueId.toString());

        return lore;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
        enchantments.put(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        return enchantments;
    }

    @Override
    public ItemStack getItem() {
        final ItemStack item = new ItemBuilder().setMaterial(getMaterial()).setName(getName()).setData(getDurability()).addLore(getLore()).addEnchant(getEnchantments()).build();
        final ItemMeta meta = item.getItemMeta();

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(meta);

        return item;
    }

    @Override
    public PrisonPearl fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.playerUniqueId = (UUID)document.get("player_id");
        this.playerUsername = document.getString("player_username");
        this.createdTime = document.getLong("created");
        this.expireTime = document.getLong("expire");
        this.reason = ChatColor.translateAlternateColorCodes('&', document.getString("reason"));
        this.expired = document.getBoolean("expired");
        this.location = new PrisonPearlLocation().fromDocument(document.get("location", Document.class));

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("player_id", playerUniqueId)
                .append("player_username", playerUsername)
                .append("created", createdTime)
                .append("expire", expireTime)
                .append("reason", reason)
                .append("expired", expired)
                .append("location", location.toDocument());
    }
}