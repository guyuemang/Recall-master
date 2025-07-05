package qwq.arcane.module.impl.display;

import qwq.arcane.event.impl.events.render.Shader2DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.ModuleWidget;

/**
 * @Author：Guyuemang
 * @Date：2025/7/4 16:06
 */
public class ArrayList extends ModuleWidget {
    public ArrayList() {
        super("ArrayList",Category.Display);
    }

    @Override
    public void onShader(Shader2DEvent event) {

    }

    @Override
    public void render() {

    }

    @Override
    public boolean shouldRender() {
        return false;
    }
}
