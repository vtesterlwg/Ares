package com.playares.factions.claims;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.playares.commons.base.connect.mongodb.MongoDocument;
import com.playares.commons.bukkit.location.BLocatable;
import com.playares.commons.bukkit.location.Locatable;
import com.playares.factions.Factions;
import com.playares.factions.factions.Faction;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class DefinedClaim implements Claimable, MongoDocument<DefinedClaim> {
    @Getter
    public Factions plugin;

    @Getter
    public UUID uniqueId;

    @Getter
    public UUID ownerId;

    @Getter @Setter
    public double x1, y1, z1;

    @Getter @Setter
    public double x2, y2, z2;

    @Getter @Setter
    public String worldName;

    public DefinedClaim(Factions plugin) {
        this.plugin = plugin;
        this.uniqueId = UUID.randomUUID();
        this.ownerId = null;
        this.x1 = 0;
        this.y1 = 0;
        this.z1 = 0;
        this.x2 = 0;
        this.y2 = 0;
        this.z2 = 0;
        this.worldName = "world";
    }

    public DefinedClaim(Factions plugin, Faction owner, BLocatable cornerA, BLocatable cornerB) {
        Preconditions.checkArgument(cornerA.getWorldName().equals(cornerB.getWorldName()), "Corner worlds do not match");

        this.plugin = plugin;

        this.uniqueId = UUID.randomUUID();
        this.ownerId = owner.getUniqueId();

        this.x1 = cornerA.getX();
        this.y1 = cornerA.getY();
        this.z1 = cornerA.getZ();

        this.x2 = cornerB.getX();
        this.y2 = cornerB.getY();
        this.z2 = cornerB.getZ();

        this.worldName = cornerA.getWorldName();
    }

    public BLocatable getCorner(int cornerId) {
        if (cornerId < 1 || cornerId > 5) {
            throw new ArrayIndexOutOfBoundsException("Corner out of bounds - Must be 1-4");
        }

        if (cornerId == 1) {
            return new BLocatable(worldName, x1, 64.0, z1);
        }

        if (cornerId == 2) {
            return new BLocatable(worldName, x2, 64.0, z1);
        }

        if (cornerId == 3) {
            return new BLocatable(worldName, x1, 64.0, z2);
        }

        if (cornerId == 4) {
            return new BLocatable(worldName, x2, 64.0, z2);
        }

        return null;
    }

    public BLocatable[] getCorners() {
        final BLocatable[] arr = new BLocatable[4];

        arr[0] = getCorner(1);
        arr[1] = getCorner(2);
        arr[2] = getCorner(3);
        arr[3] = getCorner(4);

        return arr;
    }

    public int[] getLxW() {
        final int[] result = new int[2];

        final double xMin = Math.min(x1, x2);
        final double zMin = Math.min(z1, z2);
        final double xMax = Math.max(x1, x2);
        final double zMax = Math.max(z1, z2);

        result[0] = (int)Math.round(Math.abs(xMax - xMin));
        result[1] = (int)Math.round(Math.abs(zMax - zMin));

        return result;
    }

    public double getValue() {
        final int[] lxw = getLxW();
        return lxw[0] * lxw[1] * plugin.getFactionConfig().getClaimBlockValue();
    }

    public boolean overlaps(double x1, double z1, double x2, double z2, String world) {
        if (!this.worldName.equals(world)) {
            return false;
        }

        final double[] values = new double[2];

        final double xMin = Math.min(this.x1, this.x2);
        final double zMin = Math.min(this.z1, this.z2);
        final double xMax = Math.max(this.x1, this.x2);
        final double zMax = Math.max(this.z1, this.z2);

        values[0] = x1;
        values[1] = x2;
        Arrays.sort(values);

        if (xMin > values[1] || xMax < values[0]) {
            return false;
        }

        values[0] = z1;
        values[1] = z2;
        Arrays.sort(values);

        if (zMin > values[1] || zMax < values[0]) {
            return false;
        }

        return true;
    }

    public boolean inside(Locatable location) {
        if (!location.getWorldName().equals(worldName)) {
            return false;
        }

        final double xMin = Math.min(x1, x2);
        final double yMin = Math.min(y1, y2);
        final double zMin = Math.min(z1, z2);
        final double xMax = Math.max(x1, x2);
        final double yMax = Math.max(y1, y2);
        final double zMax = Math.max(z1, z2);

        return
                location.getX() >= xMin && location.getX() <= xMax &&
                location.getY() >= yMin && location.getY() <= yMax &&
                location.getZ() >= zMin && location.getZ() <= zMax;
    }

    public boolean buffer(Locatable location, double buffer) {
        if (!location.getWorldName().equals(worldName)) {
            return false;
        }

        // Add X
        if (inside(new BLocatable(location.getWorldName(), (location.getX() + buffer), location.getY(), location.getZ()))) {
            return true;
        }

        // Add Z
        if (inside(new BLocatable(location.getWorldName(), location.getX(), location.getY(), (location.getZ() + buffer)))) {
            return true;
        }

        // Subtract X
        if (inside(new BLocatable(location.getWorldName(), (location.getX() - buffer), location.getY(), location.getZ()))) {
            return true;
        }

        // Subtract Z
        if (inside(new BLocatable(location.getWorldName(), location.getX(), location.getY(), (location.getZ() - buffer)))) {
            return true;
        }

        // Add X, Z
        if (inside(new BLocatable(location.getWorldName(), (location.getX() + buffer), location.getY(), (location.getZ() + buffer)))) {
            return true;
        }

        // Subtract X, Z
        if (inside(new BLocatable(location.getWorldName(), (location.getX() - buffer), location.getY(), (location.getZ() - buffer)))) {
            return true;
        }

        // Add X, Subtract Z
        if (inside(new BLocatable(location.getWorldName(), (location.getX() + buffer), location.getY(), (location.getZ() - buffer)))) {
            return true;
        }

        // Subtract X, Add Z
        if (inside(new BLocatable(location.getWorldName(), (location.getX() - buffer), location.getY(), (location.getZ() + buffer)))) {
            return true;
        }

        return false;
    }

    public ImmutableList<BLocatable> getPerimeter(int y) {
        final List<BLocatable> locations = Lists.newArrayList();

        final double xMin = Math.min(x1, x2);
        final double zMin = Math.min(z1, z2);
        final double xMax = Math.max(x1, x2);
        final double zMax = Math.max(z1, z2);

        for (int x = (int)xMin; x <= (int)xMax; x++) {
            for (int z = (int)zMin; z <= (int)zMax; z++) {
                if (x == xMin || x == xMax || z == zMin || z == zMax) {
                    locations.add(new BLocatable(worldName, x, y, z));
                }
            }
        }

        return ImmutableList.copyOf(locations);
    }

    @Override
    public DefinedClaim fromDocument(Document document) {
        this.uniqueId = (UUID)document.get("id");
        this.ownerId = (UUID)document.get("owner");

        this.x1 = document.getDouble("x1");
        this.y1 = document.getDouble("y1");
        this.z1 = document.getDouble("z1");

        this.x2 = document.getDouble("x2");
        this.y2 = document.getDouble("y2");
        this.z2 = document.getDouble("z2");

        this.worldName = document.getString("world");

        return this;
    }

    public boolean touching(Locatable location) {
        if (!location.getWorldName().equals(worldName)) {
            return false;
        }

        final BLocatable converted = new BLocatable(location.getWorldName(), location.getX(), location.getY(), location.getZ());
        final Block block = converted.getBukkit();

        for (BlockFace face : BlockFace.values()) {
            if (face.equals(BlockFace.NORTH) || face.equals(BlockFace.EAST) || face.equals(BlockFace.SOUTH) || face.equals(BlockFace.WEST)) {
                final Block relative = block.getRelative(face);

                if (inside(new BLocatable(relative))) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("id", uniqueId)
                .append("owner", ownerId)
                .append("x1", x1)
                .append("y1", y1)
                .append("z1", z1)
                .append("x2", x2)
                .append("y2", y2)
                .append("z2", z2)
                .append("world", worldName);
    }
}
