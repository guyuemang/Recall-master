package qwq.arcane.module.impl.movement;

import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:58 AM
 */
public class NoJumpDelay extends Module {
    public NoJumpDelay() {
        super("NoJumpDelay",Category.Movement);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        mc.thePlayer.jumpTicks = 0;
    }
}
