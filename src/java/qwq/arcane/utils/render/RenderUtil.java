package qwq.arcane.utils.render;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.Timer;
import org.lwjgl.opengl.GL11;
import qwq.arcane.Client;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.math.MathUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.Math.PI;
import static net.minecraft.client.renderer.RenderGlobal.drawSelectionBoundingBox;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_NICEST;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_POINT_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_POINT_SMOOTH_HINT;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glHint;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL11.glVertex2d;
import static qwq.arcane.utils.Instance.INSTANCE;
import static qwq.arcane.utils.Instance.mc;

/**
 * @Author：Guyuemang
 * @Date：2025/6/29 13:23
 */
public class RenderUtil {
    public static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
    private static final Frustum FRUSTUM = new Frustum();
    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        return createFrameBuffer(framebuffer, false);
    }
    public static void scissorStart(double x, double y, double width, double height) {
        glEnable(GL_SCISSOR_TEST);
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        final double scale = sr.getScaleFactor();
        double finalHeight = height * scale;
        double finalY = (sr.getScaledHeight() - y) * scale;
        double finalX = x * scale;
        double finalWidth = width * scale;
        glScissor((int) finalX, (int) (finalY - finalHeight), (int) finalWidth, (int) finalHeight);
    }

    public static void scissorEnd() {
        glDisable(GL_SCISSOR_TEST);
    }
    public static void drawItemStack(ItemStack stack, float x, float y) {
        GL11.glPushMatrix();

        Minecraft mc = Minecraft.getMinecraft();

        if (mc.theWorld != null) {
            RenderHelper.enableGUIStandardItemLighting();
        }

        GlStateManager.pushMatrix();
        GlStateManager.disableAlpha();
        GlStateManager.clear(256);
        GlStateManager.enableBlend();

        mc.getRenderItem().zLevel = -150.0F;
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, (int) x, (int) y);
        mc.getRenderItem().zLevel = 0.0F;

        GlStateManager.enableBlend();
        final float z = 0.5F;

        GlStateManager.scale(z, z, z);
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();

        GL11.glPopMatrix();
    }
    public static String sessionTime() {
        int elapsedTime = ((int)System.currentTimeMillis() - INSTANCE.getStartTime()) / 1000;
        String days = elapsedTime > 86400 ? elapsedTime / 86400 + "d " : "";
        elapsedTime = !days.isEmpty() ? elapsedTime % 86400 : elapsedTime;
        String hours = elapsedTime > 3600 ? elapsedTime / 3600 + "h " : "";
        elapsedTime = !hours.isEmpty() ? elapsedTime % 3600 : elapsedTime;
        String minutes = elapsedTime > 60 ? elapsedTime / 60 + "m " : "";
        elapsedTime = !minutes.isEmpty() ? elapsedTime % 60 : elapsedTime;
        String seconds = elapsedTime > 0 ? elapsedTime + "s" : "";
        return days + hours + minutes + seconds;
    }
    public static void drawBlockBox(final BlockPos blockPos, final Color color, final boolean outline) {
        final RenderManager renderManager = mc.getRenderManager();
        final Timer timer = mc.timer;

        final double x = blockPos.getX() - renderManager.renderPosX;
        final double y = blockPos.getY() - renderManager.renderPosY;
        final double z = blockPos.getZ() - renderManager.renderPosZ;

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0);
        final Block block = mc.theWorld.getBlockState(blockPos).getBlock();


        if (block != null) {
            final EntityPlayer player = mc.thePlayer;

            final double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) timer.renderPartialTicks;
            final double posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) timer.renderPartialTicks;
            final double posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) timer.renderPartialTicks;
            axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos)
                    .expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D)
                    .offset(-posX, -posY, -posZ);
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        glColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() != 255 ? color.getAlpha() : outline ? 26 : 35);
        drawFilledBox(axisAlignedBB);

        if (outline) {
            glLineWidth(1F);
            enableGlCap(GL_LINE_SMOOTH);
            glColor(color.getRGB());

            drawSelectionBoundingBox(axisAlignedBB);
        }

        GlStateManager.resetColor();
        glDepthMask(true);
        resetCaps();

    }
    public static void disableGlCap(final int... caps) {
        for (final int cap : caps)
            setGlCap(cap, false);
    }

    public static void drawFilledBox(final AxisAlignedBB axisAlignedBB) {
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
    }

    private static final Map<Integer, Boolean> glCapMap = new HashMap<>();
    public static void resetCaps() {
        glCapMap.forEach(RenderUtil::setGlState);
    }
    public static void setGlState(final int cap, final boolean state) {
        if (state)
            glEnable(cap);
        else
            glDisable(cap);
    }
    public static void enableGlCap(final int cap) {
        setGlCap(cap, true);
    }
    public static void setGlCap(final int cap, final boolean state) {
        glCapMap.put(cap, glGetBoolean(cap));
        setGlState(cap, state);
    }

    public static void glColor(final int red, final int green, final int blue, final int alpha) {
        GlStateManager.color(red / 255F, green / 255F, blue / 255F, alpha / 255F);
    }
    public static void renderItemStack(ItemStack stack, double x, double y, float scale) {
        renderItemStack(stack, x, y, scale, false);
    }

    public static void renderItemStack(ItemStack stack, double x, double y, float scale, boolean enchantedText) {
        renderItemStack(stack, x, y, scale, enchantedText, scale);
    }

    public static void renderItemStack(ItemStack stack, double x, double y, float scale, boolean enchantedText, float textScale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, x);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
        //mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, 0, 0);
        if (enchantedText)
            renderEnchantText(stack, 0, 0, textScale);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
    private static void drawEnchantTag(String text, double x, double y, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GL11.glTranslated(x, y, x);
        GL11.glScaled(scale, scale, scale);
        mc.fontRendererObj.drawOutlinedString(text, (float) 0, (float) 0, 1.0f, -1, new Color(0, 0, 0, 140).getRGB());
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private static String getColor(final int n) {
        if (n != 1) {
            if (n == 2) {
                return "§a";
            }
            if (n == 3) {
                return "§3";
            }
            if (n == 4) {
                return "§4";
            }
            if (n >= 5) {
                return "§e";
            }
        }
        return "§f";
    }
    public static void drawExhiRect(float x, float y, float x2, float y2, float alpha) {
        x2 = x + x2;
        y2 = y + y2;
        Gui.drawRect(x - 3.5F, y - 3.5F, x2 + 3.5F, y2 + 3.5F, new Color(0, 0, 0, alpha).getRGB());
        Gui.drawRect(x - 3F, y - 3F, x2 + 3F, y2 + 3F, new Color(50F / 255F, 50F / 255F, 50F / 255F, alpha).getRGB());
        Gui.drawRect(x - 2.5F, y - 2.5F, x2 + 2.5F, y2 + 2.5F, new Color(26F / 255F, 26F / 255F, 26F / 255F, alpha).getRGB());
        Gui.drawRect(x - 0.5F, y - 0.5F, x2 + 0.5F, y2 + 0.5F, new Color(50F / 255F, 50F / 255F, 50F / 255F, alpha).getRGB());
        Gui.drawRect(x, y, x2, y2, new Color(18F / 255F, 18 / 255F, 18F / 255F, alpha).getRGB());
    }
    public static void drawBorderedRect(float x, float y, float width, float height, final float outlineThickness, int rectColor, int outlineColor) {
        drawRect(x,y,width,height,rectColor);
        drawBorder(x,y,width,height,outlineThickness,outlineColor);
    }
    public static void drawBorder(float x, float y, float width, float height, final float outlineThickness, int outlineColor) {
        glEnable(GL_LINE_SMOOTH);
        color(outlineColor);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();

        glLineWidth(outlineThickness);
        float cornerValue = (float) (outlineThickness * .19);

        glBegin(GL_LINES);
        glVertex2d(x, y - cornerValue);
        glVertex2d(x, y + height + cornerValue);
        glVertex2d(x + width, y + height + cornerValue);
        glVertex2d(x + width, y - cornerValue);
        glVertex2d(x, y);
        glVertex2d(x + width, y);
        glVertex2d(x, y + height);
        glVertex2d(x + width, y + height);
        glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        glDisable(GL_LINE_SMOOTH);
    }
    public static void drawEntityOnScreen(float yaw, float pitch, EntityLivingBase entityLivingBase) {
        GlStateManager.resetColor();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0f, 0.0f, 50.0f);
        GlStateManager.scale(-50.0f, 50.0f, 50.0f);
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
        float renderYawOffset = entityLivingBase.renderYawOffset;
        float rotationYaw = entityLivingBase.rotationYaw;
        float rotationPitch = entityLivingBase.rotationPitch;
        float prevRotationYawHead = entityLivingBase.prevRotationYawHead;
        float rotationYawHead = entityLivingBase.rotationYawHead;
        GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate((float)(-Math.atan(pitch / 40.0f) * 20.0), 1.0f, 0.0f, 0.0f);
        entityLivingBase.renderYawOffset = yaw - 0.4f;
        entityLivingBase.rotationYaw = yaw - 0.2f;
        entityLivingBase.rotationPitch = pitch;
        entityLivingBase.rotationYawHead = entityLivingBase.rotationYaw;
        entityLivingBase.prevRotationYawHead = entityLivingBase.rotationYaw;
        GlStateManager.translate(0.0f, 0.0f, 0.0f);
        RenderManager renderManager = mc.getRenderManager();
        renderManager.setPlayerViewY(180.0f);
        renderManager.setRenderShadow(false);
        renderManager.renderEntityWithPosYaw(entityLivingBase, 0.0, 0.0, 0.0, 0.0f, 1.0f);
        renderManager.setRenderShadow(true);
        entityLivingBase.renderYawOffset = renderYawOffset;
        entityLivingBase.rotationYaw = rotationYaw;
        entityLivingBase.rotationPitch = rotationPitch;
        entityLivingBase.prevRotationYawHead = prevRotationYawHead;
        entityLivingBase.rotationYawHead = rotationYawHead;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.resetColor();
    }
    public static void drawGradientRect(final double left, final double top, double right, double bottom, final boolean sideways, final int startColor, final int endColor) {
        right = left + right;
        bottom = top + bottom;
        GL11.glDisable(3553);
        GLUtil.startBlend();
        GL11.glShadeModel(7425);
        GL11.glBegin(7);
        color(startColor);
        if (sideways) {
            GL11.glVertex2d(left, top);
            GL11.glVertex2d(left, bottom);
            color(endColor);
            GL11.glVertex2d(right, bottom);
            GL11.glVertex2d(right, top);
        } else {
            GL11.glVertex2d(left, top);
            color(endColor);
            GL11.glVertex2d(left, bottom);
            GL11.glVertex2d(right, bottom);
            color(startColor);
            GL11.glVertex2d(right, top);
        }
        GL11.glEnd();
        GL11.glDisable(3042);
        GL11.glShadeModel(7424);
        GLUtil.endBlend();
        GL11.glEnable(3553);
    }
    public static void drawHorizontalGradientSideways(double x, double y, double width, double height, int leftColor, int rightColor) {
        drawGradientRect(x,y,width,height,true,leftColor,rightColor);
    }
    public static void drawVerticalGradientSideways(double x, double y, double width, double height, int topColor, int bottomColor) {
        drawGradientRect(x,y,width,height,false,topColor,bottomColor);
    }
    public static void drawEntityOnScreen(float posX, float posY, float scale, EntityLivingBase ent) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.color(255, 255, 255);
        GlStateManager.translate(posX, posY, 50.0F);
        GlStateManager.scale(-scale, scale, scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        final RenderManager rendermanager = mc.getRenderManager();
        rendermanager.setPlayerViewY(1F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        rendermanager.setRenderShadow(true);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
    public static void renderItemStack(EntityPlayer target, float x, float y, float scale, boolean enchantedText, float textScale, boolean bg, boolean info) {
        List<ItemStack> items = new ArrayList<>();
        if (target.getHeldItem() != null) {
            items.add(target.getHeldItem());
        }
        for (int index = 3; index >= 0; index--) {
            ItemStack stack = target.inventory.armorInventory[index];
            if (stack != null) {
                items.add(stack);
            }
        }
        float i = x;

        for (ItemStack stack : items) {
            if(bg)
                RenderUtil.drawRect(i,y,16 * scale,16 * scale,new Color(0,0,0,150).getRGB());
            if(info) {
                final int damage = stack.getMaxDamage() - stack.getItemDamage();

                FontManager.Regular.get(16 / 2f * scale).drawCenteredStringWithShadow(damage + "", i + (16 * scale) / 2, (y + 16 + 2) * scale, -1);
            }
            RenderUtil.renderItemStack(stack, i, y, scale, enchantedText, textScale);
            i += 16;
        }
    }

    public static void renderItemStack(EntityPlayer target, float x, float y, float scale,boolean bg,boolean info) {
        renderItemStack(target,x,y,scale,false,0,bg,info);
    }

    public static void renderItemStack(EntityPlayer target, float x, float y, float scale, float textScale) {
        renderItemStack(target,x,y,scale,true,textScale,false,false);
    }

    public static void renderItemStack(EntityPlayer target, float x, float y, float scale) {
        renderItemStack(target,x,y,scale,scale);
    }
    public static void renderEnchantText(ItemStack stack, double x, double y, float scale) {
        int unBreakingLevel;
        RenderHelper.disableStandardItemLighting();
        double height = y;
        if (stack.getItem() instanceof ItemArmor) {
            int protectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack);
            int unBreakingLevel2 = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            int thornLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack);
            if (protectionLevel > 0) {
                drawEnchantTag("P" + getColor(protectionLevel) + protectionLevel, x, height, scale);
                height += 8 * scale;
            }
            if (unBreakingLevel2 > 0) {
                drawEnchantTag("U" + getColor(unBreakingLevel2) + unBreakingLevel2, x, height, scale);
                height += 8 * scale;
            }
            if (thornLevel > 0) {
                drawEnchantTag("T" + getColor(thornLevel) + thornLevel, x, height, scale);
                height += 8 * scale;
            }
        }
        if (stack.getItem() instanceof ItemBow) {
            int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
            int punchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
            int flameLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack);
            unBreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            if (powerLevel > 0) {
                drawEnchantTag("Pow" + getColor(powerLevel) + powerLevel, x, height, scale);
                height += 8 * scale;
            }
            if (punchLevel > 0) {
                drawEnchantTag("Pun" + getColor(punchLevel) + punchLevel, x, height, scale);
                height += 8 * scale;
            }
            if (flameLevel > 0) {
                drawEnchantTag("F" + getColor(flameLevel) + flameLevel, x, height, scale);
                height += 8 * scale;
            }
            if (unBreakingLevel > 0) {
                drawEnchantTag("U" + getColor(unBreakingLevel) + unBreakingLevel, x, height, scale);
                height += 8 * scale;
            }
        }
        if (stack.getItem() instanceof ItemSword) {
            int sharpnessLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
            int knockBackLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack);
            int fireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
            unBreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            if (sharpnessLevel > 0) {
                drawEnchantTag("S" + getColor(sharpnessLevel) + sharpnessLevel, x, height, scale);
                height += 8 * scale;
            }
            if (knockBackLevel > 0) {
                drawEnchantTag("K" + getColor(knockBackLevel) + knockBackLevel, x, height, scale);
                height += 8 * scale;
            }
            if (fireAspectLevel > 0) {
                drawEnchantTag("F" + getColor(fireAspectLevel) + fireAspectLevel, x, height, scale);
                height += 8 * scale;
            }
            if (unBreakingLevel > 0) {
                drawEnchantTag("U" + getColor(unBreakingLevel) + unBreakingLevel, x, height, scale);
                height += 8 * scale;
            }
        }
        if (stack.getRarity() == EnumRarity.EPIC) {
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            GL11.glTranslated(x, y, x);
            GL11.glScaled(scale, scale, scale);
            mc.fontRendererObj.drawOutlinedString("God", (float) (x), (float) height, 1.0f, new Color(255, 255, 0).getRGB(), new Color(100, 100, 0, 140).getRGB());
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
    }
    public static void drawGoodCircle(double x, double y, float radius, int color) {
        color(color);
        GLUtil.setup2DRendering(() -> {
            glEnable(GL_POINT_SMOOTH);
            glHint(GL_POINT_SMOOTH_HINT, GL_NICEST);
            glPointSize(radius * (2 * Minecraft.getMinecraft().gameSettings.guiScale));
            GLUtil.render(GL_POINTS, () -> glVertex2d(x, y));
        });
    }
    public static void bindTexture(int texture) {
        GlStateManager.bindTexture(texture);
    }
    public static void renderBoundingBox(EntityLivingBase entityLivingBase, Color color, float alpha) {
        AxisAlignedBB bb = getInterpolatedBoundingBox(entityLivingBase);
        GlStateManager.pushMatrix();
        GLUtil.setup2DRendering();
        GLUtil.enableCaps(GL_BLEND, GL_POINT_SMOOTH, GL_POLYGON_SMOOTH, GL_LINE_SMOOTH);

        glDisable(GL_DEPTH_TEST);
        glDepthMask(true);
        glLineWidth(1);
        float actualAlpha = .5f * alpha;
        glColor4f(color.getRed(), color.getGreen(), color.getBlue(), actualAlpha);
        color(color.getRGB(), actualAlpha);
        RenderGlobal.renderCustomBoundingBox(bb, false, true);
        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);

        GLUtil.disableCaps();
        GLUtil.end2DRendering();

        GlStateManager.popMatrix();
    }
    public static AxisAlignedBB getInterpolatedBoundingBox(Entity entity) {
        final double[] renderingEntityPos = getInterpolatedPos(entity);
        final double entityRenderWidth = entity.width / 1.5;
        return new AxisAlignedBB(renderingEntityPos[0] - entityRenderWidth,
                renderingEntityPos[1], renderingEntityPos[2] - entityRenderWidth, renderingEntityPos[0] + entityRenderWidth,
                renderingEntityPos[1] + entity.height + (entity.isSneaking() ? -0.3 : 0.18), renderingEntityPos[2] + entityRenderWidth).expand(0.15, 0.15, 0.15);
    }
    public static double[] getInterpolatedPos(Entity entity) {
        float ticks = mc.timer.renderPartialTicks;
        return new double[]{
                MathUtils.interpolate(entity.lastTickPosX, entity.posX, ticks) - mc.getRenderManager().viewerPosX,
                MathUtils.interpolate(entity.lastTickPosY, entity.posY, ticks) - mc.getRenderManager().viewerPosY,
                MathUtils.interpolate(entity.lastTickPosZ, entity.posZ, ticks) - mc.getRenderManager().viewerPosZ
        };
    }
    public static void color(int color, float alpha) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GlStateManager.color(r, g, b, alpha);
    }
    public static void drawTracerLine(Entity entity, float width, Color color, float alpha) {
        float ticks = mc.timer.renderPartialTicks;
        glPushMatrix();

        glLoadIdentity();

        mc.entityRenderer.orientCamera(ticks);
        double[] pos = getInterpolatedPos(entity);

        glDisable(GL_DEPTH_TEST);
        GLUtil.setup2DRendering();

        double yPos = pos[1] + entity.height / 2f;
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(width);

        glBegin(GL_LINE_STRIP);
        color(color.getRGB(), alpha);
        glVertex3d(pos[0], yPos, pos[2]);
        glVertex3d(0, mc.thePlayer.getEyeHeight(), 0);
        glEnd();

        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_DEPTH_TEST);

        GLUtil.end2DRendering();

        glPopMatrix();
    }
    public static void renderBreadCrumbs(final Iterable<Vec3> vec3s) {
        GlStateManager.disableDepth();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int i = 0;
        try {
            for (final Vec3 v : vec3s) {

                i++;

                boolean draw = true;

                final double x = v.xCoord - (mc.getRenderManager()).renderPosX;
                final double y = v.yCoord - (mc.getRenderManager()).renderPosY;
                final double z = v.zCoord - (mc.getRenderManager()).renderPosZ;

                final double distanceFromPlayer = mc.thePlayer.getDistance(v.xCoord, v.yCoord - 1, v.zCoord);
                int quality = (int) (distanceFromPlayer * 4 + 10);

                if (quality > 350)
                    quality = 350;

                if (i % 10 != 0 && distanceFromPlayer > 25) {
                    draw = false;
                }

                if (i % 3 == 0 && distanceFromPlayer > 15) {
                    draw = false;
                }

                if (draw) {

                    GL11.glPushMatrix();
                    GL11.glTranslated(x, y, z);

                    final float scale = 0.04f;
                    GL11.glScalef(-scale, -scale, -scale);

                    GL11.glRotated(-(mc.getRenderManager()).playerViewY, 0.0D, 1.0D, 0.0D);
                    GL11.glRotated((mc.getRenderManager()).playerViewX, 1.0D, 0.0D, 0.0D);

                    final Color c = new Color(Client.Instance.getModuleManager().getModule(InterFace.class).color(0).getRGB());

                    drawFilledCircleNoGL(0, 0, 0.7, c.hashCode(), quality);

                    if (distanceFromPlayer < 4)
                        drawFilledCircleNoGL(0, 0, 1.4, new Color(c.getRed(), c.getGreen(), c.getBlue(), 50).hashCode(), quality);

                    if (distanceFromPlayer < 20)
                        drawFilledCircleNoGL(0, 0, 2.3, new Color(c.getRed(), c.getGreen(), c.getBlue(), 30).hashCode(), quality);

                    GL11.glScalef(0.8f, 0.8f, 0.8f);

                    GL11.glPopMatrix();

                }

            }
        } catch (final ConcurrentModificationException ignored) {
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.enableDepth();

        GL11.glColor3d(255, 255, 255);
    }
    public static void drawFilledCircleNoGL(final float x, final float y, final double r, final int c, final int quality) {
        final float f = ((c >> 24) & 0xff) / 255F;
        final float f1 = ((c >> 16) & 0xff) / 255F;
        final float f2 = ((c >> 8) & 0xff) / 255F;
        final float f3 = (c & 0xff) / 255F;

        GL11.glColor4f(f1, f2, f3, f);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        for (int i = 0; i <= 360 / quality; i++) {
            final double x2 = Math.sin(((i * quality * Math.PI) / 180)) * r;
            final double y2 = Math.cos(((i * quality * Math.PI) / 180)) * r;
            GL11.glVertex2d(x + x2, y + y2);
        }

        GL11.glEnd();
    }
    public static void enableRender3D(boolean disableDepth) {
        if (disableDepth) {
            GL11.glDepthMask((boolean)false);
            GL11.glDisable((int)2929);
        }
        GL11.glDisable((int)3008);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)2848);
        GL11.glHint((int)3154, (int)4354);
        GL11.glLineWidth((float)1.0f);
    }
    public static void disableRender3D(boolean enableDepth) {
        if (enableDepth) {
            GL11.glDepthMask((boolean)true);
            GL11.glEnable((int)2929);
        }
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        GL11.glEnable((int)3008);
        GL11.glDisable((int)2848);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }
    public static boolean isInViewFrustum(Entity entity) {
        return isInViewFrustum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
    }
    private static boolean isInViewFrustum(AxisAlignedBB bb) {
        Entity current = mc.getRenderViewEntity();
        FRUSTUM.setPosition(current.posX, current.posY, current.posZ);
        return FRUSTUM.isBoundingBoxInFrustum(bb);
    }
    public static void renderBlock(BlockPos blockPos, int color, boolean outline, boolean shade) {
        renderBox(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1, 1, 1, color, outline, shade);
    }

    public static void renderBox(int x, int y, int z, double x2, double y2, double z2, int color, boolean outline, boolean shade) {
        double xPos = x - mc.getRenderManager().viewerPosX;
        double yPos = y - mc.getRenderManager().viewerPosY;
        double zPos = z - mc.getRenderManager().viewerPosZ;
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(xPos, yPos, zPos, xPos + x2, yPos + y2, zPos + z2);
        drawAxisAlignedBB(axisAlignedBB, shade, outline,color);
    }
    public static void drawAxisAlignedBB(AxisAlignedBB axisAlignedBB,boolean filled, boolean outline, int color) {
        drawSelectionBoundingBox(axisAlignedBB, outline, filled,color);
    }

    public static void drawOutlineBoundingBox(final AxisAlignedBB bb,Color color) {
        RenderGlobal.drawOutlinedBoundingBox(bb,color.getRed(),color.getGreen(),color.getBlue(),color.getAlpha());
    }
    public static void drawSelectionBoundingBox(AxisAlignedBB boundingBox)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(1, DefaultVertexFormats.POSITION);
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        tessellator.draw();
    }
    public static void drawSelectionBoundingBox(final AxisAlignedBB bb, final boolean outline, final boolean filled,int color) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GL11.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GL11.glDisable(2929);
        GlStateManager.depthMask(false);
        GlStateManager.pushMatrix();

        if (outline) {

            glEnable(GL_LINE_SMOOTH);

            drawOutlineBoundingBox(bb,new Color(color,true));

            glDisable(GL_LINE_SMOOTH);
        }
        if (filled) {
            drawFilledBoundingBox(bb,new Color(color,true));
        }

        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GL11.glEnable(2929);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    public static void drawFilledBoundingBox(final AxisAlignedBB bb,Color color) {

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        tessellator.draw();
    }
    public static int darker(int color) {
        return darker(color, 0.6F);
    }
    public static double interpolate(double old,
                                     double now,
                                     float partialTicks) {
        return old + (now - old) * partialTicks;
    }

    public static float interpolate(float old,
                                    float now,
                                    float partialTicks) {

        return old + (now - old) * partialTicks;
    }

    public static int getColorFromPercentage(float percentage) {
        return Color.HSBtoRGB(Math.min(1.0F, Math.max(0.0F, percentage)) / 3, 0.9F, 0.9F);
    }
    public static int fadeBetween(int startColor, int endColor) {
        return fadeBetween(startColor, endColor, (System.currentTimeMillis() % 2000) / 1000.0F);
    }
    public static int fadeBetween(int startColor, int endColor, float progress) {
        if (progress > 1)
            progress = 1 - progress % 1;

        return fadeTo(startColor, endColor, progress);
    }

    public static int darker(final int color, final float factor) {
        final int r = (int) ((color >> 16 & 0xFF) * factor);
        final int g = (int) ((color >> 8 & 0xFF) * factor);
        final int b = (int) ((color & 0xFF) * factor);
        final int a = color >> 24 & 0xFF;

        return ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF) |
                ((a & 0xFF) << 24);
    }
    public static boolean isBBInFrustum(AxisAlignedBB aabb) {
        EntityPlayerSP player = mc.thePlayer;
        FRUSTUM.setPosition(player.posX, player.posY, player.posZ);
        return FRUSTUM.isBoundingBoxInFrustum(aabb);
    }

    public static int fadeTo(int startColor, int endColor, float progress) {
        float invert = 1.0F - progress;
        int r = (int) ((startColor >> 16 & 0xFF) * invert +
                (endColor >> 16 & 0xFF) * progress);
        int g = (int) ((startColor >> 8 & 0xFF) * invert +
                (endColor >> 8 & 0xFF) * progress);
        int b = (int) ((startColor & 0xFF) * invert +
                (endColor & 0xFF) * progress);
        int a = (int) ((startColor >> 24 & 0xFF) * invert +
                (endColor >> 24 & 0xFF) * progress);
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF);
    }
    public static double progressiveAnimation(double now, double desired, double speed) {
        double dif = Math.abs(now - desired);

        final int fps = Minecraft.getDebugFPS();

        if (dif > 0) {
            double animationSpeed = MathUtils.roundToDecimalPlace(Math.min(
                    10.0D, Math.max(0.05D, (144.0D / fps) * (dif / 10) * speed)), 0.05D);

            if (dif != 0 && dif < animationSpeed)
                animationSpeed = dif;

            if (now < desired)
                return now + animationSpeed;
            else if (now > desired)
                return now - animationSpeed;
        }

        return now;
    }

    public static double linearAnimation(double now, double desired, double speed) {
        double dif = Math.abs(now - desired);

        final int fps = Minecraft.getDebugFPS();

        if (dif > 0) {
            double animationSpeed = MathUtils.roundToDecimalPlace(Math.min(
                    10.0D, Math.max(0.005D, (144.0D / fps) * speed)), 0.005D);

            if (dif != 0 && dif < animationSpeed)
                animationSpeed = dif;

            if (now < desired)
                return now + animationSpeed;
            else if (now > desired)
                return now - animationSpeed;
        }

        return now;
    }
    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
        if (needsNewFramebuffer(framebuffer)) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(mc.displayWidth, mc.displayHeight, depth);
        }
        return framebuffer;
    }
    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float x1 = x + width, // @off
                y1 = y + height;
        final float f = (color >> 24 & 0xFF) / 255.0F,
                f1 = (color >> 16 & 0xFF) / 255.0F,
                f2 = (color >> 8 & 0xFF) / 255.0F,
                f3 = (color & 0xFF) / 255.0F; // @on
        GL11.glPushAttrib(0);
        GL11.glScaled(0.5, 0.5, 0.5);

        x *= 2;
        y *= 2;
        x1 *= 2;
        y1 *= 2;

        glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(f1, f2, f3, f);
        GlStateManager.enableBlend();
        glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glBegin(GL11.GL_POLYGON);
        final double v = PI / 180;

        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x + radius + MathHelper.sin((float) (i * v)) * (radius * -1), y + radius + MathHelper.cos((float) (i * v)) * (radius * -1));
        }

        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x + radius + MathHelper.sin((float) (i * v)) * (radius * -1), y1 - radius + MathHelper.cos((float) (i * v)) * (radius * -1));
        }

        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x1 - radius + MathHelper.sin((float) (i * v)) * radius, y1 - radius + MathHelper.cos((float) (i * v)) * radius);
        }

        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x1 - radius + MathHelper.sin((float) (i * v)) * radius, y + radius + MathHelper.cos((float) (i * v)) * radius);
        }

        GL11.glEnd();

        glEnable(GL11.GL_TEXTURE_2D);
        glDisable(GL11.GL_LINE_SMOOTH);
        glEnable(GL11.GL_TEXTURE_2D);

        GL11.glScaled(2, 2, 2);

        GL11.glPopAttrib();
        GL11.glColor4f(1, 1, 1, 1);
    }
    public static void color(int color) {
        float f = (float) (color >> 24 & 255) / 255.0f;
        float f1 = (float) (color >> 16 & 255) / 255.0f;
        float f2 = (float) (color >> 8 & 255) / 255.0f;
        float f3 = (float) (color & 255) / 255.0f;
        GL11.glColor4f((float) f1, (float) f2, (float) f3, (float) f);
    }
    public static void renderPlayer2D(EntityLivingBase abstractClientPlayer, final float x, final float y, final float size, float radius, int color) {
        if (abstractClientPlayer instanceof AbstractClientPlayer player) {
            StencilUtils.initStencilToWrite();
            RenderUtil.drawRoundedRect(x, y, size, size, radius, -1);
            StencilUtils.readStencilBuffer(1);
            RenderUtil.color(color);
            GLUtil.startBlend();
            mc.getTextureManager().bindTexture(player.getLocationSkin());
            Gui.drawScaledCustomSizeModalRect(x, y, (float) 8.0, (float) 8.0, 8, 8, size, size, 64.0F, 64.0F);
            GLUtil.endBlend();
            StencilUtils.uninitStencilBuffer();
        }
    }
    public static void stopScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
    public static boolean needsNewFramebuffer(Framebuffer framebuffer) {
        return framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight;
    }
    public static boolean isHovering(float x, float y, float width, float height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
    public static String stripColor(final String input) {
        return COLOR_PATTERN.matcher(input).replaceAll("");
    }
    public static void drawGlow(double x, double y, double z, Color color, float radius, int segments) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL11.glTranslated(x, y, z);
        GL11.glColor4f(color.getRed()/255f, color.getGreen()/255f,
                color.getBlue()/255f, color.getAlpha()/255f);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex3d(0, 0, 0);
        for (int i = 0; i <= segments; i++) {
            double angle = Math.PI * 2 * i / segments;
            GL11.glVertex3d(Math.cos(angle) * radius, Math.sin(angle) * radius, 0);
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public static void drawTracer(double x, double y, double z, Color color) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL11.glLineWidth(1.5f);
        GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f,
                color.getBlue() / 255f, color.getAlpha() / 255f);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(0, mc.thePlayer.getEyeHeight(), 0);
        GL11.glVertex3d(x, y, z);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
    public static double deltaTime() {
        return Minecraft.getDebugFPS() > 0 ? (1.0000 / Minecraft.getDebugFPS()) : 1;
    }
    public static float animate(float end, float start, float multiple) {
        return (1 - MathHelper.clamp_float((float) (deltaTime() * multiple), 0, 1)) * end + MathHelper.clamp_float((float) (deltaTime() * multiple), 0, 1) * start;
    }
    public static double animate(double value, double target) {
        return animate(value, target, 1, false);
    }
    public static double animate(double value, double target, double speed, boolean minedelta) {
        double c = value + (target - value) / (3 + speed * deltaTime());
        double v = value
                + ((target - value)) / (2 + speed);
        return minedelta ? v : c;
    }
    public static void scaleEnd() {
        GlStateManager.popMatrix();
    }
    public static void scaleStart(float x, float y, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1);
        GlStateManager.translate(-x, -y, 0);
    }
    public static void drawRect(float left, float top, float width, float height, Color color) {
        drawRect(left,top,width,height,color.getRGB());
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
    public static double ticks = 0;
    public static long lastFrame = 0;
    public static void drawCircle(Entity entity, float partialTicks, double rad, int color, float alpha) {
        /*Got this from the people i made the Gui for*/
        ticks += .004 * (System.currentTimeMillis() - lastFrame);

        lastFrame = System.currentTimeMillis();

        glPushMatrix();
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        GlStateManager.color(1, 1, 1, 1);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        glShadeModel(GL_SMOOTH);
        GlStateManager.disableCull();

        final double x = MathUtils.interpolate(entity.lastTickPosX, entity.posX, mc.timer.renderPartialTicks) - mc.getRenderManager().renderPosX;
        final double y = MathUtils.interpolate(entity.lastTickPosY, entity.posY, mc.timer.renderPartialTicks) - mc.getRenderManager().renderPosY + Math.sin(ticks) + 1;
        final double z = MathUtils.interpolate(entity.lastTickPosZ, entity.posZ, mc.timer.renderPartialTicks) - mc.getRenderManager().renderPosZ;

        glBegin(GL_TRIANGLE_STRIP);

        for (float i = 0; i < (Math.PI * 2); i += (Math.PI * 2) / 64.F) {

            final double vecX = x + rad * Math.cos(i);
            final double vecZ = z + rad * Math.sin(i);

            color(color, 0);

            glVertex3d(vecX, y - Math.sin(ticks + 1) / 2.7f, vecZ);

            color(color, .52f * alpha);


            glVertex3d(vecX, y, vecZ);
        }

        glEnd();


        glEnable(GL_LINE_SMOOTH);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        glLineWidth(1.5f);
        glBegin(GL_LINE_STRIP);
        GlStateManager.color(1, 1, 1, 1);
        color(color, .5f * alpha);
        for (int i = 0; i <= 180; i++) {
            glVertex3d(x - Math.sin(i * MathHelper.PI2 / 90) * rad, y, z + Math.cos(i * MathHelper.PI2 / 90) * rad);
        }
        glEnd();

        glShadeModel(GL_FLAT);
        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);
        GlStateManager.enableCull();
        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
        glColor4f(1f, 1f, 1f, 1f);
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
    public static void setColor(int color) {
        GL11.glColor4ub((byte) (color >> 16 & 0xFF), (byte) (color >> 8 & 0xFF), (byte) (color & 0xFF), (byte) (color >> 24 & 0xFF));
    }
    public static void resetColor2() {
        color(1, 1, 1, 1);
    }
    public static void color(double red, double green, double blue, double alpha) {
        GL11.glColor4d(red, green, blue, alpha);
    }
    public static int colorSwitch(Color firstColor, Color secondColor, float time, int index, long timePerIndex, double speed) {
        return colorSwitch(firstColor, secondColor, time, index, timePerIndex, speed, 255);
    }
    public static int getRainbow(long currentMillis, int speed, int offset) {
        return getRainbow(currentMillis, speed, offset, 1.0F);
    }

    public static int getRainbow(long currentMillis, int speed, int offset, float alpha) {
        int rainbow = Color.HSBtoRGB(1.0F - ((currentMillis + (offset * 100)) % speed) / (float) speed,
                0.9F, 0.9F);
        int r = (rainbow >> 16) & 0xFF;
        int g = (rainbow >> 8) & 0xFF;
        int b = rainbow & 0xFF;
        int a = (int) (alpha * 255.0F);
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF);
    }
    public static void drawCircleCGUI(double x, double y, float radius, int color) {
        if (radius == 0)
            return;
        final float correctRadius = radius * 2;
        setup2DRendering(() -> {
            glColor(color);
            glEnable(GL_POINT_SMOOTH);
            glHint(GL_POINT_SMOOTH_HINT, GL_NICEST);
            glPointSize(correctRadius);
            GLUtil.setupRendering(GL_POINTS, () -> glVertex2d(x, y));
            glDisable(GL_POINT_SMOOTH);
            GlStateManager.resetColor();
        });
    }
    public static void setup2DRendering(Runnable f) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_TEXTURE_2D);
        f.run();
        glEnable(GL_TEXTURE_2D);
        GlStateManager.disableBlend();
    }
    public static void glColor(int hex) {
        float alpha = (hex >> 24 & 0xFF) / 255.0F;
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }
    public static int colorSwitch(Color firstColor, Color secondColor, float time, int index, long timePerIndex, double speed, double alpha) {
        long now = (long) (speed * System.currentTimeMillis() + index * timePerIndex);

        float redDiff = (firstColor.getRed() - secondColor.getRed()) / time;
        float greenDiff = (firstColor.getGreen() - secondColor.getGreen()) / time;
        float blueDiff = (firstColor.getBlue() - secondColor.getBlue()) / time;
        int red = Math.round(secondColor.getRed() + redDiff * (now % (long) time));
        int green = Math.round(secondColor.getGreen() + greenDiff * (now % (long) time));
        int blue = Math.round(secondColor.getBlue() + blueDiff * (now % (long) time));

        float redInverseDiff = (secondColor.getRed() - firstColor.getRed()) / time;
        float greenInverseDiff = (secondColor.getGreen() - firstColor.getGreen()) / time;
        float blueInverseDiff = (secondColor.getBlue() - firstColor.getBlue()) / time;
        int inverseRed = Math.round(firstColor.getRed() + redInverseDiff * (now % (long) time));
        int inverseGreen = Math.round(firstColor.getGreen() + greenInverseDiff * (now % (long) time));
        int inverseBlue = Math.round(firstColor.getBlue() + blueInverseDiff * (now % (long) time));

        if (now % ((long) time * 2) < (long) time)
            return ColorUtil.getColor(inverseRed, inverseGreen, inverseBlue, (int) alpha);
        else return ColorUtil.getColor(red, green, blue, (int) alpha);
    }
    public static void startGlScissor(int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        int scaleFactor = 1;
        int k = mc.gameSettings.guiScale;
        if (k == 0) {
            k = 1000;
        }
        while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        GL11.glPushMatrix();
        GL11.glEnable(3089);
        GL11.glScissor((int) (x * scaleFactor), (int) (mc.displayHeight - (y + height) * scaleFactor), (int) (width * scaleFactor), (int) (height * scaleFactor));
    }

    public static void stopGlScissor() {
        GL11.glDisable(3089);
        GL11.glPopMatrix();
    }
    public static void scissor(final double x, final double y, final double width, final double height) {
        int scaleFactor = 1;
        while (scaleFactor < 2 && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        GL11.glScissor((int) (x * scaleFactor),
                (int) (Minecraft.getMinecraft().displayHeight - (y + height) * scaleFactor),
                (int) (width * scaleFactor), (int) (height * scaleFactor));
    }
    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, (float) (limit * .01));
    }

    public static void resetColor() {
        GlStateManager.color(1, 1, 1, 1);
    }
}
