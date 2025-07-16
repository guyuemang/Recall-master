package qwq.arcane.module.impl.combat;

import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketReceiveEvent;
import qwq.arcane.event.impl.events.player.StrafeEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.value.impl.ModeValue;
import qwq.arcane.value.impl.NumberValue;

import java.util.Random;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:05 AM
 */
public class AntiKB extends Module {
    public AntiKB() {
        super("AntiKB",Category.Combat);
    }

    private final ModeValue mode = new ModeValue("Mode","Watchdog", new String[]{"Watchdog","Jump Reset","Prediction"});
    private final ModeValue jumpResetMode = new ModeValue("Jump Reset Mode", () -> mode.is("Jump Reset"), "Packet", new String[]{"Hurt Time", "Packet", "Advanced"});
    private final NumberValue jumpResetHurtTime = new NumberValue("Jump Reset Hurt Time", () -> mode.is("Jump Reset") && (jumpResetMode.is("Hurt Time") || jumpResetMode.is("Advanced")), 9, 1, 10, 1);
    private final NumberValue jumpResetChance = new NumberValue("Jump Reset Chance", () -> mode.is("Jump Reset") && jumpResetMode.is("Advanced"), 100, 0, 100, 1);
    private final NumberValue hitsUntilJump = new NumberValue("Hits Until Jump", () -> mode.is("Jump Reset") && jumpResetMode.is("Advanced"), 2, 1, 10, 1);
    private final NumberValue ticksUntilJump = new NumberValue("Ticks Until Jump", () -> mode.is("Jump Reset") && jumpResetMode.is("Advanced"), 2, 1, 20, 1);
    private boolean state;
    private int hitsCount = 0;
    private int ticksCount = 0;
    private boolean veloPacket = false;
    private boolean isFallDamage;
    private final Random random = new Random();
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        setsuffix(mode.get());
        switch (mode.get()) {
            case "Watchdog":
                if (mc.thePlayer.onGround) {
                    state = false;
                }
                break;
            case "Jump Reset":
                if (jumpResetMode.is("Advanced")) {
                    if (mc.thePlayer.hurtTime == 9) {
                        hitsCount++;
                    }
                    ticksCount++;
                }
                break;
        }
    }

    @EventTarget
    public void onPacket(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof S12PacketEntityVelocity s12 && s12.getEntityID() == mc.thePlayer.getEntityId()) {
            switch (mode.get()) {
                case "Jump Reset":
                    if (jumpResetMode.is("Packet")) {
                        veloPacket = true;
                    } else if (jumpResetMode.is("Advanced")) {
                        double velocityX = s12.getMotionX() / 8000.0;
                        double velocityY = s12.getMotionY() / 8000.0;
                        double velocityZ = s12.getMotionZ() / 8000.0;

                        isFallDamage = velocityX == 0.0 && velocityZ == 0.0 && velocityY < 0;
                    }
                    break;
                case "Watchdog":
                    if (!mc.thePlayer.onGround) {
                        if (!state) {
                            event.setCancelled(true);
                            state = true;
                            return;
                        }
                    }
                    s12.motionX = (int) (mc.thePlayer.motionX * 8000);
                    s12.motionZ = (int) (mc.thePlayer.motionZ * 8000);
                    break;
            }
        }
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (mode.is("Jump Reset")) {
            boolean shouldJump = false;

            if (jumpResetMode.is("Packet") && veloPacket) {
                shouldJump = true;
            } else if (jumpResetMode.is("Hurt Time") && mc.thePlayer.hurtTime >= jumpResetHurtTime.getValue()) {
                shouldJump = true;
            } else if (jumpResetMode.is("Advanced")) {
                if (random.nextInt(100) > jumpResetChance.getValue()) return;

                boolean hitsCondition = hitsCount >= hitsUntilJump.getValue();
                boolean ticksCondition = ticksCount >= ticksUntilJump.getValue();

                shouldJump = mc.thePlayer.hurtTime == 9 && mc.thePlayer.isSprinting() &&
                        !isFallDamage && (hitsCondition || ticksCondition);
            }

            if (shouldJump && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && !checks()) {
                mc.thePlayer.jump();
                veloPacket = false;
                hitsCount = 0;
                ticksCount = 0;
            }
        }
    }

    private boolean checks() {
        return mc.thePlayer.isInWeb || mc.thePlayer.isInLava() || mc.thePlayer.isBurning() || mc.thePlayer.isInWater();
    }
}
