package me.comfortable_andy.thathurts;

import me.comfortable_andy.thathurts.listeners.DamageListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class ThatHurtsMain extends JavaPlugin {

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(new DamageListener(), this);

    }

}
