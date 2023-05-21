package me.comfortable_andy.thathurts.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NumberUtil {

    public static double clamp(double min, double value, double max) {
        return Math.min(max, Math.max(min, value));
    }

    public static double greaterThanZero(double value) {
        return Math.max(0, value);
    }

    public static long clamp(long min, long value, long max) {
        return Math.min(max, Math.max(min, value));
    }

    public static int clamp(int min, int value, int max) {
        return Math.min(max, Math.max(min, value));
    }

    public static long greaterThanZero(long value) {
        return Math.max(0, value);
    }

    public static long msToTicks(long ms) {
        return ((Number) Math.ceil(ms / 50d)).longValue();
    }

    public static double lerp(double min, double max, double factor) {
        return min + factor * (max - min);
    }

    @Nullable
    public static Integer intOrNull(@NotNull final String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return null;
        }
    }

}
