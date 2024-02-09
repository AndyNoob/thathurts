package me.comfortable_andy.thathurts.collision;

import lombok.Getter;
import me.comfortable_andy.thathurts.utils.OrientedBox;
import me.comfortable_andy.thathurts.utils.OrientedCollider;
import org.apache.commons.lang.math.FloatRange;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static me.comfortable_andy.thathurts.utils.PositionUtil.convertBukkit;
import static me.comfortable_andy.thathurts.utils.PositionUtil.convertJoml;

@Getter
public enum BodyPart {

    HEAD(EquipmentSlot.HEAD, new FloatRange(0, 1), new FloatRange(0.8, 1), 1.5),
    ARMS(EquipmentSlot.CHEST, new FloatRange(0.75, 1), new FloatRange(0.5, 0.8), 0.75),
    BODY(EquipmentSlot.CHEST, new FloatRange(0, 0.75), new FloatRange(0.5, 0.8), 1.0),
    LEGS(EquipmentSlot.LEGS, new FloatRange(0, 1), new FloatRange(0.25, 0.5), 0.8),
    FEET(EquipmentSlot.FEET, new FloatRange(0, 1), new FloatRange(0, 0.25), 0.65);

    private final EquipmentSlot slot;
    private final FloatRange xRange;
    private final FloatRange yRange;
    private final double multiplier;

    /**
     * This class represents the player's body dissected in a 2d plane with the player's front facing towards the viewer, where the bottom center of the player is at the origin.
     *
     * @param slot       the equipment slot that covers this body part
     * @param xRange     positive and originates at 0, distance from the y-axis, covers both the positive and negative spectrum
     * @param yRange     positive and originates at 0, distance from the x-axis
     * @param multiplier damage multiplier
     */
    BodyPart(EquipmentSlot slot, FloatRange xRange, FloatRange yRange, double multiplier) {
        this.slot = slot;
        this.yRange = yRange;
        this.xRange = xRange;
        this.multiplier = multiplier;
    }

    @Nullable
    public static BodyPart findPart(LivingEntity damaged, LivingEntity damager) {
        final OrientedBox damagedBox = new OrientedBox(damaged.getBoundingBox().expand(0.1)).rotateBy(0, damaged.getLocation().getYaw(), 0);

        final Vector3f hit = damagedBox.trace(
                convertJoml(damager.getEyeLocation().toVector()),
                convertJoml(damager.getLocation().getDirection())
        );
        if (hit == null)
            return null;

        damagedBox.display(damaged.getWorld(), Particle.VILLAGER_HAPPY);
        damaged.getWorld().spawnParticle(Particle.FLAME, convertBukkit(hit).toLocation(damaged.getWorld()), 1, 0, 0, 0, 0);

        hit.sub(convertJoml(damaged.getLocation().toVector()));

        final Vector3f leftwards = convertJoml(BlockFace.EAST.getDirection())
                .rotateY((float) Math.toRadians(-damaged.getLocation().getYaw()));

        new OrientedCollider.Side(convertJoml(damaged.getEyeLocation().toVector()), convertJoml(damaged.getEyeLocation().toVector()).add(leftwards)).display(damaged.getWorld(), Particle.END_ROD);

        final double halfWidth = damaged.getWidth() / 2;
        final double height = damaged.getHeight();
        final double yLevel = Math.min(1, hit.y / height);
        final double xLevel = Math.max(-1, Math.min(1, leftwards.dot(hit) / halfWidth));

        for (BodyPart part : BodyPart.values()) {
            if (!part.xRange.containsDouble(Math.abs(xLevel))) continue;
            if (!part.yRange.containsDouble(yLevel)) continue;
            return part;
        }

        return null;
    }

}