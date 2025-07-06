package qwq.arcane.utils.pack;

import net.minecraft.network.Packet;
import qwq.arcane.utils.Instance;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:44 AM
 */
public class PacketUtil implements Instance {
    public static void sendPacket(Packet<?> packet) {
        mc.getNetHandler().addToSendQueue(packet);
    }
}
