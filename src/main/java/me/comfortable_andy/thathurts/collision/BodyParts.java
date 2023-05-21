package me.comfortable_andy.thathurts.collision;

import org.apache.commons.lang.math.DoubleRange;
import org.bukkit.inventory.EquipmentSlot;

public enum BodyParts {

    HEAD(EquipmentSlot.HEAD, new DoubleRange(1.5, 2), new DoubleRange(-1, 1)),
    ARMS(EquipmentSlot.CHEST, new DoubleRange(0.75, 1.5), new DoubleRange(-1, -0.23), new DoubleRange(0.23, 1)),
    BODY(EquipmentSlot.CHEST, new DoubleRange(0.75, 1.5), new DoubleRange(-0.23, 0.23)),
    LEGS(EquipmentSlot.LEGS, new DoubleRange(0.3, 0.75), new DoubleRange(-1, 1)),
    FEET(EquipmentSlot.FEET, new DoubleRange(0, 0.3), new DoubleRange(-1, 1));

    private final EquipmentSlot slot;
    private final DoubleRange yRange;
    private final DoubleRange[] xRanges;

    BodyParts(EquipmentSlot slot, DoubleRange yRange, DoubleRange... xRanges) {
        this.slot = slot;
        this.yRange = yRange;
        this.xRanges = xRanges;
    }



}
