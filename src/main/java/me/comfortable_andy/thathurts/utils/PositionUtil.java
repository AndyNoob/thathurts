package me.comfortable_andy.thathurts.utils;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

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
        final Vector startToEnd = end.clone().subtract(start);
        final Vector startToCheck = check.clone().subtract(start);

        final double projected = startToEnd.dot(startToCheck);
        final double lineMagnitudeSquared = start.distanceSquared(end);
        final double factor = projected / lineMagnitudeSquared;

        if (factor <= 0) return start.clone();
        else if (factor >= 1) return end.clone();
        else return lerp(start, end, factor);
    }

}
