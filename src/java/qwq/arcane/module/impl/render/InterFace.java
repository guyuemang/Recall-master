package qwq.arcane.module.impl.render;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
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
    public static TextValue name = new TextValue("ClientName","Arcane");
    public static ModeValue colorMode = new ModeValue("Color Mode", "Fade", new String[]{"Fade", "Static", "Double"});
    public static ColorValue mainColor = new ColorValue("MainColor", new Color(183, 109, 250));
    public static ColorValue secondColor = new ColorValue("SecondColor", new Color(115, 75, 109));
    public static BooleanValue waterMark = new BooleanValue("WaterMark",false);
    public static ModeValue waterMarkmode = new ModeValue("WaterMarkMode","Custom",new String[]{"Custom","Arcane"});

    private final DecimalFormat bpsFormat = new DecimalFormat("0.00");
    @EventTarget
    public void onRender(Render2DEvent e) {
        if (waterMark.get()) {
            LocalTime currentTime1 = LocalTime.now();
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("HH:mm");
            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern(" a");
            String formattedTime1 = currentTime1.format(formatter1);
            String formattedTime2 = currentTime1.format(formatter2);
            boolean shouldChange = RenderUtil.COLOR_PATTERN.matcher(name.get()).find();
            String text = shouldChange ? "§r" + name.getText() : name.getText().charAt(0) + "§r§f" + name.getText().substring(1) +
                    "§7[§f" + Minecraft.getDebugFPS() + " FPS§7]§r ";
            switch (waterMarkmode.get()) {
                case "Custom":
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
            case "Double":
                tick *= 200;
                textColor = new Color(RenderUtil.colorSwitch(mainColor.get(), secondColor.get(), 2000, -tick / 40, 75, 2));
                break;
        }
        return textColor;
    }
}
