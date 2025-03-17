package me.comfortable_andy.thathurts.collision;

import me.comfortable_andy.thathurts.ThatHurtsMain;
import me.comfortable_andy.thathurts.utils.ConvexPolygon;
import me.comfortable_andy.thathurts.utils.OrientedBox;
import me.comfortable_andy.thathurts.utils.OrientedCollider;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Math.toRadians;
import static me.comfortable_andy.thathurts.utils.PositionUtil.*;

public class CollisionManager {

    static CollisionDebugData DEBUG_DATA = null;
    public static Set<BodyPart> PARTS = new HashSet<>() {{
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
            if (damager instanceof Player player) { // we guess where the player might've hit
                Location camera = player.getEyeLocation();
                if (debugModeOn) updateDebug(player, damagedBox, camera);
                if (hit == null) {
                    OrientedCollider.Side ray = new OrientedCollider.Side(
                            convertJoml(camera),
                            convertJoml(camera.clone().add(traceDir))
                    );
                    hit = findClosest(damagedBox, ray);
                    damager.sendActionBar(Component.text("Adjusted!"));
                }
            }
            if (hit == null) return null;
        }

        if (debugModeOn) {
            damagedBox.display(world, Particle.VILLAGER_HAPPY);
            quickParticle(Particle.FLAME, convertBukkit(hit).toLocation(world));
        }

        hit.sub(convertJoml(damaged.getLocation().toVector()));

        if (debugModeOn) {
            final Vector3f leftwards = new Vector3f().set(damagedBox.getAxes().x());
            new OrientedCollider.Side(
                    convertJoml(damaged.getEyeLocation().toVector()),
                    convertJoml(damaged.getEyeLocation().toVector()).add(leftwards)
            ).display(world, Particle.END_ROD);
        }

        final double halfXWidth = damaged.getBoundingBox().getWidthX() / 2;
        final double halfZWidth = damaged.getBoundingBox().getWidthZ() / 2;
        final double height = damaged.getHeight();
        final double yLevel = Math.min(1, hit.y / height);
        final double xLevel = Math.min(1, Math.abs(damagedBox.getAxes().x().dot(hit)) / halfXWidth);
        final double zLevel = Math.min(1, Math.abs(damagedBox.getAxes().z().dot(hit)) / halfZWidth);

        if (debugModeOn)
            damager.sendMessage("" + new Vector3d(xLevel, yLevel, zLevel));

        for (BodyPart part : PARTS) {
            if (!part.getXHalfRange().contains(xLevel)) continue;
            if (!part.getYRange().contains(yLevel)) continue;
            if (!part.getZHalfRange().contains(zLevel)) continue;
            return part;
        }

        return null;
    }

    private static void updateDebug(Player player, OrientedBox damagedBox, Location camera) {
        final Quaternionf quat = new Quaternionf().rotateXYZ(
                (float) toRadians(player.getPitch()),
                (float) toRadians(player.getYaw()),
                0
        ).invert();
        OrientedCollider.Axes axes = OrientedCollider.Axes.identity().rotated(quat);

        List<Vector2f> list = damagedBox.getRelativeVertices().stream()
                .map(v -> new Vector3f().set(v.pos()).add(damagedBox.getCenter()).sub(camera.toVector().toVector3f()))
                .map(v3 -> new Vector2f(-axes.x().dot(v3), axes.y().dot(v3)))
                .toList();

        ConvexPolygon polygon = new ConvexPolygon(list);
        Vector2f origin = new Vector2f();
        Vector2f shift = polygon.isPointIn(origin) ? origin : polygon.findClosestPoint(origin);

        DEBUG_DATA = new CollisionDebugData(polygon, shift);
    }

    private static Vector3f findClosest(OrientedBox damagedBox, OrientedCollider.Side ray) {
        Vector3f[] closest = null;
        float closeDist = 0;
        for (OrientedCollider.Side side : damagedBox.computeSides()) {
            Vector3f[] pts = side.closestPointTo(ray, true, false);
            float curDist = pts[0].distanceSquared(pts[1]);
            if (closest == null || curDist < closeDist) {
                closest = pts;
                closeDist = curDist;
            }
        }
        return closest == null ? null : closest[0];
    }

    record CollisionDebugData(ConvexPolygon polygon, Vector2f closest) {
    }

}
