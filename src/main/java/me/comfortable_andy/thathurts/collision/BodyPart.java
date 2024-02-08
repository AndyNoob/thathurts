package me.comfortable_andy.thathurts.collision;

import org.apache.commons.lang.math.DoubleRange;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public enum BodyPart {

    HEAD(EquipmentSlot.HEAD, new DoubleRange(0, 2.5), new DoubleRange(1.5, 2.5)),
    ARMS(EquipmentSlot.CHEST, new DoubleRange(0.16, 2.5), new DoubleRange(0.75, 1.5)),
    BODY(EquipmentSlot.CHEST, new DoubleRange(0, 0.16), new DoubleRange(0.75, 1.5)),
    LEGS(EquipmentSlot.LEGS, new DoubleRange(0, 2.5), new DoubleRange(0.3, 0.75)),
    FEET(EquipmentSlot.FEET, new DoubleRange(0, 2.5), new DoubleRange(0, 0.3));

    private final EquipmentSlot slot;
    private final DoubleRange xRange;
    private final DoubleRange yRange;

    /**
     * This class represents the player's body dissected in a 2d plane with the player's front facing towards the viewer, where the bottom center of the player is at the origin.
     *
     * @param slot   the equipment slot that covers this body part
     * @param xRange positive and originates at 0, distance from the y-axis, covers both the positive and negative spectrum
     * @param yRange positive and originates at 0, distance from the x-axis
     */
    BodyPart(EquipmentSlot slot, DoubleRange xRange, DoubleRange yRange) {
        this.slot = slot;
        this.yRange = yRange;
        this.xRange = xRange;
    }

    @Nullable
    public static BodyPart findPart(LivingEntity damaged, LivingEntity damager) {
        final Vector damagedPosition = damaged.getLocation().toVector();
        final Vector damagerPosition = damager.getLocation().toVector();

        final Vector damagedDirection = damaged.getLocation().getDirection();
        final Vector damagerDirection = damager.getLocation().getDirection();

        final double radians = Math.toRadians(90);
        final Vector damagedRightDirection = damagedDirection.clone().rotateAroundY(radians);
        final Vector upDirection = new Vector(0, 1, 0);

        final BoundingBox box = damaged.getBoundingBox();
        final Vector boxCenter = box.getCenter();


        // now check intersection

        /*final double deltaX = ;
        final double deltaY = ;

        for (BodyPart part : values()) {
            if (part.yRange.containsDouble(deltaY) && part.xRange.containsDouble(deltaX)) return part;
        }*/

        return null;
    }

}

/*

        damaged.sendMessage("Target: " + damagerTargetPoint);
        damaged.sendMessage("Damager Dir: " + damagerDirection);
        damaged.sendMessage("Delta X: " + deltaX + ", Delta Y: " + deltaY);

        damager.sendMessage("Target: " + damagerTargetPoint);
        damager.sendMessage("Damager Dir: " + damagerDirection);
        damager.sendMessage("Delta X: " + deltaX + ", Delta Y: " + deltaY);

        damaged.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, damagerTargetPoint.toLocation(damager.getWorld()), 1, 0, 0, 0);

 */