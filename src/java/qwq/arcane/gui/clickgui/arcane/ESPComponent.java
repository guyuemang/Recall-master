package qwq.arcane.gui.clickgui.arcane;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import qwq.arcane.Client;
import qwq.arcane.gui.clickgui.Component;
import qwq.arcane.module.impl.visuals.ESP;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.math.MathUtils;
import qwq.arcane.utils.render.GLUtil;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.gui.inventory.GuiInventory.drawEntityOnScreen;
import static org.lwjgl.opengl.GL11.*;

/**
 * @Author：Guyuemang
 * @Date：7/6/2025 3:42 PM
 */
@Getter
public class ESPComponent extends Component {
    private int posX = 90, posY = 50, dragX, dragY;
    private boolean dragging = false;

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        if (dragging) {
            posX = mouseX + dragX;
            posY = mouseY + dragY;
        }
        RoundedUtil.drawRound(posX - 10, posY, 120, 200, 7, ColorUtil.applyOpacity(INSTANCE.getArcaneClickGui().backgroundColor,0.6f));
        GlStateManager.pushMatrix();
        drawEntityOnScreen((int) posX + 50, (int) posY + 180, 80, 0, 0, mc.thePlayer);
        GlStateManager.popMatrix();
        if (INSTANCE.getModuleManager().getModule(ESP.class).box.get()) {
            float x = posX + 5;
            float y = (float) (posY + 20);
            float x2 = x + 90;
            float y2 = y + 170;
            glDisable(GL_TEXTURE_2D);
            GLUtil.startBlend();

            glColor4ub((byte) 0, (byte) 0, (byte) 0, (byte) 0x96);
            glBegin(GL_QUADS);

            // Background
            {
                // Left
                glVertex2f(x, y);
                glVertex2f(x, y2);
                glVertex2f(x + 1.5F, y2);
                glVertex2f(x + 1.5F, y);

                // Right
                glVertex2f(x2 - 1.5F, y);
                glVertex2f(x2 - 1.5F, y2);
                glVertex2f(x2, y2);
                glVertex2f(x2, y);

                // Top
                glVertex2f(x + 1.5F, y);
                glVertex2f(x + 1.5F, y + 1.5F);
                glVertex2f(x2 - 1.5F, y + 1.5F);
                glVertex2f(x2 - 1.5F, y);

                // Bottom
                glVertex2f(x + 1.5F, y2 - 1.5F);
                glVertex2f(x + 1.5F, y2);
                glVertex2f(x2 - 1.5F, y2);
                glVertex2f(x2 - 1.5F, y2 - 1.5F);
            }

            if (ESP.boxSyncColor.get()) {
                ESP.color(Client.Instance.getModuleManager().getModule(InterFace.class).color(7).getRGB());
            } else {
                ESP.color(ESP.boxColor.get().getRGB());
            }

            // Box
            {
                // Left
                glVertex2f(x + 0.5F, y + 0.5F);
                glVertex2f(x + 0.5F, y2 - 0.5F);
                glVertex2f(x + 1, y2 - 0.5F);
                glVertex2f(x + 1, y + 0.5F);

                // Right
                glVertex2f(x2 - 1, y + 0.5F);
                glVertex2f(x2 - 1, y2 - 0.5F);
                glVertex2f(x2 - 0.5F, y2 - 0.5F);
                glVertex2f(x2 - 0.5F, y + 0.5F);

                // Top
                glVertex2f(x + 0.5F, y + 0.5F);
                glVertex2f(x + 0.5F, y + 1);
                glVertex2f(x2 - 0.5F, y + 1);
                glVertex2f(x2 - 0.5F, y + 0.5F);

                // Bottom
                glVertex2f(x + 0.5F, y2 - 1);
                glVertex2f(x + 0.5F, y2 - 0.5F);
                glVertex2f(x2 - 0.5F, y2 - 0.5F);
                glVertex2f(x2 - 0.5F, y2 - 1);
            }

            ESP.resetColor();

            glEnd();

            glEnable(GL_TEXTURE_2D);
            GLUtil.endBlend();
        }
        int x = posX + 4;
        int y = (int) posY + 20;
        float y2 = y + 170;

        if (INSTANCE.getModuleManager().getModule(ESP.class).healthBar.get()) {

            glDisable(GL_TEXTURE_2D);
            GLUtil.startBlend();

            float healthBarLeft = x - 2.5F;
            float healthBarRight = x - 0.5F;
            final float health = mc.thePlayer.getHealth();
            final float maxHealth = mc.thePlayer.getMaxHealth();
            final float healthPercentage = health / maxHealth;

            glColor4ub((byte) 0, (byte) 0, (byte) 0, (byte) 0x96);

            glBegin(GL_QUADS);

            // Background
            {
                glVertex2f(healthBarLeft, y);
                glVertex2f(healthBarLeft, y2);

                glVertex2f(healthBarRight, y2);
                glVertex2f(healthBarRight, y);
            }

            healthBarLeft += 0.5F;
            healthBarRight -= 0.5F;

            final float heightDif = y - y2;
            final float healthBarHeight = heightDif * healthPercentage;

            final float topOfHealthBar = y2 + 0.5F + healthBarHeight;

            if (ESP.healthBarSyncColor.get()) {
                final int syncedcolor = Client.Instance.getModuleManager().getModule(InterFace.class).color(1).getRGB();

                ESP.color(syncedcolor);
            } else {
                final int color = ColorUtil.getColorFromPercentage(healthPercentage);

                ESP.color(color);
            }

            // Bar
            {
                glVertex2f(healthBarLeft, topOfHealthBar);
                glVertex2f(healthBarLeft, y2 - 0.5F);

                glVertex2f(healthBarRight, y2 - 0.5F);
                glVertex2f(healthBarRight, topOfHealthBar);
            }


            final float absorption = mc.thePlayer.getAbsorptionAmount();

            final float absorptionPercentage = Math.min(1.0F, absorption / 20.0F);

            final int absorptionColor = INSTANCE.getModuleManager().getModule(ESP.class).absorptionColor.get().getRGB();

            final float absorptionHeight = heightDif * absorptionPercentage;

            final float topOfAbsorptionBar = y2 + 0.5F + absorptionHeight;

            if (ESP.healthBarSyncColor.get()) {
                ESP.color(Client.Instance.getModuleManager().getModule(InterFace.class).color(7).getRGB());
            } else {
                ESP.color(absorptionColor);
            }

            // Absorption Bar
            {
                glVertex2f(healthBarLeft, topOfAbsorptionBar);
                glVertex2f(healthBarLeft, y2 - 0.5F);

                glVertex2f(healthBarRight, y2 - 0.5F);
                glVertex2f(healthBarRight, topOfAbsorptionBar);
            }

            ESP.resetColor();

            glEnd();

            glEnable(GL_TEXTURE_2D);
            GLUtil.endBlend();
        }
        x = posX;
        y = posY;

        if (INSTANCE.getModuleManager().getModule(ESP.class).fontTags.get()) {
            final String healthString = ESP.fonttagsHealth.get() ? " |" + EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + " " + (MathUtils.roundToHalf(mc.thePlayer.getHealth())) + "❤" + EnumChatFormatting.RESET + "" : "";
            final String name = mc.thePlayer.getDisplayName().getFormattedText() + healthString;
            float halfWidth = (float) mc.fontRendererObj.getStringWidth(name) * 0.5f;
            final float middle = x + halfWidth;
            final float textHeight = mc.fontRendererObj.FONT_HEIGHT * 0.5f;
            float renderY = y + 12;

            final float left = middle - halfWidth - 1;
            final float right = middle + halfWidth + 1;
            if (ESP.fonttagsBackground.get()) {
                Gui.drawRect(left, renderY - 6, right, renderY + textHeight + 1, new Color(0, 0, 0, 50).getRGB());
            }

            mc.fontRendererObj.drawStringWithShadow(name, middle - halfWidth, renderY - 4f, -1);
        }
        super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (RenderUtil.isHovering(posX, posY, 100, 200, mouseX, mouseY) && mouseButton == 0) {
            dragging = true;
            dragX = posX - mouseX;
            dragY = posY - mouseY;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) {
            dragging = false;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }
}
