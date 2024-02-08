package me.comfortable_andy.thathurts;

import co.aikar.commands.BukkitCommandManager;
import me.comfortable_andy.thathurts.commands.BoundingBoxTestCommand;
import me.comfortable_andy.thathurts.commands.SetColorCommand;
import me.comfortable_andy.thathurts.listeners.DamageListener;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public final class ThatHurtsMain extends JavaPlugin {

    private static ThatHurtsMain INSTANCE;

    @Override
    public void onEnable() {

        INSTANCE = this;

        getServer().getPluginManager().registerEvents(new DamageListener(), this);

        final BukkitCommandManager manager = new BukkitCommandManager(this);
        manager.registerCommand(new BoundingBoxTestCommand(manager));
        manager.registerCommand(new SetColorCommand());

    }

    public static ThatHurtsMain getInstance() {
        return INSTANCE;
    }

}
