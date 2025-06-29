package recall.module.impl.render;

import net.minecraft.client.Minecraft;
import recall.event.annotations.EventTarget;
import recall.event.impl.events.render.Render2DEvent;
import recall.module.Category;
import recall.module.Module;
import recall.utils.render.RenderUtil;
import recall.value.impl.BooleanValue;
import recall.value.impl.ColorValue;
import recall.value.impl.TextValue;

import java.awt.*;

/**
 * @Author：Guyuemang
 * @Date：2025/6/29 01:06
 */
public class InterFace extends Module {
    public InterFace() {
        super("InterFace", Category.Render);
        setState(true);
    }
    public static TextValue name = new TextValue("ClientName","Solitude");
    public static ColorValue FirstColor = new ColorValue("FirstColor", new Color(89, 139, 184));
    public static ColorValue SecondColor = new ColorValue("SecondColor", new Color(221, 228, 255));
    public static BooleanValue sb1 = new BooleanValue("sb1",false);
    public static BooleanValue sb = new BooleanValue("svsadw",false);

    @EventTarget
    public void onRender(Render2DEvent e) {
        boolean shouldChange = RenderUtil.COLOR_PATTERN.matcher(name.get()).find();
        String text = shouldChange ? "§r" + name.getText() : name.getText().charAt(0) + "§r§f" + name.getText().substring(1) +
                "§7[§f" + Minecraft.getDebugFPS() + " FPS§7]§r ";
        mc.fontRendererObj.drawStringWithShadow(text, 2.0f, 2.0f, 1);
    }
}
