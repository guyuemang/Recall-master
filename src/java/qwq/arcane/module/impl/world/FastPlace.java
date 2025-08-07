package qwq.arcane.module.impl.world;

import net.minecraft.item.ItemBlock;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.value.impl.NumberValue;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:07 AM
 */
public class FastPlace extends Module {
    public FastPlace() {
        super("FastPlace",Category.World);
    }

    public final NumberValue speed = new NumberValue("Speed", 1, 0, 4, 1);

    @EventTarget
    public void onMotion(MotionEvent event) {
        setsuffix(String.valueOf(speed.get()));
        if (mc.thePlayer == null && mc.theWorld == null)
            return;
        if (mc.thePlayer.getHeldItem() == null)
            return;
        if (mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock)
            mc.rightClickDelayTimer = speed.getValue().intValue();
    }
}
