package me.comfortable_andy.thathurts.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import me.comfortable_andy.thathurts.ThatHurtsMain;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

@CommandPermission("thathurts.commands.plugin")
@CommandAlias("thathurts|th|hurt")
public class PluginCommand extends BaseCommand {

    @Subcommand("debug")
    public void onDebugChange(CommandSender sender, @Optional Boolean bool) {
        if (bool == null) bool = !ThatHurtsMain.getInstance().isDebugModeOn();
        ThatHurtsMain.getInstance().setDebugModeOn(bool);
        sender.sendMessage("Set to: " + ThatHurtsMain.getInstance().isDebugModeOn());
    }

    @Subcommand("collisionMap")
    public void onRequestCollisionMap(Player player) {
        if (ThatHurtsMain.getInstance().getMapId() == -1) {
            player.sendMessage("Map id isn't loaded properly, try again after restart (or contact dev).");
            return;
        }
        ItemStack item = new ItemStack(Material.FILLED_MAP);
        item.editMeta(
                MapMeta.class,
                meta -> meta.setMapView(Bukkit.getMap(ThatHurtsMain.getInstance().getMapId()))
        );
        player.getInventory().addItem(item);
    }

}
