package qwq.arcane.module.impl.combat;

import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketReceiveEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.value.impl.ModeValue;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:05 AM
 */
public class Velocity extends Module {
    public Velocity() {
        super("Velocity",Category.Combat);
    }

    private final ModeValue mode = new ModeValue("Mode","Watchdog", new String[]{"Watchdog"});
    private boolean state;

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        setsuffix(mode.get());
        switch (mode.get()) {
            case "Watchdog":
                if (mc.thePlayer.onGround) {
                    state = false;
                }
                break;
        }
    }

    @EventTarget
    public void onPacket(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof S12PacketEntityVelocity s12 && s12.getEntityID() == mc.thePlayer.getEntityId()) {
            switch (mode.get()) {
                case "Watchdog":
                    if (!mc.thePlayer.onGround) {
                        if (!state) {
                            event.setCancelled(true);
                            state = true;
                            return;
                        }
                    }
                    s12.motionX = (int) (mc.thePlayer.motionX * 8000);
                    s12.motionZ = (int) (mc.thePlayer.motionZ * 8000);
                    break;
            }
        }
    }
}
