
package qwq.arcane.gui.clickgui.dropdown.setting.impl;

import qwq.arcane.gui.clickgui.Component;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.EaseOutSine;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.value.impl.BooleanValue;
import qwq.arcane.value.impl.MultiBooleanValue;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 12:31
 */
public class MultiBooleanComponent extends Component {
    private final MultiBooleanValue setting;
    private final Map<BooleanValue, EaseOutSine> select = new HashMap<>();

    public MultiBooleanComponent(MultiBooleanValue setting) {
        this.setting = setting;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        float offset = 8;
        float heightoff = 0;

        RoundedUtil.drawRound(getX() + offset, getY() + FontManager.Bold.get(15).getHeight() + 2, getWidth() - 5, heightoff, 4, new Color(128, 128, 128));
        FontManager.Bold.get(15).drawString(setting.getName(), getX() + 4, getY(), -1);

        for (BooleanValue boolValue : setting.getValues()) {
            float off = FontManager.Bold.get(13).getStringWidth(boolValue.getName()) + 4;
            if (offset + off >= getWidth() - 5) {
                offset = 8;
                heightoff += FontManager.Bold.get(13).getHeight() + 2;
            }
            select.putIfAbsent(boolValue, new EaseOutSine(250, 1));
            select.get(boolValue).setDirection(boolValue.get() ? Direction.FORWARDS : Direction.BACKWARDS);

            FontManager.Bold.get(13).drawString(boolValue.getName(), getX() + offset, getY() + FontManager.Bold.get(15).getHeight() + 2 + heightoff, ColorUtil.interpolateColor2(InterFace.mainColor.get().brighter(), InterFace.mainColor.get().brighter().brighter(), (float) select.get(boolValue).getOutput().floatValue()));

            offset += off;
        }

        setHeight(FontManager.Bold.get(15).getHeight() + 10 + heightoff);
        super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouse) {
        float offset = 8;
        float heightoff = 0;
        for (BooleanValue boolValue : setting.getValues()) {
            float off = FontManager.Bold.get(13).getStringWidth(boolValue.getName()) + 4;
            if (offset + off >= getWidth() - 5) {
                offset = 8;
                heightoff += FontManager.Bold.get(13).getHeight() + 2;
            }
            if (RenderUtil.isHovering(getX() + offset, getY() + FontManager.Bold.get(15).getHeight() + 2 + heightoff, FontManager.Bold.get(13).getStringWidth(boolValue.getName()), FontManager.Bold.get(13).getHeight(), mouseX, mouseY) && mouse == 0) {
                boolValue.set(!boolValue.get());
            }
            offset += off;
        }
        super.mouseClicked(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.isAvailable();
    }
}
