package qwq.arcane.gui.clickgui.arcane.component.settings;


import net.minecraft.util.MathHelper;
import qwq.arcane.gui.clickgui.Component;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.animations.Animation;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.DecelerateAnimation;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.math.MathUtils;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.value.impl.NumberValue;

import java.awt.*;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 23:27
 */

public class NumberComponent extends Component {
    private final NumberValue setting;
    private boolean dragging;
    private final Animation drag = new DecelerateAnimation(250, 1);
    public NumberComponent(NumberValue setting) {
        this.setting = setting;
        setHeight(30);
        drag.setDirection(Direction.BACKWARDS);
    }
    private float anim;

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        int w = 145;
        anim = RenderUtil.animate(anim, (float) (w * (setting.get() - setting.getMin()) / (setting.getMax() - setting.getMin())), 50);
        float sliderWidth = anim;
        drag.setDirection(dragging ? Direction.FORWARDS : Direction.BACKWARDS);
        RoundedUtil.drawRound(getX() + 10, getY() + getHeight() - 4, 145, 1, 0, INSTANCE.getArcaneClickGui().linecolor);

        FontManager.Bold.get(18).drawString(setting.getName(), getX() + 10, getY() + 4, ColorUtil.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(),0.4f));
        FontManager.Bold.get(18).drawString(setting.get().toString(), getX() + 155 - FontManager.Bold.get(18).getStringWidth(setting.get().toString()), getY() + 4, ColorUtil.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(),0.4f));

        RoundedUtil.drawRound(getX() + 10, getY() + 18, w, 2, 2, INSTANCE.getArcaneClickGui().versionColor);
        RoundedUtil.drawGradientHorizontal(getX() + 10, getY() + 18, sliderWidth, 2, 2, InterFace.color(1),new Color(-1));
        RoundedUtil.drawRound(getX() + 5 + sliderWidth, getY() + 17, 8, 4, 1, INSTANCE.getArcaneClickGui().fontcolor);

        if (dragging) {
            final double difference = this.setting.getMax() - this.setting
                    .getMin(), //
                    value = this.setting.getMin() + MathHelper
                            .clamp_double((mouseX - (getX() + 10)) / w, 0, 1) * difference;
            setting.setValue((double) MathUtils.incValue(value, setting.getStep()));
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        int w = 145;
        if (RenderUtil.isHovering(getX() + 10, getY() + 16, w, 6,mouseX, mouseY) && mouseButton == 0) {
            dragging = true;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0){
            dragging = false;
        }
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
