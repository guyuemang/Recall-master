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
import qwq.arcane.utils.render.RoundedUtil;
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
    public static ModeValue colorMode = new ModeValue("Color Mode", "Fade", new String[]{"Fade", "Rainbow", "Astolfo","Tenacity", "Static", "Double"});
    public static final NumberValue colorspeed = new NumberValue("ColorSpeed", () -> colorMode.is("Tenacity"), 4, 1, 10, 1);
    public static ColorValue mainColor = new ColorValue("MainColor", new Color(183, 109, 250));
    public static ColorValue secondColor = new ColorValue("SecondColor", new Color(115, 75, 109));
    public static BooleanValue waterMark = new BooleanValue("WaterMark",false);
    public static ModeValue waterMarkmode = new ModeValue("WaterMarkMode",()-> waterMark.get(),"Exhi",new String[]{"Exhi","Arcane"});
    public static BooleanValue info = new BooleanValue("Info",true);
    public static final ModeValue infomode = new ModeValue("InfoMode",()->info.get(),"Exhi",new String[]{"Exhi"});
    public static BooleanValue renderBossHealth = new BooleanValue("BossHealth",false);
    private final DecimalFormat bpsFormat = new DecimalFormat("0.00");
    private final DecimalFormat xyzFormat = new DecimalFormat("0");
    @EventTarget
    public void onRender(Render2DEvent event) {
        setsuffix(colorMode.get());
        if (waterMark.get()) {
            LocalTime currentTime1 = LocalTime.now();
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("HH:mm");
            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern(" a");
            String formattedTime1 = currentTime1.format(formatter1);
            String formattedTime2 = currentTime1.format(formatter2);
            boolean shouldChange = RenderUtil.COLOR_PATTERN.matcher(Client.name).find();
            String text = shouldChange ? "§r" + Client.name : Client.name.charAt(0) + "§r§f" + Client.name.substring(1) +
                    "§7[§f" + Minecraft.getDebugFPS() + " FPS§7]§r ";
            switch (waterMarkmode.get()) {
                case "Exhi":
                    mc.fontRendererObj.drawStringWithShadow(text, 2.0f, 2.0f, color(1).getRGB());
                    break;
                case "Arcane":
                    RoundedUtil.drawRound(4,4,Bold.get(22).getStringWidth(text) + Bold.get(18).getStringWidth(mc.getDebugFPS() + "FPS") + Bold.get(18).getStringWidth(mc.getDebugFPS() + "FPS"),30,6,new Color(1,1,1,190));
                    Bold.get(22).drawString("ARC", 12, 10,-1);
                    Bold.get(22).drawStringDynamic("ANE", 12 + Bold.get(22).getStringWidth("ARC"), 10,1,7);
                    Semibold.get(18).drawString(mc.getDebugFPS() + "" + EnumChatFormatting.GRAY + "FPS", 12, 22,-1);
                    Semibold.get(18).drawString( this.bpsFormat.format(getBPS()) + EnumChatFormatting.GRAY + "BPS", 22 + Bold.get(18).getStringWidth(mc.getDebugFPS() + "FPS"), 22,-1);
                    Semibold.get(18).drawString(formattedTime1, 42 + Bold.get(18).getStringWidth(mc.getDebugFPS() + "FPS") + Bold.get(18).getStringWidth(mc.getDebugFPS() + "FPS"), 22,-1);
                    Semibold.get(18).drawString(EnumChatFormatting.GRAY + formattedTime2, 42 + Semibold.get(18).getStringWidth(formattedTime1) + Bold.get(18).getStringWidth(mc.getDebugFPS() + "FPS") + Bold.get(18).getStringWidth(mc.getDebugFPS() + "FPS"), 22,-1);
                    break;
            }
        }
        if (info.get()){
            switch (infomode.get()) {
                case "Exhi":
                    float textY = (event.getScaledResolution().getScaledHeight() - 9) + (mc.currentScreen instanceof GuiChat ? -14.0f : -3.0f);
                    mc.fontRendererObj.drawStringWithShadow("XYZ: " + EnumChatFormatting.WHITE +
                                    xyzFormat.format(mc.thePlayer.posX) + " " +
                                    xyzFormat.format(mc.thePlayer.posY) + " " +
                                    xyzFormat.format(mc.thePlayer.posZ) + " " + EnumChatFormatting.RESET + "BPS: " + EnumChatFormatting.WHITE + this.bpsFormat.format(getBPS())
                            , 2, textY, color());
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
