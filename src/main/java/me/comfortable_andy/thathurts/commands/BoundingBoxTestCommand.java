package me.comfortable_andy.thathurts.commands;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import me.comfortable_andy.thathurts.ThatHurtsMain;
import me.comfortable_andy.thathurts.utils.OrientedBox;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.comfortable_andy.thathurts.utils.PositionUtil.*;
import static org.bukkit.ChatColor.BOLD;
import static org.bukkit.ChatColor.RESET;

@SuppressWarnings("deprecation")
@CommandPermission("thathurts.commands.boundingboxtest")
@CommandAlias("bounding|bb|boundingbox|btest|bbtest|bbt")
public class BoundingBoxTestCommand extends BaseCommand {

    private World world = null;
    private final Map<String, OrientedBox> testingBox = new ConcurrentHashMap<>();
    private final Particle particle = Particle.VILLAGER_HAPPY;

    @SuppressWarnings("FieldCanBeLocal")
    private final Runnable renderRunnable = () -> {
        if (world == null) return;
        if (this.testingBox.isEmpty()) return;

        for (OrientedBox box : this.testingBox.values()) {
            if (this.world.getFullTime() % 20 == 0)
                box.display(this.world, this.particle);

            for (Player player : Bukkit.getOnlinePlayers()) {
                final OrientedBox playerBox = new OrientedBox(player.getBoundingBox());
                final Location location = player.getLocation();
                playerBox.rotateBy(
                        new Quaternionf().rotationY((float) Math.toRadians(-location.getYaw()))
                );
                playerBox.display(this.world, this.particle);
                final Vector3f translate = box.getMinimumTranslate(playerBox);
                if (translate == null) continue;
                player.setVelocity(convertBukkit(translate).normalize());
            }
        }
    };

    public BoundingBoxTestCommand(BukkitCommandManager manager) {
        manager.getCommandCompletions().registerCompletion("box", context -> {
            if (this.testingBox.isEmpty()) return Collections.emptyList();
            return this.testingBox.keySet();
        });
        Bukkit.getScheduler().runTaskTimer(manager.getPlugin(), this.renderRunnable, 0, 1);
    }

    @Subcommand("make")
    @CommandPermission("thathurts.commands.boundingboxtest.make")
    public void onMakeBox(CommandSender sender, String name, Location min, Location max) {
        if (!(sender instanceof Entity || sender instanceof BlockCommandSender)) {
            sender.sendMessage(ChatColor.RED + "Only entities can use this command.");
            return;
        }
        this.world = sender instanceof Entity entity ? entity.getWorld() : ((BlockCommandSender) sender).getBlock().getWorld();
        this.testingBox.put(name, new OrientedBox(BoundingBox.of(min, max)));
        sender.sendMessage("\"" + BOLD + name + RESET + "\" created.");
    }

    @Subcommand("destroy")
    @CommandPermission("thathurts.commands.boundingboxtest.destroy")
    @CommandCompletion("@box")
    public void onDestroyBox(CommandSender sender, String name) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return;
        }
        if (!this.testingBox.containsKey(name)) {
            sender.sendMessage(ChatColor.RED + "No testing box with that name.");
            return;
        }
        this.testingBox.remove(name);
        sender.sendMessage("Destroyed " + BOLD + name + RESET + ".");
    }

    @Subcommand("trace")
    @CommandPermission("thathurts.commands.boundingboxtest.trace")
    @CommandCompletion("@box")
    public void onTraceBox(CommandSender sender, String name) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return;
        }
        if (this.testingBox.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "You must first create a testing box.");
            return;
        }
        if (!this.testingBox.containsKey(name)) {
            sender.sendMessage(ChatColor.RED + "No testing box with that name.");
            return;
        }
        final Vector3f vector = (this.testingBox.get(name).trace(convertJoml(player.getEyeLocation().toVector()), convertJoml(player.getEyeLocation().getDirection())));

        if (vector == null) {
            sender.sendMessage(ChatColor.RED + "Didn't hit.");
            return;
        }

        this.world.spawnParticle(Particle.END_ROD, bukkitLoc(convertBukkit(vector), this.world), 1, 0, 0, 0, 0, null, true);
        final ArmorStand stand = this.world.spawn(bukkitLoc(convertBukkit(vector), this.world), ArmorStand.class);
        stand.setGlowing(true);
        stand.setGravity(false);

        new BukkitRunnable() {
            @Override
            public void run() {
                stand.remove();
            }
        }.runTaskLater(ThatHurtsMain.getInstance(), 20 * 5);
    }

    @Subcommand("rotate")
    @CommandPermission("thathurts.commands.boundingboxtest.rotate")
    @CommandCompletion("@box @range:-360-360 @range:-360-360 @range:-360-360")
    public void onRotateBox(CommandSender sender, String name, float x, float y, float z) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return;
        }
        if (!this.testingBox.containsKey(name)) {
            sender.sendMessage(ChatColor.RED + "No testing box with that name.");
            return;
        }
        final OrientedBox box = this.testingBox.get(name);

        // bbt make box1 0,-59,10 -2,-56,15
        // bbt make box2 0,-59,8 -2,-56,3

        box.rotateBy(x, y, z);

        sender.sendMessage("Rotated " + BOLD + name + RESET + " by " + x + ", " + y + ", " + z);
        sender.sendMessage("or," + Math.toRadians(x) + ", " + Math.toRadians(y) + ", " + Math.toRadians(z));
    }

}

