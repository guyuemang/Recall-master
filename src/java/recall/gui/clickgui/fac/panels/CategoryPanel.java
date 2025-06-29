package recall.gui.clickgui.fac.panels;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import recall.Client;
import recall.gui.clickgui.Component;
import recall.gui.clickgui.IComponent;
import recall.gui.clickgui.fac.components.ModuleComponent;
import recall.module.Category;
import recall.module.Module;
import recall.utils.animations.Animation;
import recall.utils.animations.Direction;
import recall.utils.animations.impl.DecelerateAnimation;
import recall.utils.animations.impl.SmoothStepAnimation;
import recall.utils.math.MathUtils;
import recall.utils.render.RenderUtil;

/**
 * @Author：Guyuemang
 * @Date：2025/6/29 13:56
 */
@Getter
public class CategoryPanel implements IComponent {
    private int posX, posY;
    private float maxScroll = Float.MAX_VALUE, rawScroll, scroll;
    private Animation scrollAnimation = new SmoothStepAnimation(0, 0, Direction.BACKWARDS);
    private final Category category;
    @Setter
    private boolean selected;
    private final ObjectArrayList<ModuleComponent> moduleComponents = new ObjectArrayList<>();
    private final Animation animation = new DecelerateAnimation(250,1);

    public CategoryPanel(Category category) {
        this.category = category;
        for (Module module : INSTANCE.getModuleManager().getAllModules()){
            if (module.getCategory().equals(this.category)){
                moduleComponents.add(new ModuleComponent(module));
            }
        }
    }
    @Override
    public void drawScreen(int mouseX, int mouseY) {
        //update coordinate
        posX = INSTANCE.getFatalityClickGui().getX();
        posY = INSTANCE.getFatalityClickGui().getY();
        //select anim
        animation.setDirection(selected ? Direction.FORWARDS : Direction.BACKWARDS);
        //render module components
        if (isSelected()) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            RenderUtil.scissor(getPosX(), getPosY() + 39, Client.Instance.getFatalityClickGui().getW(), 376);

            float left = 0, middle = 0, right = 0;
            for (int i = 0; i < moduleComponents.size(); i++) {
                ModuleComponent module = moduleComponents.get(i);
                float componentOffset = getComponentOffset(i, left, middle, right);
                module.drawScreen(mouseX, mouseY);
                double scroll = getScroll();
                module.setScroll((int) MathUtils.roundToHalf(scroll));
                onScroll(30, mouseX, mouseY);
                maxScroll = Math.max(0, moduleComponents.isEmpty() ? 0 : moduleComponents.get(moduleComponents.size() - 1).getMaxScroll());
                int position = i % 3;
                if (position == 0) {
                    left += 35 + componentOffset;
                } else if (position == 1) {
                    middle += 35 + componentOffset;
                } else {
                    right += 35 + componentOffset;
                }
            }

            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
        IComponent.super.drawScreen(mouseX, mouseY);
    }
    private float getComponentOffset(int i, float left, float middle, float right) {
        ModuleComponent component = moduleComponents.get(i);
        int position = i % 3;
        component.setPosition(position);
        if (position == 0) {
            component.setX(posX + 10);
        } else if (position == 1) {
            component.setX(posX + 255);
        } else {
            component.setX(posX + 495);
        }
        component.setHeight(30);
        float yOffset;
        if (position == 0) {
            yOffset = left;
        } else if (position == 1) {
            yOffset = middle;
        } else {
            yOffset = right;
        }
        component.setY(posY + 21 + component.getHeight() - 10 + yOffset);

        float componentOffset = 0;
        for (Component component2 : component.getComponents()) {
            if (component2.isVisible())
                componentOffset += component2.getHeight();
        }
        component.setHeight(component.getHeight() + componentOffset);
        return componentOffset;
    }
    public void onScroll(int ms, int mx, int my) {
        scroll = (float) (rawScroll - scrollAnimation.getOutput());
        if (RenderUtil.isHovering(getPosX() + 120, getPosY() + 39, 416, 376, mx, my) && moduleComponents.stream().noneMatch(moduleComponent -> moduleComponent.getComponents().stream().anyMatch(component -> component.isHovered(mx,my)))) {
            rawScroll += (float) Mouse.getDWheel();
        }
        rawScroll = Math.max(Math.min(0, rawScroll), -maxScroll);
        scrollAnimation = new SmoothStepAnimation(ms, rawScroll - scroll, Direction.BACKWARDS);
    }
    public float getScroll() {
        scroll = (float) (rawScroll - scrollAnimation.getOutput());
        return scroll;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isSelected()) {
            moduleComponents.forEach(moduleComponent -> moduleComponent.mouseClicked(mouseX,mouseY,mouseButton));
        }
        IComponent.super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (isSelected()) {
            moduleComponents.forEach(moduleComponent -> moduleComponent.mouseReleased(mouseX,mouseY,state));
        }
        IComponent.super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (isSelected()) {
            moduleComponents.forEach(moduleComponent -> moduleComponent.keyTyped(typedChar,keyCode));
        }
        IComponent.super.keyTyped(typedChar, keyCode);
    }
}
