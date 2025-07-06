package qwq.arcane.module.impl.visuals;

import net.minecraft.util.Vec3;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.event.impl.events.render.Render3DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.value.impl.BooleanValue;
import qwq.arcane.value.impl.NumberValue;

import java.util.ArrayDeque;

public final class Breadcrumbs extends Module {

    private final ArrayDeque<Vec3> path = new ArrayDeque<>();

    private final BooleanValue timeoutBool = new BooleanValue("Timeout", true);
    private final NumberValue timeout = new NumberValue("Time", 15, 1, 150, 0.1f);

    public Breadcrumbs() {
        super("Breadcrumbs",Category.Visuals);
    }

    @Override
    public void onEnable() {
        path.clear();
    }

    @EventTarget
    public void onPreMotion(MotionEvent e) {
        if (e.isPre()) {
            if (mc.thePlayer.lastTickPosX != mc.thePlayer.posX || mc.thePlayer.lastTickPosY != mc.thePlayer.posY || mc.thePlayer.lastTickPosZ != mc.thePlayer.posZ) {
                path.add(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
            }

            if (timeoutBool.get()) {
                while (path.size() > (int) timeout.get().intValue()) {
                    path.removeFirst();
                }
            }
        }
    }

    @EventTarget
    public void onRender3DEvent(Render3DEvent e) {
        RenderUtil.renderBreadCrumbs(path);
    }
}