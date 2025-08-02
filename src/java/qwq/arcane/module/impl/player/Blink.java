package qwq.arcane.module.impl.player;

import com.yumegod.obfuscation.FlowObfuscate;
import com.yumegod.obfuscation.InvokeDynamic;
import com.yumegod.obfuscation.Rename;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.TickEvent;
import qwq.arcane.event.impl.events.misc.WorldLoadEvent;
import qwq.arcane.event.impl.events.packet.HigherPacketEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.event.impl.events.render.Render2DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.pack.PacketUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.ModeValue;
import qwq.arcane.value.impl.NumberValue;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Rename
@FlowObfuscate
@InvokeDynamic
public class Blink extends Module {
    private final LinkedList<List<Packet<?>>> packets = new LinkedList<>();
    public EntityOtherPlayerMP fakePlayer;
    public int ticks;
    public ModeValue modeValue = new ModeValue("Mode", "Slow Release", new String[]{"Simple", "Slow Release", "Delay"});
    public final NumberValue Release_Value = new NumberValue("Delay", () -> modeValue.is("Slow Release"), 100, 50, 200, 1);
    public BoolValue AutoClose = new BoolValue("AutoClose", false);
    public BoolValue auraValue = new BoolValue("Aura Support", true);
    public BoolValue render = new BoolValue("Render", true);
    public ModeValue renderMode = new ModeValue("RenderMode", () -> render.getValue(), "Naven", new String[]{"Naven"});

    public Blink() {
        super("Blink", Category.Player);
    }

    @Override
    public void onEnable() {
        if (Blink.mc.thePlayer == null) {
            return;
        }
        this.packets.clear();
        this.packets.add(new ArrayList<>());
        this.ticks = 0;
        this.fakePlayer = new EntityOtherPlayerMP(Blink.mc.theWorld, Blink.mc.thePlayer.getGameProfile());
        this.fakePlayer.clonePlayer(Blink.mc.thePlayer, true);
        this.fakePlayer.copyLocationAndAnglesFrom(Blink.mc.thePlayer);
        this.fakePlayer.rotationYawHead = Blink.mc.thePlayer.rotationYawHead;
        this.fakePlayer.noClip = true;//移除碰撞
        Blink.mc.theWorld.addEntityToWorld(-1337, this.fakePlayer);
    }

    @EventTarget
    public void onWorld(WorldLoadEvent event) {
        this.setState(false);
    }

    @Override
    public void onDisable() {
        this.packets.forEach(this::sendTick);
        this.packets.clear();
        try {
            if (this.fakePlayer != null) {
                Blink.mc.theWorld.removeEntity(this.fakePlayer);
            }
        } catch (Exception exception) {
            // empty catch block
        }
    }

    @EventTarget
    public void onPacket(HigherPacketEvent event) {
        Packet packet = event.getPacket();
        if (PacketUtil.isCPacket(packet)) {
            mc.addScheduledTask(() -> this.packets.getLast().add(packet));
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        ++this.ticks;
        this.packets.add(new ArrayList<>());
        switch (this.modeValue.getValue()) {
            case "Delay": {
                if (this.packets.size() <= 100) break;
                this.poll();
                break;
            }
            case "Slow Release": {
                if (this.packets.size() <= Release_Value.getValue() && this.ticks % 5 != 0) break;
                this.poll();
            }
        }
    }

    private void poll() {
        if (this.packets.isEmpty()) {
            return;
        }
        this.sendTick(this.packets.getFirst());
        this.packets.removeFirst();
    }

    @Override
    public String getSuffix() {
        return this.modeValue.getValue();
    }

    private void sendTick(List<Packet<?>> tick) {
        tick.forEach(packet -> {
            mc.getNetHandler().getNetworkManager().sendUnregisteredPacket(packet);
            this.handleFakePlayerPacket(packet);
        });
    }

    private void handleFakePlayerPacket(Packet<?> packet) {
        if (packet instanceof C03PacketPlayer.C04PacketPlayerPosition) {
            C03PacketPlayer.C04PacketPlayerPosition position = (C03PacketPlayer.C04PacketPlayerPosition) packet;
            this.fakePlayer.setPositionAndRotation2(position.x, position.y, position.z, this.fakePlayer.rotationYaw, this.fakePlayer.rotationPitch, 3, true);
            this.fakePlayer.onGround = position.isOnGround();
        } else if (packet instanceof C03PacketPlayer.C05PacketPlayerLook) {
            C03PacketPlayer.C05PacketPlayerLook rotation = (C03PacketPlayer.C05PacketPlayerLook) packet;
            this.fakePlayer.setPositionAndRotation2(this.fakePlayer.posX, this.fakePlayer.posY, this.fakePlayer.posZ, rotation.getYaw(), rotation.getPitch(), 3, true);
            this.fakePlayer.onGround = rotation.isOnGround();
            this.fakePlayer.rotationYawHead = rotation.getYaw();
            this.fakePlayer.rotationYaw = rotation.getYaw();
            this.fakePlayer.rotationPitch = rotation.getPitch();
        } else if (packet instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
            C03PacketPlayer.C06PacketPlayerPosLook positionRotation = (C03PacketPlayer.C06PacketPlayerPosLook) packet;
            this.fakePlayer.setPositionAndRotation2(positionRotation.x, positionRotation.y, positionRotation.z, positionRotation.getYaw(), positionRotation.getPitch(), 3, true);
            this.fakePlayer.onGround = positionRotation.isOnGround();
            this.fakePlayer.rotationYawHead = positionRotation.getYaw();
            this.fakePlayer.rotationYaw = positionRotation.getYaw();
            this.fakePlayer.rotationPitch = positionRotation.getPitch();
        } else if (packet instanceof C0BPacketEntityAction) {
            C0BPacketEntityAction action = (C0BPacketEntityAction) packet;
            if (action.getAction() == C0BPacketEntityAction.Action.START_SPRINTING) {
                this.fakePlayer.setSprinting(true);
            } else if (action.getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                this.fakePlayer.setSprinting(false);
            } else if (action.getAction() == C0BPacketEntityAction.Action.START_SNEAKING) {
                this.fakePlayer.setSneaking(true);
            } else if (action.getAction() == C0BPacketEntityAction.Action.STOP_SNEAKING) {
                this.fakePlayer.setSneaking(false);
            }
        } else if (packet instanceof C0APacketAnimation) {
            this.fakePlayer.swingItem();
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (Blink.mc.thePlayer.hurtTime > 0 && this.AutoClose.getValue()) {
            this.setState(false);
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!render.getValue()) return;
        switch (renderMode.getValue()) {
            case "Naven": {
                float radius = 2.0f;
                ScaledResolution scaledResolution = new ScaledResolution(mc);
                int screenWidth = scaledResolution.getScaledWidth();
                int screenHeight = scaledResolution.getScaledHeight();
                float width = 80.0f;
                float height = 3.0f;
                float progress = ticks < 100 ? (float) (ticks * 0.8) : 80;
                int x = (int) (screenWidth / 2f - width / 2f);
                int y = screenHeight / 2 - 15;
                RoundedUtil.drawRound(x, y, width, height, radius, new Color(0, 0, 0, 150));
                RoundedUtil.drawRound(x, y, progress, height, radius, new Color(143, 49, 46, 220));
            }
        }
    }
}
