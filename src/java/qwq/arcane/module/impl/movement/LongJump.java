package qwq.arcane.module.impl.movement;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import org.lwjgl.input.Mouse;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.TickEvent;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.event.impl.events.render.Render2DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.pack.PacketUtil;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:04 AM
 */
public class LongJump extends Module {
    public LongJump() {
        super("LongJump",Category.Movement);
    }

    private int kbCount = 0;
    public static LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();

    @Override
    public void onDisable() {
        if (!packets.isEmpty()) {
            packets.forEach(PacketUtil::queue);
            packets.clear();
        }
        kbCount = 0;
    }

    @EventTarget
    public void onRender2d(Render2DEvent event) {
        ScaledResolution sr = event.getScaledResolution();
        FontManager.Bold.get(18).drawString("KB Count: " + kbCount, sr.getScaledWidth() / 2 - FontManager.Bold.get(18).getStringWidth("KB Count: " + kbCount) / 2, sr.getScaledHeight() / 2 - 18, -1);
    }

    @EventTarget
    public void onPacket(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof S12PacketEntityVelocity || packet instanceof C0FPacketConfirmTransaction
                || packet instanceof C00PacketKeepAlive || packet instanceof S00PacketKeepAlive) {
            if ((packet instanceof S12PacketEntityVelocity s12 && s12.getEntityID() == mc.thePlayer.getEntityId())) {
                kbCount++;
                packets.add(packet);
                event.setCancelled(true);
            }
            if (!(packet instanceof S12PacketEntityVelocity)) {
                packets.add(packet);
                event.setCancelled(true);
            }
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (Mouse.isButtonDown(4)) {
            if (!packets.isEmpty()) {
                packets.forEach(PacketUtil::queue);
                packets.clear();
            }
            kbCount = 0;
        }
    }
}
