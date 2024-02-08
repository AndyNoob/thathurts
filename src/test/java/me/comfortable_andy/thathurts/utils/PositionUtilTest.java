package me.comfortable_andy.thathurts.utils;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PositionUtilTest {

    @Test
    public void testClosestPoint() {
        Vector start = new Vector(0, 0, 0);
        Vector end = new Vector(1, 0, 0);
        Vector check = new Vector(0.5, 0, 0);

        assertEquals(PositionUtil.closestPoint(start, end, check), check);
        assertEquals(PositionUtil.closestPoint(start, end, check.clone().add(new Vector(0, 0.1, 0))), check);
    }

}