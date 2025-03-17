package me.comfortable_andy.thathurts.collision;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.awt.*;

public class CollisionDebugMapRenderer extends MapRenderer {

    public CollisionDebugMapRenderer() {
        super(false);
    }

    @Override
    public void render(@NotNull MapView mapView, @NotNull MapCanvas mapCanvas, @NotNull Player player) {
        CollisionManager.CollisionDebugData data = CollisionManager.DEBUG_DATA;
        if (data == null) return;
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                mapCanvas.setPixelColor(x, y, null);
            }
        }
        // draw polygon
        for (Vector2f[] segment : data.polygon().makeSegments()) {
            Vector2f a = segment[0].mul(5, new Vector2f()).add(64, 64, new Vector2f());
            Vector2f b = segment[1].mul(5, new Vector2f()).add(64, 64, new Vector2f());
            drawLine(mapCanvas, a, b);
        }
        // draw point
        Vector2f pt = data.closest().mul(5, new Vector2f()).add(64, 64, new Vector2f());
        mapCanvas.setPixelColor((int) pt.x, (int) pt.y, Color.RED);
        mapCanvas.setPixelColor((int) pt.x + 1, (int) pt.y, Color.RED);
        mapCanvas.setPixelColor((int) pt.x - 1, (int) pt.y, Color.RED);
        mapCanvas.setPixelColor((int) pt.x, (int) pt.y + 1, Color.RED);
        mapCanvas.setPixelColor((int) pt.x + 1, (int) pt.y + 1, Color.RED);
        mapCanvas.setPixelColor((int) pt.x - 1, (int) pt.y + 1, Color.RED);
        mapCanvas.setPixelColor((int) pt.x, (int) pt.y - 1, Color.RED);
        mapCanvas.setPixelColor((int) pt.x + 1, (int) pt.y - 1, Color.RED);
        mapCanvas.setPixelColor((int) pt.x - 1, (int) pt.y - 1, Color.RED);
        int r = 1;
        drawCircle(mapCanvas, new Vector2f(64, 64), r);
    }

    private static void drawCircle(@NotNull MapCanvas mapCanvas, Vector2f pt, int r) {
        int t1 = r / 16;
        int x = r;
        int y = 0;
        while (x >= y) {
            // plot
            setPixelHalfCircle(mapCanvas, pt, x, y);
            setPixelHalfCircle(mapCanvas, pt, y, x);
            y++;
            t1 += y;
            int t2 = t1 - x;
            if (t2 >= 0) {
                t1 = t2;
                x--;
            }
        }
    }

    private static void setPixelHalfCircle(@NotNull MapCanvas mapCanvas, Vector2f center, int dx, int dy) {
        mapCanvas.setPixelColor((int) (center.x + dx), (int) (center.y + dy), Color.BLACK);
        mapCanvas.setPixelColor((int) (center.x + dx), (int) (center.y - dy), Color.BLACK);
        mapCanvas.setPixelColor((int) (center.x - dx), (int) (center.y + dy), Color.BLACK);
        mapCanvas.setPixelColor((int) (center.x - dx), (int) (center.y - dy), Color.BLACK);
    }

    private static void drawLine(@NotNull MapCanvas mapCanvas, Vector2f a, Vector2f b) {
        int x = (int) a.x;
        int y = (int) a.y;
        int dx = Math.abs((int) b.x - x);
        int dy = Math.abs((int) b.y - y);
        int sx = (x < (int) b.x) ? 1 : -1;
        int sy = (y < (int) b.y) ? 1 : -1;

        int error = dx - dy;

        while (true) {
            mapCanvas.setPixelColor(x, y, Color.GREEN);
            if (x == (int) b.x && y == (int) b.y) break;
            int e2 = 2 * error;
            if (e2 > -dy) {
                error -= dy;
                x += sx;
            }
            if (e2 < dx) {
                error += dx;
                y += sy;
            }
        }
    }
}
