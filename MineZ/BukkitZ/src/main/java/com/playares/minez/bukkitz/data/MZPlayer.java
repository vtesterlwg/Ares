package com.playares.minez.bukkitz.data;

import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.commons.base.util.Time;
import com.playares.commons.bukkit.location.PLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class MZPlayer implements MongoDocument<MZPlayer> {
    @Getter public final UUID uniqueId;
    @Getter @Setter public String username;
    @Getter @Setter public double thirst;
    @Getter @Setter public boolean bleeding;
    @Getter @Setter public PLocatable logoutLocation;
    @Getter @Setter public long nextThirstTick;
    @Getter @Setter public long nextBleedTick;

    public MZPlayer(Player player) {
        this.uniqueId = player.getUniqueId();
        this.username = player.getName();
        this.thirst = 20.0;
        this.bleeding = false;
        this.logoutLocation = null;
        this.nextThirstTick = Time.now() + 3000L;
        this.nextBleedTick = Time.now() + 3000L;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }

    public void tickThirst() {
        final Player player = getPlayer();

        if (player == null) {
            return;
        }

        final double temperature = player.getLocation().getBlock().getTemperature();

        this.nextThirstTick = Time.now() + Math.round(3000 / temperature);
        this.thirst -= 0.1;

        player.setLevel((int)(Math.round(thirst)));
        player.sendMessage(ChatColor.AQUA + "Losing thirst in " + Time.convertToDecimal(this.nextThirstTick - Time.now()));

        if (this.thirst <= 0.0) {
            player.damage(1.0);
            player.sendMessage(ChatColor.DARK_RED + "I'm going to die of dehydration if I don't find water now!!!");
            return;
        }

        final double rounded = Math.round(thirst * 100.0) / 100.0;

        if (rounded == 10.0) {
            player.sendMessage(ChatColor.YELLOW + "My throat feels a bitch parched...");
        } else if (rounded== 3.0) {
            player.sendMessage(ChatColor.YELLOW + "I need to find water soon...");
        }
    }

    public void tickBleed(int nextTickDelay) {
        final Player player = getPlayer();

        this.nextBleedTick = Time.now() + (nextTickDelay * 1000L);

        if (player != null) {
            player.damage(1.0);
            player.sendMessage(ChatColor.DARK_RED + "That hurts... I need to find a bandage to stop the bleeding!");
        }
    }

    @Override
    public MZPlayer fromDocument(Document document) {
        this.thirst = document.getDouble("thirst");
        this.bleeding = document.getBoolean("bleeding");
        this.logoutLocation = new PLocatable().fromDocument(document.get("logoutLocation", Document.class));

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("username", username)
                .append("thirst", thirst)
                .append("bleeding", bleeding)
                .append("logoutLocation", logoutLocation.toDocument());
    }
}
