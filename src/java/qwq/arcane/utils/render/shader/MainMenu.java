package qwq.arcane.utils.render.shader;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import qwq.arcane.utils.Instance;
import qwq.arcane.utils.animations.AnimationUtils;
import qwq.arcane.utils.render.OSUtil;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.utils.render.shader.particle.ParticleEngine;

import java.awt.*;

public class MainMenu implements Instance {
    private static final ShaderUtils mainmenu = new ShaderUtils("mainmenu");

    // background(shader)
    public static float animation = 0f;
    static GLSLSandboxShader shader;
    static long initTime = System.currentTimeMillis();
    public static ParticleEngine particle = new ParticleEngine();

    static {
        try {
            shader = new GLSLSandboxShader("bg1.frag");
        } catch (Exception e) {
            OSUtil.supportShader = false;
        }
    }

    public static void draw(long initTime) {
        ScaledResolution sr = new ScaledResolution(mc);
            mainmenu.init();
            mainmenu.setUniformf("TIME", (float) (System.currentTimeMillis() - initTime) / 1000);
            mainmenu.setUniformf("RESOLUTION", (float) ((double) sr.getScaledWidth() * sr.getScaleFactor()), (float) ((double) sr.getScaledHeight() * sr.getScaleFactor()));
            ShaderUtils.drawFixedQuads();
            mainmenu.unload();
    }

    public static void drawBackground(int guiWidth, int guiHeight, int mouseX, int mouseY) {
        if (OSUtil.supportShader()) {
            if (mc.currentScreen instanceof qwq.arcane.gui.MainMenu) {
                animation = (float) AnimationUtils.base(animation, 1.0f, 0.035f);
            } else {
                animation = (float) AnimationUtils.base(animation, 1.85f, 0.035f);
            }

            GlStateManager.disableCull();
            shader.useShader(guiWidth * 2, guiHeight * 2, mouseX, mouseY, (System.currentTimeMillis() - initTime) / 1000f, animation);
            GL11.glBegin(GL11.GL_QUADS);

            GL11.glVertex2f(-1f, -1f);
            GL11.glVertex2f(-1f, 1f);
            GL11.glVertex2f(1f, 1f);
            GL11.glVertex2f(1f, -1f);

            GL11.glEnd();

            GL20.glUseProgram(0);

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            RoundedUtil.drawRound(0f, 0f, guiWidth * 2, guiHeight * 2, 0, new Color(26, 59, 109, 60));
        }

        particle.render(0, 0);
    }
}
