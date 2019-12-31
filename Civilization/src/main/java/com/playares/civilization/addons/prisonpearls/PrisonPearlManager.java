package com.playares.civilization.addons.prisonpearls;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.civilization.CivManager;
import com.playares.civilization.addons.prisonpearls.data.PrisonPearl;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class PrisonPearlManager implements CivManager {
    @Getter public final PrisonPearlAddon addon;
    @Getter public final PrisonPearlHandler prisonPearlHandler;
    @Getter public final Set<PrisonPearl> pearlRepository;

    public PrisonPearlManager(PrisonPearlAddon addon) {
        this.addon = addon;
        this.prisonPearlHandler = new PrisonPearlHandler(this);
        this.pearlRepository = Sets.newConcurrentHashSet();
    }

    @Override
    public String getName() {
        return "Prison Pearl Manager";
    }

    @Override
    public List<String> getDebug() {
        final List<String> debug = Lists.newArrayList();
        debug.add("Loaded Prison Pearls: " + pearlRepository);
        return debug;
    }

    public void load() {

    }

    public void save(PrisonPearl pearl) {

    }

    public void saveAll() {

    }

    public PrisonPearl getPearlByPearlID(UUID uniqueId) {
        return pearlRepository.stream().filter(pearl -> pearl.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public PrisonPearl getPearlByPlayerID(UUID uniqueId) {
        return pearlRepository.stream().filter(pearl -> pearl.getPlayerUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public PrisonPearl getPearl(Player player) {
        return pearlRepository.stream().filter(pearl -> pearl.getPlayerUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
    }

    public PrisonPearl getPearl(ItemStack item) {
        final UUID pearlId = getPearlID(item);

        if (pearlId == null) {
            return null;
        }

        return pearlRepository.stream().filter(pearl -> pearl.getUniqueId().equals(pearlId)).findFirst().orElse(null);
    }

    public UUID getPearlID(ItemStack item) {
        if (item == null || item.getItemMeta() == null || !item.getType().equals(Material.ENDER_PEARL)) {
            return null;
        }

        final List<String> lore = item.getItemMeta().getLore();
        String lastLine;

        if (lore.isEmpty()) {
            return null;
        }

        lastLine = lore.get(lore.size() - 1);

        if (!lastLine.startsWith(ChatColor.GRAY + "ID: ")) {
            return null;
        }

        return UUID.fromString(lastLine.replace(ChatColor.GRAY + "ID: ", ""));
    }
}