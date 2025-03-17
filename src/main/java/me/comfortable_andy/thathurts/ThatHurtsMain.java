package me.comfortable_andy.thathurts;

import co.aikar.commands.BukkitCommandManager;
import lombok.Getter;
import lombok.Setter;
import me.comfortable_andy.thathurts.collision.CollisionDebugMapRenderer;
import me.comfortable_andy.thathurts.commands.BoundingBoxTestCommand;
import me.comfortable_andy.thathurts.commands.PluginCommand;
import me.comfortable_andy.thathurts.listeners.DamageListener;
import org.bukkit.Bukkit;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Scanner;

@Getter
public final class ThatHurtsMain extends JavaPlugin {

    private static ThatHurtsMain INSTANCE;
    @Setter
    private boolean debugModeOn = true;
    private int mapId = -1;

    @Override
    public void onEnable() {
        INSTANCE = this;
        setupDebugMap();
        getServer().getPluginManager().registerEvents(new DamageListener(), this);
//        getServer().getPluginManager().registerEvents(new AttackListener(), this);

        final BukkitCommandManager manager = new BukkitCommandManager(this);
        manager.registerCommand(new BoundingBoxTestCommand(manager));
        manager.registerCommand(new PluginCommand());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void setupDebugMap() {
        File mapIdFile = new File(getDataFolder(), "id");
        MapView view;
        if (!mapIdFile.exists()) {
            mapIdFile.getParentFile().mkdirs();
            view = Bukkit.createMap(Bukkit.getWorlds().get(0));
            try (FileWriter writer = new FileWriter(mapIdFile)) {
                writer.write("" + (mapId = view.getId()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try (Scanner scanner = new Scanner(mapIdFile)) {
                mapId = scanner.nextInt();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            view = Bukkit.getMap(mapId);
        }
        assert view != null;
        view.getRenderers().forEach(view::removeRenderer);
        view.addRenderer(new CollisionDebugMapRenderer());
    }

    public static ThatHurtsMain getInstance() {
        return INSTANCE;
    }

}
