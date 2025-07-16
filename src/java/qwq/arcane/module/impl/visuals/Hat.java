
package qwq.arcane.module.impl.visuals;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.render.Render3DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.math.MathUtils;
import qwq.arcane.utils.render.GLUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.ColorValue;
import qwq.arcane.value.impl.ModeValue;
import qwq.arcane.value.impl.NumberValue;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class Hat extends Module {
    public final ModeValue mode = new ModeValue("Mode", "Sexy", new String[]{"Astolfo", "Sexy", "Fade", "Dynamic"});
    public final NumberValue points = new NumberValue("Points", 30, 3, 180,1);
    public final NumberValue size = new NumberValue("Size", 0.5f, 0.1f, 3.0f, 0.1f);
    private final NumberValue offSetValue = new NumberValue("Off Set", 2000.0f, 0.0f, 5000.0f, 100.0f);
    public final ColorValue colorValue = new ColorValue("Color", () -> mode.is("Fade") || mode.is("Dynamic"), new Color(255, 255, 255));
    public final ColorValue secondColorValue = new ColorValue("Second Color", () -> mode.is("Fade"), new Color(0, 0, 0));
    public final BoolValue target = new BoolValue("Target", true);
    private final double[][] positions = new double[(int) points.getMax() + 1][2];
    private int lastPoints;
    private double lastSize;

    public Hat() {
        super("Hat",Category.Visuals);
    }

    private void computeChineseHatPoints(final int points, final double radius) {
        for (int i = 0; i <= points; i++) {
            final double circleX = radius * StrictMath.cos(i * Math.PI * 2 / points);
            final double circleZ = radius * StrictMath.sin(i * Math.PI * 2 / points);

            this.positions[i][0] = circleX;
            this.positions[i][1] = circleZ;
        }
    }

    private void addCircleVertices(final int points, final Color[] colors, final int alpha) {
        for (int i = 0; i <= points; i++) {
            final double[] pos = this.positions[i];
            final Color clr = colors[i];
            GL11.glColor4f(clr.getRed() / 255.0f, clr.getGreen() / 255.0f, clr.getBlue() / 255.0f, alpha / 255.0f);
            glVertex3d(pos[0], 0, pos[1]);
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {

        if (this.lastSize != this.size.get() || this.lastPoints != this.points.get()) {
            this.lastSize = this.size.get();
            this.computeChineseHatPoints(this.lastPoints = (int) this.points.get().intValue(), this.lastSize);
        }

        drawHat(event, mc.thePlayer);
    }

    public void drawHat(Render3DEvent event, EntityPlayer player) {
        if (player == mc.thePlayer && this.mc.gameSettings.thirdPersonView == 0)
            return;
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_CULL_FACE);
        glDepthMask(false);
        glDisable(GL_DEPTH_TEST);
        glShadeModel(GL_SMOOTH);
        GLUtil.startBlend();

        final float partialTicks = event.partialTicks();
        final RenderManager render = this.mc.getRenderManager();

        final double rx = render.renderPosX;
        final double ry = render.renderPosY;
        final double rz = render.renderPosZ;

        glTranslated(-rx, -ry, -rz);

        final double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks();
        final double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks();
        final double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks();

        final int points = (int) this.points.get().intValue();
        final double radius = this.size.get();

        Color[] colors = new Color[181];
        Color[] colorMode = new Color[0];
        colorMode = switch (this.mode.get()) {
            case "Astolfo" -> new Color[]{new Color(252, 106, 140), new Color(252, 106, 213),
                    new Color(218, 106, 252), new Color(145, 106, 252), new Color(106, 140, 252),
                    new Color(106, 213, 252), new Color(106, 213, 252), new Color(106, 140, 252),
                    new Color(145, 106, 252), new Color(218, 106, 252), new Color(252, 106, 213),
                    new Color(252, 106, 140)};
            case "Sexy" ->
                    new Color[]{new Color(255, 150, 255), new Color(255, 132, 199), new Color(211, 101, 187), new Color(160, 80, 158), new Color(120, 63, 160), new Color(123, 65, 168), new Color(104, 52, 152), new Color(142, 74, 175), new Color(160, 83, 179), new Color(255, 110, 189), new Color(255, 150, 255)};
            case "Fade" ->
                    new Color[]{this.colorValue.get(), this.secondColorValue.get(), this.colorValue.get()};
            case "Dynamic" ->
                    new Color[]{this.colorValue.get(), new Color(ColorUtil.darker(colorValue.get().getRGB(), 0.25F)), this.colorValue.get()};
            default -> colorMode;
        };

        for (int i = 0; i < colors.length; ++i) {
            colors[i] = this.fadeBetween(colorMode, (this.offSetValue.get()), (double) i * ((double) (this.offSetValue.get()) / this.points.get()));
        }

        glPushMatrix();

        // Position
        {
            glTranslated(x, y + 1.9, z);
            if (player.isSneaking())
                glTranslated(0, -0.2, 0);
        }

        // Yaw
        {
            glRotatef(MathUtils.interpolate(player.prevRotationYawHead, player.rotationYawHead, partialTicks).floatValue(), 0, -1, 0);
        }

        // Pitch
        {
            final float pitch = MathUtils.interpolate(player.prevRotationPitchHead, player.rotationPitchHead, partialTicks).floatValue();
            glRotatef(pitch / 3.f, 1, 0, 0);
            glTranslated(0, 0, pitch / 270.f);
        }

        // Outline
        {
            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
            glLineWidth(2.0f);

            glBegin(GL_LINE_LOOP);
            {
                this.addCircleVertices(points - 1, colors, 0xFF);
            }
            glEnd();

            glDisable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
        }

        // Cone
        {
            glBegin(GL_TRIANGLE_FAN);
            {
                glVertex3d(0, radius / 2, 0);

                this.addCircleVertices(points, colors, 0x80);
            }
            glEnd();
        }

        glPopMatrix();

        glTranslated(rx, ry, rz);

        GLUtil.endBlend();
        glDepthMask(true);
        glShadeModel(GL_FLAT);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glEnable(GL_TEXTURE_2D);
    }

    public Color fadeBetween(final Color[] table, final double speed, final double offset) {
        return this.fadeBetween(table, (System.currentTimeMillis() + offset) % speed / speed);
    }

    public Color fadeBetween(final Color[] table, final double progress) {
        final int i = table.length;
        if (progress == 1.0) {
            return table[0];
        }
        if (progress == 0.0) {
            return table[i - 1];
        }
        final double max = Math.max(0.0, (1.0 - progress) * (i - 1));
        final int min = (int) max;
        return this.fadeBetween(table[min], table[min + 1], max - min);
    }

    public Color fadeBetween(final Color start, final Color end, double progress) {
        if (progress > 1.0) {
            progress = 1.0 - progress % 1.0;
        }
        return this.gradient(start, end, progress);
    }

    public Color gradient(final Color start, final Color end, final double progress) {
        final double invert = 1.0 - progress;
        return new Color((int) (start.getRed() * invert + end.getRed() * progress), (int) (start.getGreen() * invert + end.getGreen() * progress), (int) (start.getBlue() * invert + end.getBlue() * progress), (int) (start.getAlpha() * invert + end.getAlpha() * progress));
    }
}
