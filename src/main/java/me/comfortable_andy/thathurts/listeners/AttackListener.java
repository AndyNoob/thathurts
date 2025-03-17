package me.comfortable_andy.thathurts.listeners;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class AttackListener implements Listener {

    @EventHandler
    public void onAttack(PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = player.getInventory().getItemInOffHand();
        final double itemSpeed = getItemSpeed(item);
        double rawSpeed = Optional.ofNullable(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).map(AttributeInstance::getBaseValue).orElse(4.0);
        player.sendMessage("u attack speed: " + rawSpeed);
        player.sendMessage("item attack speed: " + itemSpeed);
    }

    private static double getItemSpeed(ItemStack item) {
        final String packageName = Bukkit.getServer().getClass().getPackageName();
        try {
            final net.minecraft.world.item.ItemStack nmsItem = (net.minecraft.world.item.ItemStack) Class.forName(packageName + ".inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            return nmsItem.getItemHolder().value().getDefaultAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_SPEED).stream().reduce(0.0, (val, att) -> val + att.getAmount(), Double::sum);
        } catch (ReflectiveOperationException ignored) {
        }
        return -1;
    }

}
