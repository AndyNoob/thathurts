package me.comfortable_andy.thathurts.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.awt.*;

@CommandAlias("setcolor")
public class SetColorCommand extends BaseCommand {

    public static Color COLOR = new Color(0, 0, 0, 0);

    @Subcommand("col")
    public void onCol(CommandSender sender, int r, int g, int b, int a) {
        COLOR = new Color(r, g, b, a);
        sender.sendMessage(ChatColor.AQUA + "Done.");
    }

}
