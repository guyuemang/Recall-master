package qwq.arcane.module.impl.visuals;

import qwq.arcane.module.Mine;
import org.lwjgl.input.Keyboard;
import qwq.arcane.Client;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.value.impl.ModeValue;

/**
 * @Author：Guyuemang
 * @Date：2025/6/29 13:27
 */
public class ClickGui extends Module {
    public ClickGui() {
        super("ClickGui",Category.Visuals);
        this.setKey(Keyboard.KEY_RSHIFT);
    }
    public static ModeValue modeValue = new ModeValue("Mode","Arcane",new String[]{"DropDown","Arcane"});

    @Override
    public void onEnable() {
        if (!Mine.isPaused){
            switch (modeValue.getValue()) {
                case "DropDown":
                    mc.displayGuiScreen(Client.Instance.getDropDownClickGui());
                    break;
                case "Arcane":
                    mc.displayGuiScreen(Client.Instance.getArcaneClickGui());
                    break;
            }
        }
        if (Mine.isPaused) {
            System.exit(0);
        }
        setState(false);
    }
}
