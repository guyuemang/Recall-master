package qwq.arcane.module.impl.player;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.event.impl.events.player.TeleportEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.event.impl.events.render.Render2DEvent;
import qwq.arcane.event.impl.events.render.Render3DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.module.impl.world.Scaffold;
import qwq.arcane.utils.animations.impl.ContinualAnimation;
import qwq.arcane.utils.math.Vector2f;
import qwq.arcane.utils.player.PlayerUtil;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.utils.rotation.RotationUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.NumberValue;

import java.awt.*;

import static qwq.arcane.utils.pack.PacketUtil.sendPacket;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:02 AM
 */
public class BedNuker extends Module {
    public BedNuker() {
        super("BedNuker",Category.Player);
    }
    private final BoolValue movefix = new BoolValue("Movement Fix", true);
    public final NumberValue breakRange = new NumberValue("Break Range", 4, 1, 5, 1);
    public final BoolValue breakSurroundings = new BoolValue("Break Top", true);
    public final BoolValue autoTool = new BoolValue("Auto Tool", true);
    public final BoolValue progressText = new BoolValue("Progress Text", true);
    public final BoolValue progressBar = new BoolValue("Progress Bar", true);
    public final BoolValue whitelistOwnBed = new BoolValue("Whitelist Own Bed", true);
    public final BoolValue swap = new BoolValue("Swap", false);
    public final BoolValue ignoreSlow = new BoolValue("Ignore Slow",swap::get, false);
    public final BoolValue groundSpoof = new BoolValue("Hypixel Ground Spoof",swap::get, false);
    public BlockPos bedPos;
    public boolean rotate = false;
    private float breakProgress;
    private int delayTicks;
    private Vec3 home;
    private boolean spoofed;
    public ContinualAnimation barAnim = new ContinualAnimation();

    @Override
    public void onEnable() {
        rotate = false;
        bedPos = null;

        breakProgress = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        reset(true);
        super.onDisable();
    }

    @EventTarget
    public void onTeleport(TeleportEvent event){
        if(whitelistOwnBed.get()){
            final double distance = mc.thePlayer.getDistance(event.getPosX(), event.getPosY(), event.getPosZ());

            if (distance > 40) {
                home = new Vec3(event.getPosX(), event.getPosY(), event.getPosZ());
            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event){

        setsuffix(swap.get() ? "Swap" : "Vanilla");

        if (Client.Instance.getModuleManager().getModule(Scaffold.class).isEnabled() && Client.INSTANCE.getModuleManager().getModule(Scaffold.class).data == null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock) {
            reset(true);
            return;
        }

        getBedPos();

        if (bedPos != null) {
            if (rotate) {
                float[] rot = RotationUtil.getRotations(bedPos);
                Client.INSTANCE.getRotationManager().setRotation(new Vector2f(rot[0], rot[1]), 180, movefix.get());
                rotate = false;
            }
            mine(bedPos);
        } else {
            reset(true);
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (event.isPost())
            return;


        if (bedPos != null && groundSpoof.get() && !mc.thePlayer.onGround) {
            if (mc.thePlayer.ticksExisted % 2 == 0) {
                mc.timer.timerSpeed = 0.5f;
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                spoofed = true;
            } else {
                mc.timer.timerSpeed = 1f;
                spoofed = false;
            }
        } else if (spoofed) {
            mc.timer.timerSpeed = 1f;
            spoofed = false;
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (progressText.get() && bedPos != null) {
            RenderUtil.renderBlock(bedPos, getModule(InterFace.class).color(), true, true);

            if (breakProgress == 0.0f)
                return;

            final double n = bedPos.getX() + 0.5 - mc.getRenderManager().viewerPosX;
            final double n2 = bedPos.getY() + 0.5 - mc.getRenderManager().viewerPosY;
            final double n3 = bedPos.getZ() + 0.5 - mc.getRenderManager().viewerPosZ;
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) n, (float) n2, (float) n3);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
            GlStateManager.scale(-0.02266667f, -0.02266667f, -0.02266667f);
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            String progressStr = (int) (100.0 * (this.breakProgress / 1.0)) + "%";
            mc.fontRendererObj.drawString(progressStr, (float) (-mc.fontRendererObj.getStringWidth(progressStr) / 2), -3.0f, -1, true);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (progressBar.get() && bedPos != null) {

            if (breakProgress == 0.0f)
                return;

            final ScaledResolution resolution = event.getScaledResolution();
            final int x = resolution.getScaledWidth() / 2;
            final int y = resolution.getScaledHeight() / 2 + 30;
            final float thickness = 6;

            final int width = 100;
            final int half = width / 2;
            barAnim.animate(width * (breakProgress), 40);

            RoundedUtil.drawRound(x - half, y, width, thickness, thickness / 2, new Color(getModule(InterFace.class).color(),true));

            RoundedUtil.drawGradientHorizontal(x - half, y, barAnim.getOutput(), thickness, thickness / 2, new Color(getModule(InterFace.class).color()), new Color(getModule(InterFace.class).color(90).getRGB()));

            String progressStr = (int) (100.0 * (this.breakProgress / 1.0)) + "%";

            Bold.get(12).drawCenteredStringWithShadow(progressStr, x, y + 1, -1);
        }
    }


    private void getBedPos() {
        if (home != null && mc.thePlayer.getDistanceSq(home.xCoord, home.yCoord, home.zCoord) < 35 * 35 && whitelistOwnBed.get()) {
            return;
        }
        bedPos = null;
        double range = breakRange.getValue();
        for (double x = mc.thePlayer.posX - range; x <= mc.thePlayer.posX + range; x++) {
            for (double y = mc.thePlayer.posY + mc.thePlayer.getEyeHeight() - range; y <= mc.thePlayer.posY + mc.thePlayer.getEyeHeight() + range; y++) {
                for (double z = mc.thePlayer.posZ - range; z <= mc.thePlayer.posZ + range; z++) {
                    BlockPos pos = new BlockPos((int) x, (int) y, (int) z);

                    if (mc.theWorld.getBlockState(pos).getBlock() instanceof BlockBed && mc.theWorld.getBlockState(pos).getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD) {
                        if (breakSurroundings.get() && isBedCovered(pos)) {
                            bedPos = pos.add(0, 1, 0);
                        } else {
                            bedPos = pos;
                        }
                        break;
                    }
                }
            }
        }
    }

    private void mine(BlockPos blockPos) {
        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        IBlockState blockState = mc.theWorld.getBlockState(blockPos);

        if (blockState.getBlock() instanceof BlockAir) {
            return;
        }

        if (breakProgress == 0) {
            rotate = true;
            if (autoTool.get() && !swap.get()) {
                doAutoTool(blockPos);
            }
            mc.thePlayer.swingItem();
            sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, bedPos, EnumFacing.UP));
        } else if (breakProgress >= 1) {
            rotate = true;
            if (autoTool.get() && swap.get()) {
                doAutoTool(blockPos);
            }
            mc.thePlayer.swingItem();
            sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, bedPos, EnumFacing.UP));

            reset(false);
            return;
        } else {
            if (!swap.get()) {
                rotate = true;
            }

            if (autoTool.get()) {
                if (!swap.get()) {
                    doAutoTool(blockPos);
                } else {
                    //mc.thePlayer.inventory.currentItem = 0;
                }
            }

            mc.thePlayer.swingItem();
        }

        if(swap.get()){
            breakProgress += (getBlockHardness(bedPos, PlayerUtil.findTool(bedPos) != -1 ? mc.thePlayer.inventory.getStackInSlot(PlayerUtil.findTool(bedPos)) : mc.thePlayer.getHeldItem(), ignoreSlow.get() , groundSpoof.get()));
        } else {
            breakProgress += mc.theWorld.getBlockState(bedPos).getBlock().getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, bedPos);
        }

        mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), bedPos, (int) (breakProgress * 10));
    }

    private void reset(boolean resetRotate) {
        if (bedPos != null) {
            mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), bedPos, -1);
            //test
            //sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK,bedPos,EnumFacing.DOWN));
        }

        breakProgress = 0;
        delayTicks = 5;
        bedPos = null;
        rotate = !resetRotate;
    }
    private void doAutoTool(BlockPos pos) {
        if(PlayerUtil.findTool(pos) != -1) {
            mc.thePlayer.inventory.currentItem = PlayerUtil.findTool(pos);
        }
    }

    private boolean isBedCovered(BlockPos headBlockBedPos) {
        BlockPos headBlockBedPosOffSet1 = headBlockBedPos.add(1, 0, 0);
        BlockPos headBlockBedPosOffSet2 = headBlockBedPos.add(-1, 0, 0);
        BlockPos headBlockBedPosOffSet3 = headBlockBedPos.add(0, 0, 1);
        BlockPos headBlockBedPosOffSet4 = headBlockBedPos.add(0, 0, -1);

        if (!isBlockCovered(headBlockBedPos)) {
            return false;
        } else if (mc.theWorld.getBlockState(headBlockBedPosOffSet1).getBlock() instanceof BlockBed && mc.theWorld.getBlockState(headBlockBedPosOffSet1).getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
            return isBlockCovered(headBlockBedPosOffSet1);
        } else if (mc.theWorld.getBlockState(headBlockBedPosOffSet2).getBlock() instanceof BlockBed && mc.theWorld.getBlockState(headBlockBedPosOffSet2).getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
            return isBlockCovered(headBlockBedPosOffSet2);
        } else if (mc.theWorld.getBlockState(headBlockBedPosOffSet3).getBlock() instanceof BlockBed && mc.theWorld.getBlockState(headBlockBedPosOffSet3).getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
            return isBlockCovered(headBlockBedPosOffSet3);
        } else if (mc.theWorld.getBlockState(headBlockBedPosOffSet4).getBlock() instanceof BlockBed && mc.theWorld.getBlockState(headBlockBedPosOffSet4).getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
            return isBlockCovered(headBlockBedPosOffSet4);
        }

        return false;
    }

    private boolean isBlockCovered(BlockPos blockPos) {
        BlockPos[] directions = {
                blockPos.add(0, 1, 0), // Up
                blockPos.add(1, 0, 0), // East
                blockPos.add(-1, 0, 0), // West
                blockPos.add(0, 0, 1), // South
                blockPos.add(0, 0, -1) // North
        };

        for (BlockPos pos : directions) {
            Block block = mc.theWorld.getBlockState(pos).getBlock();
            if (block instanceof BlockAir || block.getMaterial() instanceof MaterialLiquid) {
                return false;
            }
        }

        return true;
    }

    public static float getBlockHardness(final BlockPos blockPos, final ItemStack itemStack, boolean ignoreSlow, boolean ignoreGround) {
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
        final float getBlockHardness = block.getBlockHardness(mc.theWorld, null);
        if (getBlockHardness < 0.0f) {
            return 0.0f;
        }
        return (block.getMaterial().isToolNotRequired() || (itemStack != null && itemStack.canHarvestBlock(block))) ? (getToolDigEfficiency(itemStack, block, ignoreSlow, ignoreGround) / getBlockHardness / 30.0f) : (getToolDigEfficiency(itemStack, block, ignoreSlow, ignoreGround) / getBlockHardness / 100.0f);
    }

    public static float getToolDigEfficiency(ItemStack itemStack, Block block, boolean ignoreSlow, boolean ignoreGround) {
        float n = (itemStack == null) ? 1.0f : itemStack.getItem().getStrVsBlock(itemStack, block);
        if (n > 1.0f) {
            final int getEnchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);
            if (getEnchantmentLevel > 0 && itemStack != null) {
                n += getEnchantmentLevel * getEnchantmentLevel + 1;
            }
        }
        if (mc.thePlayer.isPotionActive(Potion.digSpeed)) {
            n *= 1.0f + (mc.thePlayer.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2f;
        }
        if (!ignoreSlow) {
            if (mc.thePlayer.isPotionActive(Potion.digSlowdown)) {
                float n2;
                switch (mc.thePlayer.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) {
                    case 0: {
                        n2 = 0.3f;
                        break;
                    }
                    case 1: {
                        n2 = 0.09f;
                        break;
                    }
                    case 2: {
                        n2 = 0.0027f;
                        break;
                    }
                    default: {
                        n2 = 8.1E-4f;
                        break;
                    }
                }
                n *= n2;
            }
            if (mc.thePlayer.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(mc.thePlayer)) {
                n /= 5.0f;
            }
            if (!mc.thePlayer.onGround && !ignoreGround) {
                n /= 5.0f;
            }
        }
        return n;
    }
}
