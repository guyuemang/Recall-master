package recall.gui.clickgui.fac.components.setting.impl;

import recall.Client;
import recall.gui.clickgui.Component;
import recall.utils.fontrender.FontManager;
import recall.utils.render.RenderUtil;
import recall.utils.render.RoundedUtil;
import recall.value.impl.BooleanValue;

import java.awt.*;

/**
 * @Author：Guyuemang
 * @Date：2025/6/29 16:44
 */
public class BooleanComponent extends Component {
    private final BooleanValue setting;
    public BooleanComponent(BooleanValue setting) {
        this.setting = setting;
        setHeight(22);
    }
    @Override
    public void drawScreen(int mouseX, int mouseY) {
        FontManager.Semibold.get(20).drawString(setting.getName(), getX() + 15, getY() + 12, Client.Instance.getFatalityClickGui().font1color.getRGB());
        RoundedUtil.drawRound(getX() + 210, getY() + 10, 15,15, 3,new Color(0xF2F6FC));
        if (setting.get()){
            FontManager.Icon.get(18).drawString("P", getX() + 213, getY() + 16, Client.Instance.getFatalityClickGui().font2color.getRGB());
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (RenderUtil.isHovering(getX() + 210, getY() + 10, 15,15,mouseX,mouseY) && mouseButton == 0){
            setting.toggle();
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
    }
    @Override
    public boolean isVisible() {
        return setting.isAvailable();
    }
}
