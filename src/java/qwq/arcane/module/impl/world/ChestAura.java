package qwq.arcane.module.impl.world;



import net.minecraft.block.Block;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockFurnace;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.BlockPos;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.WorldLoadEvent;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.event.impl.events.player.PlaceEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.event.impl.events.render.Render3DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.combat.Gapple;
import qwq.arcane.module.impl.combat.KillAura;
import qwq.arcane.module.impl.player.Blink;
import qwq.arcane.module.impl.player.Stealer;
import qwq.arcane.utils.math.Vector2f;
import qwq.arcane.utils.pack.PacketUtil;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.rotation.RotationUtil;
import qwq.arcane.utils.time.TimerUtil;
import qwq.arcane.value.impl.NumberValue;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class ChestAura extends Module {
    private final NumberValue range = new NumberValue("Range", 4.0, 0.0, 15.0, 0.1);
    public TimerUtil waitBoxOpenTimer = new TimerUtil();
    public TimerUtil delayAfterOpenTimer = new TimerUtil();
    public static boolean isWaitingOpen = false;
    private BlockPos globalPos;
    public static List<BlockPos> list = new ArrayList<BlockPos>();

    public ChestAura() {
        super("ContainerAura", Category.World);
    }

    @Override
    public void onEnable() {
        list.clear();
    }

    @Override
    public void onDisable() {
        list.clear();
    }

    @EventTarget
    private void onPre(UpdateEvent event) {
        setsuffix(String.valueOf(this.range.get()));
        GuiScreen guiScreen = mc.currentScreen;

        if (mc.thePlayer.isOnLadder()) {
            return;
        }
        if (guiScreen instanceof GuiChest) {
            int a = 66;
        } else {
            if (Gapple.eating) {
                return;
            }
            if (KillAura.target != null || mc.thePlayer.isUsingItem() || getModule(Scaffold.class).getState() || getModule(BlockFly.class).getState() || getModule(Blink.class).getState() || Gapple.eating) {
                return;
            }

            // 检查是否在延迟时间内，如果是则不进行查找
            if (!delayAfterOpenTimer.hasTimeElapsed(500)) {
                return;
            }

            this.globalPos = null;
            if (mc.thePlayer.ticksExisted % 20 == 0 || KillAura.target != null || mc.currentScreen instanceof GuiContainer || mc.thePlayer.isUsingItem()) {
                return;
            }

            float radius = ((Double) range.getValue()).floatValue();
            for (float y = radius; y >= -radius; y -= 1.0f) {
                for (float x = -radius; x <= radius; x += 1.0f) {
                    for (float z = -radius; z <= radius; z += 1.0f) {
                        BlockPos pos = new BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);
                        Block block = mc.theWorld.getBlockState(pos).getBlock();
                        if (mc.thePlayer.getDistance(pos.getX(), pos.getY(), pos.getZ()) < mc.playerController.getBlockReachDistance()
                                && (block instanceof BlockChest || block instanceof BlockFurnace || block instanceof BlockBrewingStand)
                                && !list.contains(pos)) {
                            float[] rotations = RotationUtil.getBlockRotations(pos.getX(), pos.getY(), pos.getZ());
                            Client.Instance.getRotationManager().setRotation(new Vector2f(rotations[0], rotations[1]), 360.0f, true);
                            C08PacketPlayerBlockPlacement packet = new C08PacketPlayerBlockPlacement(pos, 1, mc.thePlayer.getCurrentEquippedItem(), 0.0f, 0.0f, 0.0f);
                            mc.thePlayer.sendQueue.addToSendQueue(packet);

                            PacketUtil.sendPacket(new C0APacketAnimation());
                            this.globalPos = pos;
                            delayAfterOpenTimer.reset();
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onPostMotion(MotionEvent event) {
        GuiScreen guiScreen = mc.currentScreen;

        if (mc.thePlayer.isOnLadder()) {
            return;
        }
        if (guiScreen instanceof GuiChest) {
            int a = 66;
        } else {
            if (Gapple.eating) {
                return;
            }
            if (!event.isPost()) {
                return;
            }
            if (KillAura.target != null || mc.thePlayer.isUsingItem() || getModule(Scaffold.class).getState() || getModule(Blink.class).getState() || Gapple.eating) {
                return;
            }
            if (isWaitingOpen) {
                if (waitBoxOpenTimer.hasTimeElapsed(600.0)) {
                    isWaitingOpen = false;
                } else if (globalPos != null && mc.thePlayer.openContainer instanceof ContainerChest) {
                    list.add(globalPos);
                    globalPos = null;
                    isWaitingOpen = false;
                }
            }
        }
    }

    Entity entity;
    @EventTarget
    public void onPlace(PlaceEvent event) {
        GuiScreen guiScreen = mc.currentScreen;
        if (mc.thePlayer.isOnLadder()) {
            return;
        }

        if (guiScreen instanceof GuiChest) {
            int a = 66;
        } else {
            if (Gapple.eating || !Client.Instance.getModuleManager().getModule(Stealer.class).getState()) {
                return;
            }
            if (Client.Instance.getModuleManager().getModule(Scaffold.class).getState()) {
                return;
            }
            if (globalPos != null && !(mc.currentScreen instanceof GuiContainer) && list.size() < 50 && !isWaitingOpen && !list.contains(globalPos)) {

                PacketUtil.sendPacketNoEvent(new C0APacketAnimation());
                event.setShouldRightClick(false);
                list.add(globalPos);
            }
        }
    }

    @EventTarget
    public void onRender(Render3DEvent event) {
        for (TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
            if (tileEntity instanceof TileEntityChest || tileEntity instanceof TileEntityBrewingStand || tileEntity instanceof TileEntityFurnace) {
                Color color = list.contains(tileEntity.getPos()) ? new Color(255, 0, 0, 60) : new Color(25, 255, 0, 120);
                if (mc.thePlayer.getDistance(tileEntity.getPos()) < 20.0) {
                    RenderUtil.drawBlockBox(tileEntity.getPos(), color, false);
                }
            }
        }
    }

    @EventTarget
    public void onWorld(WorldLoadEvent e2) {
        list.clear();
    }
}