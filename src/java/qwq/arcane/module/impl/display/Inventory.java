package qwq.arcane.module.impl.display;


import qwq.arcane.event.impl.events.render.Shader2DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.ModuleWidget;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.value.impl.ModeValue;
import qwq.arcane.value.impl.NumberValue;
import net.minecraft.item.ItemStack;

import java.awt.*;

/**
 * @Author：Guyuemang
 * @Date：2025/6/2 13:35
 */

public class Inventory extends ModuleWidget {
    public Inventory() {
        super("Inventory",Category.Display);
    }
    public ModeValue modeValue = new ModeValue("Mode", "Custom",new String[]{"Custom","Solitude","Normal"});

    @Override
    public void onShader(Shader2DEvent event) {
        float x = renderX;
        float y = renderY;
        float itemWidth = 14;
        switch (modeValue.getValue()) {
            case "Solitude":
                RoundedUtil.drawRound(x, y, 135, 71,11, new Color(0, 0, 0, 255));
                break;
            case "Custom":
                RoundedUtil.drawRound(x, y, 130, 66,6, new Color(255, 255, 255, 255));
                break;
            case "Normal":
                RoundedUtil.drawRound(x,y, itemWidth + 120, 65, INTERFACE.radius.get().intValue(), new Color(0, 0, 0, 255));
                break;
        }
    }
    @Override
    public void render() {
        float x = renderX;
        float y = renderY;
        float itemWidth = 14;
        float itemHeight = 14;
        float y1 = 17.0F;
        float x1 = 0.7F;
        float x3 = renderX;
        float y3 = renderY;
        float itemWidth3 = 14;
        float itemHeight3 = 14;
        float y13 = 17.0F;
        float x13 = 0.7F;
        switch (modeValue.getValue()) {
            case "Solitude":
                RoundedUtil.drawRound(x, y, 135, 71,11, new Color(0, 0, 0, 190));
                FontManager.Bold.get(20).drawString("Inventory", x + 5, y + 5, -1);
                for (int i = 9; i < 36; ++i) {
                    ItemStack slot = mc.thePlayer.inventory.getStackInSlot(i);
                    RenderUtil.renderItemStack(slot, x + 2.7F, y + 20, 0.80F);
                    x += itemWidth;
                    x += x1;
                    if (i == 17) {
                        y += y1 - 1;
                        x -= itemWidth * 9.0F;
                        x -= x1 * 8.5F;
                    }

                    if (i == 26) {
                        y += y1 - 1;
                        x -= itemWidth * 9.0F;
                        x -= x1 * 9.0F;
                    }
                }

                width = (itemWidth * 9.1F + x1 * 9.0F);
                height = (itemHeight * 3.0F + 19.0F);
                break;
            case "Custom":
                RoundedUtil.drawRound(x, y, 130, 66,6, new Color(255, 255, 255, 80));
                RenderUtil.startGlScissor((int) (x - 2), (int) (y + 52), 134, 20);
                RoundedUtil.drawRound(x, y + 44, 130, 22,6, new Color(255, 255, 255, 100));
                RenderUtil.stopGlScissor();
                Bold.get(18).drawCenteredString("Inventory",x + 26,y + 55, new Color(255, 255, 255).getRGB());
                for (int i = 9; i < 36; ++i) {
                    ItemStack slot = mc.thePlayer.inventory.getStackInSlot(i);
                    RenderUtil.renderItemStack(slot, x + 0.7F, y + 4, 0.80F);
                    x += itemWidth;
                    x += x1;
                    if (i == 17) {
                        y += y1 - 1;
                        x -= itemWidth * 9.0F;
                        x -= x1 * 8.5F;
                    }

                    if (i == 26) {
                        y += y1 - 1;
                        x -= itemWidth * 9.0F;
                        x -= x1 * 9.0F;
                    }
                }
                width = (itemWidth * 9.1F + x1 * 9.0F);
                height = (itemHeight * 3.0F + 19.0F);
                break;
            case "Normal":
                RoundedUtil.drawRound(x,y, itemWidth + 120, 65, INTERFACE.radius.get().intValue(), new Color(0, 0, 0, 89));
                RenderUtil.startGlScissor((int) (x - 2), (int) (y - 1), 159, 18);
                RoundedUtil.drawRound(x,y, itemWidth + 120, 29, INTERFACE.radius.get().intValue(), ColorUtil.applyOpacity(new Color(setting.colors(1)), (float) 0.3f));
                RenderUtil.stopGlScissor();
                FontManager.Bold.get(18).drawString("Inventory",x + 5,y + 5,-1);
                for (int i = 9; i < 36; ++i) {
                    ItemStack slot = mc.thePlayer.inventory.getStackInSlot(i);
                    RenderUtil.renderItemStack(slot, x + 0.7F, y + 17.5F, 0.80F);
                    x += itemWidth;
                    x += x1;
                    if (i == 17) {
                        y += y1 - 1;
                        x -= itemWidth * 9.0F;
                        x -= x1 * 8.5F;
                    }

                    if (i == 26) {
                        y += y1 - 1;
                        x -= itemWidth * 9.0F;
                        x -= x1 * 9.0F;
                    }
                }
                width = (itemWidth * 9.1F + x1 * 9.0F);
                height = (itemHeight * 3.0F + 19.0F);
            break;
        }
    }

    @Override
    public boolean shouldRender() {
        return getState() && setting.getState();
    }
}
