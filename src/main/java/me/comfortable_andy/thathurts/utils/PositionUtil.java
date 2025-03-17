package me.comfortable_andy.thathurts.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@SuppressWarnings("unused")
public class PositionUtil {

    public static Vector lerp(@NotNull Vector min, @NotNull Vector max, final double factor) {
        return new Vector(NumberUtil.lerp(min.getX(), max.getX(), factor), NumberUtil.lerp(min.getY(), max.getY(), factor), NumberUtil.lerp(min.getZ(), max.getZ(), factor));
    }

    public static Location lerp(@NotNull Location min, @NotNull Location max, final double factor) {
        return lerp(min, max, factor, new Location(min.getWorld(), 0, 0, 0));
    }

    public static Location lerp(@NotNull Location min, @NotNull Location max, final double factor, @NotNull Location copyTo) {
        copyTo.setX(NumberUtil.lerp(min.getX(), max.getX(), factor));
        copyTo.setY(NumberUtil.lerp(min.getY(), max.getY(), factor));
        copyTo.setZ(NumberUtil.lerp(min.getZ(), max.getZ(), factor));
        return copyTo;
    }

    public static Vector closestPoint(Vector start, Vector end, Vector check) {
        final Vector originShiftedEnd = end.clone().subtract(start);
        final Vector originShiftedCheck = check.clone().subtract(start);

        final double currentLengthSquared = originShiftedEnd.dot(originShiftedCheck);
        final double lineLengthSquared = start.distanceSquared(end);
        final double percentageToEnd = currentLengthSquared / lineLengthSquared;

        if (percentageToEnd <= 0) return start.clone();
        else if (percentageToEnd >= 1) return end.clone();
        else return lerp(start, end, percentageToEnd);
    }

    public static Vector3f convertJoml(Vector vector) {
        return new Vector3f((float) vector.getX(), (float) vector.getY(), (float) vector.getZ());
    }

    public static Vector3f convertJoml(Location location) {
        return convertJoml(location.toVector());
    }

    public static Vector convertBukkit(Vector3f vector) {
        return new Vector(vector.x(), vector.y(), vector.z());
    }

    public static Location bukkitLoc(Vector vector, World world) {
        return new Location(world, vector.getX(), vector.getY(), vector.getZ());
    }

    public static boolean normalized(Vector3f v) {
        return (v.lengthSquared() - 1) < Vector.getEpsilon();
    }

}
