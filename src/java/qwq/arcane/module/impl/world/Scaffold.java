package qwq.arcane.module.impl.world;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.math.Vector2f;
import qwq.arcane.utils.player.PlaceData;
import qwq.arcane.utils.player.ScaffoldUtil;
import qwq.arcane.utils.rotation.RotationUtil;
import qwq.arcane.value.impl.BoolValue;

/**
 * @Author：Guyuemang
 * @Date：7/6/2025 11:54 PM
 */
public class Scaffold extends Module {
    public Scaffold() {
        super("Scaffold",Category.World);
    }
    public final BoolValue swing = new BoolValue("Swing", true);
    public final BoolValue sprint = new BoolValue("sprint", true);
    private PlaceData data;
    public BlockPos previousBlock;

    @EventTarget
    public void onPreMotion(MotionEvent event){
        if (sprint.get()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
            mc.thePlayer.setSprinting(false);
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event){
        double posY = mc.thePlayer.getEntityBoundingBox().minY;
        double posX = mc.thePlayer.posX;
        double posZ = mc.thePlayer.posZ;
        previousBlock = new BlockPos(posX,posY,posZ).offset(EnumFacing.DOWN);

        data = ScaffoldUtil.getPlaceData(previousBlock);
        place();
    }

    @EventTarget
    public void onPostMotion(MotionEvent event) {
        if (data != null) {
            float[] rot = new float[0];
            rot = RotationUtil.getRotationBlock2(data.getBlockPos());
            Client.Instance.rotationManager.setRotation(new Vector2f(rot[0],rot[1]),180,true,false);
        }
    }

    private void place(){
        if (data != null) {
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem(), data.getBlockPos(), data.getFacing(), getVec3(data.getBlockPos(), data.getFacing()))) {
                if (swing.getValue()) {
                    mc.thePlayer.swingItem();
                } else {
                    mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                }
            }
        }
    }

    public static Vec3 getVec3(BlockPos pos, EnumFacing face) {
        double x = (double) pos.getX() + 0.5;
        double y = (double) pos.getY() + 0.5;
        double z = (double) pos.getZ() + 0.5;
        if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
            x += 0.5;
            z += 0.5;
        } else {
            y += 0.5;
        }
        if (face == EnumFacing.WEST || face == EnumFacing.EAST) {
            z += 0.5;
        }
        if (face == EnumFacing.SOUTH || face == EnumFacing.NORTH) {
            x += 0.5;
        }
        return new Vec3(x, y, z);
    }

}
