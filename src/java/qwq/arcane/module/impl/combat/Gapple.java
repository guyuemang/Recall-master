package qwq.arcane.module.impl.combat;


import qwq.arcane.module.Mine;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.TickEvent;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.event.impl.events.player.MoveMathEvent;
import qwq.arcane.event.impl.events.player.SlowDownEvent;
import qwq.arcane.event.impl.events.render.Render2DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.animations.AnimationUtils;
import qwq.arcane.utils.animations.impl.ContinualAnimation;
import qwq.arcane.utils.animations.impl.RippleAnimation;
import qwq.arcane.utils.pack.PacketUtil;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.utils.render.shader.ShaderElement;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.ModeValue;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.concurrent.LinkedBlockingQueue;


public class Gapple extends Module {
    public BoolValue render = new BoolValue("Render", true);
    public ModeValue renderMode = new ModeValue("RenderMode", "SouthSide", new String[]{"Client", "SouthSide", "Old","Naven"});
    public int eattick;
    public static boolean isS12;
    private final LinkedBlockingQueue<Packet<?>> packets;
    private final ContinualAnimation anim = new ContinualAnimation();
    public static int i;
    private float x;
    private float y;
    private float width;
    private double progressRender;
    private final RippleAnimation rippleAnimation = new RippleAnimation();

    public Gapple() {
        super("Gapple", Category.Combat);
        this.eattick = 0;
        this.packets = new LinkedBlockingQueue<>();
    }

    public void onEnable() {
        this.eattick = 0;
        this.packets.clear();
        eating = false;
    }

    public static boolean eating = false;


    public void onDisable() {
        eating = false;
        releaseall();
    }
    @EventTarget
    public void onMoveMath(MoveMathEvent event) {
        if (Client.Instance.getModuleManager().getModule(Gapple.class).getState()) {
            if (Mine.getMinecraft().thePlayer.positionUpdateTicks < 19 && !Gapple.isS12) {
                return;
            } else if (Gapple.isS12) {
                Gapple.isS12 = false;
            }
        }
    }


    @EventTarget
    public void onTick(TickEvent e) {
        setsuffix(renderMode.get());
        mc.thePlayer.setSprinting(false);
    }

    @EventTarget
    public void onMotion(MotionEvent e) {
        if (e.isPost()) {
            this.packets.add(new C01PacketChatMessage("cnm"));
        }

        if (e.isPre()) {
            if (mc.thePlayer == null || !mc.thePlayer.isEntityAlive()) {
                setState(false);
                return;
            }

            if (findgapple() == -100) {
                setState(false);
                return;

            }

            eating = true;

            if (this.eattick >= 33) {
                PacketUtil.sendPacketNoEvent((Packet) new C09PacketHeldItemChange(findgapple()));
                PacketUtil.sendPacketNoEvent((Packet) new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                releaseall();
                PacketUtil.sendPacketNoEvent((Packet) new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                this.eattick = 0;
            } else if (mc.thePlayer.ticksExisted % 5 == 0) {
                while (!this.packets.isEmpty()) {
                    Packet<?> packet = this.packets.poll();
                    if (packet instanceof C01PacketChatMessage) {
                        break;
                    }

                    if (packet instanceof net.minecraft.network.play.client.C03PacketPlayer) {
                        this.eattick--;
                    }
                    mc.getNetHandler().addToSendQueueUnregistered(packet);
                }
            }
        }
    }


    public int findgapple() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i + 36).getStack();
            if (stack != null) {
                if (stack.getItem() instanceof net.minecraft.item.ItemAppleGold)
                    return i;
            }
        }
        return -100;
    }


    private void releaseall() {
        if (mc.getNetHandler() == null)
            return;
        while (!this.packets.isEmpty()) {
            Packet<?> packet = this.packets.poll();
            if (packet instanceof C01PacketChatMessage || packet instanceof net.minecraft.network.play.client.C07PacketPlayerDigging || packet instanceof net.minecraft.network.play.client.C0EPacketClickWindow || packet instanceof net.minecraft.network.play.client.C0DPacketCloseWindow)
                continue;
            mc.getNetHandler().addToSendQueueUnregistered(packet);
        }
        this.eattick = 0;
    }

    @EventTarget
    public void onPacket(PacketSendEvent e) {
            Packet<?> packet = e.getPacket();
            if (packet instanceof net.minecraft.network.handshake.client.C00Handshake || packet instanceof net.minecraft.network.login.client.C00PacketLoginStart || packet instanceof net.minecraft.network.status.client.C00PacketServerQuery || packet instanceof net.minecraft.network.status.client.C01PacketPing || packet instanceof net.minecraft.network.login.client.C01PacketEncryptionResponse || packet instanceof C01PacketChatMessage) {
                return;
            }

            if (packet instanceof net.minecraft.network.play.client.C03PacketPlayer) {
                this.eattick++;
            }
            if (packet instanceof net.minecraft.network.play.client.C07PacketPlayerDigging || packet instanceof C09PacketHeldItemChange || packet instanceof net.minecraft.network.play.client.C0EPacketClickWindow || packet instanceof net.minecraft.network.play.client.C0DPacketCloseWindow) {
                e.setCancelled(true);
                return;
            }
            if (!(packet instanceof C08PacketPlayerBlockPlacement) && eating) {
                this.packets.add(packet);
                e.setCancelled(true);
            }
    }


    @EventTarget
    public void onSlow(SlowDownEvent eventSlowDown) {
        eventSlowDown.setCancelled(false);
        eventSlowDown.setForward(0.2F);
        eventSlowDown.setStrafe(0.2F);
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!render.getValue()) return;
        switch (renderMode.getValue()) {
            case "Client": {
                if (mc.thePlayer.getHeldItem() != null && eating) {
                    this.x = AnimationUtils.animate((float) event.getScaledResolution().getScaledWidth() / 2.0f - 42.0f, this.x, 0.5f);
                    this.y = AnimationUtils.animate(!eating ? (float) (event.getScaledResolution().getScaledHeight() - 20) : (float) event.getScaledResolution().getScaledHeight() / 2.0f + 20.0f, this.y, 0.5f);
                    this.width = AnimationUtils.animate(eating ? 86.0f : 20.0f, this.width, 0.5f);
                    this.progressRender = AnimationUtils.animate(eattick * 1.78f, this.progressRender, 0.2f);
                    ShaderElement.addBlurTask(() -> RoundedUtil.drawRound(this.x - 2.0f, this.y - 2.0f, this.width, 20.0f, 4.0f, false, new Color(0, 0, 0, 255)));
                    ShaderElement.addBloomTask(() -> RoundedUtil.drawRound(this.x - 2.0f, this.y - 2.0f, this.width, 20.0f, 4.0f, false, new Color(0, 0, 0, 255)));
                    this.rippleAnimation.draw(() -> RoundedUtil.drawRound(this.x - 2.0f, this.y - 2.0f, this.width, 20.0f, 4.0f, false, new Color(0, 0, 0, 100)));
                    RoundedUtil.drawRound(this.x - 2.0f, this.y - 2.0f, this.width, 20.0f, 4.0f, false, new Color(0, 0, 0, 100));
                    if (eating) {
                        RoundedUtil.drawRound(this.x + 20.0f, this.y + 3.0f, 60.0f, 10.0f, 4.0f, false, new Color(0, 0, 0, 60));
                        RoundedUtil.drawRound(this.x + 20.0f, this.y + 3.0f, (float) this.progressRender, 10.0f, 4.0f, false, Color.WHITE);
                    }
                    ItemStack goldenAppleStack = new ItemStack(Item.getItemById(322));
                    RenderUtil.drawItemStack(goldenAppleStack, (int) this.x, (int) this.y);
                }
                break;
            }
            case "SouthSide": {
                ScaledResolution resolution = new ScaledResolution(mc);
                int x = resolution.getScaledWidth() / 2;
                int y = resolution.getScaledHeight() - 75;
                float thickness = 5F;

                float percentage = (float) (120.0f * (eattick / 34.0)) * ((float)100 / 100);

                final int width = 100;
                final int half = width / 2;
                AnimationUtils.animate((width - 2) * percentage, 40, 1);

                RoundedUtil.drawRound(x - half - 1, y - 1 - 12, width + 1, (int) (thickness + 1) + 12 + 3, 2, new Color(17, 17, 17, 215));
                RoundedUtil.drawRound(x - half - 1, y - 1, width + 1, (int) (thickness + 1), 2, new Color(17, 17, 17, 215));

                RoundedUtil.drawGradientHorizontal(x - half, y + 1,Math.min(percentage,100), (int) (thickness), 2, new Color(128, 255, 255), new Color(128, 128, 255));
                Bold.get(22).drawString("Time", x - 15, y - 1 - 10, Color.WHITE.getRGB());
                Bold.get(22).drawString(new DecimalFormat("0.0").format(percentage * 0.9) + "%", x - 11, y + 1.5f, new Color(207, 207, 207).getRGB());
                break;
            }
            case "Old": {
                ScaledResolution sr = event.getScaledResolution();
                int startX = sr.getScaledWidth() / 2 - 58;
                int startY = sr.getScaledHeight() / 2 + 50;
                anim.animate(Math.min(3.75f * i, 120.0f), 20);
                float target = (float) (120.0f * (eattick / 34.0)) * ((float) 120 / 120);

        GlStateManager.disableAlpha();
        //RoundedUtil.drawRound(startX, startY + 7.5f, 120.0f, 11f, new Color(0, 0, 0, 60).getRGB());
        RoundedUtil.drawRound(startX - 38, (float) (startY), 170.0f, 28.0f, 8.0f, new Color(0, 0, 0, 80));
        RoundedUtil.drawRound(startX, (float) (startY + 7.5), 124.0f, 11.0f, 5.0f, new Color(0, 0, 0, 80));
        RoundedUtil.drawGradientRound(startX, startY + 7.5f, Math.min(target, 120.0f), 11f,5.0f, InterFace.color(1),InterFace.color(7),InterFace.color(14),InterFace.color(21));
        //FontManager.tenacitybold18.drawCenteredString("C03Packet", startX + 58, startY + 9f, -1);
                Bold.get(18).drawString("Gapple", startX - 34, startY + 9f, -1);
        GlStateManager.disableAlpha();
        break;
            }
            case "Naven": {
                ScaledResolution sr = new ScaledResolution(mc);
                float progress = Math.min((eattick / 34.0f), 1.0f);
                float barWidth = 80.0f;
                float barHeight = 2.0f;
                int centerX = sr.getScaledWidth() / 2;
                int centerY = sr.getScaledHeight() / 2;
                float startX = centerX - barWidth / 2;
                float startY = centerY - 30;
                String text = "Eating Ticks";
                int textWidth = Bold.get(18).getStringWidth(text);
                Bold.get(18).drawString(text, centerX - textWidth / 2f, startY - 3, -1);
                RoundedUtil.drawGradientRound(startX, startY + 7.5F, barWidth, barHeight, 3.0F, new Color(0, 0, 0, 200), new Color(0, 0, 0, 150), new Color(0, 0, 0, 150), new Color(0, 0, 0, 150));
                float target = barWidth * progress;
                RoundedUtil.drawGradientRound(startX, startY + 7.5F, target, barHeight, 3.0F, new Color(143, 49, 46, 220), new Color(143, 49, 46, 220), new Color(143, 49, 46, 220), new Color(143, 49, 46, 220));

                break;
            }
        }
    }
}

