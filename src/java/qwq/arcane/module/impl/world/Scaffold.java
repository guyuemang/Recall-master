package qwq.arcane.module.impl.world;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.TickEvent;
import qwq.arcane.event.impl.events.player.MotionEvent;
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

import java.awt.*;

/**
 * @Author：Guyuemang
 * @Date：7/6/2025 11:54 PM
 */
public class Scaffold extends Module {
    public Scaffold() {
        super("Scaffold",Category.World);
    }
    public ModeValue mode = new ModeValue("Mode","Normal",new String[]{"Normal","Telly"});
    public final BoolValue swing = new BoolValue("Swing", true);
    public final BoolValue sprint = new BoolValue("sprint", true);
    public BoolValue rotation = new BoolValue("Rotation",true);
    public NumberValue rotationspeed = new NumberValue("RotationSpeed",()->rotation.get(),180.0,1.0,360,1);
    public ModeValue modeValue = new ModeValue("RotationMode","Normal",new String[]{"Normal","Telly"});
    public static BoolValue rayCastValue = new BoolValue("RayCast", true);
    public BoolValue movefix = new BoolValue("MoveFix",true);
    public final BoolValue esp = new BoolValue("ESP", true);
    private PlaceData data;
    public BlockPos previousBlock;
    public int slot;
    private int prevItem = 0;
    private TimerUtil timerUtil = new TimerUtil();
    private double onGroundY;
    private float[] smoothRotation = new float[]{0, 85F};
    @Override
    public void onEnable() {
        timerUtil.reset();
        if (mc.thePlayer != null) {
            prevItem = mc.thePlayer.inventory.currentItem;
        }
        onGroundY = mc.thePlayer.getEntityBoundingBox().minY;
        this.slot = -1;
        smoothRotation = new float[]{mc.thePlayer.rotationYaw, 85F};
    }
    @Override
    public void onDisable() {
        timerUtil.reset();
        mc.thePlayer.inventory.currentItem = prevItem;
        SlotSpoofComponent.stopSpoofing();
    }
    @EventTarget
    public void Tickevent(TickEvent event){
        this.slot = getBlockSlot();
    }
    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.thePlayer.onGround && mode.is("Telly") && !mc.thePlayer.isJumping && MovementUtil.isMoving()) {
            mc.thePlayer.jump();
        }
    }
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        this.slot = getBlockSlot();
        if (this.slot < 0) return;
        mc.thePlayer.inventory.currentItem = this.slot;
        SlotSpoofComponent.startSpoofing(prevItem);
        if (sprint.get()) {
            Sprint.keepSprinting = true;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        } else {
            Sprint.keepSprinting = false;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
            mc.thePlayer.setSprinting(false);
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

        place();

        if (data != null && rotation.get()) {
            float[] rotation;
            switch (modeValue.get()){
                case "Normal":
                    rotation = RotationUtil.getRotations(getVec3(data));
                    Client.Instance.rotationManager.setRotation(new Vector2f(rotation[0],rotation[1]),rotationspeed.get().intValue(),movefix.get(),false);
                    break;
                case "Telly":
                    if (mc.gameSettings.keyBindJump.isKeyDown()) {
                        smoothRotation[0] = MovementUtil.getBindsDirection(mc.thePlayer.rotationYaw) - 115;
                    } else {
                        if (mc.thePlayer.offGroundTicks > 5) {
                            smoothRotation[0] = MovementUtil.getBindsDirection(mc.thePlayer.rotationYaw) - 180;
                        } else if (mc.thePlayer.offGroundTicks > 2) {
                            smoothRotation[0] = MovementUtil.getBindsDirection(mc.thePlayer.rotationYaw) - 90;
                        } else {
                            smoothRotation[0] = MovementUtil.getBindsDirection(mc.thePlayer.rotationYaw) - 125;
                        }
                    }
                    Client.Instance.rotationManager.setRotation(new Vector2f(smoothRotation[0],smoothRotation[1]),rotationspeed.get().intValue(),movefix.get(),false);
                    break;
            }
        }
    }
    private void place(){
        if (this.slot < 0) return;
        if (data != null) {
            if (rayCastValue.get()) {
                MovingObjectPosition ray = Client.Instance.rotationManager.rayTrace(mc.playerController.getBlockReachDistance(), 1);
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
