package me.comfortable_andy.thathurts.collision;

import lombok.Getter;
import org.apache.commons.lang3.Range;
import org.bukkit.inventory.EquipmentSlot;

@Getter
public enum HumanoidBodyPart implements BodyPart {

    HEAD(EquipmentSlot.HEAD, Range.between(0.0, 1.0), Range.between(0.8, 1.0), 1.5),
    ARMS(EquipmentSlot.CHEST, Range.between(0.75, 1.0), Range.between(0.5, 0.8), 0.75),
    BODY(EquipmentSlot.CHEST, Range.between(0.0, 0.75), Range.between(0.5, 0.8), 1.0),
    LEGS(EquipmentSlot.LEGS, Range.between(0.0, 1.0), Range.between(0.25, 0.5), 0.8),
    FEET(EquipmentSlot.FEET, Range.between(0.0, 1.0), Range.between(0.0, 0.25), 0.65);

    private final EquipmentSlot slot;
    private final Range<Double> xHalfRange;
    private final Range<Double> yRange;
    private final double multiplier;

    /**
     * This class represents the player's body dissected in a 2d plane with the player's front facing towards the viewer, where the bottom center of the player is at the origin.
     *
     * @param slot       the equipment slot that covers this body part
     * @param xHalfRange positive and originates at 0 going left and right, [0, 1] relative to the half size of the width of the entity's bounding box
     * @param yRange positive and originates at 0 going up, [0, 1] relative to the height of the entity
     * @param multiplier damage multiplier
     */
    HumanoidBodyPart(EquipmentSlot slot, Range<Double> xHalfRange, Range<Double> yRange, double multiplier) {
        this.slot = slot;
        this.yRange = yRange;
        this.xHalfRange = xHalfRange;
        this.multiplier = multiplier;
    }

}