package qwq.arcane.module.impl.movement;

import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.player.MovementUtil;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:03 AM
 */
public class Speed extends Module {
    public Speed() {
        super("Speed",Category.Movement);
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        MovementUtil.strafe(0.47,0.47);
    }
}
