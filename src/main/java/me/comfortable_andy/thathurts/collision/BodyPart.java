package me.comfortable_andy.thathurts.collision;

import me.comfortable_andy.thathurts.utils.PositionUtil;
import org.apache.commons.lang.math.DoubleRange;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public enum BodyPart {

    HEAD(EquipmentSlot.HEAD, new DoubleRange(0, 1), new DoubleRange(1.5, 2)),
    ARMS(EquipmentSlot.CHEST, new DoubleRange(0.23, 1), new DoubleRange(0.75, 1.5)),
    BODY(EquipmentSlot.CHEST, new DoubleRange(0, 0.23), new DoubleRange(0.75, 1.5)),
    LEGS(EquipmentSlot.LEGS, new DoubleRange(0, 1), new DoubleRange(0.3, 0.75)),
    FEET(EquipmentSlot.FEET, new DoubleRange(0, 1), new DoubleRange(0, 0.3));

    private final EquipmentSlot slot;
    private final DoubleRange xRange;
    private final DoubleRange yRange;

    /**
     * This class represents the player's body dissected in a 2d plane with the player's front facing towards the viewer, where the bottom center of the player is at the origin.
     * @param slot the equipment slot that covers this body part
     * @param xRange positive and originates at 0, distance from the y-axis, covers both the positive and negative spectrum
     * @param yRange positive and originates at 0, distance from the x-axis
     */
    BodyPart(EquipmentSlot slot, DoubleRange xRange, DoubleRange yRange) {
        this.slot = slot;
        this.yRange = yRange;
        this.xRange = xRange;
    }

    @Nullable
    public static BodyPart findPart(Player player, Entity toCheck) {
        final Vector playerDirection = player.getLocation().getDirection();
        final Vector checkDirection = toCheck.getLocation().getDirection();

        final double deltaY = toCheck.getLocation().getY() - player.getLocation().getY();
        final double deltaX = PositionUtil.closestPoint(new Vector(0, 0, 0), playerDirection, checkDirection).distance(checkDirection);

        player.sendMessage("Delta X: " + deltaX + ", Delta Y: " + deltaY);

        for (BodyPart part : values()) {
            if (part.yRange.containsDouble(deltaY) && part.xRange.containsDouble(deltaX)) return part;
        }

        return null;
    }

}
