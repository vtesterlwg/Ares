package com.playares.commons.bukkit.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class ItemBuilder {
    private Material material;
    private int amount;
    private short data;
    private String name;
    private List<ItemFlag> flags;
    private List<String> lore;
    private Map<Enchantment, Integer> enchantments;

    public ItemBuilder() {
        this.amount = 1;
    }

    public ItemBuilder setMaterial(@Nonnull Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder setData(short data) {
        this.data = data;
        return this;
    }

    public ItemBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ItemBuilder addFlag(@Nonnull  ItemFlag flag) {
        if (this.flags == null) {
            this.flags = Lists.newArrayList();
        }

        this.flags.add(flag);
        return this;
    }

    public ItemBuilder addFlag(Collection<ItemFlag> flags) {
        flags.forEach(this::addFlag);
        return this;
    }

    public ItemBuilder addLore(String line) {
        if (this.lore == null) {
            this.lore = Lists.newArrayList();
        }

        this.lore.add(line);
        return this;
    }

    public ItemBuilder addLore(Collection<String> lines) {
        lines.forEach(this::addLore);
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, Integer level) {
        if (this.enchantments == null) {
            this.enchantments = Maps.newHashMap();
        }

        this.enchantments.put(enchantment, level);
        return this;
    }

    public ItemBuilder addEnchant(Map<Enchantment, Integer> enchantments) {
        this.enchantments = enchantments;
        return this;
    }

    public ItemStack build() {
        if (this.material == null) {
            throw new NullPointerException("Material can not be null");
        }

        final ItemStack item = new ItemStack(material, amount, data);
        final ItemMeta meta = item.getItemMeta();

        if (this.name != null) {
            meta.setDisplayName(this.name);
        }

        if (this.flags != null && !this.flags.isEmpty()) {
            flags.forEach(meta::addItemFlags);
        }

        if (this.lore != null && !this.lore.isEmpty()) {
            meta.setLore(lore);
        }

        if (this.enchantments != null && !this.enchantments.isEmpty()) {
            this.enchantments.keySet().forEach(enchantment -> meta.addEnchant(enchantment, enchantments.get(enchantment), true));
        }

        item.setItemMeta(meta);

        return item;
    }
}