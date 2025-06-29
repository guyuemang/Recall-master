package recall.module.impl.render;

import org.lwjgl.input.Keyboard;
import recall.Client;
import recall.module.Category;
import recall.module.Module;

/**
 * @Author：Guyuemang
 * @Date：2025/6/29 13:27
 */
public class ClickGui extends Module {
    public ClickGui() {
        super("ClickGui",Category.Render);
        this.setKey(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnable() {
        mc.displayGuiScreen(Client.Instance.getFatalityClickGui());
        setState(false);
    }
}
