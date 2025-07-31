package qwq.arcane.module.impl.movement;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketReceiveEvent;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.event.impl.events.player.SlowDownEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.pack.BlinkComponent;
import qwq.arcane.utils.pack.PacketUtil;
import qwq.arcane.utils.player.MovementUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.ModeValue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:00 AM
 */
public class Noslow extends Module {
    public Noslow() {
        super("Noslow",Category.Movement);
    }
    private final ModeValue mode = new ModeValue("Mode", "Blink", new String[]{"Blink"});
    private final LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();
    boolean usingItem;
    @EventTarget
    public void onMotion(MotionEvent event) {
        setsuffix(String.valueOf(mode.get()));
        if (mode.is("Blink")) {
            if (mc.thePlayer.isUsingItem() && usingItem){
                mc.thePlayer.setJumping(true);
            }
            if (mc.thePlayer.getCurrentEquippedItem() == null) return;

            final Item item = mc.thePlayer.getCurrentEquippedItem().getItem();

            if (mc.thePlayer.isUsingItem() && item instanceof ItemFood || item instanceof ItemPotion || item instanceof ItemBow || item instanceof ItemSword) {
                BlinkComponent.blinking = true;
                BlinkComponent.dispatch();
            }else {
                usingItem = true;
                BlinkComponent.blinking = false;
            }
        }
    }
    @EventTarget
    public void onSlowDown(SlowDownEvent event) {
        final Item item = mc.thePlayer.getCurrentEquippedItem().getItem();

        if (mc.thePlayer.isUsingItem() && usingItem) {
            if (item instanceof ItemFood || item instanceof ItemPotion || item instanceof ItemBow || item instanceof ItemSword) {
                event.setCancelled();
                event.setForward(1);
                event.setStrafe(1);
                event.setSprinting(false);
            }
        }
    }
    @EventTarget
    public void onPacketSend(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();

        if (mc.thePlayer == null) return;

        if (mode.is("Blink")) {
            if (packet instanceof C08PacketPlayerBlockPlacement status){
                if (status.getPlacedBlockDirection() >= 4) {
                     usingItem = true;
                    packets.add(status);
                }
            }else if (!packets.isEmpty()){;
                packets.forEach(mc.getNetHandler()::addToSendQueueUnregistered);
                packets.clear();
            }
        }
    }
}
