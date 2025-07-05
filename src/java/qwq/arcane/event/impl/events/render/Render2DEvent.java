package qwq.arcane.event.impl.events.render;

import lombok.Getter;
import net.minecraft.client.gui.ScaledResolution;
import qwq.arcane.event.impl.CancellableEvent;

/**
 * @Author：Guyuemang
 * @Date：2025/6/29 01:59
 */
@Getter
public class Render2DEvent extends CancellableEvent {
    private final ScaledResolution scaledResolution;
    private final float partialTicks;

    public Render2DEvent(ScaledResolution scaledResolution,float partialTicks) {
        this.scaledResolution = scaledResolution;
        this.partialTicks = partialTicks;
    }
}
