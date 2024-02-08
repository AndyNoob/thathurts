package me.comfortable_andy.thathurts.listeners;

import me.comfortable_andy.thathurts.collision.BodyPart;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (!(event.getDamager() instanceof LivingEntity)) return;

        final BodyPart part = BodyPart.findPart((LivingEntity) event.getEntity(), (LivingEntity) event.getDamager());

        event.getEntity().sendMessage("Damaged part: " + part);
        event.getDamager().sendMessage("Damaged part: " + part);
    }

}
