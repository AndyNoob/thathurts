package me.comfortable_andy.thathurts.collision;

import me.comfortable_andy.thathurts.ThatHurtsMain;
import me.comfortable_andy.thathurts.utils.ConvexPolygon;
import me.comfortable_andy.thathurts.utils.OrientedBox;
import me.comfortable_andy.thathurts.utils.OrientedCollider;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Math.toRadians;
import static me.comfortable_andy.thathurts.utils.PositionUtil.convertBukkit;
import static me.comfortable_andy.thathurts.utils.PositionUtil.convertJoml;

public class CollisionManager {

    static CollisionDebugData DEBUG_DATA = null;
    public static Set<BodyPart> PARTS = new HashSet<>(){{
        addAll(List.of(HumanoidBodyPart.values()));
    }};

    public static BodyPart findPart(LivingEntity damaged, LivingEntity damager) {
        final OrientedBox damagedBox = new OrientedBox(damaged.getBoundingBox())
                .rotateBy(
                        0,
                        damaged.getLocation().getYaw(),
                        0
                );

        Vector traceDir = damager.getLocation().getDirection();
        Vector3f hit = damagedBox.trace(
                convertJoml(damager.getEyeLocation().toVector()),
                convertJoml(traceDir)
        );
        World world = damaged.getWorld();
        boolean debugModeOn = ThatHurtsMain.getInstance().isDebugModeOn();

        if (hit == null || debugModeOn) {
            boolean adjusted = false;
            if (damager instanceof Player player) { // we guess where the player might've hit
                final Quaternionf quat = new Quaternionf().rotateXYZ(
                        (float) toRadians(player.getPitch()),
                        (float) toRadians(player.getYaw()),
                        0
                ).invert();
                OrientedCollider.Axes axes = OrientedCollider.Axes.identity().rotated(quat);
                Location camera = player.getEyeLocation();

                List<Vector2f> list = damagedBox.getRelativeVertices().stream()
                        .map(v -> new Vector3f().set(v.pos())
                                .add(damagedBox.getCenter()).sub(camera.toVector().toVector3f())
                        )
                        .map(v3 -> {
                            Vector2f f = new Vector2f(
                                    -axes.x().dot(v3),
                                    axes.y().dot(v3)
                            );
                            return f.mul(10);
                        })
                        .toList();

                ConvexPolygon polygon = new ConvexPolygon(list);
                Vector2f origin = new Vector2f();
                Vector2f closest = polygon.isPointIn(origin) ? origin : polygon.findClosestPoint(origin).div(10);

                Location newOrigin = camera
                        .add(convertBukkit(axes.x().mul(-closest.x, new Vector3f())))
                        .add(convertBukkit(axes.y().mul(closest.y, new Vector3f())));

                if (debugModeOn) {
                    axes.display(world, camera.toVector().toVector3f());
                    world.spawnParticle(Particle.FLAME, newOrigin, 1, 0, 0, 0, 0);
                    DEBUG_DATA = new CollisionDebugData(polygon, closest);
                }
                if (hit == null) {
                    if (debugModeOn)
                        damager.sendMessage("Attempt adjustment " + closest
                                .mul(-1, 1, new Vector2f()));
                    hit = damagedBox.trace(
                            convertJoml(newOrigin.toVector()),
                            convertJoml(traceDir)
                    );
                    adjusted = true;
                }
            }
            if (hit == null) return null;
            if (debugModeOn && adjusted) damager.sendMessage("Hit adjustment");
        }

        if (debugModeOn) {
            damagedBox.display(world, Particle.VILLAGER_HAPPY);
            world.spawnParticle(
                    Particle.FLAME,
                    convertBukkit(hit).toLocation(world),
                    1,
                    0,
                    0,
                    0,
                    0
            );
        }

        hit.sub(convertJoml(damaged.getLocation().toVector()));

        final Vector3f leftwards = convertJoml(BlockFace.EAST.getDirection())
                .rotateY((float) toRadians(-damaged.getLocation().getYaw()));

        if (debugModeOn)
            new OrientedCollider.Side(convertJoml(damaged.getEyeLocation().toVector()), convertJoml(damaged.getEyeLocation().toVector()).add(leftwards)).display(world, Particle.END_ROD);

        final double halfWidth = damaged.getWidth() / 2;
        final double height = damaged.getHeight();
        final double yLevel = Math.min(1, hit.y / height);
        final double xLevel = Math.max(-1, Math.min(1, leftwards.dot(hit) / halfWidth));

        for (BodyPart part : PARTS) {
            if (!part.getXHalfRange().contains(Math.abs(xLevel))) continue;
            if (!part.getYRange().contains(yLevel)) continue;
            return part;
        }

        return null;
    }

    record CollisionDebugData(ConvexPolygon polygon, Vector2f closest) {
    }

}
