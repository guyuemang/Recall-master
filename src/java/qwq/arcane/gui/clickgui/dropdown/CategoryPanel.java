package qwq.arcane.gui.clickgui.dropdown;

import qwq.arcane.Client;
import qwq.arcane.gui.clickgui.IComponent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.Instance;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.EaseInOutQuad;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import qwq.arcane.utils.render.shader.KawaseBlur;

import java.awt.*;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 12:31
 */
@Getter
@Setter
public class CategoryPanel implements IComponent, Instance {
    private float x, y, dragX, dragY;
    private float width = 100, height;
    private final Category category;
    private boolean dragging, opened;
    private final ObjectArrayList<ModuleComponent> moduleComponents = new ObjectArrayList<>();
    private final EaseInOutQuad openAnimation = new EaseInOutQuad(250, 1);
    public static int i;
    public CategoryPanel(Category category) {
        this.category = category;
        this.openAnimation.setDirection(Direction.BACKWARDS);
        for (i = 0; i < (Client.Instance.getModuleManager().getModsByCategory(category).size()); ++i){
            Module module = Client.Instance.getModuleManager().getModsByCategory(category).get(i);
            moduleComponents.add(new ModuleComponent(module));
        }
    }
    @Override
    public void drawScreen(int mouseX, int mouseY) {
        update(mouseX, mouseY);

        KawaseBlur.startBlur();
        RoundedUtil.drawRound(x, y - 2, width, (float) (19 + ((height - 19) * openAnimation.getOutput())), 8,new Color(1,1,1));
        KawaseBlur.endBlur(6,2);
        RoundedUtil.drawRound(x, y - 2, width, (float) (19 + ((height - 19) * openAnimation.getOutput())), 8,new Color(1,1,1,120));

        FontManager.Bold.get(20).drawCenteredString(category.name(), x + width / 2, y + 4.5, -1);

        float componentOffsetY = 18;

        for (ModuleComponent component : moduleComponents) {
            component.setX(x);
            component.setY(y + componentOffsetY);
            component.setWidth(width);
            if (openAnimation.getOutput() > 0.7f) {
                component.drawScreen(mouseX, mouseY);
            }
            componentOffsetY += (float) (component.getHeight() * openAnimation.getOutput());
        }
        height = componentOffsetY + 8;

        IComponent.super.drawScreen(mouseX, mouseY);
    }
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (RenderUtil.isHovering(x, y - 2, width, 19, mouseX, mouseY)) {
            switch (mouseButton) {
                case 0 -> {
                    dragging = true;
                    dragX = x - mouseX;
                    dragY = y - mouseY;
                }
                case 1 -> opened = !opened;
            }
        }
        if (opened && !RenderUtil.isHovering(x, y - 2, width, 19, mouseX, mouseY)) {
            moduleComponents.forEach(component -> component.mouseClicked(mouseX, mouseY, mouseButton));
        }
        IComponent.super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        moduleComponents.forEach(component -> component.keyTyped(typedChar, keyCode));
        IComponent.super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) dragging = false;
        moduleComponents.forEach(component -> component.mouseReleased(mouseX, mouseY, state));
        IComponent.super.mouseReleased(mouseX, mouseY, state);
    }

    public void update(int mouseX, int mouseY) {
        this.openAnimation.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);

        if (dragging) {
            x = (mouseX + dragX);
            y = (mouseY + dragY);
        }
    }
}
