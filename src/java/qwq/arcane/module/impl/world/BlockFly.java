package qwq.arcane.module.impl.world;


import net.minecraft.block.*;
import qwq.arcane.Client;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Mine;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.TickEvent;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.event.impl.events.player.PlaceEvent;
import qwq.arcane.event.impl.events.player.StrafeEvent;
import qwq.arcane.module.Category;
import qwq.arcane.utils.animations.Animation;
import qwq.arcane.utils.animations.impl.DecelerateAnimation;
import qwq.arcane.utils.math.MathUtils;
import qwq.arcane.utils.math.Vector2f;
import qwq.arcane.utils.player.*;
import qwq.arcane.utils.render.BlockUtil;
import qwq.arcane.utils.render.PlaceInfo;
import qwq.arcane.utils.rotation.RotationUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.NumberValue;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class BlockFly
extends qwq.arcane.module.Module {
    private final Animation anim = new DecelerateAnimation(250, 1.0);
    boolean idk = false;
    public static final List<Block> invalidBlocks = Arrays.asList(Blocks.enchanting_table, Blocks.furnace, Blocks.carpet, Blocks.crafting_table, Blocks.trapped_chest, Blocks.chest, Blocks.dispenser, Blocks.air, Blocks.water, Blocks.lava, Blocks.flowing_water, Blocks.flowing_lava, Blocks.sand, Blocks.snow_layer, Blocks.torch, Blocks.anvil, Blocks.jukebox, Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.noteblock, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.wooden_pressure_plate, Blocks.heavy_weighted_pressure_plate, Blocks.stone_slab, Blocks.wooden_slab, Blocks.stone_slab2, Blocks.red_mushroom, Blocks.brown_mushroom, Blocks.yellow_flower, Blocks.red_flower, Blocks.anvil, Blocks.glass_pane, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.cactus, Blocks.ladder, Blocks.web, Blocks.tnt);
    public static double keepYCoord;
    public final BoolValue swing = new BoolValue("Swing", true);
    public final BoolValue sprintValue = new BoolValue("Sprint", false);
    private static final BoolValue keepYValue;
    private final NumberValue tellyTicks = new NumberValue("TellyTicks", 2.9, 0.5, 8.0, 0.01);
    public final BoolValue eagle = new BoolValue("Eagle", false);
    public final BoolValue telly = new BoolValue("Telly", true);
    public final BoolValue upValue = new BoolValue("Up", () -> this.telly.getValue() && !keepYValue.getValue(), false);
    public boolean tip = false;
    private int direction;
    int idkTick = 0;
    private int slot;
    private PlaceInfo data;
    protected Random rand = new Random();
    private boolean canTellyPlace;
    private int prevItem = 0;
    int towerTick = 0;

    public BlockFly() {
        super("BlockFly", Category.World);
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer != null && mc.gameSettings != null) {
            this.idkTick = 5;
            this.prevItem = mc.thePlayer.inventory.currentItem;
            mc.thePlayer.setSprinting(this.sprintValue.getValue() || !this.canTellyPlace);
            mc.gameSettings.keyBindSprint.pressed = this.sprintValue.getValue() || !this.canTellyPlace;
            this.canTellyPlace = false;
            this.tip = false;
            this.data = null;
            this.slot = -1;
        }
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer == null) {
            return;
        }
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        mc.thePlayer.inventory.currentItem = this.prevItem;
        SlotSpoofComponent.stopSpoofing();
    }

    @EventTarget
    public void onUpdate(MotionEvent event) {
        setsuffix(String.valueOf(this.tellyTicks.get()));
        if (this.idkTick > 0) {
            --this.idkTick;
        }
        if (event.isPre() && this.eagle.getValue()) {
            if (mc.thePlayer.onGround) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            }
        }
    }
    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if ((this.upValue.getValue() || keepYValue.getValue()) && mc.thePlayer.onGround && MovementUtil.isMoving() && !mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.thePlayer.jump();
        }
    }

    @EventTarget
    private void onTick(TickEvent event) {
        if (mc.thePlayer == null) {
            return;
        }
        if (this.slot < 0) {
            return;
        }
        if (!this.telly.getValue()) {
            this.canTellyPlace = true;
        }
    }

    @EventTarget
    private void onPlace(PlaceEvent event) {
        this.slot = this.getBlockSlot();
        if (this.slot < 0) {
            return;
        }
        if (!this.telly.getValue()) {
            mc.thePlayer.setSprinting(this.sprintValue.getValue());
            mc.gameSettings.keyBindSprint.pressed = false;
        }
        event.setCancelled(true);
        this.place();
        mc.sendClickBlockToController(mc.currentScreen == null && mc.gameSettings.keyBindAttack.isKeyDown() && mc.inGameHasFocus);
    }

    public static double getYLevel() {
        if (!keepYValue.getValue()) {
            return mc.thePlayer.posY - 1.0;
        }
        return !MovementUtil.isMoving() ? mc.thePlayer.posY - 1.0 : keepYCoord;
    }

    @EventTarget
    public void onSetValue(TickEvent e) {
        if (this.telly.getValue()) {
            if (mc.gameSettings.keyBindJump.pressed) {
                this.upValue.set(true);
                keepYValue.set(false);
            } else {
                this.upValue.set(false);
                keepYValue.set(true);
            }
        }
    }

    @EventTarget
    private void onUpdateMotionEvent(UpdateEvent event) {
        if (mc.thePlayer.onGround) {
            keepYCoord = Math.floor(mc.thePlayer.posY - 1.0);
        }
        this.slot = this.getBlockSlot();
        if (this.slot < 1) {
            return;
        }
        this.findBlock();
        mc.thePlayer.inventory.currentItem = this.slot;
        SlotSpoofComponent.startSpoofing(this.prevItem);
        if (this.telly.getValue()) {
            if (this.canTellyPlace && !mc.thePlayer.onGround && MovementUtil.isMoving()) {
                mc.thePlayer.setSprinting(false);
            }
            this.canTellyPlace = (double) mc.thePlayer.offGroundTicks >= (this.upValue.getValue() ? (double) (mc.thePlayer.ticksExisted % 16 == 0 ? 2 : 1) : (Double) this.tellyTicks.getValue());
        }
        if (!this.canTellyPlace) {
            return;
        }
        if (this.data != null) {
            float yaw = RotationUtil.getRotationBlock2(data.getBlockPos())[0];
            float pitch = RotationUtil.getRotationBlock2(data.getBlockPos())[1];
            Client.Instance.getRotationManager().setRotation(new Vector2f(yaw, pitch), 180.0f, true);
            mc.thePlayer.setSprinting(this.sprintValue.getValue());
            if (this.idkTick != 0) {
                this.towerTick = 0;
                return;
            }
            if (this.towerTick > 0) {
                ++this.towerTick;
                if (this.towerTick > 6) {
                    this.idk1(MovementUtil.speed() * 0.05);
                }
                if (this.towerTick > 16) {
                    this.towerTick = 0;
                }
            }
        }
    }

    public void idk1(double d) {
        float f = MathHelper.wrapAngleTo180_float((float)Math.toDegrees(Math.atan2(mc.thePlayer.motionZ, mc.thePlayer.motionX)) - 90.0f);
        MovementUtil.setMotion2(d, f);
    }

    @EventTarget
    private void onMotion(MotionEvent event) {
        if (event.isPre()) {
            if (this.getBlockCount() < 1) {
                return;
            }
            if (this.getBlockCount() <= 0) {
                int spoofSlot = this.getBestSpoofSlot();
                this.getBlock(spoofSlot);
            }
            if (this.slot < 0) {
                return;
            }
            mc.thePlayer.inventoryContainer.getSlot(this.slot + 36).getStack();
        }
    }

    private void towerMove() {
        if (mc.thePlayer.onGround) {
            if (this.towerTick == 0 || this.towerTick == 5) {
                float f = mc.thePlayer.rotationYaw * ((float)Math.PI / 180);
                mc.thePlayer.motionX -= (double)(MathHelper.sin(f) * 0.2f) * 95.0 / 100.0;
                mc.thePlayer.motionY = 0.42f;
                mc.thePlayer.motionZ += (double)(MathHelper.cos(f) * 0.2f) * 95.0 / 100.0;
                this.towerTick = 1;
            }
        } else if (mc.thePlayer.motionY > -0.0784000015258789) {
            int n = (int)Math.round(mc.thePlayer.posY % 1.0 * 100.0);
            switch (n) {
                case 42: {
                    mc.thePlayer.motionY = 0.33;
                    break;
                }
                case 75: {
                    mc.thePlayer.motionY = 1.0 - mc.thePlayer.posY % 1.0;
                    this.idk = true;
                    break;
                }
                case 0: {
                    mc.thePlayer.motionY = -0.0784000015258789;
                }
            }
        }
    }

    private void place() {
        if (!this.canTellyPlace) {
            return;
        }
        this.slot = this.getBlockSlot();
        if (this.slot < 0) {
            return;
        }
        if (PlayerUtil.block(mc.thePlayer.posX, getYLevel(), mc.thePlayer.posZ) instanceof BlockAir) {
            if (this.getBlockCount() < 1) {
                return;
            }
            if (this.data != null) {
                boolean normalPlace = this.sprintValue.getValue();
                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem(), this.data.getBlockPos(), normalPlace ? this.data.getEnumFacing() : mc.objectMouseOver.sideHit, normalPlace ? getVec3(this.data.getBlockPos(), this.data.getEnumFacing()) : mc.objectMouseOver.hitVec)) {
                    if (this.swing.getValue()) {
                        mc.thePlayer.swingItem();
                    } else {
                        mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                    }
                }
            }
        }
    }

    private void findBlock() {
        boolean shouldGoDown = false;
        BlockPos blockPosition = new BlockPos(mc.thePlayer.posX, getYLevel(), mc.thePlayer.posZ);
        Block block = Mine.getMinecraft().theWorld.getBlockState(blockPosition).getBlock();
        if (BlockUtil.isValidBock(blockPosition) || this.search(blockPosition, !shouldGoDown)) {
            return;
        }
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                if (!this.search(blockPosition.add(x, 0, z), !shouldGoDown)) continue;
                return;
            }
        }
    }

    private double calcStepSize(double range) {
        double accuracy = 6.0;
        accuracy += accuracy % 2.0;
        return Math.max(range / accuracy, 0.01);
    }

    private boolean search(BlockPos blockPosition, boolean checks) {
        if (BlockUtil.isValidBock(blockPosition)) {
            return false;
        }
        Vec3 eyesPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + (double)mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        PlaceRotation placeRotation = null;
        double xzRV = 0.5;
        double yRV = 0.5;
        double xzSSV = this.calcStepSize(xzRV);
        double ySSV = this.calcStepSize(xzRV);
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbor = blockPosition.offset(side);
            if (!BlockUtil.isValidBock(neighbor)) continue;
            Vec3 dirVec = new Vec3(side.getDirectionVec());
            for (double xSearch = 0.5 - xzRV / 2.0; xSearch <= 0.5 + xzRV / 2.0; xSearch += xzSSV) {
                for (double ySearch = 0.5 - yRV / 2.0; ySearch <= 0.5 + yRV / 2.0; ySearch += ySSV) {
                    for (double zSearch = 0.5 - xzRV / 2.0; zSearch <= 0.5 + xzRV / 2.0; zSearch += xzSSV) {
                        Vec3 posVec = new Vec3(blockPosition).addVector(xSearch, ySearch, zSearch);
                        double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
                        Vec3 hitVec = posVec.add(new Vec3(dirVec.xCoord * 0.5, dirVec.yCoord * 0.5, dirVec.zCoord * 0.5));
                        if (checks && (eyesPos.squareDistanceTo(hitVec) > 18.0 || distanceSqPosVec > eyesPos.squareDistanceTo(posVec.add(dirVec)) || mc.theWorld.rayTraceBlocks(eyesPos, hitVec, false, true, false) != null)) continue;
                        double diffX = hitVec.xCoord - eyesPos.xCoord;
                        double diffY = hitVec.yCoord - eyesPos.yCoord;
                        double diffZ = hitVec.zCoord - eyesPos.zCoord;
                        double diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
                        Rotation rotation = new Rotation(MathHelper.wrapAngleTo180_float((float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f), MathHelper.wrapAngleTo180_float((float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)))));
                        Vec3 rotationVector = new Vec3(RotationUtil.getVectorForRotation(rotation).xCoord, RotationUtil.getVectorForRotation(rotation).yCoord, RotationUtil.getVectorForRotation(rotation).zCoord);
                        Vec3 vector = eyesPos.addVector(rotationVector.xCoord * 4.0, rotationVector.yCoord * 4.0, rotationVector.zCoord * 4.0);
                        MovingObjectPosition obj = mc.theWorld.rayTraceBlocks(eyesPos, vector, false, false, true);
                        if (obj.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !obj.getBlockPos().equals(neighbor) || placeRotation != null && !(Client.Instance.getRotationManager().getRotationDifference(rotation) < Client.Instance.getRotationManager().getRotationDifference(placeRotation.getRotation()))) continue;
                        placeRotation = new PlaceRotation(new PlaceInfo(neighbor, side.getOpposite(), hitVec), rotation);
                    }
                }
            }
        }
        if (placeRotation == null) {
            return false;
        }
        this.data = placeRotation.getPlaceInfo();
        return true;
    }

    public static Vec3 getVec3(BlockPos pos, EnumFacing face) {
        double x = (double)pos.getX() + 0.5;
        double y = (double)pos.getY() + 0.5;
        double z = (double)pos.getZ() + 0.5;
        if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
            x += MathUtils.getRandomInRange(0.3, -0.3);
            z += MathUtils.getRandomInRange(0.3, -0.3);
        } else {
            y += MathUtils.getRandomInRange(0.3, -0.3);
        }
        if (face == EnumFacing.WEST || face == EnumFacing.EAST) {
            z += MathUtils.getRandomInRange(0.3, -0.3);
        }
        if (face == EnumFacing.SOUTH || face == EnumFacing.NORTH) {
            x += MathUtils.getRandomInRange(0.3, -0.3);
        }
        return new Vec3(x, y, z);
    }

    public int getBlockSlot() {
        for (int i = 0; i < 9; ++i) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i + 36).getHasStack() || !(mc.thePlayer.inventoryContainer.getSlot(i + 36).getStack().getItem() instanceof ItemBlock)) continue;
            return i;
        }
        return -1;
    }

    private PlaceInfo getPlaceInfo(BlockPos pos) {
        if (this.isPosSolid(pos.add(0, -1, 0))) {
            return new PlaceInfo(pos.add(0, -1, 0), EnumFacing.UP);
        }
        if (this.isPosSolid(pos.add(-1, 0, 0))) {
            return new PlaceInfo(pos.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(pos.add(1, 0, 0))) {
            return new PlaceInfo(pos.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(pos.add(0, 0, 1))) {
            return new PlaceInfo(pos.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(pos.add(0, 0, -1))) {
            return new PlaceInfo(pos.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos pos1 = pos.add(-1, 0, 0);
        if (this.isPosSolid(pos1.add(0, -1, 0))) {
            return new PlaceInfo(pos1.add(0, -1, 0), EnumFacing.UP);
        }
        if (this.isPosSolid(pos1.add(-1, 0, 0))) {
            return new PlaceInfo(pos1.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(pos1.add(1, 0, 0))) {
            return new PlaceInfo(pos1.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(pos1.add(0, 0, 1))) {
            return new PlaceInfo(pos1.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(pos1.add(0, 0, -1))) {
            return new PlaceInfo(pos1.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos pos2 = pos.add(1, 0, 0);
        if (this.isPosSolid(pos2.add(0, -1, 0))) {
            return new PlaceInfo(pos2.add(0, -1, 0), EnumFacing.UP);
        }
        if (this.isPosSolid(pos2.add(-1, 0, 0))) {
            return new PlaceInfo(pos2.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(pos2.add(1, 0, 0))) {
            return new PlaceInfo(pos2.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(pos2.add(0, 0, 1))) {
            return new PlaceInfo(pos2.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(pos2.add(0, 0, -1))) {
            return new PlaceInfo(pos2.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos pos3 = pos.add(0, 0, 1);
        if (this.isPosSolid(pos3.add(0, -1, 0))) {
            return new PlaceInfo(pos3.add(0, -1, 0), EnumFacing.UP);
        }
        if (this.isPosSolid(pos3.add(-1, 0, 0))) {
            return new PlaceInfo(pos3.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(pos3.add(1, 0, 0))) {
            return new PlaceInfo(pos3.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(pos3.add(0, 0, 1))) {
            return new PlaceInfo(pos3.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(pos3.add(0, 0, -1))) {
            return new PlaceInfo(pos3.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos pos4 = pos.add(0, 0, -1);
        if (this.isPosSolid(pos4.add(0, -1, 0))) {
            return new PlaceInfo(pos4.add(0, -1, 0), EnumFacing.UP);
        }
        if (this.isPosSolid(pos4.add(-1, 0, 0))) {
            return new PlaceInfo(pos4.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(pos4.add(1, 0, 0))) {
            return new PlaceInfo(pos4.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(pos4.add(0, 0, 1))) {
            return new PlaceInfo(pos4.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(pos4.add(0, 0, -1))) {
            return new PlaceInfo(pos4.add(0, 0, -1), EnumFacing.SOUTH);
        }
        pos.add(-2, 0, 0);
        if (this.isPosSolid(pos1.add(0, -1, 0))) {
            return new PlaceInfo(pos1.add(0, -1, 0), EnumFacing.UP);
        }
        if (this.isPosSolid(pos1.add(-1, 0, 0))) {
            return new PlaceInfo(pos1.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(pos1.add(1, 0, 0))) {
            return new PlaceInfo(pos1.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(pos1.add(0, 0, 1))) {
            return new PlaceInfo(pos1.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(pos1.add(0, 0, -1))) {
            return new PlaceInfo(pos1.add(0, 0, -1), EnumFacing.SOUTH);
        }
        pos.add(2, 0, 0);
        if (this.isPosSolid(pos2.add(0, -1, 0))) {
            return new PlaceInfo(pos2.add(0, -1, 0), EnumFacing.UP);
        }
        if (this.isPosSolid(pos2.add(-1, 0, 0))) {
            return new PlaceInfo(pos2.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(pos2.add(1, 0, 0))) {
            return new PlaceInfo(pos2.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(pos2.add(0, 0, 1))) {
            return new PlaceInfo(pos2.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(pos2.add(0, 0, -1))) {
            return new PlaceInfo(pos2.add(0, 0, -1), EnumFacing.SOUTH);
        }
        pos.add(0, 0, 2);
        if (this.isPosSolid(pos3.add(0, -1, 0))) {
            return new PlaceInfo(pos3.add(0, -1, 0), EnumFacing.UP);
        }
        if (this.isPosSolid(pos3.add(-1, 0, 0))) {
            return new PlaceInfo(pos3.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(pos3.add(1, 0, 0))) {
            return new PlaceInfo(pos3.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(pos3.add(0, 0, 1))) {
            return new PlaceInfo(pos3.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(pos3.add(0, 0, -1))) {
            return new PlaceInfo(pos3.add(0, 0, -1), EnumFacing.SOUTH);
        }
        pos.add(0, 0, -2);
        if (this.isPosSolid(pos4.add(0, -1, 0))) {
            return new PlaceInfo(pos4.add(0, -1, 0), EnumFacing.UP);
        }
        if (this.isPosSolid(pos4.add(-1, 0, 0))) {
            return new PlaceInfo(pos4.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(pos4.add(1, 0, 0))) {
            return new PlaceInfo(pos4.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(pos4.add(0, 0, 1))) {
            return new PlaceInfo(pos4.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(pos4.add(0, 0, -1))) {
            return new PlaceInfo(pos4.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos pos5 = pos.add(0, -1, 0);
        if (this.isPosSolid(pos5.add(0, -1, 0))) {
            return new PlaceInfo(pos5.add(0, -1, 0), EnumFacing.UP);
        }
        if (this.isPosSolid(pos5.add(-1, 0, 0))) {
            return new PlaceInfo(pos5.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(pos5.add(1, 0, 0))) {
            return new PlaceInfo(pos5.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(pos5.add(0, 0, 1))) {
            return new PlaceInfo(pos5.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(pos5.add(0, 0, -1))) {
            return new PlaceInfo(pos5.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos pos6 = pos5.add(1, 0, 0);
        if (this.isPosSolid(pos6.add(0, -1, 0))) {
            return new PlaceInfo(pos6.add(0, -1, 0), EnumFacing.UP);
        }
        if (this.isPosSolid(pos6.add(-1, 0, 0))) {
            return new PlaceInfo(pos6.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(pos6.add(1, 0, 0))) {
            return new PlaceInfo(pos6.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(pos6.add(0, 0, 1))) {
            return new PlaceInfo(pos6.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(pos6.add(0, 0, -1))) {
            return new PlaceInfo(pos6.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos pos7 = pos5.add(-1, 0, 0);
        if (this.isPosSolid(pos7.add(0, -1, 0))) {
            return new PlaceInfo(pos7.add(0, -1, 0), EnumFacing.UP);
        }
        if (this.isPosSolid(pos7.add(-1, 0, 0))) {
            return new PlaceInfo(pos7.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(pos7.add(1, 0, 0))) {
            return new PlaceInfo(pos7.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(pos7.add(0, 0, 1))) {
            return new PlaceInfo(pos7.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(pos7.add(0, 0, -1))) {
            return new PlaceInfo(pos7.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos pos8 = pos5.add(0, 0, 1);
        if (this.isPosSolid(pos8.add(0, -1, 0))) {
            return new PlaceInfo(pos8.add(0, -1, 0), EnumFacing.UP);
        }
        if (this.isPosSolid(pos8.add(-1, 0, 0))) {
            return new PlaceInfo(pos8.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(pos8.add(1, 0, 0))) {
            return new PlaceInfo(pos8.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(pos8.add(0, 0, 1))) {
            return new PlaceInfo(pos8.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(pos8.add(0, 0, -1))) {
            return new PlaceInfo(pos8.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos pos9 = pos5.add(0, 0, -1);
        if (this.isPosSolid(pos9.add(0, -1, 0))) {
            return new PlaceInfo(pos9.add(0, -1, 0), EnumFacing.UP);
        }
        if (this.isPosSolid(pos9.add(-1, 0, 0))) {
            return new PlaceInfo(pos9.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(pos9.add(1, 0, 0))) {
            return new PlaceInfo(pos9.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(pos9.add(0, 0, 1))) {
            return new PlaceInfo(pos9.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(pos9.add(0, 0, -1))) {
            return new PlaceInfo(pos9.add(0, 0, -1), EnumFacing.SOUTH);
        }
        return null;
    }

    private boolean isPosSolid(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        return (block.getMaterial().isSolid() || !block.isTranslucent() || block.isVisuallyOpaque() || block instanceof BlockLadder || block instanceof BlockCarpet || block instanceof BlockSnow || block instanceof BlockSkull) && !block.getMaterial().isLiquid() && !(block instanceof BlockContainer);
    }

    public int getBlockCount() {
        int n = 0;
        for (int i = 36; i < 45; ++i) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) continue;
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            Item item = stack.getItem();
            if (!(stack.getItem() instanceof ItemBlock) || !this.isValid(item)) continue;
            n += stack.stackSize;
        }
        return n;
    }

    private boolean isValid(Item item) {
        return item instanceof ItemBlock && !invalidBlocks.contains(((ItemBlock)item).getBlock());
    }

    private float getYaw() {
        if (mc.gameSettings.keyBindBack.isKeyDown()) {
            return mc.thePlayer.rotationYaw;
        }
        if (mc.gameSettings.keyBindLeft.isKeyDown()) {
            return mc.thePlayer.rotationYaw + 90.0f;
        }
        if (mc.gameSettings.keyBindRight.isKeyDown()) {
            return mc.thePlayer.rotationYaw - 90.0f;
        }
        return mc.thePlayer.rotationYaw - 180.0f;
    }

    private void getBlock(int switchSlot) {
        for (int i = 9; i < 45; ++i) {
            ItemBlock block;
            ItemStack is;
            if (!mc.thePlayer.inventoryContainer.getSlot(i).getHasStack() || mc.currentScreen != null && !(mc.currentScreen instanceof GuiInventory) || !((is = mc.thePlayer.inventoryContainer.getSlot(i).getStack()).getItem() instanceof ItemBlock) || !this.isValid(block = (ItemBlock)is.getItem())) continue;
            if (36 + switchSlot == i) break;
            InventoryUtil.swap(i, switchSlot);
            break;
        }
    }

    int getBestSpoofSlot() {
        int spoofSlot = 5;
        for (int i = 36; i < 45; ++i) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) continue;
            spoofSlot = i - 36;
            break;
        }
        return spoofSlot;
    }

    public int getSlot() {
        return this.slot;
    }

    static {
        keepYValue = new BoolValue("Keep Y", false);
    }
}

