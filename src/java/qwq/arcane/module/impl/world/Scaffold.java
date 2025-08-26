package qwq.arcane.module.impl.world;


import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import org.apache.commons.lang3.RandomUtils;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.event.impl.events.player.StrafeEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.event.impl.events.render.Render3DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.movement.Sprint;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.math.MathUtils;
import qwq.arcane.utils.math.Vector2f;
import qwq.arcane.utils.player.*;
import qwq.arcane.utils.render.BlockUtil;
import qwq.arcane.utils.render.PlaceInfo;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.rotation.RotationUtil;
import qwq.arcane.utils.time.TimerUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.ModeValue;
import qwq.arcane.value.impl.NumberValue;

/**
 * @Author：Guyuemang
 * @Date：7/6/2025 11:54 PM
 */

public class Scaffold extends Module {
    public Scaffold() {
        super("Scaffold",Category.World);
    }
    public ModeValue mode = new ModeValue("Mode","Normal",new String[]{"Normal","Telly"});
    private final NumberValue minTellyTicks = new NumberValue("Min Telly Ticks", () -> mode.is("Telly"), 2, 1, 5,1);
    private final NumberValue maxTellyTicks = new NumberValue("Max Telly Ticks", () -> mode.is("Telly"), 4, 1, 5,1);
    public final BoolValue biggestStack = new BoolValue("Biggest Stack", false);
    public final BoolValue swing = new BoolValue("Swing", true);
    public final BoolValue sprint = new BoolValue("sprint", true);
    public BoolValue rotation = new BoolValue("Rotation",true);
    public NumberValue rotationspeed = new NumberValue("RotationSpeed",()->rotation.get(),180.0,1.0,360,1);
    public ModeValue modeValue = new ModeValue("RotationMode","Normal",new String[]{"Normal","Telly","Telly2"});
    public static BoolValue rayCastValue = new BoolValue("RayCast", true);
    public BoolValue movefix = new BoolValue("MoveFix",true);
    public final BoolValue esp = new BoolValue("ESP", true);
    public PlaceData data;
    public BlockPos previousBlock;
    public int slot;
    private int prevItem = 0;
    private TimerUtil timerUtil = new TimerUtil();
    private double onGroundY;
    private boolean canPlace = true;
    private int tellyTicks;
    private float[] previousRotation;
    private boolean tellyStage;
    private float[] rotations;
    @Override
    public void onEnable() {
        timerUtil.reset();
        if (mc.thePlayer != null) {
            prevItem = mc.thePlayer.inventory.currentItem;
            onGroundY = mc.thePlayer.getEntityBoundingBox().minY;
            previousRotation = new float[]{mc.thePlayer.rotationYaw + 180, 82};
        }
        this.slot = -1;
        canPlace = true;
    }
    @Override
    public void onDisable() {
        timerUtil.reset();
        tellyTicks = 0;
        previousRotation = rotations = null;
        mc.thePlayer.inventory.currentItem = prevItem;
        SlotSpoofComponent.stopSpoofing();
    }
    @EventTarget
    public void Tickevent(UpdateEvent event){
        SlotSpoofComponent.startSpoofing(prevItem);
        mc.thePlayer.inventory.currentItem = this.slot;
        this.slot = getBlockSlot();
    }

    @EventTarget
    public void onStrafe(StrafeEvent event){
        if (this.slot < 0) return;
        if (ScaffoldUtil.getBlockSlot() == -1)
            return;
        if (mc.thePlayer.onGround && mode.is("Telly") && !mc.thePlayer.isJumping && MovementUtil.isMoving()) {
           tellyStage = !tellyStage;
           mc.thePlayer.jump();
        }
    }
    private static final float NORMAL_PITCH = 82.5f;
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        this.slot = getBlockSlot();
        SlotSpoofComponent.startSpoofing(prevItem);
        mc.thePlayer.inventory.currentItem = this.slot;
        if (ScaffoldUtil.getBlockSlot() == -1)
            return;
        if (this.slot < 0) return;

        if (sprint.get()) {
            Sprint.keepSprinting = true;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        } else {
            Sprint.keepSprinting = false;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
            mc.thePlayer.setSprinting(false);
        }
        if (mode.is("Telly") && mc.thePlayer.onGround) {
            tellyTicks = MathUtils.randomizeInt(minTellyTicks.getValue().intValue(), maxTellyTicks.getValue().intValue());
        }
        data = null;
        if (mc.thePlayer.onGround) {
            onGroundY = mc.thePlayer.getEntityBoundingBox().minY;
        }
        double posY = mc.thePlayer.getEntityBoundingBox().minY;

        if (!mc.gameSettings.keyBindJump.isKeyDown()) {
            posY = onGroundY;
        }
        double posX = mc.thePlayer.posX;
        double posZ = mc.thePlayer.posZ;
        previousBlock = new BlockPos(posX, posY, posZ).offset(EnumFacing.DOWN);
        data = ScaffoldUtil.getPlaceData(previousBlock);

        canPlace = mode.is("Telly") && mc.thePlayer.offGroundTicks >= tellyTicks && data != null || mode.is("Normal") && data != null;
        if (!canPlace){
            rotations = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
        }
        place();
        if (mode.is("Telly") && mc.thePlayer.onGround) {
            tellyTicks = MathUtils.randomizeInt(minTellyTicks.getValue().intValue(), maxTellyTicks.getValue().intValue());
        }

    }
    @EventTarget
    public void onMotion(UpdateEvent event){
        setsuffix(String.valueOf(this.mode.get()));
        if (ScaffoldUtil.getBlockSlot() == -1)
            return;
        if (this.slot < 0) return;
        if (data != null && rotation.get() && mode.is("Normal") || mode.is("Telly") && canPlace && rotation.get()) {
            switch (modeValue.get()){
                case "Normal":
                    rotations = RotationUtil.getRotations(getVec3(data));
                    break;
                case "Telly2":
                    float yaw2 = MovementUtil.getRawDirection() - 125;
                    float pitch2 = RotationUtil.getRotations(getVec3(data))[1];
                    rotations = new float[]{yaw2, pitch2};
                    break;
                case "Telly":
                    float yaw = MovementUtil.getRawDirection() - 127;
                    if (GameSettings.isKeyDown(mc.gameSettings.keyBindJump)){
                        yaw = MovementUtil.getRawDirection() - 125;
                    }
                    if (GameSettings.isKeyDown(mc.gameSettings.keyBindLeft))
                        yaw = MovementUtil.getRawDirection() + 120;
                    float pitch = RotationUtil.getRotations(getVec3(data))[1];
                    rotations = new float[]{yaw, pitch};
                    break;
            }
        }
        if (canPlace){
            Client.Instance.getRotationManager().setRotation(new Vector2f(rotations[0],rotations[1]),rotationspeed.get().intValue(),movefix.get());
        }
    }
    private void place(){
        if (this.slot < 0) return;
        if (data != null) {
            if (rayCastValue.get()) {
                MovingObjectPosition ray = Client.Instance.getRotationManager().rayTrace(mc.playerController.getBlockReachDistance(), 1);
                if (ray != null) {
                    if (ray.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem(), data.getBlockPos(), data.getFacing(), getVec3(data))) {
                        if (swing.getValue()) {
                            mc.thePlayer.swingItem();
                        } else {
                            mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                        }
                    }
                }
            }else {
                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem(), data.getBlockPos(), data.getFacing(), getVec3(data))) {
                    if (swing.getValue()) {
                        mc.thePlayer.swingItem();
                    } else {
                        mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                    }
                }
            }
        }
    }
    public int getBlockSlot() {
        for (int i = 0; i < 9; ++i) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i + 36).getHasStack() || !(mc.thePlayer.inventoryContainer.getSlot(i + 36).getStack().getItem() instanceof ItemBlock))
                continue;
            return i;
        }
        return -1;
    }
    public static Vec3 getVec3(PlaceData data) {
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
        if (data == null) return;
        for (int i = 0; i < 2; i++) {
            final BlockPos blockPos = data.getBlockPos();

            final PlaceInfo placeInfo = PlaceInfo.get(blockPos);

            if (BlockUtil.isValidBock(blockPos) && placeInfo != null && esp.get()) {
                RenderUtil.drawBlockBox(blockPos,ColorUtil.applyOpacity(INSTANCE.getModuleManager().getModule(InterFace.class).color(1),0.5f), false);
                break;
            }
        }
    }
}
