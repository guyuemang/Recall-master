package qwq.arcane.module.impl.world;

import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketReceiveEvent;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.player.Blink;
import qwq.arcane.utils.math.Vector2f;
import qwq.arcane.utils.pack.PacketUtil;

public class Stuck
        extends Module {
    private static Stuck INSTANCE;
    private double x;
    private double y;
    private double z;
    private double motionX;
    private double motionY;
    private double motionZ;
    private boolean onGround = false;
    private Vector2f rotation;
    private boolean delayingC0F = false;
    public boolean thrown = false;
    private boolean closing = false;

    public Stuck() {
        super("Stuck", Category.World);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        Client.Instance.getModuleManager().getModule(Blink.class).setState(false);
        if (Stuck.mc.thePlayer == null) {
            return;
        }
        this.onGround = Stuck.mc.thePlayer.onGround;
        this.x = Stuck.mc.thePlayer.posX;
        this.y = Stuck.mc.thePlayer.posY;
        this.z = Stuck.mc.thePlayer.posZ;
        this.motionX = Stuck.mc.thePlayer.motionX;
        this.motionY = Stuck.mc.thePlayer.motionY;
        this.motionZ = Stuck.mc.thePlayer.motionZ;
        this.rotation = new Vector2f(Stuck.mc.thePlayer.rotationYaw, Stuck.mc.thePlayer.rotationPitch);
        float f = Stuck.mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        float gcd = f * f * f * 1.2f;
        this.rotation.x -= this.rotation.x % gcd;
        this.rotation.y -= this.rotation.y % gcd;
        this.delayingC0F = true;
        this.thrown = false;
    }

    @Override
    public void onDisable() {

        this.delayingC0F = false;
    }

    @EventTarget
    public void onPacket(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            this.setState(false);
        }
    }
@EventTarget
public void onPacketSend(PacketSendEvent event) {
    if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
        C08PacketPlayerBlockPlacement packet = (C08PacketPlayerBlockPlacement)event.getPacket();
        Vector2f current = new Vector2f(Stuck.mc.thePlayer.rotationYaw, Stuck.mc.thePlayer.rotationPitch);
        float f = Stuck.mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        float gcd = f * f * f * 1.2f;
        current.x -= current.x % gcd;
        current.y -= current.y % gcd;
        if (this.rotation.equals(current)) {
            return;
        }
        this.rotation = current;
        event.setCancelled(true);
        PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C05PacketPlayerLook(current.x, current.y, this.onGround));
        PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(Stuck.mc.thePlayer.getHeldItem()));
    }
    if (event.getPacket() instanceof C03PacketPlayer) {
        event.setCancelled(true);
    }
}
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        Stuck.mc.thePlayer.motionX = 0.0;
        Stuck.mc.thePlayer.motionY = 0.0;
        Stuck.mc.thePlayer.motionZ = 0.0;
        Stuck.mc.thePlayer.setPosition(this.x, this.y, this.z);
    }

    public static boolean isStuck() {
        return false;
    }

    public static void onS08() {
        Stuck.INSTANCE.closing = true;
        INSTANCE.setState(false);
        Stuck.INSTANCE.closing = false;
    }

    public static void throwPearl(Vector2f current) {
        if (!INSTANCE.getState()) {
            return;
        }
        Stuck.mc.thePlayer.rotationYaw = current.x;
        Stuck.mc.thePlayer.rotationPitch = current.y;
        float f = Stuck.mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        float gcd = f * f * f * 1.2f;
        current.x -= current.x % gcd;
        current.y -= current.y % gcd;
        if (!Stuck.INSTANCE.rotation.equals(current)) {
            PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C05PacketPlayerLook(current.x, current.y, Stuck.INSTANCE.onGround));
        }
        Stuck.INSTANCE.rotation = current;
        PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(Stuck.mc.thePlayer.getHeldItem()));
    }
}

