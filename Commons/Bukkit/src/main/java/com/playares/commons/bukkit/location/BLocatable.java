package com.playares.commons.bukkit.location;

import com.playares.commons.base.connect.mongodb.MongoDocument;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Represents a single block location
 */
public class BLocatable implements Locatable, MongoDocument<BLocatable> {
    @Nonnull @Getter @Setter public String worldName;
    @Getter @Setter double x;
    @Getter @Setter double y;
    @Getter @Setter double z;

    public BLocatable(@Nonnull Block block) {
        this.worldName = block.getWorld().getName();
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
    }

    public BLocatable(@Nonnull String worldName, double x, double y, double z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Block getBukkit() {
        if (Bukkit.getWorld(worldName) == null) {
            throw new NullPointerException("World not found for Block Locatable");
        }

        return Objects.requireNonNull(Bukkit.getWorld(worldName)).getBlockAt((int)getX(), (int)getY(), (int)getZ());
    }

    @Override
    public String toString() {
        return Math.round(x) + ", " + Math.round(y) + ", " + Math.round(z) + ", " + getBukkit().getWorld().getEnvironment().name();
    }

    @Override
    public BLocatable fromDocument(Document document) {
        return new BLocatable(
                document.getString("world"),
                document.getDouble("x"),
                document.getDouble("y"),
                document.getDouble("z"));
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("world", getWorldName())
                .append("x", getX())
                .append("y", getY())
                .append("z", getZ());
    }
}
