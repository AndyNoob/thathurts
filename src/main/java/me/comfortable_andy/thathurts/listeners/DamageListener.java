package me.comfortable_andy.thathurts.listeners;

import me.comfortable_andy.thathurts.collision.BodyPart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        player.sendMessage("Damaged part: " + BodyPart.findPart(player, event.getDamager()));
    }

}
