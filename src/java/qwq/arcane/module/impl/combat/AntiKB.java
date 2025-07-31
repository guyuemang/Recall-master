package qwq.arcane.module.impl.combat;

import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.viamcp.fixes.AttackOrder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.*;
import net.minecraft.world.WorldSettings;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketReceiveEvent;
import qwq.arcane.event.impl.events.player.AttackEvent;
import qwq.arcane.event.impl.events.player.MoveInputEvent;
import qwq.arcane.event.impl.events.player.StrafeEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.math.Vector2f;
import qwq.arcane.utils.pack.PacketUtil;
import qwq.arcane.utils.player.PlayerUtil;
import qwq.arcane.utils.time.TimerUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.ModeValue;
import qwq.arcane.value.impl.NumberValue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:05 AM
 */
public class AntiKB extends Module {
    public AntiKB() {
        super("AntiKB",Category.Combat);
    }

    private final ModeValue mode = new ModeValue("Mode","Predicted", new String[]{"Watchdog","Grim","Predicted","Jump Reset","Prediction"});
    private final ModeValue jumpResetMode = new ModeValue("Jump Reset Mode", () -> mode.is("Jump Reset"), "Packet", new String[]{"Hurt Time", "Packet", "Advanced"});
    private final NumberValue jumpResetHurtTime = new NumberValue("Jump Reset Hurt Time", () -> mode.is("Jump Reset") && (jumpResetMode.is("Hurt Time") || jumpResetMode.is("Advanced")), 9, 1, 10, 1);
    private final NumberValue jumpResetChance = new NumberValue("Jump Reset Chance", () -> mode.is("Jump Reset") && jumpResetMode.is("Advanced"), 100, 0, 100, 1);
    private final NumberValue hitsUntilJump = new NumberValue("Hits Until Jump", () -> mode.is("Jump Reset") && jumpResetMode.is("Advanced"), 2, 1, 10, 1);
    private final NumberValue ticksUntilJump = new NumberValue("Ticks Until Jump", () -> mode.is("Jump Reset") && jumpResetMode.is("Advanced"), 2, 1, 20, 1);
    private final BoolValue flagCheckValue = new BoolValue("Flag Check", false);
    public NumberValue flagTicksValue = new NumberValue("Flag Ticks", 6.0, 0.0, 30.0, 1.0);
    public NumberValue attackCountValue = new NumberValue("Attack Counts", 12.0, 1.0, 16.0, 1.0);

    private final BoolValue fireCheckValue = new BoolValue("FireCheck", false);
    private final BoolValue waterCheckValue = new BoolValue("WaterCheck", false);
    private final BoolValue fallCheckValue = new BoolValue("FallCheck", false);
    private final BoolValue consumecheck = new BoolValue("ConsumableCheck", false);
    private final BoolValue raycastValue = new BoolValue("Ray cast", false);
    private boolean state;
    private int hitsCount = 0;
    private int ticksCount = 0;
    private boolean veloPacket = false;
    private boolean isFallDamage;
    private final Random random = new Random();
    private TimerUtil timer = new TimerUtil();
    private TimerUtil flagtimer = new TimerUtil();

    public boolean velocityInput;
    private boolean grim_1_17Velocity;
    private boolean attacked;
    private double reduceXZ;
    private int flags;
    @Override
    public void onEnable() {
        velocityInput = false;
        attacked = false;
    }
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        this.setsuffix(mode.is("Grim") ? (ViaLoadingBase.getInstance().getTargetVersion().getVersion() >= 755 ? "Grim1.17+" : "Reduce") : mode.getValue());
        switch (mode.get()) {
            case "Watchdog":
                if (mc.thePlayer.onGround) {
                    state = false;
                }
                break;
            case "Grim": {
                if (grim_1_17Velocity) {
                    PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround));
                    PacketUtil.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, new BlockPos(mc.thePlayer).up(), EnumFacing.DOWN));
                    PacketUtil.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.thePlayer).up(), EnumFacing.DOWN));
                    grim_1_17Velocity = false;
                }
                if (flagCheckValue.getValue()) {
                    if (flags > 0)
                        flags--;
                }
                if (ViaLoadingBase.getInstance().getTargetVersion().getVersion() > 47) {

                    if (velocityInput) {

                        if (attacked) {
                            mc.thePlayer.motionX *= reduceXZ;
                            mc.thePlayer.motionZ *= reduceXZ;
                            attacked = false;
                        }
                        if (mc.thePlayer.hurtTime == 0) {
                            velocityInput = false;
                        }

                    }


                } else {
                    //The velocity mode 1.8.9 ok!
                    if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
                        mc.thePlayer.addVelocity(-1.3E-10, -1.3E-10, -1.3E-10);
                        mc.thePlayer.setSprinting(false);
                    }
                }
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
        if (!mode.is("Grim")) {
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
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            flagtimer.reset();
            if (flagCheckValue.getValue()) {
                flags = flagTicksValue.getValue().intValue();
            }
        }
        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            if (mode.is("Grim")) {
                if (flags != 0) return;
                if (mc.thePlayer.isDead) return;
                if (mc.currentScreen instanceof GuiGameOver) return;
                if (mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR) return;
                if (mc.thePlayer.isOnLadder()) return;
                if (mc.thePlayer.isBurning() && fireCheckValue.getValue()) return;
                if (mc.thePlayer.isInWater() && waterCheckValue.getValue()) return;
                if (mc.thePlayer.fallDistance > 1.5 && fallCheckValue.getValue()) return;
                if (flagCheckValue.getValue() && !flagtimer.hasTimeElapsed(1000)) return;
                if (mc.thePlayer.isEatingOrDrinking() && consumecheck.getValue()) return;
                if (soulSandCheck()) return;
            }
            if (((S12PacketEntityVelocity) event.getPacket()).getEntityID() == mc.thePlayer.getEntityId()) {
                S12PacketEntityVelocity s12 = ((S12PacketEntityVelocity) event.getPacket());
                attacked = false;
                switch (mode.getValue()) {
                    case "Grim": {
                        if (ViaLoadingBase.getInstance().getTargetVersion().getVersion() >= 755) {
                            event.setCancelled(true);
                            grim_1_17Velocity = true;
                        } else {
                            double horizontalStrength = new Vector2f(s12.getMotionX(), s12.getMotionZ()).length();
                            if (horizontalStrength <= 1000) return;
                            MovingObjectPosition mouse = mc.objectMouseOver;
                            velocityInput = true;
                            Entity entity = null;
                            reduceXZ = 1;

                            if (mouse.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && mouse.entityHit instanceof EntityLivingBase && mc.thePlayer.getClosestDistanceToEntity(mouse.entityHit) <= 3) {
                                entity = mouse.entityHit;
                            }

                            if (entity == null && !raycastValue.getValue()) {
                                Entity target = KillAura.target;
                                if (target != null && KillAura.shouldAttack()) {
                                    entity = KillAura.target;
                                }
                            }

                            boolean state = mc.thePlayer.serverSprintState;

                            if (entity != null) {
                                if (!state) {
                                    PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                                }
                                Client.Instance.getEventManager().call(new AttackEvent(entity));
                                int count = attackCountValue.get().intValue();
                                for (int i = 1; i <= count; i++) {
                                    AttackOrder.sendFixedAttackByPacket(mc.thePlayer, entity);
                                }
                                if (!state) {
                                    PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                                }
                                attacked = true;
                                reduceXZ = 0.07776;
                            }
                        }
                    }
                    break;
                }
            }
        }
        if (packet instanceof S27PacketExplosion && ViaLoadingBase.getInstance().getTargetVersion().getVersion() >= 755) {
            event.setCancelled(true);
            grim_1_17Velocity = true;
        }
    }
    public static boolean soulSandCheck() {
        final AxisAlignedBB par1AxisAlignedBB = Minecraft.getMinecraft().thePlayer.getEntityBoundingBox().contract(0.001, 0.001,
                0.001);
        final int var4 = MathHelper.floor_double(par1AxisAlignedBB.minX);
        final int var5 = MathHelper.floor_double(par1AxisAlignedBB.maxX + 1.0);
        final int var6 = MathHelper.floor_double(par1AxisAlignedBB.minY);
        final int var7 = MathHelper.floor_double(par1AxisAlignedBB.maxY + 1.0);
        final int var8 = MathHelper.floor_double(par1AxisAlignedBB.minZ);
        final int var9 = MathHelper.floor_double(par1AxisAlignedBB.maxZ + 1.0);
        for (int var11 = var4; var11 < var5; ++var11) {
            for (int var12 = var6; var12 < var7; ++var12) {
                for (int var13 = var8; var13 < var9; ++var13) {
                    final BlockPos pos = new BlockPos(var11, var12, var13);
                    final Block var14 = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
                    if (var14 instanceof BlockSoulSand) {
                        return true;
                    }
                }
            }
        }
        return false;
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
    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (mode.is("Predicted") && getModule(KillAura.class).target != null && mc.thePlayer.hurtTime > 0) {
            ArrayList<Vec3> vec3s = new ArrayList<>();
            HashMap<Vec3, Integer> map = new HashMap<>();
            Vec3 playerPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            Vec3 onlyForward = PlayerUtil.getPredictedPos(1.0F, 0.0F).add(playerPos);
            Vec3 strafeLeft = PlayerUtil.getPredictedPos(1.0F, 1.0F).add(playerPos);
            Vec3 strafeRight = PlayerUtil.getPredictedPos(1.0F, -1.0F).add(playerPos);
            map.put(onlyForward, 0);
            map.put(strafeLeft, 1);
            map.put(strafeRight, -1);
            vec3s.add(onlyForward);
            vec3s.add(strafeLeft);
            vec3s.add(strafeRight);
            Vec3 targetVec = new Vec3(getModule(KillAura.class).target.posX, getModule(KillAura.class).target.posY, getModule(KillAura.class).target.posZ);
            vec3s.sort(Comparator.comparingDouble(targetVec::distanceXZTo));
            if (!mc.thePlayer.movementInput.sneak) {
                System.out.println(map.get(vec3s.get(0)));
                mc.thePlayer.movementInput.moveStrafe = map.get(vec3s.get(0));
            }
        }
    }

    private boolean checks() {
        return mc.thePlayer.isInWeb || mc.thePlayer.isInLava() || mc.thePlayer.isBurning() || mc.thePlayer.isInWater();
    }
}
