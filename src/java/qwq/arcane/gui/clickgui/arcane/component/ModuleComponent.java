package qwq.arcane.gui.clickgui.arcane.component;


import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import qwq.arcane.gui.clickgui.Component;
import qwq.arcane.gui.clickgui.arcane.component.settings.*;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.animations.Animation;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.DecelerateAnimation;
import qwq.arcane.module.Module;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.value.Value;
import qwq.arcane.value.impl.*;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 22:47
 */

@Getter
public class ModuleComponent extends Component {
    private final Module module;
    @Setter
    private int scroll = 0;
    @Setter
    private boolean left = true;
    private final ObjectArrayList<Component> components = new ObjectArrayList<>();
    private final Animation enabled = new DecelerateAnimation(250,1);
    private final Animation hover = new DecelerateAnimation(250,1);
    public ModuleComponent(Module module) {
        this.module = module;
        for (Value setting : module.getSettings()) {
            if (setting instanceof BoolValue bool) {
                components.add(new BooleanComponent(bool));
            }else if (setting instanceof NumberValue number) {
                components.add(new NumberComponent(number));
            }else if (setting instanceof ModeValue modeValue) {
                components.add(new ModeComponent(modeValue));
            }else if (setting instanceof MultiBooleanValue booleanValue) {
                components.add(new MultiBoxComponent(booleanValue));
            }else if (setting instanceof ColorValue colorValue) {
                components.add(new ColorPickerComponent(colorValue));
            }else if (setting instanceof TextValue textValue) {
                components.add(new StringComponent(textValue));
            }
        }
        enabled.setDirection(module.getState() ? Direction.FORWARDS : Direction.BACKWARDS);
    }
    @Override
    public void drawScreen(int mouseX, int mouseY) {
        float y = getY() + 6 + scroll;
        enabled.setDirection(module.getState() ? Direction.FORWARDS : Direction.BACKWARDS);
        hover.setDirection(RenderUtil.isHovering(getX() + 135, y + 4, 22, 12,mouseX,mouseY) ? Direction.FORWARDS : Direction.BACKWARDS);
        Animation moduleAnimation = module.getAnimations();
        moduleAnimation.setDirection(module.getState() ? Direction.FORWARDS : Direction.BACKWARDS);
        if (!module.getState() && moduleAnimation.finished(Direction.BACKWARDS));

        RoundedUtil.drawRound(getX(), y, 165, getHeight(), 2, INSTANCE.getArcaneClickGui().backgroundColor);
        FontManager.Bold.get(18).drawString(module.name + " Module", getX() + 10, y + 5,ColorUtil.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(),0.6f));
        RoundedUtil.drawRound(getX() + 135, y + 4, 20, 10, 4, ColorUtil.applyOpacity(InterFace.color(1),0.4f));
        RenderUtil.drawCircleCGUI(getX() + 141 + moduleAnimation.getOutput().floatValue() * 9f, y + 9, 8, InterFace.color(1).darker().getRGB());
        RoundedUtil.drawRound(getX() + 10, y + 20, 145, 1, 0, INSTANCE.getArcaneClickGui().linecolor);

        float componentY = y + 22;
        ObjectArrayList<Component> filtered = components.stream()
                .filter(Component::isVisible)
                .collect(ObjectArrayList::new, ObjectArrayList::add, ObjectArrayList::addAll);
        for (Component component : filtered) {
            component.setX(getX());
            component.setY(componentY);
            component.drawScreen(mouseX, mouseY);
            componentY += component.getHeight();
        }
    }
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float y = getY() + 6 + scroll;
        if (RenderUtil.isHovering(getX() + 135, y + 4, 20, 10,mouseX,mouseY) && mouseButton == 0){
            module.toggle();
        }
        for (Component component : components) {
            component.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        for (Component component : components) {
            component.mouseReleased(mouseX, mouseY, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        for (Component component : components) {
            component.keyTyped(typedChar, keyCode);
        }
        super.keyTyped(typedChar, keyCode);
    }
    public int getMaxScroll() {
        return (int) (((getY() - INSTANCE.getArcaneClickGui().getY()) + getHeight()) * 4);
    }
}
