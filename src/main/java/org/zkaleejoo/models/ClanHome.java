package org.zkaleejoo.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class ClanHome {

    private final String name;
    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final long createdAt;

    public ClanHome(String name, String worldName, double x, double y, double z, float yaw, float pitch) {
        this(name, worldName, x, y, z, yaw, pitch, System.currentTimeMillis());
    }

    public ClanHome(String name, String worldName, double x, double y, double z, float yaw, float pitch, long createdAt) {
        this.name = Objects.requireNonNull(name, "Home name cannot be null").trim();
        this.worldName = Objects.requireNonNull(worldName, "World name cannot be null");
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.createdAt = createdAt;
    }

    public static ClanHome fromLocation(String name, Location location) {
        Objects.requireNonNull(location, "Location cannot be null");
        String worldName = location.getWorld() != null ? location.getWorld().getName() : "world";
        return new ClanHome(
                name,
                worldName,
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch(),
                System.currentTimeMillis()
        );
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    public String getFormattedCoordinates() {
        return String.format(java.util.Locale.US, "%.1f, %.1f, %.1f", x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClanHome clanHome = (ClanHome) o;
        return name.equalsIgnoreCase(clanHome.name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return "ClanHome{" +
                "name='" + name + '\'' +
                ", world='" + worldName + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
