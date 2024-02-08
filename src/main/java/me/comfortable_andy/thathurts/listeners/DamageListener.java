package me.comfortable_andy.thathurts.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.comfortable_andy.thathurts.collision.BodyPart;
import me.comfortable_andy.thathurts.commands.SetColorCommand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class DamageListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (!(event.getDamager() instanceof LivingEntity)) return;

        final BodyPart part = BodyPart.findPart((LivingEntity) event.getEntity(), (LivingEntity) event.getDamager());

        event.getEntity().sendMessage("Damaged part: " + part);
        event.getDamager().sendMessage("Damaged part: " + part);
    }

    @EventHandler
    public void onBlockAttack(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.STICK) return;
        if (event.getClickedBlock() == null) return;
        final var block = event.getClickedBlock();

        final var packet = new PacketContainer(PacketType.Play.Server.CUSTOM_PAYLOAD);
        packet.getMinecraftKeys().write(0, new MinecraftKey("debug/game_test_add_marker"));
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

        buf.writeBlockPos(new BlockPos(block.getX(), block.getY(), block.getZ()));
        buf.writeInt(SetColorCommand.COLOR.getRGB());
        buf.writeUtf("test");
        buf.writeInt(3 * 1000);

        packet.getModifier().withType(ByteBuf.class).write(0, buf);

        ProtocolLibrary.getProtocolManager().sendServerPacket(event.getPlayer(), packet);
        event.getPlayer().sendMessage("sent marker");
    }

}
