package qwq.arcane.module.impl.combat;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.CancellableEvent;
import qwq.arcane.event.impl.events.misc.TickEvent;
import qwq.arcane.event.impl.events.packet.PacketReceiveEvent;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.event.impl.events.render.Render3DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.world.Scaffold;
import qwq.arcane.utils.animations.impl.ContinualAnimation;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.time.TimerUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.NumberValue;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:05 AM
 */
public class BackTrack extends Module {
    public BackTrack() {
        super("BackTrack", Category.Combat);
    }
    public static NumberValue delayProperty = new NumberValue("Delayed Position Time", 400.0, 0.0, 1000.0, 10.0);
    public BoolValue legitProperty = new BoolValue("Legit", false);
    public BoolValue releaseOnHitProperty = new BoolValue("Release Upon Hit", legitProperty::isAvailable, true);
    public NumberValue hitRangeProperty = new NumberValue("Hit Range", 3.0, 0.0, 10.0, 0.1);
    public BoolValue onlyIfNeedProperty = new BoolValue("Only If Needed", true);

    public static final ArrayList<Packet> incomingPackets = new ArrayList<>();

    public static final ArrayList<Packet> outgoingPackets = new ArrayList<>();

    public double lastRealX;

    public double lastRealY;

    public double lastRealZ;

    private WorldClient lastWorld;

    private EntityLivingBase entity;

    public TimerUtil timer = new TimerUtil();
    private final Map<UUID, Deque<Vec3>> backtrackPositions = new HashMap<UUID, Deque<Vec3>>();

    @Override
    public void onEnable() {
        incomingPackets.clear();
        outgoingPackets.clear();
    }

    @EventTarget
    public void onPacketReceiveEvent(PacketReceiveEvent e){
        EntityLivingBase entityLivingBase;
        Entity packetEntity;
        if (mc.thePlayer == null || mc.theWorld == null || !Client.INSTANCE.getModuleManager().getModule(KillAura.class).isEnabled() || mc.getNetHandler().getNetworkManager().getNetHandler() == null) {
            incomingPackets.clear();
            return;
        }
        if (Client.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled()) {
            incomingPackets.clear();
            return;
        }

        if (e.getPacket() instanceof S08PacketPlayerPosLook) {
            incomingPackets.clear();
            return;
        }

        this.entity = KillAura.target;

        if (e.getPacket() instanceof S14PacketEntity) {
            final S14PacketEntity packet = (S14PacketEntity) e.getPacket();
            packetEntity = mc.theWorld.getEntityByID(packet.entityId);
            if (packetEntity instanceof EntityLivingBase) {
                entityLivingBase = (EntityLivingBase) packetEntity;
                entityLivingBase.realPosX += packet.func_149062_c();
                entityLivingBase.realPosY += packet.func_149061_d();
                entityLivingBase.realPosZ += packet.func_149064_e();
            }
        }
        if (e.getPacket() instanceof S18PacketEntityTeleport) {
            final S18PacketEntityTeleport packet2 = (S18PacketEntityTeleport) e.getPacket();
            packetEntity = mc.theWorld.getEntityByID(packet2.getEntityId());
            if (packetEntity instanceof EntityLivingBase) {
                entityLivingBase = (EntityLivingBase) packetEntity;
                entityLivingBase.realPosX = packet2.getX();
                entityLivingBase.realPosY = packet2.getY();
                entityLivingBase.realPosZ = packet2.getZ();
            }
        }

        if (mc.theWorld != null && lastWorld != mc.theWorld) {
            resetIncomingPackets(mc.getNetHandler().getNetworkManager().getNetHandler());
            lastWorld = mc.theWorld;
            return;
        }
        if (this.entity == null || onlyIfNeedProperty.getValue() && mc.thePlayer.getDistanceToEntity(this.entity) < 3.0f) {
            resetIncomingPackets(mc.getNetHandler().getNetworkManager().getNetHandler());
        } else {
            addIncomingPackets(e.getPacket(), e);
        }
    };

    @EventTarget
    public void onPacketSendEvent(PacketSendEvent e){
        if (mc.thePlayer == null || mc.theWorld == null || !Client.INSTANCE.getModuleManager().getModule(KillAura.class).isEnabled() || mc.getNetHandler().getNetworkManager().getNetHandler() == null) {
            outgoingPackets.clear();
            return;
        }
        if (Client.INSTANCE.getModuleManager().getModule(Scaffold.class).isEnabled()) {
            outgoingPackets.clear();
            return;
        }
        this.entity = KillAura.target;
        if (mc.theWorld != null && lastWorld != mc.theWorld) {
            resetOutgoingPackets(mc.getNetHandler().getNetworkManager().getNetHandler());
            lastWorld = mc.theWorld;
            return;
        }
        if (this.entity == null || onlyIfNeedProperty.getValue() && mc.thePlayer.getDistanceToEntity(this.entity) < 3.0f) {
            resetOutgoingPackets(mc.getNetHandler().getNetworkManager().getNetHandler());
        } else {
            addOutgoingPackets(e.getPacket(), e);
        }
    };

    @EventTarget
    public void onUpdate(UpdateEvent event){
        setsuffix(delayProperty.get().toString());
        if (this.entity != null && this.entity.getEntityBoundingBox() != null && mc.thePlayer != null && mc.theWorld != null && this.entity.realPosX != 0.0 && this.entity.realPosY != 0.0 && this.entity.realPosZ != 0.0 && this.entity.width != 0.0f && this.entity.height != 0.0f && this.entity.posX != 0.0 && this.entity.posY != 0.0 && this.entity.posZ != 0.0) {
            double realX = this.entity.realPosX / 32.0;
            double realY = this.entity.realPosY / 32.0;
            double realZ = this.entity.realPosZ / 32.0;
            if (!onlyIfNeedProperty.getValue()) {
                if (mc.thePlayer.getDistanceToEntity(this.entity) > 3.0f && mc.thePlayer.getDistance(this.entity.posX, this.entity.posY, this.entity.posZ) >= mc.thePlayer.getDistance(realX, realY, realZ)) {
                    resetIncomingPackets(mc.getNetHandler().getNetworkManager().getNetHandler());
                    resetOutgoingPackets(mc.getNetHandler().getNetworkManager().getNetHandler());
                }
            } else if (mc.thePlayer.getDistance(this.entity.posX, this.entity.posY, this.entity.posZ) >= mc.thePlayer.getDistance(realX, realY, realZ) || mc.thePlayer.getDistance(realX, realY, realZ) < mc.thePlayer.getDistance(lastRealX, lastRealY, lastRealZ)) {
                resetIncomingPackets(mc.getNetHandler().getNetworkManager().getNetHandler());
                resetOutgoingPackets(mc.getNetHandler().getNetworkManager().getNetHandler());
            }
            if (legitProperty.getValue() && releaseOnHitProperty.getValue() && this.entity.hurtTime <= 1) {
                resetIncomingPackets(mc.getNetHandler().getNetworkManager().getNetHandler());
                resetOutgoingPackets(mc.getNetHandler().getNetworkManager().getNetHandler());
            }
            if (mc.thePlayer.getDistance(realX, realY, realZ) > hitRangeProperty.getValue() || timer.hasTimeElapsed(delayProperty.getValue().intValue(), true)) {
                resetIncomingPackets(mc.getNetHandler().getNetworkManager().getNetHandler());
                resetOutgoingPackets(mc.getNetHandler().getNetworkManager().getNetHandler());
            }
            lastRealX = realX;
            lastRealY = realY;
            lastRealZ = realZ;
        }
    };

   @EventTarget
   public void onRender3D(Render3DEvent event){
        if (this.entity == null || this.entity.getEntityBoundingBox() == null || mc.thePlayer == null || mc.theWorld == null || this.entity.realPosX == 0.0 || this.entity.realPosY == 0.0 || this.entity.realPosZ == 0.0 || this.entity.width == 0.0f || this.entity.height == 0.0f || this.entity.posX == 0.0 || this.entity.posY == 0.0 || this.entity.posZ == 0.0)
            return;

        boolean render = true;
        double realX = this.entity.realPosX / 32.0;
        double realY = this.entity.realPosY / 32.0;
        double realZ = this.entity.realPosZ / 32.0;

        if (!onlyIfNeedProperty.getValue()) {
            if (mc.thePlayer.getDistanceToEntity(this.entity) > 3.0f && mc.thePlayer.getDistance(this.entity.posX, this.entity.posY, this.entity.posZ) >= mc.thePlayer.getDistance(realX, realY, realZ)) {
                render = false;
            }
        } else if (mc.thePlayer.getDistance(this.entity.posX, this.entity.posY, this.entity.posZ) >= mc.thePlayer.getDistance(realX, realY, realZ) || mc.thePlayer.getDistance(realX, realY, realZ) < mc.thePlayer.getDistance(lastRealX, lastRealY, lastRealZ)) {
            render = false;
        }

        if (legitProperty.getValue() && releaseOnHitProperty.getValue() && this.entity.hurtTime <= 1) {
            render = false;
        }
        if (mc.thePlayer.getDistance(realX, realY, realZ) > hitRangeProperty.getValue() || timer.hasTimeElapsed(delayProperty.getValue().intValue(), false)) {
            render = false;
        }

        if (this.entity == null || this.entity == mc.thePlayer || this.entity.isInvisible() || !render)
            return;

        if (this.entity.width == 0.0f || this.entity.height == 0.0f) {
            return;
        }

        Color color = Color.WHITE;
        int alpha = 145;

        double x = (this.entity.realPosX / 32.0) - RenderManager.renderPosX;
        double y = (this.entity.realPosY / 32.0) - RenderManager.renderPosY;
        double z = (this.entity.realPosZ / 32.0) - RenderManager.renderPosZ;

        GlStateManager.pushMatrix();
        RenderUtil.start3D();
        RenderUtil.renderBoundingBox(new AxisAlignedBB(x - (double) (this.entity.width / 2.0f), y, z - (double) (this.entity.width / 2.0f), x + (double) (this.entity.width / 2.0f), y + (double) this.entity.height, z + (double) (this.entity.width / 2.0f)), color, alpha);
        RenderUtil.stop3D();
        GlStateManager.popMatrix();
    };

    private void resetIncomingPackets(INetHandler netHandler) {
        if (!incomingPackets.isEmpty()) {
            while (!incomingPackets.isEmpty()) {
                final Packet packet = incomingPackets.get(0);
                try {
                    packet.processPacket(netHandler);
                } catch (ThreadQuickExitException ignored) {
                    // Ignored exception
                }
                incomingPackets.remove(0);
            }
        }
        timer.reset();
    }

    private void addIncomingPackets(Packet packet, CancellableEvent event) {
        if (event != null && packet != null) {
            synchronized (incomingPackets) {
                if (blockPacketIncoming(packet)) {
                    incomingPackets.add(packet);
                    event.setCancelled(true);
                }
            }
        }
    }

    private void resetOutgoingPackets(INetHandler netHandler) {
        if (!outgoingPackets.isEmpty()) {
            while (!outgoingPackets.isEmpty()) {
                final Packet packet = outgoingPackets.get(0);
                try {
                    packet.processPacket(netHandler);
                } catch (ThreadQuickExitException ignored) {
                    // Ignored exception
                }
                outgoingPackets.remove(0);
            }
        }
        timer.reset();
    }

    private void addOutgoingPackets(Packet packet, CancellableEvent event) {
        if (event != null && packet != null) {
            synchronized (outgoingPackets) {
                if (blockPacketsOutgoing(packet)) {
                    outgoingPackets.add(packet);
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isEntityPacket(Packet packet) {
        return packet instanceof S14PacketEntity || packet instanceof S19PacketEntityHeadLook || packet instanceof S18PacketEntityTeleport || packet instanceof S0FPacketSpawnMob;
    }

    private boolean blockPacketIncoming(Packet packet) {
        return packet instanceof S03PacketTimeUpdate || packet instanceof S00PacketKeepAlive || packet instanceof S12PacketEntityVelocity || packet instanceof S27PacketExplosion || packet instanceof S32PacketConfirmTransaction || packet instanceof S08PacketPlayerPosLook || packet instanceof S01PacketPong || this.isEntityPacket(packet);
    }

    private boolean blockPacketsOutgoing(Packet packet) {
        if (!this.legitProperty.getValue()) {
            return false;
        }
        return packet instanceof C03PacketPlayer || packet instanceof C02PacketUseEntity || packet instanceof C0FPacketConfirmTransaction || packet instanceof C08PacketPlayerBlockPlacement || packet instanceof C09PacketHeldItemChange || packet instanceof C07PacketPlayerDigging || packet instanceof C0APacketAnimation || packet instanceof C01PacketPing || packet instanceof C00PacketKeepAlive || packet instanceof C0BPacketEntityAction;
    }
}
