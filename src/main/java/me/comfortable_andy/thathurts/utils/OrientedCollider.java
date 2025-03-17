package me.comfortable_andy.thathurts.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.math.FloatRange;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;

import static me.comfortable_andy.thathurts.utils.NumberUtil.clamp;
import static me.comfortable_andy.thathurts.utils.PositionUtil.*;

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

    public <C extends OrientedCollider> C rotateBy(float xDeg, float yDeg, float zDeg) {
        return this.rotateBy(new Quaternionf()
                .rotationXYZ(
                        (float) Math.toRadians(xDeg),
                        (float) Math.toRadians(yDeg),
                        (float) Math.toRadians(zDeg)
                )
                .invert()
        );
    }

    @SuppressWarnings("unchecked")
    public <C extends OrientedCollider> C rotateBy(@NotNull Quaternionf rotation) {
        this.axes.rotated(rotation);
        this.relativeVertices.replaceAll(vertex -> vertex.rotated(rotation));
        return (C) this;
    }

    @Nullable
    public Vector3f trace(@NotNull Vector3f ori, @NotNull Vector3f dir) {
        if (!PositionUtil.normalized(dir)) dir = dir.normalize();

        final Vector3f rayOrigin = new Vector3f(ori);
        final Vector3f normalizedDir = new Vector3f(dir);

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

                    derived from intersecting ray and plane:
                        0         = n•(p - p_0)
                        p(t)      = r_0 + (t*dir)
                        0         = n•(r_0 + t*dir - p_0)
                        0         = n•r_0 + n•(t*dir) - n•p_0
                        n•(t*dir) = n•p_0 - n•r_0
                        t         = (n•r_0 - n•p_0) / n•dir
                 */
                final float alignedMinPlane = relMinPlane / dirOrientation;
                final float alignedMaxPlane = relMaxPlane / dirOrientation;

                final float smallerPlane = Math.min(alignedMinPlane, alignedMaxPlane);
                final float biggerPlane = Math.max(alignedMinPlane, alignedMaxPlane);

                smallestFarHit = Math.min(biggerPlane, smallestFarHit);
                greatestCloseHit = Math.max(smallerPlane, greatestCloseHit);

                if (greatestCloseHit > smallestFarHit) return null;
            }
        }

        return rayOrigin
                .add(normalizedDir.mul(greatestCloseHit, new Vector3f()), new Vector3f());
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
        // source: https://dyn4j.org/2010/01/sat/#sat-contain

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

            float overlap = maxPlane - minPlane;

            final float minPlaneDepth = Math.abs(thisProjectionMinMax[0] - otherProjectionMinMax[0]);
            final float maxPlaneDepth = Math.abs(thisProjectionMinMax[1] - otherProjectionMinMax[1]);

            /*
                -                                     + (dir of axis)
                              {overlap}
                <----------|--[-------]-------|------->
                             ^
                             |
                      smaller depth
                there's two wrong things here:
                1. the overlap movement direction is completely wrong
                2. on top of that the depth is sending it the other way even more

                the first issue situation is also a problem here
                where containment isn't an issue
                -                                     + (dir of axis)
                            { overlap }
                <-------[---|---------]-------|------->
                          ^                ^
                          |                |
                     min depth             |
                                       max depth
             */
            if (thisRange.containsRange(otherRange) || otherRange.containsRange(thisRange))
                overlap += Math.min(minPlaneDepth, maxPlaneDepth);

            if (overlap < smallestOverlap) {
                smallestOverlap = overlap;
                smallestAxis = axis;
                if (minPlaneDepth < maxPlaneDepth) {
                    smallestAxis.negate();
                }
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

        public void display(World world, Vector3f center) {
            final Iterator<Color> colors = Arrays.asList(Color.RED, Color.GREEN, Color.BLUE).iterator();
            for (Vector3f vector3f : this.makeArr()) {
                final Color col = colors.next();
                new Side(
                        new Vertex(new Vector3f(center)),
                        new Vertex(new Vector3f(center).add(vector3f))
                ).display(world, loc -> loc.getWorld()
                        .spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(col, 1)
                        )
                );
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

        public Side(Vector3f a, Vector3f b) {
            this(new Vertex(a), new Vertex(b));
        }

        public void display(World world, Particle particle) {
            display(world, loc -> quickParticle(particle, loc));
        }

        public void display(World world, Consumer<Location> spawnParticle) {
            final Vector3f current = new Vector3f(this.a.pos);
            final Vector3f next = new Vector3f(this.b.pos);
            for (int j = 0; j < 10; j++) {
                final float progress = ((float) j) / 10;
                spawnParticle.accept(bukkitLoc(convertBukkit(current.lerp(next, progress, new Vector3f())), world));
            }
        }

        public Side offset(Vector3f offset) {
            return new Side(
                    new Vertex(new Vector3f(this.a.pos).add(offset)),
                    new Vertex(new Vector3f(this.b.pos).add(offset))
            );
        }

        public Vector3f direction(boolean normalize) {
            Vector3f dir = b.pos.sub(a.pos, new Vector3f());
            return normalize ? dir.normalize() : dir;
        }

        public float length() {
            return a.pos.distance(b.pos);
        }

        public Vector3f[] closestPointTo(Side other, boolean clampThis, boolean clampOther) {
            float len = length();
            Vector3f dir = direction(false).div(len);
            float lenOther = other.length();
            Vector3f dirOther = other.direction(false).div(lenOther);
            Vector3f dirShortest = dir.cross(dirOther, new Vector3f());
            Vector3f planeA = dir.cross(dirShortest, new Vector3f()).normalize();
            Vector3f planeB = dirOther.cross(dirShortest, new Vector3f()).normalize();
            /*
            n • (p - p0) = 0
            p(t) = d * t + d0

            n • (d * t + d0 - p0) = 0
            n • d * t + n • (d0 - p0) = 0
            n • d * t = -n • (d0 - p0)
            t = (-n • (d0 - p0)) / (n • d)
            t = (p0 • n - d0 • n) / (n • d)
             */
            float tA = (other.a.pos.dot(planeB) - a.pos.dot(planeB)) / (planeB.dot(dir));
            float tB = (a.pos.dot(planeA) - other.a.pos.dot(planeA)) / (planeA.dot(dirOther));
            return new Vector3f[]{
                    a.pos.add(dir.mul(clampThis ? (clamp(0, tA, len)) : tA), new Vector3f()),
                    other.a.pos.add(dirOther.mul(clampOther ? (clamp(0, tB, lenOther)) : tB), new Vector3f())
            };
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Side other)) return false;
            return (other.a.equals(this.a) && other.b.equals(this.b)) || (other.a.equals(this.b) && other.b.equals(this.a));
        }
    }

}
