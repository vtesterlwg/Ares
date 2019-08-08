package com.playares.factions.addons.economy.data;

import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.factions.addons.economy.EconomyAddon;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class EconomyPlayer implements MongoDocument<EconomyPlayer> {
    @Getter public EconomyAddon addon;
    @Getter public UUID uniqueId;
    @Getter @Setter public double balance;

    public EconomyPlayer(EconomyAddon addon) {
        this.addon = addon;
        this.uniqueId = null;
        this.balance = addon.getStartingBalance();
    }

    public EconomyPlayer(EconomyAddon addon, UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.balance = addon.getStartingBalance();
    }

    public EconomyPlayer(EconomyAddon addon, Player player, double balance) {
        this.addon = addon;
        this.uniqueId = player.getUniqueId();
        this.balance = balance;
    }

    public void add(double amount) {
        this.balance += amount;
    }

    public void subtract(double amount) {
        if (!canAfford(amount)) {
            this.balance = 0.0;
            return;
        }

        this.balance -= amount;
    }

    public void transferTo(EconomyPlayer other, double amount) {
        subtract(amount);
        other.add(amount);
    }

    public boolean canAfford(double amount) {
        return this.balance >= amount;
    }

    @Override
    public EconomyPlayer fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.balance = document.getDouble("balance");
        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("balance", balance);
    }
}