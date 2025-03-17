package me.comfortable_andy.thathurts.utils;

import org.joml.Intersectionf;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class ConvexPolygon {

    private final Vector2f[] vertices;

    public ConvexPolygon(List<Vector2f> verts) {
        verts = new ArrayList<>(verts);
        Vector2f ref = verts.stream().min(Comparator.comparing(Vector2f::y).thenComparing(vector2f -> -vector2f.x())).orElseThrow();
        verts.sort(Comparator.comparing((Vector2f v) -> {
            Vector2f toV = v.sub(ref, new Vector2f());
            return toV.x / toV.length();
        }).thenComparing(v -> {
            Vector2f toV = v.sub(ref, new Vector2f());
            return toV.length();
        }));
        Stack<Vector2f> stack = new Stack<>();
        for (Vector2f vert : verts) {
            while (stack.size() > 1) {
                if ((getCross(stack.get(stack.size() - 2), stack.peek(), vert) > 0))
                    stack.pop();
                else break;
            }
            stack.push(vert);
        }
        this.vertices = stack.toArray(Vector2f[]::new);
    }

    public Vector2f[][] makeSegments() {
        Vector2f[][] segments = new Vector2f[vertices.length][2];
        for (int i = 0; i < vertices.length; i++) {
            Vector2f vertex = vertices[i];
            Vector2f next = vertices[(i + 1) % vertices.length];
            segments[i] = new Vector2f[]{vertex, next};
        }
        return segments;
    }

    public Vector2f findClosestPoint(Vector2f point) {
        Vector2f closest = null;
        for (Vector2f[] segment : makeSegments()) {
            Vector2f vertex = segment[0];
            Vector2f next = segment[1];
            Vector2f dir = next.sub(vertex, new Vector2f()).normalize();
            Vector2f curToPt = point.sub(vertex, new Vector2f());
            float dot = dir.dot(curToPt);
            Vector2f close;
            if (dot <= 0) close = vertex;
            else close = (dot >= 1) ? next : vertex.add(dir.mul(dot), new Vector2f());
            if (closest == null || close.distanceSquared(point) < closest.distanceSquared(point))
                closest = close;
        }
        return closest;
    }

    public boolean isPointIn(Vector2f point) {
        return Intersectionf.intersectPolygonRay(vertices, point.x, point.y, 1, 0, new Vector2f()) != -1;
    }

    private static float getCross(Vector2f a, Vector2f b, Vector2f ref) {
        return (b.x - a.x) * (ref.y - a.y) - (b.y - a.y) * (ref.x - a.x);
    }

    public String toString() {
        return Arrays.toString(vertices);
    }

}
