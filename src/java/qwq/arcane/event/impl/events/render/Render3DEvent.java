
package qwq.arcane.event.impl.events.render;

import net.minecraft.client.gui.ScaledResolution;
import qwq.arcane.event.impl.Event;

public record Render3DEvent(float partialTicks, ScaledResolution scaledResolution) implements Event {

}
