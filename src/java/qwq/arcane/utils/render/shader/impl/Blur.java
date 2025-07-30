/*
 * MoonLight Hacked Client
 *
 * A free and open-source hacked client for Minecraft.
 * Developed using Minecraft's resources.
 *
 * Repository: https://github.com/randomguy3725/MoonLight
 *
 * Author(s): [Randumbguy & opZywl & lucas]
 */
package qwq.arcane.utils.render.shader.impl;

import qwq.arcane.utils.Instance;
import qwq.arcane.utils.math.MathUtils;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.StencilUtils;
import qwq.arcane.utils.render.shader.ShaderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

import static net.minecraft.client.renderer.OpenGlHelper.glUniform1;


public class Blur implements Instance {

    private static final ShaderUtils gaussianBlur = new ShaderUtils("gaussianBlur");
    private static Framebuffer framebuffer = new Framebuffer(1, 1, false);
    private static void setupUniforms(float dir1, float dir2, float radius) {
        gaussianBlur.setUniformi("textureIn", 0);
        gaussianBlur.setUniformf("texelSize", 1.0F / (float) mc.displayWidth, 1.0F / (float) mc.displayHeight);
        gaussianBlur.setUniformf("direction", dir1, dir2);
        gaussianBlur.setUniformf("radius", radius);

        final FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(256);
        for (int i = 0; i <= radius; i++) {
            weightBuffer.put(MathUtils.calculateGaussianValue(i, radius / 2));
        }

        weightBuffer.rewind();
        glUniform1(gaussianBlur.getUniform("weights"), weightBuffer);
    }

    public static void startBlur(){
        StencilUtils.initStencilToWrite();
    }
    public static void endBlur(float radius, float compression) {
        StencilUtils.readStencilBuffer(1);

        framebuffer = RenderUtil.createFrameBuffer(framebuffer);

        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(false);
        gaussianBlur.init();
        setupUniforms(compression, 0, radius);

        RenderUtil.bindTexture(mc.getFramebuffer().framebufferTexture);
        ShaderUtils.drawQuads();
        framebuffer.unbindFramebuffer();
        gaussianBlur.unload();

        mc.getFramebuffer().bindFramebuffer(false);
        gaussianBlur.init();
        setupUniforms(0, compression, radius);

        RenderUtil.bindTexture(framebuffer.framebufferTexture);
        ShaderUtils.drawQuads();
        gaussianBlur.unload();

        StencilUtils.uninitStencilBuffer();
        RenderUtil.resetColor();
        GlStateManager.bindTexture(0);

    }

    public static void renderBlur(float radius) {
        mc.getFramebuffer().bindFramebuffer(true);

        GlStateManager.enableBlend();
        GL11.glDisable(2929);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        update(framebuffer);
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);
        gaussianBlur.init();
        setupUniforms(1.0f, 0.0f, radius);
        RenderUtil.bindTexture(mc.getFramebuffer().framebufferTexture);
        ShaderUtils.drawQuads();
        framebuffer.unbindFramebuffer();
        gaussianBlur.unload();
        mc.getFramebuffer().bindFramebuffer(false);
        gaussianBlur.init();
        setupUniforms(0.0f, 1.0f, radius);
        RenderUtil.bindTexture(framebuffer.framebufferTexture);
        ShaderUtils.drawQuads();
        gaussianBlur.unload();
        GlStateManager.resetColor();
        GlStateManager.bindTexture(0);
        GL11.glEnable(2929);
    }

    public static void update(Framebuffer framebuffer) {
        if (framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            framebuffer.createBindFramebuffer(mc.displayWidth, mc.displayHeight);
        }
    }
}
