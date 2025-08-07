package qwq.arcane.module.impl.misc;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.Vec3;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.TickEvent;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.combat.KillAura;
import qwq.arcane.module.impl.world.Scaffold;
import qwq.arcane.utils.rotation.RotationUtil;
import qwq.arcane.utils.time.TimerUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.NumberValue;

import java.util.ArrayList;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:06 AM
 */
public class FakeLag extends Module {
    public FakeLag() {
        super("FakeLag", Category.Misc);
    }
    public final BoolValue combat = new BoolValue("Combat", false);
    public final BoolValue onlyMove = new BoolValue("Only Move", false);
    private final NumberValue startDelay = new NumberValue("Start Delay", 300, 0, 1000, 1);
    private final NumberValue lagDuration = new NumberValue("Lag Packets", 600, 0, 1000, 1);

    public int sentC03Packets = 0;
    private boolean shouldBlockPackets;
    private final TimerUtil delayTimer = new TimerUtil();
    private final ArrayList<Packet<?>> packets = new ArrayList<>();

    @Override
    public void onEnable() {
        this.shouldBlockPackets = false;
    }

    @Override
    public void onDisable() {
        this.resetPackets();
    }

    @EventTarget
    public void onTick(TickEvent event) {
        int count = 0;
        for (final Packet<?> p : this.packets) {
            if (p instanceof C03PacketPlayer) {
                ++count;
            }
        }
        this.sentC03Packets = count;

        if (this.combat.get()) {
            if (count > this.lagDuration.getValue() || getModule(Scaffold.class).isEnabled()) {
                this.shouldBlockPackets = false;
            }
        } else if (count <= this.lagDuration.getValue() && !getModule(Scaffold.class).isEnabled()) {
            this.shouldBlockPackets = true;
        } else {
            this.shouldBlockPackets = false;
            this.resetPackets();
        }

        if (count <= this.lagDuration.getValue() && !getModule(Scaffold.class).isEnabled()) {
            if (!this.combat.get()) {
                this.shouldBlockPackets = true;
            }
        } else {
            this.shouldBlockPackets = false;
            this.resetPackets();
        }
    }

    @EventTarget
    public void onPacket(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();

        if (this.combat.get()) {
            if (packet instanceof C02PacketUseEntity) {
                this.shouldBlockPackets = false;
                this.resetPackets();
            } else if (packet instanceof C03PacketPlayer && getModule(KillAura.class).isEnabled() && getModule(KillAura.class).target != null) {
                EntityLivingBase entityLivingBase = getModule(KillAura.class).target;
                if (entityLivingBase instanceof EntityPlayer player) {
                    Vec3 positionEyes = mc.thePlayer.getPositionEyes(1.0f);
                    Vec3 positionEyesServer = mc.thePlayer.getSeverPosition().addVector(0.0, mc.thePlayer.getEyeHeight(), 0.0);
                    Vec3 bestHitVec = RotationUtil.getBestHitVec(player);

                    if (!this.shouldBlockPackets && player.hurtTime < 3 && positionEyes.distanceTo(bestHitVec) > 2.9 &&
                            positionEyes.distanceTo(bestHitVec) < 3.3 && positionEyes.distanceTo(bestHitVec) < positionEyesServer.distanceTo(bestHitVec)) {
                        this.shouldBlockPackets = true;
                    }
                }
            }
        }

        if (mc.theWorld != null && this.shouldBlockPackets && this.delayTimer.reached(this.startDelay.getValue().longValue())) {
            if (this.onlyMove.get()) {
                if (packet instanceof C03PacketPlayer && !this.packets.contains(packet)) {
                    this.packets.add(packet);
                    event.setCancelled(true);
                }
            } else if (!this.packets.contains(packet)) {
                this.packets.add(packet);
                event.setCancelled(true);
            }
        }
    }

    private void resetPackets() {
        if (mc.thePlayer != null) {
            if (!this.packets.isEmpty()) {
                this.packets.forEach(packet -> mc.thePlayer.sendQueue.addToSendQueueDirect(packet));
                this.packets.clear();
            }
        }
        else {
            this.packets.clear();
        }
    }
}
