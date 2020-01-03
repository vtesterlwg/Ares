package com.playares.civilization.addons.prisonpearls;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.playares.civilization.CivManager;
import com.playares.civilization.addons.prisonpearls.data.PrisonPearl;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.logger.Logger;
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
        pearlRepository.addAll(PrisonPearlDAO.get(addon.getAddonManager().getPlugin()));
        Logger.print("Loaded " + pearlRepository.size() + " Prison Pearls");
    }

    public void save(PrisonPearl pearl) {
        PrisonPearlDAO.save(addon.getAddonManager().getPlugin(), pearl);
    }

    public void saveAll() {
        final long start = Time.now();
        Logger.warn("Preparing to save " + pearlRepository.size() + " Prison Pearls...");

        PrisonPearlDAO.save(addon.getAddonManager().getPlugin(), pearlRepository);

        final long finish = Time.now();
        Logger.print("Finished saving " + pearlRepository.size() + " Prison Pearls. Duration: " + (finish - start) + "ms");
    }

    public PrisonPearl getPearlByPearlID(UUID uniqueId) {
        return pearlRepository.stream().filter(pearl -> pearl.getUniqueId().equals(uniqueId) && !pearl.isExpired()).findFirst().orElse(null);
    }

    public PrisonPearl getPearlByPlayerID(UUID uniqueId) {
        return pearlRepository.stream().filter(pearl -> pearl.getPlayerUniqueId().equals(uniqueId) && !pearl.isExpired()).findFirst().orElse(null);
    }

    public PrisonPearl getPearl(Player player) {
        return pearlRepository.stream().filter(pearl -> pearl.getPlayerUniqueId().equals(player.getUniqueId()) && !pearl.isExpired()).findFirst().orElse(null);
    }

    public PrisonPearl getPearl(ItemStack item) {
        final UUID pearlId = getPearlID(item);

        if (pearlId == null) {
            return null;
        }

        return pearlRepository.stream().filter(pearl -> pearl.getUniqueId().equals(pearlId) && !pearl.isExpired()).findFirst().orElse(null);
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