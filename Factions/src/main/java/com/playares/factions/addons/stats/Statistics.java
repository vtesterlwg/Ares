package com.playares.factions.addons.stats;

import com.google.common.collect.Maps;
import com.playares.commons.base.connect.mongodb.MongoDocument;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Material;

import java.util.Map;
import java.util.UUID;

public final class Statistics implements MongoDocument<Statistics> {
    @Getter
    public UUID uniqueId;

    @Getter
    public Map<Material, Integer> minedOres;

    @Getter @Setter
    public int kills;

    @Getter @Setter
    public int deaths;

    @Getter @Setter
    public int minorEventCaptures;

    @Getter @Setter
    public int majorEventCaptures;

    public int getMinedOre(Material material) {
        return this.minedOres.getOrDefault(material, 0);
    }

    public void addMinedOre(Material material, int amount) {
        int previous = getMinedOre(material);
        this.minedOres.put(material, (previous + amount));
    }

    public void addKill() {
        setKills(getKills() + 1);
    }

    public void addDeath() {
        setDeaths(getDeaths() + 1);
    }

    public void addMinorEvent() {
        setMinorEventCaptures(getMinorEventCaptures() + 1);
    }

    public void addMajorEvent() {
        setMajorEventCaptures(getMajorEventCaptures() + 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Statistics fromDocument(Document document) {
        final Map<String, Integer> convertedOres = (Map<String, Integer>)document.get("ores");

        this.uniqueId = (UUID)document.get("id");
        this.minedOres = Maps.newConcurrentMap();
        this.kills = document.getInteger("kills");
        this.deaths = document.getInteger("deaths");
        this.minorEventCaptures = document.getInteger("minorEvents");
        this.majorEventCaptures = document.getInteger("majorEvents");

        convertedOres.keySet().forEach(oreName -> {
            final int amt = convertedOres.get(oreName);
            this.minedOres.put(Material.valueOf(oreName), amt);
        });

        return this;
    }

    @Override
    public Document toDocument() {
        final Map<String, Integer> convertedOres = Maps.newHashMap();

        for (Material mat : minedOres.keySet()) {
            final int amt = minedOres.get(mat);

            convertedOres.put(mat.toString(), amt);
        }

        return new Document()
                .append("id", uniqueId)
                .append("ores", convertedOres)
                .append("kills", kills)
                .append("deaths", deaths)
                .append("minorEvents", minorEventCaptures)
                .append("majorEvents", majorEventCaptures);
    }
}