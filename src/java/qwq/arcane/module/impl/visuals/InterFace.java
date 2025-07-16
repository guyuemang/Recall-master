package qwq.arcane.module.impl.visuals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.render.Render2DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.value.impl.*;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
    public static ColorValue mainColor = new ColorValue("MainColor", new Color(183, 109, 250));
    public static ColorValue secondColor = new ColorValue("SecondColor", new Color(115, 75, 109));
    public static BoolValue waterMark = new BoolValue("WaterMark",false);
    public static ModeValue waterMarkmode = new ModeValue("WaterMarkMode",()-> waterMark.get(),"Exhi",new String[]{"Exhi","Arcane"});
    public static BoolValue info = new BoolValue("Info",true);
    public static final ModeValue infomode = new ModeValue("InfoMode",()->info.get(),"Exhi",new String[]{"Exhi","Arcane"});
    public static BoolValue renderBossHealth = new BoolValue("BossHealth",false);
    private final DecimalFormat bpsFormat = new DecimalFormat("0.00");
    private final DecimalFormat xyzFormat = new DecimalFormat("0");
    @EventTarget
    public void onRender(Render2DEvent event) {
        setsuffix(colorMode.get());
        if (waterMark.get()) {
            LocalTime currentTime1 = LocalTime.now();
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("HH:mm");
            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern(" a");
            boolean shouldChange = RenderUtil.COLOR_PATTERN.matcher(Client.name).find();
            String text = shouldChange ? "§r" + Client.name : Client.name.charAt(0) + "§r§f" + Client.name.substring(1) +
                    "§7[§f" + Minecraft.getDebugFPS() + " FPS§7]§r ";
            String text2 = shouldChange ? "§r" + Client.name : Client.name.charAt(0) + "§r§f" + Client.name.substring(1);
            switch (waterMarkmode.get()) {
                case "Exhi":
                    mc.fontRendererObj.drawStringWithShadow(text, 2.0f, 2.0f, color(1).getRGB());
                    break;
                case "Arcane":
                    Bold.get(40).drawString(text2, 5, 5,color(1).getRGB());
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
