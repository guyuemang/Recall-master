package qwq.arcane.module.impl.combat;

import de.florianmichael.viamcp.fixes.AttackOrder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.network.INetHandler;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import org.lwjgl.input.Keyboard;
import qwq.arcane.event.impl.events.misc.WorldLoadEvent;
import qwq.arcane.event.impl.events.packet.PacketReceiveEvent;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.event.impl.events.player.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.util.*;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.module.Category;
import qwq.arcane.module.Mine;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.movement.LongJump;
import qwq.arcane.module.impl.movement.Sprint;
import qwq.arcane.utils.chats.ChatUtils;
import qwq.arcane.utils.math.Vector2f;
import qwq.arcane.utils.math.Vector3d;
import qwq.arcane.utils.pack.PacketUtil;
import qwq.arcane.utils.player.MovementUtil;
import qwq.arcane.utils.player.PlayerUtil;
import qwq.arcane.utils.rotation.RayCastUtil;
import qwq.arcane.utils.rotation.RotationUtil;
import qwq.arcane.utils.time.TimerUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.ModeValue;
import qwq.arcane.value.impl.NumberValue;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static qwq.arcane.utils.pack.PacketUtil.sendPacket;
import static qwq.arcane.utils.pack.PacketUtil.sendPacketNoEvent;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:05 AM
 */
public class AntiKB extends Module {
    public AntiKB() {
        super("AntiKB",Category.Combat);
    }
    public final ModeValue mode = new ModeValue("Mode", "Reduce",
            new String[]{"Grim", "Legit", "Legit 2", "Polar", "Intave", "Reduce", "Boost", "Skip Tick","Prediction", "Jump Reset", "Matrix Semi", "Matrix Reverse", "Polar Under-Block"});

    private final ModeValue grimMode = new ModeValue("Grim Mode", () -> mode.is("Grim"), "Reduce", new String[]{"Reduce", "1.17"});
    private final BoolValue invalidEntity = new BoolValue("Attack Invalid Entity", () -> mode.is("Grim") && grimMode.is("Reduce"), true);

    private final NumberValue reverseTick = new NumberValue("Boost Tick", () -> mode.is("Boost"), 1, 1, 5, 1);
    private final NumberValue reverseStrength = new NumberValue("Boost Strength", () -> mode.is("Boost"), 1, 0.1f, 1, 0.01f);

    private final NumberValue skipTicks = new NumberValue("Skip Ticks", () -> mode.is("Skip Tick"), 1, 1, 20, 1);
    private final NumberValue skipChance = new NumberValue("Skip Chance", () -> mode.is("Skip Tick"), 100, 0, 100, 1);

    private final ModeValue jumpResetMode = new ModeValue("Jump Reset Mode", () -> mode.is("Jump Reset") || mode.is("Reduce"), "Packet", new String[]{"Legit", "Packet", "Advanced", "Hurt Time"});
    private final NumberValue jumpResetHurtTime = new NumberValue("Jump Reset Hurt Time", () -> mode.is("Jump Reset") || mode.is("Reduce") && (jumpResetMode.is("Hurt Time") || jumpResetMode.is("Advanced")), 9, 1, 10, 1);
    private final NumberValue jumpResetChance = new NumberValue("Jump Reset Chance", () -> mode.is("Jump Reset") || mode.is("Reduce") &&
            (jumpResetMode.is("Legit") || jumpResetMode.is("Advanced")), 100, 0, 100, 1);
    private final NumberValue hitsUntilJump = new NumberValue("Hits Until Jump", () -> mode.is("Jump Reset") || mode.is("Reduce") && jumpResetMode.is("Advanced"), 2, 1, 10, 1);
    private final NumberValue ticksUntilJump = new NumberValue("Ticks Until Jump", () -> mode.is("Jump Reset") || mode.is("Reduce") && jumpResetMode.is("Advanced"), 2, 1, 20, 1);

    private final BoolValue debugMessage = new BoolValue("Verbose Output", () -> mode.is("Prediction"), false);
    public final NumberValue chance = new NumberValue("Prediction Chance",()->mode.is("Prediction"), 1.0F, 0.0F, 1.0F, 0.01);
    private final BoolValue onlySprint = new BoolValue("Sprint Only", () -> mode.is("Prediction"), false);
    private final BoolValue hypixelPrediction = new BoolValue("Hypixel Prediction", () -> mode.is("Prediction"), false);
    private final BoolValue jumpRotate = new BoolValue("Prediction Rotation", () -> mode.is("Prediction") && hypixelPrediction.getValue(), false);
    private final NumberValue jumpDelay = new NumberValue("Jump Delay", () -> mode.is("Prediction") && hypixelPrediction.getValue(), 5, 0, 20, 1);

    private Entity target;
    private long lastAttackTime;
    private final TimerUtil velocityTimer = new TimerUtil();

    private int idk = 0;
    private int hitsCount = 0;
    private int ticksCount = 0;
    private int skipTickCounter = 0;
    private int reduceTick, reduceDamageTick;
    private boolean sb = false;

    boolean enable;
    private boolean attacked;
    private boolean reducing;
    private boolean isFallDamage;
    public boolean shouldVelocity;
    private boolean veloPacket = false;
    public static boolean jump = false;
    private boolean canSpoof, canCancel;

    private final Random random = new Random();
    public static List<Packet<INetHandler>> storedPackets = new CopyOnWriteArrayList<>();

    @Override
    public void onEnable() {
        ticksCount = 0;
        reducing = false;
        veloPacket = false;
        skipTickCounter = 0;

        storedPackets.clear();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        ticksCount = 0;
        veloPacket = false;
        skipTickCounter = 0;
        if (this.mode.is("Prediction")) {
            mc.gameSettings.keyBindJump.pressed = false;
            mc.gameSettings.keyBindForward.pressed = false;
        }
        if (mode.is("Jump Reset") || mode.is("Reduce") && jumpResetMode.is("Legit")) {
            mc.gameSettings.keyBindJump.setPressed(false);
            mc.gameSettings.keyBindForward.setPressed(false);
        }

        storedPackets.clear();
        super.onDisable();
    }

    @EventTarget
    public void onWorld(WorldLoadEvent event) {
        if (mode.is("Grim") && grimMode.is("Reduce")) {
            this.reset();
        }
        if (this.mode.is("Prediction")) {
            mc.gameSettings.keyBindJump.pressed = false;
            mc.gameSettings.keyBindForward.pressed = false;
        }
        if (mode.is("Jump Reset") || mode.is("Reduce") && jumpResetMode.is("Legit")) {
            mc.gameSettings.keyBindJump.setPressed(false);
            mc.gameSettings.keyBindForward.setPressed(false);
        }
    }

    private void reset() {
        this.shouldVelocity = false;
        this.target = null;
    }
    public static boolean hasReceivedVelocity;

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        setsuffix(mode.getValue());

        if (getModule(LongJump.class).isEnabled()) return;

        if (mode.is("Skip Tick")) {
            if (skipTickCounter > 0) {
                skipTickCounter--;
                return;
            }
        }

        if (mode.is("Grim")) {
            if (grimMode.is("1.17")) {
                if (canSpoof) {
                    sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround));
                    sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, new BlockPos(mc.thePlayer).down(), EnumFacing.DOWN));
                    canSpoof = false;
                }
            }
        }



        if (mode.is("Jump Reset") || mode.is("Reduce")) {
            if (jumpResetMode.is("Advanced")) {
                if (mc.thePlayer.hurtTime == 9) {
                    hitsCount++;
                }
                ticksCount++;
            }

            if (jumpResetMode.is("Legit")) {
                if (mc.currentScreen == null && Client.INSTANCE.getModuleManager().getModule(KillAura.class).target != null) {
                    if (mc.thePlayer.hurtTime == 10) {
                        this.enable = MathHelper.getRandomDoubleInRange(new Random(), 0.0d, 1.0d) <= jumpResetChance.getValue().doubleValue();
                    }

                    if (this.enable && getModule(KillAura.class).isEnabled()) {
                        if (mc.thePlayer.hurtTime >= 8) {
                            mc.gameSettings.keyBindJump.setPressed(true);
                        }
                        if (mc.thePlayer.hurtTime >= 7) {
                            mc.gameSettings.keyBindForward.setPressed(true);
                            return;
                        }
                        if (mc.thePlayer.hurtTime >= 4) {
                            mc.gameSettings.keyBindJump.setPressed(false);
                            mc.gameSettings.keyBindForward.setPressed(false);
                        } else if (mc.thePlayer.hurtTime > 1) {
                            mc.gameSettings.keyBindForward.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindForward));
                            mc.gameSettings.keyBindJump.setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindJump));
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onPacket(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();

        if (getModule(LongJump.class).isEnabled()) return;

        if (packet instanceof S12PacketEntityVelocity velocity && velocity.getEntityID() == mc.thePlayer.getEntityId()) {
            if (mode.is("Prediction")) {
                handlePredictionVelocity(velocity, event);
                return;
            }

            switch (mode.getValue()) {
                case "Skip Tick": {
                    if (random.nextInt(100) < skipChance.getValue()) {
                        skipTickCounter = skipTicks.getValue().intValue();
                    }
                    break;
                }

                case "Boost": {
                    if (mc.thePlayer.onGround) {
                        velocity.motionX = (int) (mc.thePlayer.motionX * 8000);
                        velocity.motionZ = (int) (mc.thePlayer.motionZ * 8000);
                    } else {
                        veloPacket = true;
                    }
                    break;
                }

                case "Polar Under-Block": {
                    AxisAlignedBB axisAlignedBB = mc.thePlayer.getEntityBoundingBox().offset(0.0, 1.0, 0.0);

                    if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, axisAlignedBB).isEmpty()) {
                        event.setCancelled(true);
                        mc.thePlayer.motionY = ((S12PacketEntityVelocity) packet).getMotionY() / 8000.0;
                    }
                    break;
                }

                case "Matrix Reverse": {
                    if (mc.thePlayer.hurtTime > 0) {
                        mc.thePlayer.motionX *= -0.3;
                        mc.thePlayer.motionZ *= -0.3;
                    }
                    break;
                }

                case "Matrix Semi": {
                    if (mc.thePlayer.hurtTime > 0) {
                        mc.thePlayer.motionX *= 0.6;
                        mc.thePlayer.motionZ *= 0.6;
                    }
                    break;
                }

                case "Legit": {
                    if (mc.currentScreen == null) {
                        mc.gameSettings.keyBindSprint.setPressed(true);
                        mc.gameSettings.keyBindForward.setPressed(true);
                        mc.gameSettings.keyBindJump.setPressed(true);
                        mc.gameSettings.keyBindBack.setPressed(false);

                        reducing = true;
                    }
                    break;
                }

                case "Polar": {
                    if (mc.thePlayer.isSwingInProgress) {
                        attacked = true;
                    }

                    if (mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.ENTITY) && mc.thePlayer.hurtTime > 0 && !attacked) {
                        mc.thePlayer.motionX *= 0.45D;
                        mc.thePlayer.motionZ *= 0.45D;
                        mc.thePlayer.setSprinting(false);
                    }

                    attacked = false;
                    break;
                }

                case "Intave": {
                    if (mc.thePlayer.isSwingInProgress) {
                        attacked = true;
                    }

                    if (mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.ENTITY) && mc.thePlayer.hurtTime > 0 && !attacked) {
                        mc.thePlayer.motionX *= 0.6D;
                        mc.thePlayer.motionZ *= 0.6D;
                        mc.thePlayer.setSprinting(false);
                    }

                    attacked = false;
                    break;
                }
                case "Reduce":{
                    hasReceivedVelocity = true;
                }
                case "Jump Reset": {
                    if (jumpResetMode.is("Packet")) {
                        veloPacket = true;
                    } else if (jumpResetMode.is("Advanced")) {
                        double velocityX = velocity.getMotionX() / 8000.0;
                        double velocityY = velocity.getMotionY() / 8000.0;
                        double velocityZ = velocity.getMotionZ() / 8000.0;

                        isFallDamage = velocityX == 0.0 && velocityZ == 0.0 && velocityY < 0;
                    }
                    if(velocity.getEntityID()== mc.thePlayer.getEntityId()) {
                        double velX = velocity.getMotionX() / 8000.0D;
                        double velZ = velocity.getMotionZ() / 8000.0D;
                        float desiredYaw = (float) Math.toDegrees(Math.atan2(velZ, velX));
                        if (desiredYaw < -180) desiredYaw += 360;
                        if (desiredYaw > 180) desiredYaw -= 360;
                        Client.Instance.getRotationManager().setRotation(
                                new Vector2f(desiredYaw + 90F, 180),
                                180,
                                true,
                                true
                        );
                        KillAura.useExternalRotation = true;
                    }
                    break;
                }

                case "Grim": {
                    switch (grimMode.getValue()) {
                        case "Reduce": {
                            double strength = new Vector3d(velocity.getMotionX(), velocity.getMotionY(), velocity.getMotionZ()).length();
                            if (velocity.getEntityID() == mc.thePlayer.getEntityId() && !mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWeb) {
                                target = getNearTarget();
                                if (target == null) return;

                                if (mc.thePlayer.getDistanceToEntity(target) > 3.3F) {
                                    reset();
                                    return;
                                }

                                shouldVelocity = true;
                                ChatUtils.sendMessage("[M]" + strength + " " + (mc.thePlayer.onGround ? "on Ground" : "on Air") + (target != null ? " - Distance: " + mc.thePlayer.getClosestDistanceToEntity(target) : ""));
                            }
                            break;
                        }

                        case "1.17": {
                            if (canCancel) {
                                canCancel = false;
                                canSpoof = true;
                                event.setCancelled(true);
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        }

        if (mode.is("Grim") && grimMode.is("1.17")) {
            if (event.getPacket() instanceof S19PacketEntityStatus s19PacketEntityStatus) {

                if (s19PacketEntityStatus.getEntity(mc.theWorld) == mc.thePlayer) {
                    canCancel = true;
                }
            }
        }
    }
    private Optional<Entity> findEntity() {
        if (mc.theWorld == null || mc.thePlayer == null) return Optional.empty();

        return mc.theWorld.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityLivingBase)
                .filter(entity -> entity.getEntityId() != mc.thePlayer.getEntityId())
                .filter(entity -> !entity.isDead && ((EntityLivingBase) entity).getHealth() > 0)

                .findAny();
    }

    private final TimerUtil disableHelper = new TimerUtil();
    private void handlePredictionVelocity(S12PacketEntityVelocity packet, PacketReceiveEvent e) {
        double x = packet.motionX / 8000D;
        double z = packet.motionZ / 8000D;
        double speed = Math.sqrt(x * x + z * z);


        if (mc.thePlayer.isInWeb || mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || mc.thePlayer.isOnLadder()) {
            if (debugMessage.getValue()) {

            }
            return;
        }

        if (!disableHelper.delay(1000)) {
            if (debugMessage.getValue()) {

            }
            return;
        }


        if (speed < 0.1) {
            if (debugMessage.getValue()) {

            }
            return;
        }


        if (onlySprint.getValue() && !mc.thePlayer.serverSprintState) {
            if (debugMessage.getValue()) {

            }
            return;
        }


        if (hypixelPrediction.getValue()) {
            double velocityX = packet.motionX / 8000.0;
            double velocityY = packet.motionY / 8000.0;
            double velocityZ = packet.motionZ / 8000.0;
            isFallDamage = velocityX == 0.0 && velocityZ == 0.0 && velocityY < 0;
        } else {

            Optional<Entity> targetEntity = findEntity();
            if (targetEntity.isPresent()) {
                velocityTimer.reset();
                Entity entity = targetEntity.get();
                e.setCancelled(true);


                boolean needSprint = !mc.thePlayer.serverSprintState;
                if (needSprint) {
                    mc.getNetHandler().getNetworkManager().sendPacket(
                            new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING)
                    );



                }


                for (int i = 0; i < 8; i++) {
                    AttackOrder.sendFixedAttack(mc.thePlayer, entity);
                }


                x *= Math.pow(0.6, 5);
                z *= Math.pow(0.6, 5);


                if (needSprint) {
                    mc.getNetHandler().getNetworkManager().sendPacket(
                            new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING)
                    );
                }


                packet.motionX = (int) (x * 8000);
                packet.motionZ = (int) (z * 8000);
                velocityPacket = packet;
            }
        }
    }
    public static S12PacketEntityVelocity velocityPacket;
    @EventTarget
    public void onAttack(AttackEvent event) {

    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (getModule(LongJump.class).isEnabled()) return;

        if (mode.is("Boost")) {
            if (veloPacket) {
                idk++;
            }
            if (idk == reverseTick.getValue()) {
                MovementUtil.strafe(MovementUtil.getSpeed() * reverseStrength.getValue(), Client.Instance.getRotationManager().lastRotation != null ? Client.Instance.getRotationManager().lastRotation.x : MovementUtil.getDirection());
                veloPacket = false;
                idk = 0;
            }
        }

        if (mode.is("Legit 2")) {
            if (reducing) {
                if (mc.currentScreen == null) {
                    resetKeybindings(mc.gameSettings.keyBindSprint, mc.gameSettings.keyBindForward,
                            mc.gameSettings.keyBindJump, mc.gameSettings.keyBindBack);
                }

                reducing = false;
            }
        }
    }

    @EventTarget
    public void onVelocity(VelocityEvent event) {
        if (mc.thePlayer == null) return;

        if (this.shouldVelocity) {
            if (mc.thePlayer.getDistanceToEntity(target) > 3.0) {
                this.reset();
                return;
            }

            if (!mc.thePlayer.serverSprintState) {
                mc.thePlayer.setSprinting(true);
            }

                PacketUtil.sendPacket(new C0APacketAnimation());
                PacketUtil.sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));

            if (!mc.thePlayer.serverSprintState) {
            }

            event.setReduceAmount(0.07776D);
            this.shouldVelocity = false;
        }
    }

    private Entity getNearTarget() {
        Entity target = null;
        EntityLivingBase clientTarget = getModule(KillAura.class).target;
        if (clientTarget != null) {
            target = clientTarget;
            return target;
        } else {
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (!entity.equals(mc.thePlayer) && !entity.isDead && invalidEntity.get()) {
                    if (entity instanceof EntityArrow entityArrow) {
                        if (entityArrow.ticksInGround <= 0) target = entityArrow;
                    }

                    if (entity instanceof EntitySnowball) target = entity;

                    if (entity instanceof EntityEgg) target = entity;

                    if (entity instanceof EntityTNTPrimed) target = entity;

                    if (entity instanceof EntityFishHook) target = entity;
                }
            }
        }
        return target;
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (getModule(LongJump.class).isEnabled()) return;

        if (mode.is("Jump Reset") || mode.is("Reduce")) {
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
        if (getModule(LongJump.class).isEnabled()) return;

        if (mode.is("Legit") && getModule(KillAura.class).target != null && mc.thePlayer.hurtTime > 0) {
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

    public static boolean isPressed(KeyBinding key) {
        return Keyboard.isKeyDown(key.getKeyCode());
    }

    public static void resetKeybinding(KeyBinding key) {
        if (mc.currentScreen != null) {
            key.setPressed(false);
        } else {
            key.setPressed(isPressed(key));
        }
    }

    public static void resetKeybindings(KeyBinding... keys) {
        for (KeyBinding key : keys) {
            resetKeybinding(key);
        }
    }

    @EventTarget
    public void onMotionEvent(MotionEvent e) {

        if (mode.is("Prediction")) {
            handlePredictionMotion();
            return;
        }


        if (mode.getValue().equals("Prediction")) {
            if (Mine.getCurrentScreen() != null) {
                return;
            }

            if (KillAura.target == null) {
                return;
            }

            if (mc.thePlayer.hurtTime == 10) {
                this.enable = MathHelper.getRandomDoubleInRange(new Random(), 0.0, 1.0) <= chance.getValue();
            }

            if (!this.enable) {
                return;
            }

            if (mc.thePlayer.hurtTime >= 8) {
                mc.gameSettings.keyBindJump.pressed = true;
            } else if (mc.thePlayer.hurtTime > 6) {
                mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward);
                mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump);
            }
        }
    }


    private boolean needJump = false;
    private final TimerUtil timerUtil = new TimerUtil();
    private void handlePredictionMotion() {
        if (!hypixelPrediction.getValue()) return;

        needJump = false;
        if (mc.thePlayer.hurtTime >= 9) {
            needJump = true;
        }

        if (needJump && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && !checks()) {


            if (timerUtil.reached((long)(jumpDelay.getValue() * 50))) {
                mc.thePlayer.jump();
                timerUtil.reset();
            }


            if (jumpRotate.getValue() && KillAura.target != null) {
                sendLookPacket();
            }
        }
    }
    public static int direction = 1;
    private void sendLookPacket() {

        direction *= -1;
        float playerYaw = Client.Instance.getRotationManager().lastRotation.x + 0.0001f * direction;
        mc.getNetHandler().getNetworkManager().sendPacket(
                new C03PacketPlayer.C05PacketPlayerLook(playerYaw, Client.Instance.getRotationManager().lastRotation.y, mc.thePlayer.onGround)
        );
    }
    private boolean isPlayerValid() {
        return mc.thePlayer != null && !mc.thePlayer.isDead && !mc.thePlayer.isRiding() &&
                mc.thePlayer.hurtResistantTime <= 10 && Client.Instance.getModuleManager().getModule(Sprint.class).isEnabled() &&
                KillAura.target != null;
    }
}
