package qwq.arcane.gui.clickgui.arcane.component.settings;


import qwq.arcane.gui.clickgui.Component;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.animations.Animation;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.DecelerateAnimation;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.value.impl.BoolValue;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 23:16
 */

public class BooleanComponent extends Component {
    private final BoolValue setting;
    private final Animation enabled = new DecelerateAnimation(250,1);
    private final Animation hover = new DecelerateAnimation(250,1);
    public BooleanComponent(BoolValue setting) {
        this.setting = setting;
        setHeight(22);
        enabled.setDirection(setting.get() ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        enabled.setDirection(setting.get() ? Direction.FORWARDS : Direction.BACKWARDS);
        hover.setDirection(RenderUtil.isHovering(getX() + 172, getY() + 15, 22, 12,mouseX,mouseY) ? Direction.FORWARDS : Direction.BACKWARDS);
        RoundedUtil.drawRound(getX() + 10, getY() + getHeight() - 4, 145, 1, 0, INSTANCE.getArcaneClickGui().linecolor);
        FontManager.Bold.get(18).drawString(setting.getName(), getX() + 10, getY() + 4,ColorUtil.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(),0.4f));
        RoundedUtil.drawRound(getX() + 135, getY() + 4, 20, 10, 4, ColorUtil.applyOpacity(InterFace.color(1),0.4f));
        RenderUtil.drawCircleCGUI(getX() + 141 + enabled.getOutput().floatValue() * 9f, getY() + 9, 8,InterFace.color(1).darker().getRGB());

        super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (RenderUtil.isHovering(getX() + 135, getY() + 4, 20, 10,mouseX,mouseY) && mouseButton == 0){
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
