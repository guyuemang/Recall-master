package qwq.arcane.utils.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import qwq.arcane.utils.Instance;
import qwq.arcane.utils.color.ColorUtil;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class RoundedUtil implements Instance {

    public static ShaderUtils roundedShader = new ShaderUtils("roundedRect");
    public static ShaderUtils roundedOutlineShader = new ShaderUtils("roundRectOutline");
    private static final ShaderUtils roundedTexturedShader = new ShaderUtils("roundRectTexture");
    private static final ShaderUtils roundedGradientShader = new ShaderUtils("roundedRectGradient");
    public static void scissor(final double x, final double y, final double width, final double height) {
        int scaleFactor = 1;
        while (scaleFactor < 2 && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        GL11.glScissor((int) (x * scaleFactor),
                (int) (Minecraft.getMinecraft().displayHeight - (y + height) * scaleFactor),
                (int) (width * scaleFactor), (int) (height * scaleFactor));
    }
    public static Color reAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
    public static void drawCircle(float x, float y, float start, float end, float radius, float width, boolean filled, int color) {
        float i;
        float endOffset;
        if (start > end) {
            endOffset = end;
            end = start;
            start = endOffset;
        }

        GlStateManager.enableBlend();
        GL11.glDisable(GL_TEXTURE_2D);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(width);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (i = end; i >= start; i--) {
            setColor(color);
            float cos = MathHelper.cos((float) (i * Math.PI / 180)) * radius;
            float sin = MathHelper.sin((float) (i * Math.PI / 180)) * radius;
            GL11.glVertex2f(x + cos, y + sin);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);

        if (filled) {
            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            for (i = end; i >= start; i--) {
                setColor(color);
                float cos = MathHelper.cos((float) (i * Math.PI / 180)) * radius;
                float sin = MathHelper.sin((float) (i * Math.PI / 180)) * radius;
                GL11.glVertex2f(x + cos, y + sin);
            }
            GL11.glEnd();
        }

        GL11.glEnable(GL_TEXTURE_2D);
        GlStateManager.disableBlend();
        resetColor();
    }
    public static void color(double red, double green, double blue, double alpha) {
        GL11.glColor4d(red, green, blue, alpha);
    }
    public static void setColor(int color) {
        GL11.glColor4ub((byte) (color >> 16 & 0xFF), (byte) (color >> 8 & 0xFF), (byte) (color & 0xFF), (byte) (color >> 24 & 0xFF));
    }
    public static void resetColor() {
        color(1, 1, 1, 1);
    }
    public static void drawRect(float left, float top, float width, float height, int color) {
        float right = left + width, bottom = top + height;
        if (left < right) {
            float i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            float j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    public static void drawRound(float x, float y, float width, float height, float radius, Color color) {
        drawRound(x, y, width, height, radius, false, color);
    }

    public static void drawGradientHorizontal(float x, float y, float width, float height, float radius, Color left, Color right) {
        drawGradientRound(x, y, width, height, radius, left, left, right, right);
    }
    public static void drawGradientVertical(float x, float y, float width, float height, float radius, Color top, Color bottom) {
        drawGradientRound(x, y, width, height, radius, bottom, top, bottom, top);
    }
    public static void drawGradientCornerLR(float x, float y, float width, float height, float radius, Color topLeft, Color bottomRight) {
        Color mixedColor = ColorUtil.interpolateColorC(topLeft, bottomRight, .5f);
        drawGradientRound(x, y, width, height, radius, mixedColor, topLeft, bottomRight, mixedColor);
    }

    public static void drawGradientCornerRL(float x, float y, float width, float height, float radius, Color bottomLeft, Color topRight) {
        Color mixedColor = ColorUtil.interpolateColorC(topRight, bottomLeft, .5f);
        drawGradientRound(x, y, width, height, radius, bottomLeft, mixedColor, mixedColor, topRight);
    }

    public static void drawGradientRound(float x, float y, float width, float height, float radius, Color bottomLeft, Color topLeft, Color bottomRight, Color topRight) {
        RenderUtil.setAlphaLimit(0);
        RenderUtil.resetColor();
        GLUtil.startBlend();
        roundedGradientShader.init();
        setupRoundedRectUniforms(x, y, width, height, radius, roundedGradientShader);
        //Top left
        roundedGradientShader.setUniformf("color1", topLeft.getRed() / 255f, topLeft.getGreen() / 255f, topLeft.getBlue() / 255f, topLeft.getAlpha() / 255f);
        // Bottom Left
        roundedGradientShader.setUniformf("color2", bottomLeft.getRed() / 255f, bottomLeft.getGreen() / 255f, bottomLeft.getBlue() / 255f, bottomLeft.getAlpha() / 255f);
        //Top Right
        roundedGradientShader.setUniformf("color3", topRight.getRed() / 255f, topRight.getGreen() / 255f, topRight.getBlue() / 255f, topRight.getAlpha() / 255f);
        //Bottom Right
        roundedGradientShader.setUniformf("color4", bottomRight.getRed() / 255f, bottomRight.getGreen() / 255f, bottomRight.getBlue() / 255f, bottomRight.getAlpha() / 255f);
        ShaderUtils.drawQuads(x - 1, y - 1, width + 2, height + 2);
        roundedGradientShader.unload();
        GLUtil.endBlend();
    }




    public static void drawRound(float x, float y, float width, float height, float radius, boolean blur, Color color) {
        RenderUtil.resetColor();
        GLUtil.startBlend();
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        RenderUtil.setAlphaLimit(0);
        roundedShader.init();

        setupRoundedRectUniforms(x, y, width, height, radius, roundedShader);
        roundedShader.setUniformi("blur", blur ? 1 : 0);
        roundedShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

        ShaderUtils.drawQuads(x - 1, y - 1, width + 2, height + 2);
        roundedShader.unload();
        GLUtil.endBlend();
    }


    public static void drawRoundOutline(float x, float y, float width, float height, float radius, float outlineThickness, Color color, Color outlineColor) {
        RenderUtil.resetColor();
        GLUtil.startBlend();
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        RenderUtil.setAlphaLimit(0);
        roundedOutlineShader.init();

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        setupRoundedRectUniforms(x, y, width, height, radius, roundedOutlineShader);
        roundedOutlineShader.setUniformf("outlineThickness", outlineThickness * sr.getScaleFactor());
        roundedOutlineShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        roundedOutlineShader.setUniformf("outlineColor", outlineColor.getRed() / 255f, outlineColor.getGreen() / 255f, outlineColor.getBlue() / 255f, outlineColor.getAlpha() / 255f);


        ShaderUtils.drawQuads(x - (2 + outlineThickness), y - (2 + outlineThickness), width + (4 + outlineThickness * 2), height + (4 + outlineThickness * 2));
        roundedOutlineShader.unload();
        GLUtil.endBlend();
    }


    public static void drawRoundTextured(float x, float y, float width, float height, float radius, float alpha) {
        RenderUtil.resetColor();
        RenderUtil.setAlphaLimit(0);
        GLUtil.startBlend();
        roundedTexturedShader.init();
        roundedTexturedShader.setUniformi("textureIn", 0);
        setupRoundedRectUniforms(x, y, width, height, radius, roundedTexturedShader);
        roundedTexturedShader.setUniformf("alpha", alpha);
        ShaderUtils.drawQuads(x - 1, y - 1, width + 2, height + 2);
        roundedTexturedShader.unload();
        GLUtil.endBlend();
    }

    private static void setupRoundedRectUniforms(float x, float y, float width, float height, float radius, ShaderUtils roundedTexturedShader) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        roundedTexturedShader.setUniformf("location", x * sr.getScaleFactor(),
                (Minecraft.getMinecraft().displayHeight - (height * sr.getScaleFactor())) - (y * sr.getScaleFactor()));
        roundedTexturedShader.setUniformf("rectSize", width * sr.getScaleFactor(), height * sr.getScaleFactor());
        roundedTexturedShader.setUniformf("radius", radius * sr.getScaleFactor());
    }


}
