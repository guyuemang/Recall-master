package qwq.arcane.module.impl.visuals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.TickEvent;
import qwq.arcane.event.impl.events.render.Render2DEvent;
import qwq.arcane.event.impl.events.render.Shader2DEvent;
import qwq.arcane.gui.clickgui.arcane.ArcaneClickGui;
import qwq.arcane.gui.clickgui.dropdown.DropDownClickGui;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.combat.KillAura;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.DecelerateAnimation;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.player.PingerUtils;
import qwq.arcane.utils.render.GradientUtil;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.shader.impl.Bloom;
import qwq.arcane.utils.render.shader.impl.Blur;
import qwq.arcane.utils.render.shader.impl.Shadow;
import qwq.arcane.value.impl.*;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author：Guyuemang
 * @Date：2025/6/29 01:06
 */
public class InterFace extends Module {
    public InterFace() {
        super("InterFace", Category.Visuals);
        setState(true);
    }
    public static ModeValue colorMode = new ModeValue("Color Mode", "Fade", new String[]{"Fade", "Rainbow", "Astolfo", "Dynamic","Tenacity", "Static", "Double"});
    public static final NumberValue colorspeed = new NumberValue("ColorSpeed", () -> colorMode.is("Tenacity"), 4, 1, 10, 1);
    public static ColorValue mainColor = new ColorValue("MainColor", new Color(213, 63, 119));
    public static ColorValue secondColor = new ColorValue("SecondColor", new Color(157, 68, 110));
    public static BoolValue waterMark = new BoolValue("WaterMark",true);
    public static ModeValue waterMarkmode = new ModeValue("WaterMarkMode",()-> waterMark.get(),"Exhi",new String[]{"Exhi","Arcane","Exhibition","Sigma"});
    public static BoolValue info = new BoolValue("Info",true);
    public static final ModeValue infomode = new ModeValue("InfoMode",()->info.get(),"Exhi",new String[]{"Exhi","Arcane"});
    public static NumberValue radius = new NumberValue("radius",6,0,8,1);
    public static BoolValue renderBossHealth = new BoolValue("BossHealth",false);
    public static final BoolValue blur = new BoolValue("Blur", false);
    public static final NumberValue blurRadius = new NumberValue("Blur Radius", blur::get, 8.0, 1.0, 50.0, 1.0);
    public static final NumberValue blurCompression = new NumberValue("Blur Compression", blur::get,2.0, 1.0, 50.0, 1.0);
    public static final BoolValue shadow = new BoolValue("Shadow", false);
    public static final NumberValue shadowRadius = new NumberValue("Shadow Radius", shadow::get,10.0, 1.0, 20.0, 1.0);
    public static final NumberValue shadowOffset = new NumberValue("Shadow Offset", shadow::get,1.0, 1.0, 15.0, 1.0);
    public static final BoolValue bloom = new BoolValue("Bloom", false);
    public static final NumberValue glowRadius = new NumberValue("Bloom Radius", bloom::get, 3.0, 1.0, 10.0, 1.0);
    public static final NumberValue glowOffset = new NumberValue("Bloom Offset", bloom::get ,1.0, 1.0, 10.0, 1.0);
    private final DecimalFormat bpsFormat = new DecimalFormat("0.00");
    private final DecimalFormat xyzFormat = new DecimalFormat("0");
    public static Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);
    public final Map<EntityPlayer, DecelerateAnimation> animationEntityPlayerMap = new HashMap<>();
    @EventTarget
    public void onRender(Render2DEvent event) {
        setsuffix(colorMode.get());
        if (waterMark.get()) {
            boolean shouldChange = RenderUtil.COLOR_PATTERN.matcher(Client.name).find();
            String text = shouldChange ? "§r" + Client.name : Client.name.charAt(0) + "§r§f" + Client.name.substring(1) +
                    "§7[§f" + Minecraft.getDebugFPS() + " FPS§7]§r ";
            String text3 = shouldChange ? "§r" + Client.name : Client.name.charAt(0) + "§r§f" + Client.name.substring(1);
            switch (waterMarkmode.get()) {
                case "Exhi":
                    mc.fontRendererObj.drawStringWithShadow(text, 2.0f, 2.0f, color(1).getRGB());
                    break;
                case "Arcane":
                    Bold.get(60).drawString(text3, 5, 5,color(1).getRGB());
                    break;
                case "Sigma":
                    Light.get(60).drawString(text3, 5, 5,new Color(255, 255, 255,190).getRGB());
                    Light.get(20).drawString("Beta1.0", 5, 35,new Color(255, 255, 255,190).getRGB());
                    break;
                case "Exhibition":
                    String text2 = "§fArc§rance§f" + " - " + mc.thePlayer.getName() + " - " + " - " + PingerUtils.getPing() + "ms ";

                    float x = 4.5f, y = 4.5f;

                    int lineColor = new Color(59, 57, 57).darker().getRGB();
                    Gui.drawRect2(x, y, Semibold.get(16).getStringWidth(text2) + 7, 18.5, new Color(59, 57, 57).getRGB());

                    Gui.drawRect2(x + 2.5, y + 2.5, Semibold.get(16).getStringWidth(text2) + 2, 13, new Color(23, 23, 23).getRGB());

                    // Top small bar
                    Gui.drawRect2(x + 1, y + 1, Semibold.get(16).getStringWidth(text2) + 5, .5, lineColor);

                    // Bottom small bar
                    Gui.drawRect2(x + 1, y + 17, Semibold.get(16).getStringWidth(text2) + 5, .5, lineColor);

                    // Left bar
                    Gui.drawRect2(x + 1, y + 1.5, .5, 16, lineColor);

                    // Right Bar
                    Gui.drawRect2((x + 1.5) + Semibold.get(16).getStringWidth(text2) + 4, y + 1.5, .5, 16, lineColor);

                    // Lowly saturated rainbow bar

                    GradientUtil.drawGradientLR(x + 2.5f, y + 14.5f, Semibold.get(16).getStringWidth(text2) + 2, 1, 1,mainColor.get(), secondColor.get());

                    // Bottom of the rainbow bar
                    Gui.drawRect2(x + 2.5, y + 16, Semibold.get(16).getStringWidth(text2) + 2, .5, lineColor);
                    Semibold.get(16).drawString(text2, x + 4.5f, y + 5.5f, secondColor.get().getRGB());
                    break;
            }
        }
        if (info.get()){
            switch (infomode.get()) {
                case "Exhi":
                    mc.fontRendererObj.drawStringWithShadow("XYZ: " + EnumChatFormatting.WHITE +
                                    xyzFormat.format(mc.thePlayer.posX) + " " +
                                    xyzFormat.format(mc.thePlayer.posY) + " " +
                                        xyzFormat.format(mc.thePlayer.posZ) + " " + EnumChatFormatting.RESET + "BPS: " + EnumChatFormatting.WHITE + this.bpsFormat.format(getBPS())
                            , 2, (int) ((event.getScaledResolution().getScaledHeight() - 9) + (mc.currentScreen instanceof GuiChat ? -14.0f : -3.0f)), color());
                    break;
                case "Arcane":
                    Bold.get(18).drawString("XYZ: " + EnumChatFormatting.WHITE +
                            xyzFormat.format(mc.thePlayer.posX) + " " +
                            xyzFormat.format(mc.thePlayer.posY) + " " +
                            xyzFormat.format(mc.thePlayer.posZ) + " " + EnumChatFormatting.RESET,5,event.getScaledResolution().getScaledHeight() - 20,color());
                    Bold.get(18).drawString("BPS: " + EnumChatFormatting.WHITE + this.bpsFormat.format(getBPS()),5,event.getScaledResolution().getScaledHeight() - 30,color());
                    break;
            }
        }
    }

    public void renderShaders() {
        if (!this.getState()) return;

        if (blur.get()) {
            Blur.startBlur();
            Client.Instance.getEventManager().call(new Shader2DEvent(Shader2DEvent.ShaderType.BLUR));
            Blur.endBlur(blurRadius.getValue().floatValue(), blurCompression.getValue().floatValue());
        }

        if (bloom.get()) {
            stencilFramebuffer = RenderUtil.createFrameBuffer(stencilFramebuffer);
            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(false);
            Client.Instance.getEventManager().call(new Shader2DEvent(Shader2DEvent.ShaderType.GLOW));
            stencilFramebuffer.unbindFramebuffer();

            Bloom.renderBlur(stencilFramebuffer.framebufferTexture, (int) glowRadius.get().floatValue(), (int) glowOffset.get().floatValue());
        }

        if (shadow.get()) {
            stencilFramebuffer = RenderUtil.createFrameBuffer(stencilFramebuffer, true);
            stencilFramebuffer.framebufferClear();
            stencilFramebuffer.bindFramebuffer(true);
            Client.Instance.getEventManager().call(new Shader2DEvent(Shader2DEvent.ShaderType.SHADOW));
            stencilFramebuffer.unbindFramebuffer();

            Shadow.renderBloom(stencilFramebuffer.framebufferTexture, (int) shadowRadius.get().floatValue(), (int) shadowOffset.get().floatValue());
        }
    }
    @EventTarget
    public void onTick(TickEvent event) {
        mainColor.setRainbow(colorMode.is("Rainbow"));
        KillAura aura = getModule(KillAura.class);
        if (aura.isEnabled()) {
            animationEntityPlayerMap.entrySet().removeIf(entry -> entry.getKey().isDead || (!aura.targets.contains(entry.getKey()) && entry.getKey() != mc.thePlayer));
        }
        if (!aura.isEnabled() && !(mc.currentScreen instanceof GuiChat)) {
            Iterator<Map.Entry<EntityPlayer, DecelerateAnimation>> iterator = animationEntityPlayerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<EntityPlayer, DecelerateAnimation> entry = iterator.next();
                DecelerateAnimation animation = entry.getValue();

                animation.setDirection(Direction.BACKWARDS);
                if (animation.finished(Direction.BACKWARDS)) {
                    iterator.remove();
                }
            }
        }
        if (!aura.targets.isEmpty() && !(mc.currentScreen instanceof GuiChat)) {
            for (EntityLivingBase entity : aura.targets) {
                if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
                    animationEntityPlayerMap.putIfAbsent((EntityPlayer) entity, new DecelerateAnimation(175, 1));
                    animationEntityPlayerMap.get(entity).setDirection(Direction.FORWARDS);
                }
            }
        }
        if (aura.isEnabled() && aura.target == null && !(mc.currentScreen instanceof GuiChat)) {
            Iterator<Map.Entry<EntityPlayer, DecelerateAnimation>> iterator = animationEntityPlayerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<EntityPlayer, DecelerateAnimation> entry = iterator.next();
                DecelerateAnimation animation = entry.getValue();

                animation.setDirection(Direction.BACKWARDS);
                if (animation.finished(Direction.BACKWARDS)) {
                    iterator.remove();
                }
            }
        }
        if (mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiChat DropDownClickGui || mc.currentScreen instanceof GuiChat ArcaneClickGui) {
            animationEntityPlayerMap.putIfAbsent(mc.thePlayer, new DecelerateAnimation(175, 1));
            animationEntityPlayerMap.get(mc.thePlayer).setDirection(Direction.FORWARDS);
        }
    }
    public static String getPing() {
        int latency = 0;
        if (!mc.isSingleplayer()) {
            NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
            if (info != null) latency = info.getResponseTime();

            if (isOnHypixel() && latency == 1) {
                int temp = Client.INSTANCE.getPingerUtils().getServerPing().intValue();
                if (temp != -1) {
                    latency = temp;
                }
            }
        } else {
            return "SinglePlayer";
        }

        return latency == 0 ? "?" : String.valueOf(latency);
    }
    public static boolean isOnHypixel() {
        if (mc.isSingleplayer() || mc.getCurrentServerData() == null || mc.getCurrentServerData().serverIP == null)
            return false;
        String ip = mc.getCurrentServerData().serverIP.toLowerCase();
        if (ip.contains("hypixel")) {
            if (mc.thePlayer == null) return true;
            String brand = mc.thePlayer.getClientBrand();
            return brand != null && brand.startsWith("Hypixel BungeeCord");
        }
        return false;
    }
    public int color() {
        return color(1).getRGB();
    }

    public static double getBPS() {
        return getBPS(mc.thePlayer);
    }

    public static double getBPS(EntityPlayer player) {
        if (player == null || player.ticksExisted < 1) {
            return 0.0;
        }
        return getDistance(player.lastTickPosX, player.lastTickPosZ) * (20.0f * mc.timer.timerSpeed);
    }
    public static double getDistance(final double x, final double z) {
        final double xSpeed = mc.thePlayer.posX - x;
        final double zSpeed = mc.thePlayer.posZ - z;
        return MathHelper.sqrt_double(xSpeed * xSpeed + zSpeed * zSpeed);
    }
    public static int colors(int tick) {
        return color(tick).getRGB();
    }
    public static Color color(int tick) {
        Color textColor = new Color(-1);
        switch (colorMode.get()) {
            case "Fade":
                textColor = ColorUtil.fade(5, tick * 20, new Color(mainColor.get().getRGB()), 1);
                break;
            case "Static":
                textColor = mainColor.get();
                break;
            case "Astolfo" :
                textColor = new Color(ColorUtil.swapAlpha(astolfoRainbow(tick, mainColor.getSaturation(), mainColor.getBrightness()), 255));
                break;
            case "Rainbow":
                textColor = new Color(RenderUtil.getRainbow(System.currentTimeMillis(), 2000, tick));;
                break;
            case "Tenacity":
                textColor = ColorUtil.interpolateColorsBackAndForth(colorspeed.getValue().intValue(), Client.Instance.getModuleManager().getAllModules().size() * tick, mainColor.get(), secondColor.get(), false);
                break;
            case "Dynamic":
                textColor = new Color(ColorUtil.swapAlpha(ColorUtil.colorSwitch(mainColor.get(), new Color(ColorUtil.darker(mainColor.get().getRGB(), 0.25F)), 2000.0F, 0, 1 * 10, colorspeed.get()).getRGB(), 255));
                break;
            case "Double":
                tick *= 200;
                textColor = new Color(RenderUtil.colorSwitch(mainColor.get(), secondColor.get(), 2000, -tick / 40, 75, 2));
                break;
        }
        return textColor;
    }
    public static int astolfoRainbow(final int offset, final float saturation, final float brightness) {
        double currentColor = Math.ceil((double)(System.currentTimeMillis() + offset * 20L)) / 6.0;
        return Color.getHSBColor(((float)((currentColor %= 360.0) / 360.0) < 0.5) ? (-(float)(currentColor / 360.0)) : ((float)(currentColor / 360.0)), saturation, brightness).getRGB();
    }
}
