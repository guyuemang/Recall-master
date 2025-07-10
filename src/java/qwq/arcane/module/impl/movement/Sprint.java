package qwq.arcane.module.impl.movement;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.world.Scaffold;
import qwq.arcane.utils.player.MovementUtil;
import qwq.arcane.value.impl.BooleanValue;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:03 AM
 */
public class Sprint extends Module {
    public Sprint() {
        super("Sprint",Category.Movement);
    }
    private final BooleanValue omni = new BooleanValue("Omni", false);

    @Override
    public void onDisable() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
        mc.thePlayer.omniSprint = false;
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled(Scaffold.class)) KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);

        if(omni.get()){
            mc.thePlayer.omniSprint = MovementUtil.isMoving();
        }
    }
}
