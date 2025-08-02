package qwq.arcane.module.impl.visuals;

import com.yumegod.obfuscation.FlowObfuscate;
import com.yumegod.obfuscation.InvokeDynamic;
import com.yumegod.obfuscation.Rename;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import org.lwjgl.opengl.GL11;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketReceiveEvent;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.event.impl.events.render.Render2DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.render.OGLUtils;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.time.TimerUtil;
import qwq.arcane.value.impl.*;

import java.awt.*;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 12:31
 */
@Rename
@FlowObfuscate
@InvokeDynamic
public final class Hitmarkers extends Module {
    public Hitmarkers() {
        super("Hitmarkers",Category.Visuals);
    }
    private final ColorValue hitColorProperty = new ColorValue("Hit Color", new Color(0xFFFFFFFF));
    private final ColorValue killColorProperty = new ColorValue("Kill Color", new Color(0xFFFF0000));

    private final NumberValue xOffsetProperty = new NumberValue(
            "X Offset", 2.0D, 0.5D, 10.0D, 0.5D);
    private final NumberValue lengthProperty = new NumberValue(
            "Length", 4.0D, 0.5D, 10.0D, 0.5D);
    private final NumberValue hitMarkerThicknessProperty = new NumberValue(
            "Thickness", 1.0D, 0.5D, 3.0D, 0.5D);

    private final BoolValue soundsProperty = new BoolValue("Sounds", true);
    public final NumberValue volumeProperty = new NumberValue("Volume",()-> soundsProperty.get(), 100, 0, 100, 1);
    private final ModeValue soundTypeProperty = new ModeValue("Sound Type",()-> soundsProperty.get(),"Custom", new String[]{"BASIC", "RIFK", "SKEET"});

    private final TimerUtil attackTimeOut = new TimerUtil();
    private final TimerUtil killTimeOut = new TimerUtil();

    private int color;
    private double progress;

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (progress > 0.0D) {
            progress = RenderUtil.linearAnimation(progress, 0.0D, 0.02D);

            final ScaledResolution resolution = event.getScaledResolution();

            final double xMiddle = resolution.getScaledWidth() / 2.0D;
            final double yMiddle = resolution.getScaledHeight() / 2.0D;

            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
            OGLUtils.enableBlending();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glTranslated(xMiddle, yMiddle, 0.0D);
            GL11.glRotatef(45.0F, 0.0F, 0.0F, 1.0F);
            OGLUtils.color(RenderUtil.fadeTo(
                    removeAlphaComponent(this.color),
                    this.color,
                    (float) progress));
            for (int i = 0; i < 4; i++) {
                drawHitMarker(xOffsetProperty.getValue(), lengthProperty.getValue(), hitMarkerThicknessProperty.getValue());
                if (i != 3)
                    GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
            }
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glPopMatrix();
        }
    };

    private int lastAttackedEntity;
    @EventTarget
    public void onPacketSendEvent(PacketSendEvent event){
        final Packet<?> packet = event.getPacket();
        if (packet instanceof C02PacketUseEntity) {
            final C02PacketUseEntity packetUseEntity = (C02PacketUseEntity) packet;
            if (packetUseEntity.getAction() == C02PacketUseEntity.Action.ATTACK) {
                lastAttackedEntity = packetUseEntity.getEntityId();
                attackTimeOut.reset2();
            }
        } else if (packet instanceof C03PacketPlayer) {
            if (lastAttackedEntity != -1 && attackTimeOut.hasElapsed(500))
                lastAttackedEntity = -1;
        }
    };
    private int toBeKilledEntity;
    @EventTarget
    public void onPacketReceiveEvent(PacketReceiveEvent event){
        final Packet<?> packet = event.getPacket();
        if (packet instanceof S19PacketEntityStatus) {
            S19PacketEntityStatus packetEntityStatus = (S19PacketEntityStatus) packet;
            final int entityId = packetEntityStatus.getEntityId();
            if (entityId == lastAttackedEntity || (!killTimeOut.hasElapsed(50) && entityId == toBeKilledEntity)) {
                switch (packetEntityStatus.getOpCode()) {
                    case 2:
                        color = hitColorProperty.get().getRGB();
                        progress = 1.0D;
                        killTimeOut.reset2();
                        toBeKilledEntity = lastAttackedEntity;
                        if (soundsProperty.getValue())
                            playSound();
                        break;
                    case 3:
                        color = killColorProperty.get().getRGB();
                        progress = 1.0D;
                        toBeKilledEntity = -1;
                        break;
                }
                lastAttackedEntity = -1;
            }
        }
    };

    private static int removeAlphaComponent(int color) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;

        return ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF) |
                ((0) << 24);
    }

    private static void drawHitMarker(double xOffset, double length, double width) {
        final double halfWidth = width * 0.5D;

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2d(-(xOffset + length), -halfWidth);
        GL11.glVertex2d(-(xOffset + length), halfWidth);
        GL11.glVertex2d(-xOffset, halfWidth);
        GL11.glVertex2d(-xOffset, -halfWidth);
        GL11.glEnd();
    }

    private void playSound() {
        switch(soundTypeProperty.getValue()) {
            case "SKEET":
                Minecraft.getMinecraft().getSoundHandler().playSoundFromFile("skeet.ogg", mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                break;
            case "NEKO":
                Minecraft.getMinecraft().getSoundHandler().playSoundFromFile("neko.ogg", mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                break;
            case "RIFK":
                Minecraft.getMinecraft().getSoundHandler().playSoundFromFile("rifk.ogg", mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                break;
            case "BASIC":
                Minecraft.getMinecraft().getSoundHandler().playSoundFromFile("basic.ogg", mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                break;
        }
    }
}
