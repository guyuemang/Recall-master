package qwq.arcane.utils.pack;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.exception.CancelException;

import net.minecraft.network.Packet;
import qwq.arcane.utils.Instance;

import java.util.Arrays;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:44 AM
 */

public class PacketUtil implements Instance {
    public static void sendPacket(Packet<?> packet) {
        mc.getNetHandler().addToSendQueue(packet);
    }
    public static void queue(final Packet packet) {
        if (packet == null) {
            System.out.println("Packet is null");
            return;
        }

        if (isClientPacket(packet)) {
            mc.getNetHandler().addToSendQueueUnregistered(packet);
        } else {
            packet.processPacket(mc.getNetHandler().getNetworkManager().getNetHandler());
        }
    }
    public static boolean isCPacket(Packet<?> packet) {
        return packet.getClass().getSimpleName().startsWith("C");
    }

    public static boolean isClientPacket(final Packet<?> packet) {
        return Arrays.stream(NetworkAPI.serverbound).anyMatch(clazz -> clazz == packet.getClass());
    }
    public static void sendToServer(PacketWrapper packet, Class<? extends Protocol> packetProtocol, boolean skipCurrentPipeline, boolean currentThread) {
        try {
            if (currentThread) {
                packet.sendToServer(packetProtocol, skipCurrentPipeline);
            } else {
                packet.scheduleSendToServer(packetProtocol, skipCurrentPipeline);
            }
        } catch (CancelException var5) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    public static void sendPacketNoEvent(Packet packet) {
        mc.getNetHandler().addToSendQueueUnregistered(packet);
    }
}
