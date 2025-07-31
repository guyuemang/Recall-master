package qwq.arcane.module.impl.movement;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketReceiveEvent;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.combat.KillAura;
import qwq.arcane.module.impl.player.Blink;
import qwq.arcane.module.impl.world.Scaffold;
import qwq.arcane.utils.player.MovementUtil;
import qwq.arcane.utils.player.PlayerUtil;
import qwq.arcane.utils.rotation.RotationManager;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.ModeValue;

import java.util.Iterator;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:03 AM
 */
public class Speed extends Module {
    public Speed() {
        super("Speed",Category.Movement);
    }

    private final ModeValue mode = new ModeValue("Mode", "Grim", new String[]{"Watchdog", "Grim", "AutoJump"});
    private final ModeValue watchdogmode = new ModeValue("WatchDog Mode", () -> this.mode.is("Watchdog"), "Ground", new String[]{"Ground", "Glide", "Glide2", "Test","LowHop"});
    private final BoolValue lagbackcheck = new BoolValue("LagBackCheck", true);
    public final BoolValue strafe = new BoolValue("Grim-Strafe", () -> mode.is("Grim"), false);
    public final BoolValue strafe1 = new BoolValue("Strafe", () -> mode.is("Watchdog"), false);
    private final BoolValue scaffoldCheck = new BoolValue("Scaffold Check", false);
    private final BoolValue blinkCheck = new BoolValue("Blink Check", false);

    private int inAirTicks;

    @Override
    public void onEnable() {
        inAirTicks = 0;
        if (mc.thePlayer == null) return;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1.0f;
        super.onDisable();
    }

    @EventTarget
    private void onPacketReceive(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof S08PacketPlayerPosLook) {
            if (this.lagbackcheck.getValue()) {
                this.setState(false);
            }
        }
    }

    @EventTarget
    private void onUpdate(UpdateEvent event) {
        this.setsuffix(mode.getValue());
        if ((blinkCheck.getValue() && Client.Instance.getModuleManager().getModule(Blink.class).getState()) || (scaffoldCheck.getValue() && Client.Instance.getModuleManager().getModule(Scaffold.class).getState())) {
            return;
        }
        if (mode.is("Watchdog")) {
            if (watchdogmode.is("Ground")) {
                if (mc.thePlayer.onGround && MovementUtil.isMoving()) {
                    mc.thePlayer.jump();
                    MovementUtil.strafe(0.45);
                }
            } else if (watchdogmode.is("Glide")) {
                if (MovementUtil.isMoving()) {
                    if ((mc.thePlayer.offGroundTicks == 10) && MovementUtil.isOnGround(0.769)) {
                        mc.thePlayer.motionY = 0;
                    }

                    if (MovementUtil.isOnGround(0.769) && mc.thePlayer.offGroundTicks >= 9) {
                        MovementUtil.strafe(0.29);
                    }

                    if (mc.thePlayer.onGround) {
                        if (mc.gameSettings.keyBindForward.isPressed()) MovementUtil.strafe(0.28);
                        else MovementUtil.strafe(0.45);
                        mc.thePlayer.jump();
                    }
                }
            } else if (watchdogmode.is("Glide2")) {
                if (MovementUtil.isMoving()) {
                    if ((mc.thePlayer.offGroundTicks == 10 || mc.thePlayer.offGroundTicks == 11) && MovementUtil.isOnGround(0.769)) {
                        mc.thePlayer.motionY = 0;
                        MovementUtil.strafe(0.15);
                    }

                    if (mc.thePlayer.onGround) {
                        if (mc.gameSettings.keyBindForward.pressed) MovementUtil.strafe(0.28);
                        else MovementUtil.strafe(0.45);
                        mc.thePlayer.jump();
                    }
                }
            } else if (watchdogmode.is("LowHop")) {
                if (MovementUtil.isMoving()) {
                    if (PlayerUtil.isBlockUnder(mc.thePlayer)) {
                        if (mc.thePlayer.onGround) {
                            mc.thePlayer.jump();
                        }
                        if (mc.thePlayer.offGroundTicks == 9 && strafe.getValue()) {
                            mc.thePlayer.motionY = -0.06;
                            MovementUtil.strafe(0.3F);
                        } else {
                            if (mc.thePlayer.onGround) {
                                mc.thePlayer.jump();
                                MovementUtil.strafe((0.476 + MovementUtil.getSpeedEffect() * 0.04));
                            }
                        }
                    } else {
                        switch (mc.thePlayer.offGroundTicks) {
                            case 0:
                                mc.thePlayer.jump();
                                MovementUtil.strafe(0.485);
                                break;
                            case 5:
                                if (strafe1.getValue())
                                    MovementUtil.strafe(0.315);
                                mc.thePlayer.motionY = MovementUtil.predictedMotion(mc.thePlayer.motionY, 2);
                                break;
                            case 6:
                                if (strafe1.getValue())
                                    MovementUtil.strafe(0.415);
                                break;
                        }
                    }
                }
            }
        } else if (watchdogmode.is("Test")) {
            if (mc.thePlayer.onGround) {
                if (MovementUtil.isMoving()) {
                    mc.thePlayer.jump();
                    MovementUtil.setSpeed(MovementUtil.getBaseMoveSpeed() * 1.6);
                    this.inAirTicks = 0;
                }
            } else {
                ++this.inAirTicks;
                if (this.inAirTicks == 1) {
                    MovementUtil.setSpeed(MovementUtil.getBaseMoveSpeed() * 1.16);
                }
            }

        } else if (mode.is("AutoJump")) {
            if (MovementUtil.isMoving() && mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically) {
                mc.thePlayer.jump();
            }
        }
    }


    @EventTarget
    private void onMotion(MotionEvent event) {
        if ((blinkCheck.getValue() && Client.Instance.getModuleManager().getModule(Blink.class).getState()) || (scaffoldCheck.getValue() && Client.Instance.getModuleManager().getModule(Scaffold.class).getState())) {
            return;
        }
        if (mode.is("Grim")) {
            AxisAlignedBB playerBox = mc.thePlayer.boundingBox.expand(1.0D, 1.0D, 1.0D);
            int c = 0;
            Iterator<Entity> entitys = mc.theWorld.loadedEntityList.iterator();

            while(true) {
                Entity entity;
                do {
                    if (!entitys.hasNext()) {
                        if (c > 0 && MovementUtil.isMoving()) {
                            double strafeOffset = (double)Math.min(c, 3) * 0.03D;
                            float yaw = this.getMoveYaw();
                            double mx = -Math.sin(Math.toRadians(yaw));
                            double mz = Math.cos(Math.toRadians(yaw));
                            mc.thePlayer.addVelocity(mx * strafeOffset, 0.0D, mz * strafeOffset);
                            if (c < 4 && KillAura.target != null && this.shouldFollow()) {
                                mc.gameSettings.keyBindLeft.pressed = true;
                            } else {
                                mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft);
                            }
                            return;
                        } else {
                            mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft);
                            return;
                        }
                    }

                    entity = entitys.next();
                } while(!(entity instanceof EntityLivingBase) && !(entity instanceof EntityBoat) && !(entity instanceof EntityMinecart) && !(entity instanceof EntityFishHook));

                if (!(entity instanceof EntityArmorStand) && entity.getEntityId() != mc.thePlayer.getEntityId() && playerBox.intersectsWith(entity.boundingBox) && entity.getEntityId() != -8 && entity.getEntityId() != -1337 && !(Client.Instance.getModuleManager().getModule(Blink.class)).getState()) {
                    ++c;
                }
            }
        }
    }

    public boolean shouldFollow() {
        return this.getState() && mc.gameSettings.keyBindJump.isKeyDown();
    }

    private float getMoveYaw() {
        EntityPlayerSP thePlayer = mc.thePlayer;
        float moveYaw = thePlayer.rotationYaw;
        if (thePlayer.moveForward != 0.0F && thePlayer.moveStrafing == 0.0F) {
            moveYaw += thePlayer.moveForward > 0.0F ? 0.0F : 180.0F;
        } else if (thePlayer.moveForward != 0.0F) {
            if (thePlayer.moveForward > 0.0F) {
                moveYaw += thePlayer.moveStrafing > 0.0F ? -45.0F : 45.0F;
            } else {
                moveYaw -= thePlayer.moveStrafing > 0.0F ? -45.0F : 45.0F;
            }

            moveYaw += thePlayer.moveForward > 0.0F ? 0.0F : 180.0F;
        } else if (thePlayer.moveStrafing != 0.0F) {
            moveYaw += thePlayer.moveStrafing > 0.0F ? -70.0F : 70.0F;
        }

        if (KillAura.target != null && mc.gameSettings.keyBindJump.isKeyDown()) {
            moveYaw = RotationManager.rotation.x;
        }

        return moveYaw;
    }
}
