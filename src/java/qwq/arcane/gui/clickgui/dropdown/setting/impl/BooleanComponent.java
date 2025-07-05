/*
 * MoonLight Hacked Client
 *
 * A free and open-source hacked client for Minecraft.
 * Developed using Minecraft's resources.
 *
 * Repository: https://github.com/randomguy3725/MoonLight
 *
 * Author(s): [Randumbguy & wxdbie & opZywl & MukjepScarlet & lucas & eonian]
 */
package qwq.arcane.gui.clickgui.dropdown.setting.impl;

import qwq.arcane.gui.clickgui.Component;
import qwq.arcane.module.impl.render.InterFace;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.SmoothStepAnimation;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.value.impl.BooleanValue;

import java.awt.*;

public class BooleanComponent extends Component {
    private final BooleanValue setting;
    private final SmoothStepAnimation toggleAnimation = new SmoothStepAnimation(175, 1);

    public BooleanComponent(BooleanValue setting) {
        this.setting = setting;
        this.toggleAnimation.setDirection(Direction.BACKWARDS);
        setHeight(FontManager.Bold.get(15).getHeight() + 5);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        this.toggleAnimation.setDirection(setting.getValue() ? Direction.FORWARDS : Direction.BACKWARDS);
        FontManager.Bold.get(15).drawString(setting.getName(), getX() + 4, getY() + 2.5f, new Color(234, 234, 234).getRGB());

        RoundedUtil.drawRound(getX() + getWidth() - 15.5f, getY() + 2.5f, 13f, 6, 2.7f, InterFace.mainColor.get().brighter());
        RenderUtil.drawCircleCGUI(getX() + getWidth() - 12.5f + 7 * (float) toggleAnimation.getOutput().floatValue(), getY() + 5.5f, 7f, new Color(219, 226, 239).getRGB());
        super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (RenderUtil.isHovering(getX() + getWidth() - 15.5f, getY() + 4f, 13f, 6, mouseX, mouseY) && mouseButton == 0)
            this.setting.set(!this.setting.get());
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean isVisible() {
        return this.setting.isAvailable();
    }
}
