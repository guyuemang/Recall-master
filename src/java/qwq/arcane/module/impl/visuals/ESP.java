package qwq.arcane.module.impl.visuals;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.WorldLoadEvent;
import qwq.arcane.event.impl.events.render.Render2DEvent;
import qwq.arcane.event.impl.events.render.Render3DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.math.MathUtils;
import qwq.arcane.utils.render.GLUtil;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.value.impl.BooleanValue;
import qwq.arcane.value.impl.ColorValue;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4ub;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2f;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 12:31
 */
public final class ESP extends Module {
    public ESP() {
        super("ESP",Category.Visuals);
    }
    public static final BooleanValue fontTags = new BooleanValue("TagsName", true);
    public static final BooleanValue fonttagsBackground = new BooleanValue("TagsBackground", fontTags::get, true);
    public static final BooleanValue fonttagsHealth = new BooleanValue("TagsHealth", fontTags::get, true);
    public static final BooleanValue esp2d = new BooleanValue("2DESP", true);
    public static final BooleanValue box = new BooleanValue("Box", esp2d::get, true);
    public static final BooleanValue boxSyncColor = new BooleanValue("BoxSyncColor", () -> esp2d.get() && box.get(), false);
    public static final ColorValue boxColor = new ColorValue("BoxColor", () -> esp2d.get() && box.get() && !boxSyncColor.get(), Color.RED);
    public static final BooleanValue healthBar = new BooleanValue("Health", esp2d::get, true);
    public static final BooleanValue healthBarSyncColor = new BooleanValue("HealthColor", () -> esp2d.get() && healthBar.get(),false);
    public static final ColorValue absorptionColor = new ColorValue("AbsorptionColor", () -> esp2d.get() && healthBar.get() && !healthBarSyncColor.get(), new Color(255, 255, 50));
    public static final BooleanValue armorBar = new BooleanValue("Armor", esp2d::get,true);
    public static final ColorValue armorBarColor = new ColorValue("ArmorColor", () -> esp2d.get() && armorBar.get(), new Color(50, 255, 255));
    public final Map<EntityPlayer, float[][]> playerRotationMap = new HashMap<>();
    private final Map<EntityPlayer, float[]> entityPosMap = new HashMap<>();

    @Override
    public void onDisable() {
        entityPosMap.clear();
        playerRotationMap.clear();
    }

    @EventTarget
    public void onWorld(WorldLoadEvent event) {
        entityPosMap.clear();
        playerRotationMap.clear();
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        for (EntityPlayer player : entityPosMap.keySet()) {
            if ((player.getDistanceToEntity(mc.thePlayer) < 1.0F && mc.gameSettings.thirdPersonView == 0) ||
                    !RenderUtil.isInViewFrustum(player))
                continue;
            final float[] positions = entityPosMap.get(player);
            final float x = positions[0];
            final float y = positions[1];
            final float x2 = positions[2];
            final float y2 = positions[3];

            final float health = player.getHealth();
            final float maxHealth = player.getMaxHealth();
            final float healthPercentage = health / maxHealth;

            if (fontTags.get()) {
                final String healthString = fonttagsHealth.get() ? " |" + EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + " " + (MathUtils.roundToHalf(player.getHealth())) + "❤" + EnumChatFormatting.RESET + "" : "";
                final String name = player.getDisplayName().getFormattedText() + healthString;
                float halfWidth = (float) mc.fontRendererObj.getStringWidth(name) * 0.5f;
                final float xDif = x2 - x;
                final float middle = x + (xDif / 2);
                final float textHeight = mc.fontRendererObj.FONT_HEIGHT * 0.5f;
                float renderY = y - textHeight - 2;

                final float left = middle - halfWidth - 1;
                final float right = middle + halfWidth + 1;

                if (fonttagsBackground.get()) {
                    Gui.drawRect(left, renderY - 6, right, renderY + textHeight + 1, new Color(0, 0, 0, 50).getRGB());
                }

                mc.fontRendererObj.drawStringWithShadow(name, middle - halfWidth, renderY - 4f, -1);
            }

            if (esp2d.get()) {
                glDisable(GL_TEXTURE_2D);
                GLUtil.startBlend();

                if (armorBar.get()) {
                    final float armorPercentage = player.getTotalArmorValue() / 20.0F;
                    final float armorBarWidth = (x2 - x) * armorPercentage;

                    glColor4ub((byte) 0, (byte) 0, (byte) 0, (byte) 0x96);
                    glBegin(GL_QUADS);

                    // Background
                    {
                        glVertex2f(x, y2 + 0.5F);
                        glVertex2f(x, y2 + 2.5F);

                        glVertex2f(x2, y2 + 2.5F);
                        glVertex2f(x2, y2 + 0.5F);
                    }

                    if (armorPercentage > 0) {
                        color(armorBarColor.get().getRGB());

                        // Bar
                        {
                            glVertex2f(x + 0.5F, y2 + 1);
                            glVertex2f(x + 0.5F, y2 + 2);

                            glVertex2f(x + armorBarWidth - 0.5F, y2 + 2);
                            glVertex2f(x + armorBarWidth - 0.5F, y2 + 1);
                        }
                        resetColor();
                    }

                    if (!healthBar.get())
                        glEnd();
                }

                if (healthBar.get()) {
                    float healthBarLeft = x - 2.5F;
                    float healthBarRight = x - 0.5F;

                    glColor4ub((byte) 0, (byte) 0, (byte) 0, (byte) 0x96);

                    if (!armorBar.get())
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

                    if (healthBarSyncColor.get()) {
                        final int syncedcolor = Client.Instance.getModuleManager().getModule(InterFace.class).color(0).getRGB();

                        color(syncedcolor);
                    } else {
                        final int color = ColorUtil.getColorFromPercentage(healthPercentage);

                        color(color);
                    }
                    // Bar
                    {
                        glVertex2f(healthBarLeft, topOfHealthBar);
                        glVertex2f(healthBarLeft, y2 - 0.5F);

                        glVertex2f(healthBarRight, y2 - 0.5F);
                        glVertex2f(healthBarRight, topOfHealthBar);
                    }

                    resetColor();


                    final float absorption = player.getAbsorptionAmount();

                    final float absorptionPercentage = Math.min(1.0F, absorption / 20.0F);

                    final int absorptionColor = this.absorptionColor.get().getRGB();

                    final float absorptionHeight = heightDif * absorptionPercentage;

                    final float topOfAbsorptionBar = y2 + 0.5F + absorptionHeight;

                    if (healthBarSyncColor.get()) {
                        color(Client.Instance.getModuleManager().getModule(InterFace.class).color(1).getRGB());
                    } else {
                        color(absorptionColor);
                    }

                    // Absorption Bar
                    {
                        glVertex2f(healthBarLeft, topOfAbsorptionBar);
                        glVertex2f(healthBarLeft, y2 - 0.5F);

                        glVertex2f(healthBarRight, y2 - 0.5F);
                        glVertex2f(healthBarRight, topOfAbsorptionBar);
                    }

                    resetColor();

                    if (!box.get())
                        glEnd();
                }

                if (box.get()) {
                    glColor4ub((byte) 0, (byte) 0, (byte) 0, (byte) 0x96);
                    if (!healthBar.get())
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

                    if (boxSyncColor.get()) {
                        color(Client.Instance.getModuleManager().getModule(InterFace.class).color(7).getRGB());
                    } else {
                        color(boxColor.get().getRGB());
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

                    resetColor();

                    glEnd();
                }

                glEnable(GL_TEXTURE_2D);
                GLUtil.endBlend();
            }
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        final boolean project2D = esp2d.get();
        if (project2D && !entityPosMap.isEmpty())
            entityPosMap.clear();

        final float partialTicks = event.partialTicks();

        for (final EntityPlayer player : mc.theWorld.playerEntities) {
            if (isHypixelSpecialEntity(player)) {
                continue;
            }
            if (project2D) {

                final double posX = (MathUtils.interpolate(player.prevPosX, player.posX, partialTicks) -
                        mc.getRenderManager().viewerPosX);
                final double posY = (MathUtils.interpolate(player.prevPosY, player.posY, partialTicks) -
                        mc.getRenderManager().viewerPosY);
                final double posZ = (MathUtils.interpolate(player.prevPosZ, player.posZ, partialTicks) -
                        mc.getRenderManager().viewerPosZ);

                final double halfWidth = player.width / 2.0D;
                final AxisAlignedBB bb = new AxisAlignedBB(posX - halfWidth, posY, posZ - halfWidth,
                        posX + halfWidth, posY + player.height + (player.isSneaking() ? -0.2D : 0.1D), posZ + halfWidth).expand(0.1, 0.1, 0.1);

                final double[][] vectors = {{bb.minX, bb.minY, bb.minZ},
                        {bb.minX, bb.maxY, bb.minZ},
                        {bb.minX, bb.maxY, bb.maxZ},
                        {bb.minX, bb.minY, bb.maxZ},
                        {bb.maxX, bb.minY, bb.minZ},
                        {bb.maxX, bb.maxY, bb.minZ},
                        {bb.maxX, bb.maxY, bb.maxZ},
                        {bb.maxX, bb.minY, bb.maxZ}};

                float[] projection;
                final float[] position = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, -1.0F, -1.0F};

                for (final double[] vec : vectors) {
                    projection = GLUtil.project2D((float) vec[0], (float) vec[1], (float) vec[2], event.scaledResolution().getScaleFactor());
                    if (projection != null && projection[2] >= 0.0F && projection[2] < 1.0F) {
                        final float pX = projection[0];
                        final float pY = projection[1];
                        position[0] = Math.min(position[0], pX);
                        position[1] = Math.min(position[1], pY);
                        position[2] = Math.max(position[2], pX);
                        position[3] = Math.max(position[3], pY);
                    }
                }

                entityPosMap.put(player, position);
            }
        }
    }

    private boolean isHypixelSpecialEntity(EntityPlayer player) {
        if (mc.getCurrentServerData() == null || !mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel")) {
            return false;
        }
        if (player.getDisplayName().getUnformattedText().startsWith("[NPC] ") ||
                player.getDisplayName().getUnformattedText().startsWith("NPC ")) {
            return true;
        }
        if (player.getDisplayName().getUnformattedText().toLowerCase().contains("helper") ||
                player.getDisplayName().getUnformattedText().toLowerCase().contains("mod") ||
                player.getDisplayName().getUnformattedText().toLowerCase().contains("admin")) {
            return true;
        }
        if (player.getDisplayName().getUnformattedText().startsWith("BOT ") ||
                player.getDisplayName().getUnformattedText().endsWith(" BOT") ||
                player.getDisplayName().getUnformattedText().contains("Robot")) {
            return true;
        }
        if (player.getScore() == -9999 || player.getScore() == 0) {
            return true;
        }
        if (player.posX == player.prevPosX && player.posZ == player.prevPosZ &&
                player.rotationYaw == player.prevRotationYaw) {
            return true;
        }

        return false;
    }
    public boolean isValid(Entity entity) {
        if (entity instanceof EntityPlayer player) {
            if (!player.isEntityAlive()) {
                return false;
            }
            return RenderUtil.isBBInFrustum(entity.getEntityBoundingBox()) && mc.theWorld.playerEntities.contains(player);
        }

        return false;
    }
    public void addEntity(EntityPlayer e, ModelPlayer model) {
        playerRotationMap.put(e, new float[][]{
                {model.bipedHead.rotateAngleX, model.bipedHead.rotateAngleY, model.bipedHead.rotateAngleZ},
                {model.bipedRightArm.rotateAngleX, model.bipedRightArm.rotateAngleY, model.bipedRightArm.rotateAngleZ},
                {model.bipedLeftArm.rotateAngleX, model.bipedLeftArm.rotateAngleY, model.bipedLeftArm.rotateAngleZ},
                {model.bipedRightLeg.rotateAngleX, model.bipedRightLeg.rotateAngleY, model.bipedRightLeg.rotateAngleZ},
                {model.bipedLeftLeg.rotateAngleX, model.bipedLeftLeg.rotateAngleY, model.bipedLeftLeg.rotateAngleZ}
        });
    }
    public static void resetColor() {
        color(1, 1, 1, 1);
    }
    public static void color(double red, double green, double blue, double alpha) {
        GL11.glColor4d(red, green, blue, alpha);
    }

    public static void color(int color) {
        glColor4ub(
                (byte) (color >> 16 & 0xFF),
                (byte) (color >> 8 & 0xFF),
                (byte) (color & 0xFF),
                (byte) (color >> 24 & 0xFF));
    }

}