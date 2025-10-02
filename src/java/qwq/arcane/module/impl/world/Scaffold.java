package qwq.arcane.module.impl.world;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.PreUpdateEvent;
import qwq.arcane.event.impl.events.player.StrafeEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.event.impl.events.render.Render3DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.math.MathUtils;
import qwq.arcane.utils.math.Vector2f;
import qwq.arcane.utils.player.MovementUtil;
import qwq.arcane.utils.player.PlaceData;
import qwq.arcane.utils.player.ScaffoldUtil;
import qwq.arcane.utils.player.SlotSpoofComponent;
import qwq.arcane.utils.render.BlockUtil;
import qwq.arcane.utils.render.PlaceInfo;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.rotation.RotationUtil;
import qwq.arcane.utils.time.TimerUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.ModeValue;
import qwq.arcane.value.impl.NumberValue;

import static qwq.arcane.utils.pack.PacketUtil.sendPacketNoEvent;

/**
 * @Author：Guyuemang
 * @Date：2025/9/11 16:35
 * @Description: 修复和优化后的自动搭路模块
 */
public class Scaffold extends Module {
    private static final double[] placeOffsets = new double[]{
            0.03125,
            0.09375,
            0.15625,
            0.21875,
            0.28125,
            0.34375,
            0.40625,
            0.46875,
            0.53125,
            0.59375,
            0.65625,
            0.71875,
            0.78125,
            0.84375,
            0.90625,
            0.96875
    };

    private EnumFacing targetFacing = null;
    private final ModeValue switchBlock = new ModeValue("Switch Block", () -> true, "Spoof", new String[]{"Silent", "Switch", "Spoof"});
    public ModeValue mode = new ModeValue("Mode", () -> true, "Normal", new String[]{"Normal", "Telly"});
    private final NumberValue minTellyTicks = new NumberValue("Min Telly Ticks", () -> mode.is("Telly"), 1, 1, 15, 0.5);
    private final NumberValue maxTellyTicks = new NumberValue("Max Telly Ticks", () -> mode.is("Telly"), 1.0, 1, 15, 0.5);
    public final BoolValue biggestStack = new BoolValue("Biggest Stack", false);
    private final NumberValue yaws = new NumberValue("Telly Yaw", () -> mode.is("Telly"), 120, 1.0, 180, 0.1);
    public final BoolValue pitchfix = new BoolValue("pitchfix", true);
    private final NumberValue pitchs = new NumberValue("Telly pitch", () -> mode.is("Telly") && !pitchfix.get(), 85.5, 1.0, 360.0, 0.1);
    public final BoolValue swing = new BoolValue("Swing", true);
    public final BoolValue sprint = new BoolValue("sprint", true);
    public BoolValue rotation = new BoolValue("Rotation", true);
    public NumberValue rotationspeed = new NumberValue("RotationSpeed", () -> rotation.get(), 180.0, 1.0, 360.0, 1.0);
    public ModeValue modeValue = new ModeValue("RotationMode", () -> true, "Normal", new String[]{"Normal", "Hypixel", "Telly", "Telly2", "Telly3"});
    public static BoolValue rayCastValue = new BoolValue("RayCast",  true);
    public BoolValue movefix = new BoolValue("MoveFix", true);
    public final BoolValue esp = new BoolValue("ESP",  true);

    public PlaceData data;
    public BlockPos previousBlock;
    private TimerUtil timerUtil = new TimerUtil();
    private double onGroundY;
    private boolean canPlace = true;
    private int tellyTicks;
    private boolean tellyStage;
    private float[] rotations;
    private int oloSlot = -1;
    private float yaw = -180.0F;
    private float pitch = 0.0F;
    private boolean hasSwitchedItems = false;
    private int blockSlot = -1;

    public Scaffold() {
        super("Scaffold", Category.World);
    }

    @Override
    public void onEnable() {
        this.yaw = -180.0F;
        rotations = null;
        tellyStage = false;
        timerUtil.reset();
        hasSwitchedItems = false;

        if (mc.thePlayer != null) {
            oloSlot = mc.thePlayer.inventory.currentItem;
            onGroundY = mc.thePlayer.getEntityBoundingBox().minY;
        }
        canPlace = true;
    }

    @Override
    public void onDisable() {
        rotations = null;
        timerUtil.reset();
        tellyTicks = 0;
        tellyStage = false;

        // 恢复物品栏
        restoreItemSlot();
    }

    /**
     * 恢复原始物品栏位置
     */
    private void restoreItemSlot() {
        switch (switchBlock.getValue()) {
            case "Silent":
                sendPacketNoEvent(new C09PacketHeldItemChange(oloSlot));
                break;
            case "Switch":
                mc.thePlayer.inventory.currentItem = oloSlot;
                break;
            case "Spoof":
                mc.thePlayer.inventory.currentItem = oloSlot;
                SlotSpoofComponent.stopSpoofing();
                break;
        }
    }

    /**
     * 切换到方块物品
     */
    private void switchToBlockItem() {
        if (hasSwitchedItems) return;

        blockSlot = ScaffoldUtil.getBlockSlot();
        if (blockSlot == -1) return;

        switch (switchBlock.getValue()) {
            case "Silent":
                sendPacketNoEvent(new C09PacketHeldItemChange(blockSlot));
                break;
            case "Switch":
                mc.thePlayer.inventory.currentItem = blockSlot;
                break;
            case "Spoof":
                mc.thePlayer.inventory.currentItem = blockSlot;
                SlotSpoofComponent.startSpoofing(oloSlot);
                break;
        }

        hasSwitchedItems = true;
    }

    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (mc.thePlayer.onGround && mode.is("Telly") && MovementUtil.canSprint(true) && !mc.thePlayer.isJumping && MovementUtil.isMoving()) {
            tellyStage = !tellyStage;
            mc.thePlayer.jump();
        }
    }

    @EventTarget
    public void onUpdate(PreUpdateEvent event) {
        if (minTellyTicks.get() > maxTellyTicks.get()){
            minTellyTicks.set(minTellyTicks.get() - 1);
        }
        data = null;
        hasSwitchedItems = false;

        // 检查是否有方块可用
        blockSlot = ScaffoldUtil.getBlockSlot();
        if (blockSlot == -1) {
            restoreItemSlot();
            return;
        }

        // 处理冲刺
        handleSprinting();

        // 更新Telly模式计时器
        if (mode.is("Telly") && mc.thePlayer.onGround) {
            tellyTicks = MathUtils.randomizeInt(minTellyTicks.getValue().intValue(), maxTellyTicks.getValue().intValue());
        }

        // 更新地面高度
        if (mc.thePlayer.onGround) {
            onGroundY = mc.thePlayer.getEntityBoundingBox().minY;
        }

        // 计算放置位置
        calculatePlacementPosition();

        // 检查是否可以放置
        canPlace = (mode.is("Telly") && mc.thePlayer.offGroundTicks >= tellyTicks && data != null) ||
                (mode.is("Normal") && data != null);

        if (!canPlace) {
            Client.Instance.getRotationManager().setRotation(new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch), 180, true);
        }
    }

    /**
     * 处理冲刺逻辑
     */
    private void handleSprinting() {
        if (sprint.get()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
            mc.thePlayer.setSprinting(false);
        }
    }

    /**
     * 计算放置位置
     */
    private void calculatePlacementPosition() {
        double posY = mc.thePlayer.getEntityBoundingBox().minY;

        if (!mc.gameSettings.keyBindJump.isKeyDown()) {
            posY = onGroundY;
        }

        double posX = mc.thePlayer.posX;
        double posZ = mc.thePlayer.posZ;
        previousBlock = new BlockPos(posX, posY, posZ).offset(EnumFacing.DOWN);
        data = ScaffoldUtil.getPlaceData(previousBlock);
    }

    /**
     * 获取当前Yaw角度
     */
    private float getCurrentYaw() {
        return MovementUtil.adjustYaw(
                mc.thePlayer.rotationYaw, (float) MovementUtil.getForwardValue(), (float) MovementUtil.getLeftValue()
        );
    }

    /**
     * 检查是否为对角线移动
     */
    private boolean isDiagonal(float yaw) {
        float absYaw = Math.abs(yaw % 90.0F);
        return absYaw > 20.0F && absYaw < 70.0F;
    }

    @EventTarget
    public void onUpdateEvent(UpdateEvent event) {
        setsuffix(String.valueOf(this.mode.get()));

        // 如果没有方块可用，恢复物品栏并返回
        if (blockSlot == -1) {
            restoreItemSlot();
            return;
        }

        // 处理旋转和放置
        if (data != null && rotation.get() && (mode.is("Normal") || (mode.is("Telly") && canPlace))) {
            // 切换到方块物品
            switchToBlockItem();

            // 计算旋转
            calculateRotations();

            // 设置旋转
            Client.Instance.getRotationManager().setRotation(new Vector2f(rotations[0], rotations[1]), rotationspeed.get().intValue(), movefix.get());

            // 尝试放置
            tryPlaceOnce();
        } else {
            // 处理冲刺
            handleSprinting();
            Client.Instance.getRotationManager().setRotation(new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch), 180, true);
        }
    }

    /**
     * 计算旋转角度
     */
    private void calculateRotations() {
        float currentYaw = this.getCurrentYaw();
        float yawDiffTo180 = RotationUtil.wrapAngleDiff(currentYaw - 180.0F, mc.thePlayer.rotationYaw);
        float diagonalYaw = this.isDiagonal(currentYaw)
                ? yawDiffTo180
                : RotationUtil.wrapAngleDiff(currentYaw - 135.0F * ((currentYaw + 180.0F) % 90.0F < 45.0F ? 1.0F : -1.0F), mc.thePlayer.rotationYaw);

        PlaceData blockData = this.data;
        Vec3 hitVec = null;

        if (blockData != null) {
            double[] x = placeOffsets;
            double[] y = placeOffsets;
            double[] z = placeOffsets;

            switch (blockData.getFacing()) {
                case NORTH:
                    z = new double[]{0.0};
                    break;
                case EAST:
                    x = new double[]{1.0};
                    break;
                case SOUTH:
                    z = new double[]{1.0};
                    break;
                case WEST:
                    x = new double[]{0.0};
                    break;
                case DOWN:
                    y = new double[]{0.0};
                    break;
                case UP:
                    y = new double[]{1.0};
            }

            float bestYaw = -180.0F;
            float bestPitch = 0.0F;
            float bestDiff = Float.MAX_VALUE;

            for (double dx : x) {
                for (double dy : y) {
                    for (double dz : z) {
                        double relX = (double) blockData.getBlockPos().getX() + dx - mc.thePlayer.posX;
                        double relY = (double) blockData.getBlockPos().getY() + dy - mc.thePlayer.posY - (double) mc.thePlayer.getEyeHeight();
                        double relZ = (double) blockData.getBlockPos().getZ() + dz - mc.thePlayer.posZ;
                        float baseYaw = RotationUtil.wrapAngleDiff(this.yaw, mc.thePlayer.rotationYaw);
                        float[] rotations = RotationUtil.getRotationsTo(relX, relY, relZ, baseYaw, this.pitch);

                        MovingObjectPosition mop = RotationUtil.rayTrace(rotations, mc.playerController.getBlockReachDistance(), 1.0F);
                        if (mop != null
                                && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                                && mop.getBlockPos().equals(blockData.getBlockPos())
                                && mop.sideHit == blockData.getFacing()) {
                            float totalDiff = Math.abs(rotations[0] - baseYaw) + Math.abs(rotations[1] - this.pitch);
                            if (totalDiff < bestDiff) {
                                bestYaw = rotations[0];
                                bestPitch = rotations[1];
                                bestDiff = totalDiff;
                                hitVec = mop.hitVec;
                            }
                        }
                    }
                }
            }

            if (bestDiff < Float.MAX_VALUE) {
                this.yaw = bestYaw;
                this.pitch = bestPitch;
                this.canPlace = true;
            }
        }

        // 根据旋转模式计算最终旋转
        switch (modeValue.get()) {
            case "Normal":
                rotations = RotationUtil.getRotations(getVec3(data));
                break;
            case "Hypixel":
                if (this.yaw == -180.0F && this.pitch == 0.0F) {
                    this.yaw = RotationUtil.quantizeAngle(diagonalYaw);
                    this.pitch = RotationUtil.quantizeAngle(85.0F);
                } else {
                    this.yaw = RotationUtil.quantizeAngle(diagonalYaw);
                }
                rotations = new float[]{this.yaw, this.pitch};
                break;
            case "Telly2":
                float yaw2 = MovementUtil.getRawDirection() - 125;
                float pitch2 = RotationUtil.getRotations(getVec3(data))[1];
                rotations = new float[]{yaw2, pitch2};
                break;
            case "Telly3":
                float yaw3 = RotationUtil.getRotations(getVec3(data))[0];
                if (tellyStage) {
                    yaw3 = RotationUtil.getRotations(getVec3(data))[0];
                }
                float pitch3 = RotationUtil.getRotations(getVec3(data))[1];
                rotations = new float[]{yaw3, pitch3};
                break;
            case "Telly":
                float yaw = MovementUtil.getRawDirection() - yaws.get().floatValue();
                if (tellyStage) {
                    yaw = MovementUtil.getRawDirection() + yaws.get().floatValue();
                }
                float pitch = pitchfix.get() ? RotationUtil.getRotations(getVec3(data))[1] : pitchs.get().floatValue();
                rotations = new float[]{yaw, pitch};
                break;
            default:
                rotations = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
                break;
        }
    }

    /**
     * 尝试放置一次方块
     */
    private void tryPlaceOnce() {
        if (data == null) return;

        boolean success = false;

        if (rayCastValue.get()) {
            MovingObjectPosition ray = Client.Instance.getRotationManager().rayTrace(mc.playerController.getBlockReachDistance(), 1);
            if (ray != null && ray.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                success = mc.playerController.onPlayerRightClick(
                        mc.thePlayer,
                        mc.theWorld,
                        mc.thePlayer.inventory.getStackInSlot(blockSlot),
                        data.getBlockPos(),
                        data.getFacing(),
                        getVec3(data)
                );
            }
        } else {
            success = mc.playerController.onPlayerRightClick(
                    mc.thePlayer,
                    mc.theWorld,
                    mc.thePlayer.getCurrentEquippedItem(),
                    data.getBlockPos(),
                    data.getFacing(),
                    getVec3(data)
            );
        }

        if (success) {
            if (swing.getValue()) {
                mc.thePlayer.swingItem();
            } else {
                mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
            }
        }
    }

    /**
     * 获取方块物品栏位置
     */
    public int getBlockSlot() {
        for (int i = 0; i < 9; ++i) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i + 36).getHasStack() ||
                    !(mc.thePlayer.inventoryContainer.getSlot(i + 36).getStack().getItem() instanceof ItemBlock)) {
                continue;
            }
            return i;
        }
        return -1;
    }

    /**
     * 获取放置位置的Vec3
     */
    public static Vec3 getVec3(PlaceData data) {
        if (data == null) return null;

        BlockPos pos = data.blockPos;
        EnumFacing face = data.facing;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        x += face.getFrontOffsetX() / 2.0D;
        z += face.getFrontOffsetZ() / 2.0D;
        y += face.getFrontOffsetY() / 2.0D;

        return new Vec3(x, y, z);
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (data == null || !esp.get()) return;

        final BlockPos blockPos = data.getBlockPos();
        final PlaceInfo placeInfo = PlaceInfo.get(blockPos);

        if (BlockUtil.isValidBock(blockPos) && placeInfo != null) {
            RenderUtil.drawBlockBox(blockPos, ColorUtil.applyOpacity(Client.Instance.getModuleManager().getModule(InterFace.class).color(1), 0.5f), false);
        }
    }
}