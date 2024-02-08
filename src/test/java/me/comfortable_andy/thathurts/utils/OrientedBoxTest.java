package me.comfortable_andy.thathurts.utils;

import org.bukkit.util.BoundingBox;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrientedBoxTest {

    @Test
    public void testRotation() {
        final OrientedBox box = new OrientedBox(new BoundingBox());
        System.out.println("Original: " + box.getAxes());
        box.rotateBy(new Quaternionf().rotateXYZ(0, (float) Math.toRadians(270), 0));
        System.out.println("Rotated: " + box.getAxes());
        assertEquals(new OrientedCollider.Axes(
                new Vector3f(0, 0, 1),
                new Vector3f(0, 1, 0),
                new Vector3f(-1, 0, 0)), box.getAxes());
    }

}