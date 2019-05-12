package com.playares.minez.bukkitz.item;

import com.playares.commons.bukkit.item.custom.CustomItem;
import com.playares.minez.bukkitz.MineZ;
import com.playares.services.customitems.CustomItemService;
import lombok.Getter;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.NBTTagString;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.UUID;

public final class ItemManager {
    @Getter public final MineZ plugin;

    public ItemManager(MineZ plugin) {
        this.plugin = plugin;
    }

    public ItemStack getBandage(int amount) {
        final CustomItemService itemService = (CustomItemService)plugin.getService(CustomItemService.class);

        if (itemService != null) {
            final Optional<CustomItem> bandage = itemService.getItem(BandageItem.class);

            if (!bandage.isPresent()) {
                return null;
            }

            final CustomItem customItem = bandage.get();
            final net.minecraft.server.v1_13_R2.ItemStack nms = CraftItemStack.asNMSCopy(customItem.getItem());
            final NBTTagCompound compound = nms.getOrCreateTag();

            compound.set("unstackable", new NBTTagString(UUID.randomUUID().toString()));
            nms.setTag(compound);

            final ItemStack asBukkit = CraftItemStack.asBukkitCopy(nms);
            asBukkit.setAmount(amount);

            return asBukkit;
        }

        return null;
    }
}
