package qwq.arcane.module.impl.world;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.movement.Sprint;
import qwq.arcane.utils.math.Vector2f;
import qwq.arcane.utils.player.MovementUtil;
import qwq.arcane.utils.player.PlaceData;
import qwq.arcane.utils.player.ScaffoldUtil;
import qwq.arcane.utils.rotation.RayCastUtil;
import qwq.arcane.utils.rotation.RotationUtil;
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
    public final BoolValue swing = new BoolValue("Swing", true);
    public final BoolValue sprint = new BoolValue("sprint", false);
    public BoolValue rotation = new BoolValue("Rotation",true);
    public NumberValue rotationspeed = new NumberValue("RotationSpeed",()->rotation.get(),180.0,1.0,180.0,1);
    public static BoolValue rayCastValue = new BoolValue("RayCast", false);
    public BoolValue movefix = new BoolValue("MoveFix",false);

    private PlaceData data;
    public BlockPos previousBlock;

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (sprint.get()) {
            Sprint.keepSprinting = true;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        } else {
            Sprint.keepSprinting = false;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
            mc.thePlayer.setSprinting(false);
        }

        data = null;

        double posY = mc.thePlayer.getEntityBoundingBox().minY;
        double posX = mc.thePlayer.posX;
        double posZ = mc.thePlayer.posZ;
        previousBlock = new BlockPos(posX, posY, posZ).offset(EnumFacing.DOWN);
        data = ScaffoldUtil.getPlaceData(previousBlock);
        place();
    }

    @EventTarget
    public void onPostMotion(MotionEvent event) {
        if (data != null && rotation.get()) {
            float[] rot;
            rot = RotationUtil.getRotations(getVec3(data));
            Client.Instance.rotationManager.setRotation(new Vector2f(rot[0],rot[1]),rotationspeed.get().intValue(),movefix.get(),false);
        }
    }

    private void place(){
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

}
