
package qwq.arcane.gui.clickgui.dropdown;

import qwq.arcane.gui.clickgui.Component;
import qwq.arcane.gui.clickgui.IComponent;
import qwq.arcane.gui.clickgui.dropdown.setting.impl.*;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.EaseInOutQuad;
import qwq.arcane.utils.animations.impl.EaseOutSine;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.shader.KawaseBlur;
import qwq.arcane.value.Value;
import qwq.arcane.value.impl.*;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 12:31
 */
@Getter
@Setter
public class ModuleComponent implements IComponent {
    private float x, y, width, height = 19;
    private final Module module;
    private boolean opened;
    private final EaseInOutQuad openAnimation = new EaseInOutQuad(250, 1);
    private final EaseOutSine toggleAnimation = new EaseOutSine(300, 1);
    private final EaseOutSine hoverAnimation = new EaseOutSine(200, 1);
    private final CopyOnWriteArrayList<Component> settings = new CopyOnWriteArrayList<>();

    public ModuleComponent(Module module) {
        this.module = module;
        openAnimation.setDirection(Direction.BACKWARDS);
        toggleAnimation.setDirection(Direction.BACKWARDS);
        hoverAnimation.setDirection(Direction.BACKWARDS);
        for (Value value : module.getSettings()) {
            if (value instanceof BoolValue boolValue) {
                settings.add(new BooleanComponent(boolValue));
            }else if (value instanceof ModeValue modeSetting) {
                settings.add(new ModeComponent(modeSetting));
            }else if (value instanceof NumberValue numberSetting) {
                settings.add(new SliderComponent(numberSetting));
            }else if (value instanceof ColorValue colorSetting) {
                settings.add(new ColorPickerComponent(colorSetting));
            }else if (value instanceof MultiBooleanValue enumSetting) {
                settings.add(new MultiBooleanComponent(enumSetting));
            }else if (value instanceof TextValue textValue) {
                settings.add(new StringComponent(textValue));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        float yOffset = 19;
        openAnimation.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);
        toggleAnimation.setDirection(module.getState() ? Direction.FORWARDS : Direction.BACKWARDS);
        hoverAnimation.setDirection(isHovered(mouseX, mouseY) ? Direction.FORWARDS : Direction.BACKWARDS);

        RenderUtil.drawRect(x,y,width,yOffset, ColorUtil.applyOpacity(InterFace.mainColor.get().getRGB(), (float) toggleAnimation.getOutput().floatValue()));

        FontManager.Bold.get((float) (14 - 1 * hoverAnimation.getOutput())).drawCenteredString(module.getName(), x + getWidth() / 2, y + yOffset / 2 - 3 + 0.5 * hoverAnimation.getOutput(), new Color(234, 234, 234).getRGB());

        for (Component component : settings) {
            if (!component.isVisible()) continue;
            component.setX(x);
            component.setY((float) (y + 2 + yOffset * openAnimation.getOutput()));
            component.setWidth(width);
            if (openAnimation.getOutput() > .7f) {
                component.drawScreen(mouseX, mouseY);
            }
            yOffset += (float) (component.getHeight() * openAnimation.getOutput());
            this.height = yOffset ;
        }

        IComponent.super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovered(mouseX, mouseY)) {
            switch (mouseButton) {
                case 0 -> module.toggle();
                case 1 -> opened = !opened;
            }
        }
        if (opened && !isHovered(mouseX, mouseY)) {
            settings.forEach(setting -> setting.mouseClicked(mouseX, mouseY, mouseButton));
        }
        IComponent.super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (opened && !isHovered(mouseX, mouseY)) {
            settings.forEach(setting -> setting.mouseReleased(mouseX, mouseY, state));
        }
        IComponent.super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (opened) {
            settings.forEach(setting -> setting.keyTyped(typedChar, keyCode));
        }
        IComponent.super.keyTyped(typedChar, keyCode);
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return RenderUtil.isHovering(x + 2, y, width - 2, 17, mouseX, mouseY);
    }
}
