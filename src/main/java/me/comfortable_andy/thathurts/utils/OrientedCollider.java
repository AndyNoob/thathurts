package me.comfortable_andy.thathurts.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.FloatRange;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.text.DecimalFormat;
import java.util.*;

import static me.comfortable_andy.thathurts.utils.PositionUtil.*;
import static me.comfortable_andy.thathurts.utils.PositionUtil.convertJoml;

@Getter
@RequiredArgsConstructor
public abstract class OrientedCollider {

    protected final Vector3f center;
    protected final List<Vertex> relativeVertices;
    protected final Axes axes = Axes.identity();

    protected Collection<Side> computeSides() {
        final Set<Side> sides = new HashSet<>();
        for (Vertex vertex : this.relativeVertices) {
            for (Vertex connected : vertex.connected) {
                sides.add(new Side(vertex, connected).offset(this.center));
            }
        }
        return sides;
    }

    public void rotateBy(float xDeg, float yDeg, float zDeg) {
        this.rotateBy(new Quaternionf().rotationXYZ((float) Math.toRadians(xDeg), (float) Math.toRadians(yDeg), (float) Math.toRadians(zDeg)));
    }

    public void rotateBy(@NotNull Quaternionf rotation) {
        this.axes.rotated(rotation);
        this.relativeVertices.replaceAll(vertex -> vertex.rotated(rotation));
    }

    @Nullable
    public Vector trace(@NotNull Vector ori, @NotNull Vector dir) {
        if (!dir.isNormalized()) dir = dir.normalize();

        Vector3f rayOrigin = convertJoml(ori);
        Vector3f normalizedDir = convertJoml(dir);

        // source: https://github.com/opengl-tutorials/ogl/blob/316cccc5f76f47f09c16089d98be284b689e057d/misc05_picking/misc05_picking_custom.cpp#L83C13-L83C13

        // from origin to center
        final Vector3f relativeOrigin = new Vector3f(rayOrigin).sub(this.center);

        final Vector3f[] makeArr = axes.makeArr();
        float smallestFarHit = Float.POSITIVE_INFINITY;
        float greatestCloseHit = Float.NEGATIVE_INFINITY;

        for (Vector3f axis : makeArr) {
            final float origin = axis.dot(relativeOrigin);
            // will be +/-, depending on if the ray is facing
            // the positive end of the axis or not
            final float dirOrientation = axis.dot(normalizedDir);

            final float[] projection = this.projectMinMax(axis);

            final float minPlanePos = projection[0];
            final float maxPlanePos = projection[1];
            final boolean isPointingBetween = NumberUtil.lenientZero(dirOrientation);
            final boolean isOriginBetween = new FloatRange(minPlanePos, maxPlanePos).containsFloat(origin);

            if (isPointingBetween) {
                /*

                              +-----------+  (max plane)
                             /|          /|
                            +-----------+ |
                            | |         | |
                            |-+---/-----|-+
                            |    /      |/
              (min plane)   +---/-------+
                               /              <----+(ray is outside & pointing in)
                              /
                             +

                 */
                if (!isOriginBetween) return null;
            } else {
                /*

                               (corner)    (corner)
                                  |           |
                    -             v     0     v            +
                    <---------*---|-----+-)---|------------>
                              ^           ^
                              |       (ray dir)
                          (ray ori)

                 */
                final float relMinPlane = minPlanePos - origin;
                final float relMaxPlane = maxPlanePos - origin;

                /*
                    these 2 values indicate points along the ray,
                    and there needs to be "aligned":

                    1. effectively flip the number line, if the ray is pointing
                       towards negative.
                    2. in the most ideal case, the dirOrientation is 1, meaning the ray is
                       pointing into the plane directly. but as the orientation deviates
                       to the side, the dirOrientation grows smaller towards zero. by
                       dividing, a ray pointing away will also "move", or enlarge the plane
                       values in respect to the ray orientation.
                 */
                final float alignedMinPlane = relMinPlane / dirOrientation;
                final float alignedMaxPlane = relMaxPlane / dirOrientation;

                final float smallerPlane = Math.min(alignedMinPlane, alignedMaxPlane);
                final float biggerPlane = Math.max(alignedMinPlane, alignedMaxPlane);

                smallestFarHit = Math.min(biggerPlane, smallestFarHit);
                greatestCloseHit = Math.max(smallerPlane, greatestCloseHit);

                System.out.println();
                System.out.println("smallerPlane = " + smallerPlane);
                System.out.println("biggerPlane = " + biggerPlane);
                System.out.println("greatestCloseHit = " + greatestCloseHit);
                System.out.println("smallestFarHit = " + smallestFarHit);

                if (greatestCloseHit > smallestFarHit) return null;
            }
        }

        return convertBukkit(rayOrigin.add(normalizedDir.mul(greatestCloseHit, new Vector3f()), new Vector3f()));
    }

    private float[/* 2 */] projectMinMax(Vector3f axis) {
        return this.projectMinMax(axis, 0);
    }

    private float[/* 2 */] projectMinMax(Vector3f axis, float offset) {
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;

        for (Vertex vert : this.relativeVertices) {
            final Vector3f vertex = vert.pos;
            final float dot = axis.dot(vertex);
            min = Math.min(min, dot);
            max = Math.max(max, dot);
        }

        return new float[]{min + offset, max + offset};
    }

    public @Nullable Vector3f getMinimumTranslate(@NotNull OrientedCollider other) {
        float smallestOverlap = Float.POSITIVE_INFINITY;
        Vector3f smallestAxis = null;

        final List<Vector3f> list = new ArrayList<>();
        list.addAll(Arrays.asList(this.axes.makeArr()));
        list.addAll(Arrays.asList(other.axes.makeArr()));

        for (Object axisObj : list) {
            if (!(axisObj instanceof Vector3f axis)) continue;

            final float thisPos = axis.dot(this.center);
            final float otherPos = axis.dot(other.center);

            final float[] thisProjectionMinMax = this.projectMinMax(axis, thisPos);
            final float[] otherProjectionMinMax = other.projectMinMax(axis, otherPos);

            final FloatRange thisRange = new FloatRange(
                    thisProjectionMinMax[0], thisProjectionMinMax[1]
            );
            final FloatRange otherRange = new FloatRange(
                    otherProjectionMinMax[0], otherProjectionMinMax[1]
            );

            if (!thisRange.overlapsRange(otherRange)) return null;

            /*
                overlap is confirmed, goal now is to
                find the smallest axis of separation
                (the axis with the smallest overlap)
            */

            final float minPlane = Math.max(
                    thisProjectionMinMax[0], otherProjectionMinMax[0]
            );
            final float maxPlane = Math.min(
                    thisProjectionMinMax[1], otherProjectionMinMax[1]
            );

            final float overlap = maxPlane - minPlane;

            if (overlap < smallestOverlap) {
                smallestOverlap = overlap;
                smallestAxis = axis;
            }
        }

        return smallestAxis;
    }

    public record Axes(Vector3f x, Vector3f y, Vector3f z) {
        public static Axes identity() {
            return new Axes(new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1));
        }

        @SuppressWarnings("UnusedReturnValue")
        public Axes rotated(Quaternionf rotation) {
            this.x.rotate(rotation);
            this.y.rotate(rotation);
            this.z.rotate(rotation);
            return this;
        }

        public Vector3f[] makeArr() {
            return new Vector3f[]{
                    this.x(),
                    this.y(),
                    this.z()
            };
        }

        public Vector3f x() {
            return new Vector3f(this.x);
        }

        public Vector3f y() {
            return new Vector3f(this.y);
        }

        public Vector3f z() {
            return new Vector3f(this.z);
        }

        public void display(World world, Particle particle, Vector3f center) {
            for (Vector3f vector3f : this.makeArr()) {
                new Side(new Vertex(new Vector3f(center)), new Vertex(new Vector3f(center).add(vector3f))).display(world, particle);
            }
        }

        @Override
        public String toString() {
            final DecimalFormat format = new DecimalFormat("#.##");
            return "axes " +
                    "x=" + x.toString(format) +
                    ", y=" + y.toString(format) +
                    ", z=" + z.toString(format);
        }
    }

    public record Vertex(Vector3f pos, List<Vertex> connected) {
        public Vertex(Vector3f pos) {
            this(pos, new ArrayList<>(5));
        }

        public Vertex connect(Vertex vertex) {
            this.connected.add(vertex);
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Vertex other)) return false;
            return other.pos.equals(this.pos, (float) Vector.getEpsilon());
        }

        public Vertex rotated(Quaternionf rotation) {
            this.pos.rotate(rotation);
            return this;
        }
    }

    public record Side(Vertex a, Vertex b) {

        public void display(World world, Particle particle) {
            final Vector3f current = new Vector3f(this.a.pos);
            final Vector3f next = new Vector3f(this.b.pos);
            for (int j = 0; j < 10; j++) {
                final float progress = ((float) j) / 10;
                world.spawnParticle(particle, bukkitLoc(convertBukkit(current.lerp(next, progress, new Vector3f())), world), 1, 0, 0, 0, 0);
            }
        }

        public Side offset(Vector3f offset) {
            return new Side(new Vertex(new Vector3f(this.a.pos).add(offset)),
                    new Vertex(new Vector3f(this.b.pos).add(offset)));
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Side other)) return false;
            return (other.a.equals(this.a) && other.b.equals(this.b)) || (other.a.equals(this.b) && other.b.equals(this.a));
        }
    }

}
