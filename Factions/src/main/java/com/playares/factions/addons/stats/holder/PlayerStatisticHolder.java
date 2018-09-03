package com.playares.factions.addons.stats.holder;

import com.google.common.collect.Maps;
import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.commons.base.util.Time;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.Material;

import java.util.Map;

public final class PlayerStatisticHolder implements StatisticHolder, MongoDocument<PlayerStatisticHolder> {
    @Getter
    public int kills, deaths, minorEventCaptures, majorEventCaptures;

    @Getter
    public Map<Material, Integer> minedOres;

    @Getter
    public long playtime;

    public PlayerStatisticHolder() {
        this.kills = 0;
        this.deaths = 0;
        this.minorEventCaptures = 0;
        this.majorEventCaptures = 0;
        this.minedOres = Maps.newConcurrentMap();
        this.playtime = 0L;

        this.minedOres.put(Material.COAL_ORE, 0);
        this.minedOres.put(Material.IRON_ORE, 0);
        this.minedOres.put(Material.REDSTONE_ORE, 0);
        this.minedOres.put(Material.LAPIS_ORE, 0);
        this.minedOres.put(Material.GOLD_ORE, 0);
        this.minedOres.put(Material.DIAMOND_ORE, 0);
        this.minedOres.put(Material.EMERALD_ORE, 0);
    }

    @Override
    public void addKill() {
        this.kills += 1;
    }

    @Override
    public void addDeath() {
        this.deaths += 1;
    }

    @Override
    public void addMinorEventCapture() {
        this.minorEventCaptures += 1;
    }

    @Override
    public void addMajorEventCapture() {
        this.majorEventCaptures += 1;
    }

    public void addOre(Material material, int amount) {
        minedOres.put(material, (minedOres.getOrDefault(material, 0) + amount));
    }

    public void addPlaytime(long loginTime) {
        final long ms = Time.now() - loginTime;
        this.playtime += ms;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PlayerStatisticHolder fromDocument(Document document) {
        final Map<String, Integer> mappedMinedOres = (Map<String, Integer>)document.get("minedOres");
        this.kills = document.getInteger("kills");
        this.deaths = document.getInteger("deaths");
        this.minorEventCaptures = document.getInteger("minorEvents");
        this.majorEventCaptures = document.getInteger("majorEvents");
        this.playtime = document.getLong("playtime");

        final Map<Material, Integer> remapped = Maps.newHashMap();

        mappedMinedOres.keySet().forEach(oreName -> {
            final Material material = Material.matchMaterial(oreName);
            final int amt = mappedMinedOres.get(oreName);

            if (material != null) {
                remapped.put(material, amt);
            }
        });

        minedOres.putAll(remapped);
        return this;
    }

    @Override
    public Document toDocument() {
        final Map<String, Integer> convertedOres = Maps.newHashMap();

        minedOres.keySet().forEach(ore -> {
            final int amt = minedOres.get(ore);
            convertedOres.put(ore.name(), amt);
        });

        return new Document()
                .append("kills", kills)
                .append("deaths", deaths)
                .append("minorEvents", minorEventCaptures)
                .append("majorEvents", majorEventCaptures)
                .append("playtime", playtime)
                .append("minedOres", convertedOres);
    }
}