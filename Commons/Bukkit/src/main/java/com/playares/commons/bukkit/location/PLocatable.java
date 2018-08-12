package com.playares.commons.bukkit.location;

import com.playares.commons.base.connect.mongodb.MongoDocument;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;

public class PLocatable implements Locatable, MongoDocument<PLocatable> {
    @Nonnull @Getter @Setter
    public String worldName;

    @Getter @Setter
    double x;

    @Getter @Setter
    double y;

    @Getter @Setter
    double z;

    @Getter @Setter
    float yaw;

    @Getter @Setter
    float pitch;

    public PLocatable(@Nonnull LivingEntity entity) {
        this.worldName = entity.getLocation().getWorld().getName();
        this.x = entity.getLocation().getX();
        this.y = entity.getLocation().getY();
        this.z = entity.getLocation().getZ();
        this.yaw = entity.getLocation().getYaw();
        this.pitch = entity.getLocation().getPitch();
    }

    public PLocatable(@Nonnull String worldName, double x, double y, double z, float yaw, float pitch) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location getBukkit() {
        if (Bukkit.getWorld(worldName) == null) {
            throw new NullPointerException("World not found for Player Locatable");
        }

        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    @Override
    public PLocatable fromDocument(Document document) {
        return new PLocatable(
                document.getString("world"),
                document.getDouble("x"),
                document.getDouble("y"),
                document.getDouble("z"),
                document.getDouble("yaw").floatValue(),
                document.getDouble("pitch").floatValue());
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("world", getWorldName())
                .append("x", getX())
                .append("y", getY())
                .append("z", getZ())
                .append("yaw", getYaw())
                .append("pitch", getPitch());
    }
}
