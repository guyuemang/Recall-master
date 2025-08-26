package qwq.arcane.module.impl.display;

import net.minecraft.client.gui.ScaledResolution;
import qwq.arcane.event.impl.events.render.Shader2DEvent;
import qwq.arcane.gui.notification.IslandRender;
import qwq.arcane.module.Category;
import qwq.arcane.module.ModuleWidget;
import qwq.arcane.value.impl.BoolValue;

/**
 * @Author：Guyuemang
 * @Date：2025/6/2 13:38
 */
public class IsLand extends ModuleWidget {
    public IsLand() {
        super("IsLand", Category.Display);
    }
    public static BoolValue island = new BoolValue("IP",true);
    @Override
    public void onShader(Shader2DEvent event) {
        IslandRender.INSTANCE.rendershader(new ScaledResolution(mc));
    }

    @Override
    public void render() {
        IslandRender.INSTANCE.render(new ScaledResolution(mc));
    }

    @Override
    public boolean shouldRender() {
        return getState();
    }
}
