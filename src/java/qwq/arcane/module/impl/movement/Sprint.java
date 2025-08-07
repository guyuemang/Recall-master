package qwq.arcane.module.impl.movement;


import net.minecraft.client.settings.KeyBinding;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.world.Scaffold;
import qwq.arcane.utils.player.MovementUtil;
import qwq.arcane.value.impl.BoolValue;


public class Sprint extends Module {
    public Sprint() {
        super("Sprint", Category.Movement);
    }
    private final BoolValue omni = new BoolValue("Omni", false);

    public static boolean keepSprinting = false;
    @EventTarget
    public void onSuffix(UpdateEvent event){
        setsuffix("Omni" + " " + omni.get().toString());
    }
    @Override
    public void onDisable() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
        mc.thePlayer.omniSprint = false;
        keepSprinting = false;
        super.onDisable();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (!keepSprinting) {
            if (!isEnabled(Scaffold.class))
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }else {
            keepSprinting = false;
        }

        if (omni.get()) {
            mc.thePlayer.omniSprint = MovementUtil.isMoving();
        }
    }
}