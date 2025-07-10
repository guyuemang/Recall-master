
package qwq.arcane.gui.clickgui.dropdown.setting.impl;

import qwq.arcane.gui.clickgui.Component;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.math.MathUtils;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.value.impl.NumberValue;
import net.minecraft.util.MathHelper;

import java.awt.*;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 12:31
 */
public class SliderComponent extends Component {

    private final NumberValue setting;
    private float anim;
    private boolean dragging;

    public SliderComponent(NumberValue setting) {
        this.setting = setting;
        setHeight(FontManager.Bold.get(15).getHeight() * 2 + FontManager.Bold.get(15).getHeight() + 2);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        FontManager.Bold.get(15).drawString(setting.getName(), getX() + 4, getY(), -1);

        anim = RenderUtil.animate(anim, (float) ((getWidth() - 8) * (setting.get() - setting.getMin()) / (setting.getMax() - setting.getMin())), 15);
        float sliderWidth = anim;

        RoundedUtil.drawRound(getX() + 4, getY() + FontManager.Bold.get(15).getHeight() + 2, getWidth() - 8, 2, 2,new Color(1,1,1));
        RoundedUtil.drawGradientHorizontal(getX() + 4, getY() + FontManager.Bold.get(15).getHeight() + 2, sliderWidth, 2, 2, InterFace.mainColor.get(), InterFace.mainColor.get().brighter());
        RenderUtil.drawCircleCGUI(getX() + 4 + sliderWidth, getY() + FontManager.Bold.get(15).getHeight() + 3, 6, -1);

        FontManager.Bold.get(15).drawString(setting.getMin() + "", getX() + 2, getY() + FontManager.Bold.get(15).getHeight() * 2 + 2, new Color(160, 160, 160).getRGB());
        FontManager.Bold.get(15).drawCenteredString(setting.get() + "", getX() + getWidth() / 2, getY() + FontManager.Bold.get(15).getHeight() * 2 + 2, -1);
        FontManager.Bold.get(15).drawString(setting.getMax() + "", getX() - 2 + getWidth() - FontManager.Bold.get(15).getStringWidth(setting.getMax() + ""), getY() + 2 + FontManager.Bold.get(15).getHeight() * 2 + 2, new Color(160, 160, 160).getRGB());

        if (dragging) {
            final double difference = setting.getMax() - setting.getMin(), value = setting.getMin() + MathHelper.clamp_float((mouseX - getX()) / getWidth(), 0, 1) * difference;
            setting.setValue(MathUtils.incValue(value, setting.getStep()));
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && RenderUtil.isHovering(getX() + 2, getY() + FontManager.Bold.get(15).getHeight() + 2, getWidth(), 6, mouseX, mouseY))
            dragging = true;
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) dragging = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean isVisible() {
        return this.setting.isAvailable();
    }
}
