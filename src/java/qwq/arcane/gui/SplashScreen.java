package qwq.arcane.gui;


import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.Instance;
import qwq.arcane.utils.animations.Animation;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.DecelerateAnimation;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;

import java.awt.*;

/**
 * @Author：Guyuemang
 * @Date：2025/6/28 23:23
 */

public class SplashScreen implements Instance {
    public static Animation progressAnim;
    public static boolean menu;
    public static Animation animation = new DecelerateAnimation(250, 1);
    private static Framebuffer framebuffer;
    private static Animation progress2Anim;
    private static Animation progress3Anim;
    private static Animation progress4Anim;

    private static int count;

    public static void continueCount() {
        continueCount(true);
    }

    public static void continueCount(boolean continueCount) {
        drawScreen();
        if(continueCount){
            count++;
        }
    }

    public static void drawScreen() {
        ScaledResolution sr = new ScaledResolution(mc);
        int scaleFactor = sr.getScaleFactor();
        // Create the scale factor
        // Bind the width and height to the framebuffer
        framebuffer = RenderUtil.createFrameBuffer(framebuffer);
        progressAnim = new DecelerateAnimation(7000, 1);
        progress2Anim = new DecelerateAnimation(5000, 1);
        progress3Anim = new DecelerateAnimation(400, 1).setDirection(Direction.BACKWARDS);
        progress4Anim = new DecelerateAnimation(5000, 1).setDirection(Direction.BACKWARDS);

        while (!progressAnim.isDone()) {
            framebuffer.framebufferClear();
            framebuffer.bindFramebuffer(true);
            // Create the projected image to be rendered
            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, sr.getScaledWidth(), sr.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            GlStateManager.disableDepth();
            GlStateManager.enableTexture2D();


            GlStateManager.color(0, 0, 0, 0);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            drawScreen(sr.getScaledWidth(), sr.getScaledHeight());

            // Unbind the width and height as it's no longer needed
            framebuffer.unbindFramebuffer();

            // Render the previously used frame buffer
            framebuffer.framebufferRender(sr.getScaledWidth() * scaleFactor, sr.getScaledHeight() * scaleFactor);

            // Update the texture to enable alpha drawing
            RenderUtil.setAlphaLimit(1);

            // Update the users screen
            mc.updateDisplay();
        }
    }

    private static void drawScreen(float width, float height) {
        animation.setDirection(progressAnim.getOutput().floatValue() >= 0.5 ? Direction.FORWARDS : Direction.BACKWARDS);
        float progress = progress2Anim.getOutput().floatValue();
        RoundedUtil.drawGradientHorizontal(0,0,width,height,0,ColorUtil.applyOpacity(InterFace.mainColor.get(),0.2f + 0.2f * animation.getOutput().floatValue()),ColorUtil.applyOpacity(InterFace.secondColor.get(),0.2f + 0.2f * animation.getOutput().floatValue()));

        float aWidth = FontManager.Bold.get(80).getStringWidth("A");

       if (progress2Anim.getOutput().floatValue() >= 0.99f) {
            progress3Anim.setDirection(Direction.FORWARDS);
            progress4Anim.setDirection(Direction.FORWARDS);

            FontManager.Bold.get(80).drawString("A", 5 + width / 2 - aWidth / 2 - (FontManager.Bold.get(80).getStringWidth("rcane") / 2) * progress3Anim.getOutput() - 5,7 + height / 2 - 50, ColorUtil.applyOpacity(InterFace.color(1),1).getRGB());
            FontManager.Bold.get(80).drawString("rcane",width / 2 - (FontManager.Bold.get(80).getStringWidth("rcane") / 2) + aWidth / 2, 7 + height / 2 - 50 - 120 * progress3Anim.getOutput() + 120, ColorUtil.applyOpacity(-1,progress4Anim.getOutput().floatValue()));
        } else {
            FontManager.Bold.get(80).drawStringDynamic("A",5 + width / 2 - aWidth / 2, height / 2 - 50 + 7 , -1, 7);
        }

        RoundedUtil.drawRound(width / 2 - 170 / 2, height / 2 + 15, 170, 5, 3, new Color(221, 228, 255));
        RoundedUtil.drawGradientHorizontal(width / 2 - 170 / 2, height / 2 + 15, 170 * progress, 5, 3, InterFace.color(1),InterFace.color(7));
    }
}
