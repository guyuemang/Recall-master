package recall.gui.clickgui.fac.components;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Setter;
import recall.Client;
import recall.gui.clickgui.Component;
import lombok.Getter;
import recall.gui.clickgui.fac.components.setting.impl.BooleanComponent;
import recall.module.Module;
import recall.utils.fontrender.FontManager;
import recall.utils.render.RenderUtil;
import recall.utils.render.RoundedUtil;
import recall.value.Value;
import recall.value.impl.BooleanValue;

import java.awt.*;

/**
 * @Author：Guyuemang
 * @Date：2025/6/29 16:18
 */
@Getter
public class ModuleComponent extends Component {
    private final Module module;
    private final ObjectArrayList<Component> components = new ObjectArrayList<>();
    @Setter
    private int scroll = 0;
    @Setter
    private boolean left = true;
    @Getter @Setter
    private int position;

    public ModuleComponent(Module module) {
        this.module = module;
        for (Value setting : module.getSettings()) {
            if (setting instanceof BooleanValue bool) {
                components.add(new BooleanComponent(bool));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        float y = getY() + 12 + scroll;
        RoundedUtil.drawRoundOutline(getX(), y, 235,getHeight(), 4,0.01f, new Color(226, 228, 232, 255), new Color(0xF2F6FC));
        FontManager.Semibold.get(20).drawString(module.name, getX() + 15, y - 6, Client.Instance.getFatalityClickGui().font1color.getRGB());
        FontManager.Semibold.get(20).drawString("Enable", getX() + 15, y + 12, Client.Instance.getFatalityClickGui().font1color.getRGB());
        RoundedUtil.drawRound(getX() + 210, y + 10, 15,15, 3,new Color(0xF2F6FC));
        if (module.State){
            FontManager.Icon.get(18).drawString("P", getX() + 213, y + 16, Client.Instance.getFatalityClickGui().font2color.getRGB());
        }
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
        if (RenderUtil.isHovering(getX() + 210, getY() + 12 + scroll + 10, 15,15,mouseX,mouseY) && mouseButton == 0){
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
        return (int) (((getY() - INSTANCE.getFatalityClickGui().getY()) + getHeight()) * 4);
    }
}
