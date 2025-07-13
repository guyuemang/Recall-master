package qwq.arcane.module.impl.world;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.TickEvent;
import qwq.arcane.event.impl.events.player.*;
import qwq.arcane.event.impl.events.render.Render2DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.animations.Animation;
import qwq.arcane.utils.animations.impl.DecelerateAnimation;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.math.MathUtils;
import qwq.arcane.utils.math.Vector2f;
import qwq.arcane.utils.player.*;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.utils.rotation.MovementFix;
import qwq.arcane.utils.rotation.RayCastUtil;
import qwq.arcane.utils.rotation.RotationComponent;
import qwq.arcane.utils.rotation.RotationUtil;
import qwq.arcane.value.impl.BooleanValue;
import qwq.arcane.value.impl.ModeValue;
import qwq.arcane.value.impl.NumberValue;

import java.awt.*;
import java.util.*;

/**
 * @Author：Guyuemang
 * @Date：7/6/2025 11:54 PM
 */
public class Scaffold extends Module {
    public Scaffold() {
        super("Scaffold",Category.World);
    }
    public static ModeValue mode = new ModeValue("Mode","WatchdogTelly",new String[]{"WatchdogTelly"});
    public static NumberValue tick = new NumberValue("Tick",1.0,0.0,3.0,1.0);
    public static BooleanValue sprint = new BooleanValue("Sprint",false);
    //public static BooleanValue keepy = new BooleanValue("KeepY",false);
    public int slot;
    //你妈死了badpacketA
    public int airtick = 0;
    public int startslot;
    private BlockPos pos;
    private EnumFacing facing;
    private Vector2f targetRotation;
    public int baseY = -1;

    public boolean sb;

//    public float yaw, pitch, blockYaw, yawOffset, lastOffset;
//    private boolean was451, was452;
//    private float minPitch, minOffset, pOffset;
//    private long firstStroke, yawEdge, vlS;
//    private float lastEdge2, yawAngle, theYaw;
//    private boolean set2;
//    private int randomF, yawChanges, dynamic;
//    private int switchvl;
//    private float[] blockRotations;

    public boolean fristjump;
    public boolean needtelly;
    private float y;
    private int floatticks;

    @Override
    public void onEnable(){
        startslot = mc.thePlayer.inventory.currentItem;
        y = 80;
        fristjump = true;
        baseY = -1;
    }
    @Override
    public void onDisable(){
        mc.thePlayer.inventory.currentItem = startslot;
    }
    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if ((mode.is("WatchdogTelly"))) {
            if ((mc.thePlayer.onGround && (mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0) && !mc.gameSettings.keyBindJump.isKeyDown())) {
                mc.thePlayer.jump();
            }
        }
    }

    public static float hardcodedYaw() {
        float simpleYaw = 0F;
        float f = 0.8F;

        if (mc.thePlayer.moveForward >= f) {
            simpleYaw -= 180;
            if (mc.thePlayer.moveStrafing >= f) simpleYaw += 45;
            if (mc.thePlayer.moveStrafing <= -f) simpleYaw -= 45;
        } else if (mc.thePlayer.moveForward == 0) {
            simpleYaw -= 180;
            if (mc.thePlayer.moveStrafing >= f) simpleYaw += 90;
            if (mc.thePlayer.moveStrafing <= -f) simpleYaw -= 90;
        } else if (mc.thePlayer.moveForward <= -f) {
            if (mc.thePlayer.moveStrafing >= f) simpleYaw -= 45;
            if (mc.thePlayer.moveStrafing <= -f) simpleYaw += 45;
        }
        return simpleYaw;
    }
    @EventTarget
    public void onUpdate(UpdateEvent e){
        if (!sprint.getValue()){
            mc.thePlayer.setSprinting(false);
            mc.gameSettings.keyBindSprint.pressed = false;
        }
        if (baseY == -1 || baseY > (int) mc.thePlayer.posY - 1 || mc.thePlayer.onGround || mc.gameSettings.keyBindJump.isKeyDown()) {
            baseY = (int) mc.thePlayer.posY - 1;
        }
        if (mode.is("Telly")) {
            slot = getBlockSlot();

            if (slot == -1) {
                return;
            }
            findBlock();
            if (!mc.thePlayer.onGround) {
                airtick++;
            } else {
                airtick = 0;
            }
            needtelly = airtick > tick.get();
            if (needtelly) {
                mc.thePlayer.inventory.currentItem = slot;
                if (pos != null) {
                    float yaw = RotationUtil.getRotationBlock(this.pos)[0];
                    float pitch = RotationUtil.getRotationBlock(this.pos)[1];
                    RotationComponent.setRotations(new Vector2f(yaw, pitch), 180, MovementFix.NORMAL);
                }
            } else {
                mc.thePlayer.inventory.currentItem = startslot;
            }
        } else {
            if (!mc.thePlayer.onGround) {
                airtick++;
            } else {
                airtick = 0;
            }
            slot = getBlockSlot();

            if (slot == -1) {
                return;
            }
            findBlock();

            mc.thePlayer.inventory.currentItem = slot;

            if (mode.is("WatchdogTelly") && mc.thePlayer.onGround){
                return;
            }
            float[] rotations = new float[]{0, 0};

//            float moveAngle = (float) getMovementAngle();
//            float relativeYaw = mc.thePlayer.rotationYaw + moveAngle;
//            float normalizedYaw = (relativeYaw % 360 + 360) % 360;
//            float quad = normalizedYaw % 90;
//
//            float side = MathHelper.wrapAngleTo180_float(getMotionYaw() - yaw);
//            float yawBackwards = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) - hardcodedYaw();
//            float blockYawOffset = MathHelper.wrapAngleTo180_float(yawBackwards - blockYaw);
//
//            long strokeDelay = 250;
//
//            float first = 74.25F;
//            float sec = 79.25F;
//
//            if (quad <= 5 || quad >= 85) {
//                yawAngle = 123.625F;
//                minOffset = 11;
//                minPitch = first;
//            }
//            if (quad > 5 && quad <= 15 || quad >= 75 && quad < 85) {
//                yawAngle = 126.625F;
//                minOffset = 9;
//                minPitch = first;
//            }
//            if (quad > 15 && quad <= 25 || quad >= 65 && quad < 75) {
//                yawAngle = 128.625F;
//                minOffset = 8;
//                minPitch = first;
//            }
//            if (quad > 25 && quad <= 32 || quad >= 58 && quad < 65) {
//                yawAngle = 131.425F;
//                minOffset = 7;
//                minPitch = sec;
//            }
//            if (quad > 32 && quad <= 38 || quad >= 52 && quad < 58) {
//                yawAngle = 133.825F;
//                minOffset = 6;
//                minPitch = sec;
//            }
//            if (quad > 38 && quad <= 42 || quad >= 48 && quad < 52) {
//                yawAngle = 136.825F;
//                minOffset = 4;
//                minPitch = sec;
//            }
//            if (quad > 42 && quad <= 45 || quad >= 45 && quad < 48) {
//                yawAngle = 140.325F;
//                minOffset = 4;
//                minPitch = sec;
//            }
//            //Utils.print("" + minOffset);
//            //float offsetAmountD = ((((float) offsetAmount.getInput() / 10) - 10) * -2) - (((float) offsetAmount.getInput() / 10) - 10);
//            //yawAngle += offsetAmountD;
//            //Utils.print("" + offsetAmountD);
//
//            float offset = yawAngle;//(!Utils.scaffoldDiagonal(false)) ? 125.500F : 143.500F;
//
//
//            float nigger = 0;
//
//            if (quad > 45) {
//                nigger = 10;
//            }
//            else {
//                nigger = -10;
//            }
//            if (switchvl > 0) {
//                if (vlS > 0 && (System.currentTimeMillis() - vlS) > strokeDelay) {
//                    firstStroke = 0;
//                    switchvl = 0;
//                    vlS = 0;
//                }
//                if (switchvl > 1) {
//                    firstStroke = MovementUtils.time();
//                    switchvl = 0;
//                    vlS = 0;
//                }
//            }
//            else {
//                vlS = MovementUtils.time();
//            }
//            if (firstStroke > 0 && (System.currentTimeMillis() - firstStroke) > strokeDelay) {
//                firstStroke = 0;
//            }
//
//            if (blockRotations != null) {
//                blockYaw = blockRotations[0];
//                pitch = blockRotations[1];
//                yawOffset = blockYawOffset;
//                if (pitch < minPitch) {
//                    pitch = minPitch;
//                }
//            } else {
//                pitch = minPitch;
//                yawOffset = 5;
//                dynamic = 2;
//            }
//
//            float motionYaw = getMotionYaw();
//
//            float newYaw = motionYaw - offset * Math.signum(
//                    MathHelper.wrapAngleTo180_float(motionYaw - yaw)
//            );
//            yaw = MathHelper.wrapAngleTo180_float(newYaw);
//
//            if (quad > 5 && quad < 85 && dynamic > 0) {
//                if (quad < 45F) {
//                    if (firstStroke == 0) {
//                        if (side >= 0) {
//                            set2 = false;
//                        } else {
//                            set2 = true;
//                        }
//                    }
//                    if (was452) {
//                        switchvl++;
//                    }
//                    was451 = true;
//                    was452 = false;
//                } else {
//                    if (firstStroke == 0) {
//                        if (side >= 0) {
//                            set2 = true;
//                        } else {
//                            set2 = false;
//                        }
//                    }
//                    if (was451) {
//                        switchvl++;
//                    }
//                    was452 = true;
//                    was451 = false;
//                }
//            }
//            double minSwitch = (!MovementUtils.scaffoldDiagonal(false)) ? 9 : 15;
//            if (side >= 0) {
//                if (yawOffset <= -minSwitch && firstStroke == 0 && dynamic > 0) {
//                    if (quad <= 5 || quad >= 85) {
//                        if (set2) {
//                            switchvl++;
//                        }
//                        set2 = false;
//                    }
//                } else if (yawOffset >= 0 && firstStroke == 0 && dynamic > 0) {
//                    if (quad <= 5 || quad >= 85) {
//                        if (yawOffset >= minSwitch) {
//                            if (!set2) {
//                                switchvl++;
//                            }
//                            set2 = true;
//                        }
//                    }
//                }
//                if (set2) {
//                    if (yawOffset <= -0) yawOffset = -0;
//                    if (yawOffset >= minOffset) yawOffset = minOffset;
//                    theYaw = (yaw + offset * 2) - yawOffset;
//                    RotationManager.setRotation(new Vector2f(yaw, pitch), 1, false,false);;
//                    return;
//                }
//            } else if (side <= -0) {
//                if (yawOffset >= minSwitch && firstStroke == 0 && dynamic > 0) {
//                    if (quad <= 5 || quad >= 85) {
//                        if (set2) {
//                            switchvl++;
//                        }
//                        set2 = false;
//                    }
//                } else if (yawOffset <= 0 && firstStroke == 0 && dynamic > 0) {
//                    if (quad <= 5 || quad >= 85) {
//                        if (yawOffset <= -minSwitch) {
//                            if (!set2) {
//                                switchvl++;
//                            }
//                            set2 = true;
//                        }
//                    }
//                }
//                if (set2) {
//                    if (yawOffset >= 0) yawOffset = 0;
//                    if (yawOffset <= -minOffset) yawOffset = -minOffset;
//                    theYaw = (yaw - offset * 2) - yawOffset;
//                    RotationManager.setRotation(new Vector2f(yaw , pitch), 1, false,false);;
//                    return;
//                }
//            }
//
//            if (side >= 0) {
//                if (yawOffset >= 0) yawOffset = 97;
//                if (yawOffset <= -minOffset) yawOffset = -minOffset - 98;
//            } else if (side <= -0) {
//                if (yawOffset <= -0) yawOffset = -99;
//                if (yawOffset >= minOffset) yawOffset = minOffset + 98;
//            }
//
//            theYaw = yaw - yawOffset - 5;
//            if (Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && MovementUtils.isMoving()) {
//                theYaw = 160;
//            }
            rotations = new float[] {MovementUtil.getMoveYaw(mc.thePlayer.rotationYaw) - 180f, y};
            if (mc.thePlayer.onGround && !MovementUtil.isMoving()) {
                if (this.pos != null && (mc.thePlayer.ticksExisted % 3 == 0 || mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)).getBlock() == Blocks.air)) {
                    rotations = RotationUtil.getRotations(pos, facing);
                }
            }

            if (mode.is("WatchdogTelly") && mc.thePlayer.onGround){
                return;
            }
            RotationComponent.setRotations(new Vector2f(rotations[0],rotations[1]), 180, MovementFix.NORMAL);
        }

    }
    public float getMotionYaw() {
        return (float) Math.toDegrees(Math.atan2(mc.thePlayer.motionZ, mc.thePlayer.motionX)) - 90.0F;
    }
    @EventTarget
    public void onUpdate(MotionEvent e) {
        if (e.isPost()) return;
    }
    @EventTarget
    private void onPlace(PlaceEvent event) {
        // findBlock();
        this.slot = this.getBlockSlot();
        if (this.slot < 0) {
            return;
        }
        event.setCancelled(true);
        if (mc.thePlayer == null) {
            return;
        }
        if (mode.is("WatchdogTelly") && mc.thePlayer.onGround){
            return;
        }
        this.place();
        mc.sendClickBlockToController(mc.currentScreen == null && mc.gameSettings.keyBindAttack.isKeyDown() && mc.inGameHasFocus);

    }

    private void place() {
        this.slot = getBlockSlot();
        if (this.slot < 0) return;
        if (pos != null) {
            Vector2f rotation = null;
            rotation = RotationComponent.rotations != null ? RotationComponent.rotations : new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
                   if (RayCastUtil.overBlock(RotationComponent.rotations, facing, pos, false) || !mode.is("Telly")) {
                Vec3 hitvec = !mode.is("Telly") ? getHypixelVec3(pos, facing) : getVec3(pos, facing);

                if (!mode.is("Telly")){
                    if (validateBlockRange(hitvec)) {
                        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem(), pos, facing, getHypixelVec3(pos, facing))) {
                            mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                            y = (float) MathUtils.getRandomInRange(79.5f, 83.5f);
                        }
                    }
                }else {
                    if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem(), pos, facing, hitvec)) {
                        mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                    }
                    if (!sb) {
                        pos = null;
                    }
                }


            }
        }

    }

    private static boolean validateBlockRange(final Vec3 pos) {
        if (pos == null)
            return false;
        final EntityPlayerSP player = mc.thePlayer;
        final double x = (pos.xCoord - player.posX);
        final double y = (pos.yCoord - (player.posY + player.getEyeHeight()));
        final double z = (pos.zCoord - player.posZ);
        return StrictMath.sqrt(x * x + y * y + z * z) <= 5.0D;
    }
    private void findBlock() {
        Vec3 baseVec = mc.thePlayer.getPositionEyes(2F);
//        BlockPos base = new BlockPos(baseVec.x, baseY + 0.1f, baseVec.z);
        BlockPos base = new BlockPos(baseVec.xCoord, baseY + 0.1f, baseVec.zCoord);
        int baseX = base.getX();
        int baseZ = base.getZ();
        IBlockState state = mc.theWorld.getBlockState(base);
        Block block = state.getBlock();
        if (block instanceof BlockLiquid) {
            // 这些块通常不是顶部实心的
            return;
        }
        if (block.isOpaqueCube()) {
            // 如果方块是不透明的立方体，通常认为它是顶部实心的
            return;
        }
        if (checkBlock(baseVec, base)) {
            return;
        }
        for (int d = 1; d <= 6; d++) {
            if (checkBlock(baseVec, new BlockPos(
                    baseX,
                    baseY - d,
                    baseZ
            ))) {
                return;
            }
            for (int x = 1; x <= d; x++) {
                for (int z = 0; z <= d - x; z++) {
                    int y = d - x - z;
                    for (int rev1 = 0; rev1 <= 1; rev1++) {
                        for (int rev2 = 0; rev2 <= 1; rev2++) {
                            if (checkBlock(baseVec, new BlockPos(
                                    baseX + (rev1 == 0 ? x : -x),
                                    baseY - y,
                                    baseZ + (rev2 == 0 ? z : -z)
                            ))) return;
                        }
                    }
                }
            }
        }
    }
    private boolean checkBlock(Vec3 baseVec, BlockPos pos1) {
        if (!(mc.theWorld.getBlockState(pos1).getBlock() instanceof BlockAir)) return false;
        Vec3 center = new Vec3(pos1.getX() + 0.5, pos1.getY(), pos1.getZ() + 0.5);
        for (EnumFacing 脸 : EnumFacing.values()) {
            Vec3 hit = center.add(new Vec3(脸.getDirectionVec()).scale(0.5));
            Vec3i baseBlock = pos1.add(脸.getDirectionVec());
            // 获取 Block 对象并调用 isBlockNormalCube 方法
            if (!mc.theWorld.getBlockState(new BlockPos(baseBlock.getX(), baseBlock.getY(), baseBlock.getZ())).getBlock().isBlockNormalCube())
                continue;
            Vec3 relevant = hit.subtract(baseVec);
            if (relevant.lengthSquared() <= 4.5 * 4.5 && relevant.dotProduct(
                    new Vec3(脸.getDirectionVec())
            ) >= 0) {
                pos = new BlockPos(baseBlock);
                facing = 脸.getOpposite();
                return true;
            }
        }
        return false;
    }

    private void search(){
        Vec3 vec3 = null;
        EntityPlayerSP player = mc.thePlayer;
        WorldClient world = mc.theWorld;
        double posX = player.posX;
        double posZ = player.posZ;
        double minY = player.getEntityBoundingBox().minY;
        if (mc.gameSettings.keyBindJump.pressed) {
            vec3 = getPlacePossibility(0.0, 0.0, 0.0, true);
        }else {
            vec3 = getPlacePossibility(0.0, 0.0, 0.0, false);
        }
        if (vec3 == null) {
            return;
        }
        BlockPos pos = new BlockPos(vec3.xCoord, vec3.yCoord, vec3.zCoord);
        if (!mc.theWorld.getBlockState(pos).getBlock().getMaterial().isReplaceable()) {
            return;
        }
        for (EnumFacing facingType : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(facingType);
            if (!canBeClick(neighbor)) continue;
            Vec3 dirVec = new Vec3(facingType.getDirectionVec());
            for (double xSearch = 0.5; xSearch <= 0.5; xSearch += 0.01) {
                for (double ySearch = 0.5; ySearch <= 0.5; ySearch += 0.01) {
                    double zSearch = 0.5;
                    while (zSearch <= 0.5) {
                        Vec3 eyesPos = new Vec3(posX, minY + (double)player.getEyeHeight(), posZ);
                        Vec3 posVec = new Vec3(pos).addVector(xSearch, ySearch, zSearch);
                        Vec3 hitVec = posVec.add(new Vec3(dirVec.xCoord * 0.5, dirVec.yCoord * 0.5, dirVec.zCoord * 0.5));
                        double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
                        if (eyesPos.distanceTo(hitVec) > 5.0 || distanceSqPosVec > eyesPos.squareDistanceTo(posVec.add(dirVec)) || world.rayTraceBlocks(eyesPos, hitVec, false, true, false) != null) {
                            zSearch += 0.01;
                            continue;
                        }
                        double diffX = hitVec.xCoord - eyesPos.xCoord;
                        double diffY = hitVec.yCoord - eyesPos.yCoord;
                        double diffZ = hitVec.zCoord - eyesPos.zCoord;
                        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
                        if (facingType != EnumFacing.UP && facingType != EnumFacing.DOWN && (facingType == EnumFacing.NORTH || facingType == EnumFacing.SOUTH ? Math.abs(diffZ) : Math.abs(diffX)) < 0.0) {
                            zSearch += 0.01;
                            continue;
                        }
                        Vector2f rotation = new Vector2f(MathHelper.wrapAngleTo180_float((float)(Math.toDegrees(MathHelper.atan2(diffZ, diffX)) - 90.0)), MathHelper.wrapAngleTo180_float((float)(-Math.toDegrees(MathHelper.atan2(diffY, diffXZ)))));

                        rotation.x += (float)(new Random(System.currentTimeMillis()).nextInt() % 50 * 50 % 165 * 360);

                        Vec3 rotVec = getVectorForRotation(rotation);
                        Vec3 vector = eyesPos.addVector(rotVec.xCoord * 5.0, rotVec.yCoord * 5.0, rotVec.zCoord * 5.0);
                        MovingObjectPosition obj = world.rayTraceBlocks(eyesPos, vector, false, false, true);
                        if (obj == null) continue;
                        if (obj.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || obj.getBlockPos().getX() != neighbor.getX() || obj.getBlockPos().getZ() != neighbor.getZ() || obj.getBlockPos().getY() != neighbor.getY() || obj.sideHit != facingType.getOpposite()) {
                            zSearch += 0.01;
                            continue;
                        }
                        this.pos = neighbor;
                        this.facing = facingType.getOpposite();
                        this.targetRotation = rotation;
                        return;
                    }
                }
            }
        }
    }
    public static Vec3 getHypixelVec3(BlockPos pos, EnumFacing face) {
        double x = (double) pos.getX() + 0.5, y = (double) pos.getY() + 0.5, z = (double) pos.getZ() + 0.5;
        if (face != EnumFacing.UP && face != EnumFacing.DOWN) {
            y += 0.5;
        } else {
            x += 0.3;
            z += 0.3;
        }
        if (face == EnumFacing.WEST || face == EnumFacing.EAST) {
            z += 0.15;
        }
        if (face == EnumFacing.SOUTH || face == EnumFacing.NORTH) {
            x += 0.15;
        }
        return new Vec3(x, y, z);
    }

    public static Vec3 getVec3(BlockPos pos, EnumFacing face) {
        double x = (double)pos.getX() + 0.5;
        double y = (double)pos.getY() + 0.5;
        double z = (double)pos.getZ() + 0.5;
        if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
            x += MathUtils.getRandomInRange(0.3, -0.3);
            z += MathUtils.getRandomInRange(0.3, -0.3);
        } else {
            y += 0.08;
        }
        if (face == EnumFacing.WEST || face == EnumFacing.EAST) {
            z += MathUtils.getRandomInRange(0.3, -0.3);
        }
        if (face == EnumFacing.SOUTH || face == EnumFacing.NORTH) {
            x += MathUtils.getRandomInRange(0.3, -0.3);
        }
        return new Vec3(x, y, z);
    }
    public static boolean canBeClick(BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock().canCollideCheck(mc.theWorld.getBlockState(pos), false) && mc.theWorld.getWorldBorder().contains(pos);
    }

    public static Vec3 getPlacePossibility(double offsetX, double offsetY, double offsetZ, boolean searchUP) {
        ArrayList<Vec3> possibilities = new ArrayList<>();
        int range = (int)(6.0 + (Math.abs(offsetX) + Math.abs(offsetZ)));
        Vec3 playerPos = new Vec3(mc.thePlayer.posX + offsetX, mc.thePlayer.posY - 1.0 + offsetY, mc.thePlayer.posZ + offsetZ);
        if (!(mc.theWorld.getBlockState(new BlockPos(playerPos)).getBlock() instanceof BlockAir)) {
            return playerPos;
        }
        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= 0; ++y) {
                for (int z = -range; z <= range; ++z) {
                    Block block = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).add(x, y, z)).getBlock();
                    if (block instanceof BlockAir) continue;
                    for (int x2 = -1; x2 <= 1; x2 += 2) {
                        possibilities.add(new Vec3(mc.thePlayer.posX + (double)x + (double)x2, mc.thePlayer.posY + (double)y, mc.thePlayer.posZ + (double)z));
                    }
                    for (int y2 = -1; y2 <= 1; y2 += 2) {
                        possibilities.add(new Vec3(mc.thePlayer.posX + (double)x, mc.thePlayer.posY + (double)y + (double)y2, mc.thePlayer.posZ + (double)z));
                    }
                    for (int z2 = -1; z2 <= 1; z2 += 2) {
                        possibilities.add(new Vec3(mc.thePlayer.posX + (double)x, mc.thePlayer.posY + (double)y, mc.thePlayer.posZ + (double)z + (double)z2));
                    }
                }
            }
        }
        possibilities.removeIf(vec3 -> {
            BlockPos blockPos = new BlockPos(vec3.xCoord, vec3.yCoord, vec3.zCoord);
            if (mc.thePlayer.getPosition().getX() == blockPos.getX() && mc.thePlayer.getPosition().getY() == blockPos.getY() && mc.thePlayer.getPosition().getZ() == blockPos.getZ()) {
                return true;
            }
            BlockPos position = new BlockPos(vec3.xCoord, vec3.yCoord, vec3.zCoord);
            return mc.thePlayer.getDistance((double)position.getX() + 0.5, (double)position.getY() + 0.5, (double)position.getZ() + 0.5) > 6.0 || !(mc.theWorld.getBlockState(new BlockPos(vec3.xCoord, vec3.yCoord, vec3.zCoord)).getBlock() instanceof BlockAir);
        });
        possibilities.removeIf(e -> {
            boolean hasBlock = false;
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos position;
                if (facing == EnumFacing.UP || facing == EnumFacing.DOWN && !searchUP || mc.theWorld.getBlockState((position = new BlockPos(e.xCoord, e.yCoord, e.zCoord)).offset(facing)) == null || mc.theWorld.getBlockState(position.offset(facing)).getBlock() instanceof BlockAir) continue;
                BlockPos facePos = position.offset(facing);
                if (mc.thePlayer.getDistance((double)position.getX() + 0.5, (double)position.getY() + 0.5, (double)position.getZ() + 0.5) > mc.thePlayer.getDistance((double)facePos.getX() + 0.5, (double)facePos.getY() + 0.5, (double)facePos.getZ() + 0.5)) {
                    return true;
                }
                hasBlock = true;
            }
            if (e.yCoord > mc.thePlayer.getEntityBoundingBox().minY && !searchUP) {
                return true;
            }
            return !hasBlock;
        });
        if (possibilities.isEmpty()) {
            return null;
        }
        possibilities.sort(Comparator.comparingDouble(vec3 -> {
            double d0 = mc.thePlayer.posX + offsetX - vec3.xCoord;
            double d1 = mc.thePlayer.posY - 1.0 + offsetY - vec3.yCoord;
            double d2 = mc.thePlayer.posZ + offsetZ - vec3.zCoord;
            return MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);
        }));
        return possibilities.get(0);
    }
    public static Vec3 getVectorForRotation(Vector2f rotation) {
        float yawCos = (float)Math.cos(-rotation.x * ((float)Math.PI / 180) - (float)Math.PI);
        float yawSin = (float)Math.sin(-rotation.x * ((float)Math.PI / 180) - (float)Math.PI);
        float pitchCos = (float)(-Math.cos(-rotation.y * ((float)Math.PI / 180)));
        float pitchSin = (float)Math.sin(-rotation.y * ((float)Math.PI / 180));
        return new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }

    public int getBlockSlot() {
        for (int i = 0; i < 9; ++i) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i + 36).getHasStack() || !(mc.thePlayer.inventoryContainer.getSlot(i + 36).getStack().getItem() instanceof ItemBlock)) continue;
            return i;
        }
        return -1;
    }
    @EventTarget
    public void render(Render2DEvent e){
        if (mc.thePlayer == null || mc.theWorld == null) return;
        ScaledResolution sr = new ScaledResolution(mc);

        float width = 50 + mc.fontRendererObj.getStringWidth(String.valueOf(mc.thePlayer.inventory.getStackInSlot(getBlockSlot()).stackSize));
        int x = (int) (sr.getScaledWidth() / 2 - width / 2);
        int y = sr.getScaledHeight() / 2 + 72;
        float height = 18;
        GL11.glPushMatrix();
        GL11.glTranslated(x + (width / 2F), y + (height / 2F), 0);
        GL11.glTranslated(-(x + (width / 2F)), -(y + (height / 2F)), 0);
        RoundedUtil.drawRound(x,y,width,height,2.0f,new Color(0,0,0,100));
        FontManager.Semibold.get(18).drawString("Total: " + mc.thePlayer.inventory.getStackInSlot(getBlockSlot()).stackSize, x + 13, (y + 4), new Color(255, 255, 255).getRGB());
        GL11.glPopMatrix();
    }

}
