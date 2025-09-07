package qwq.arcane.module.impl.visuals;

import net.minecraft.util.ResourceLocation;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.TickEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.value.impl.NumberValue;

public class MotionBlur extends Module {
    public final NumberValue amount = new NumberValue("Amount", 1, 1, 10, 1);

    public MotionBlur() {
        super("MotionBlur",Category.Visuals);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.theWorld != null) {
            if (isEnabled()) {
                if ((mc.entityRenderer.getShaderGroup() == null))
                    mc.entityRenderer.loadShader(new ResourceLocation("minecraft", "shaders/post/motion_blur.json"));
                float uniform = 1F - Math.min(amount.getValue().floatValue() / 10F, 0.9f);
                if (mc.entityRenderer.getShaderGroup() != null) {
                    mc.entityRenderer.getShaderGroup().listShaders.get(0).getShaderManager().getShaderUniform("Phosphor").set(uniform, 0F, 0F);
                }
            } else {
                if (mc.entityRenderer.isShaderActive())
                    mc.entityRenderer.stopUseShader();
            }
        }
    }
}
