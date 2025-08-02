package qwq.arcane.utils.player;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.apache.commons.lang3.StringUtils;
import qwq.arcane.utils.Instance;
import qwq.arcane.utils.rotation.RotationUtil;

/**
 * @Author：Guyuemang
 * @Date：7/6/2025 11:57 PM
 */
public class PlayerUtil implements Instance {
    private static final Int2IntMap GOOD_POTIONS = new Int2IntOpenHashMap() {{
        put(6, 1); // Instant Health
        put(10, 2); // Regeneration
        put(11, 3); // Resistance
        put(21, 4); // Health Boost
        put(22, 5); // Absorption
        put(23, 6); // Saturation
        put(5, 7); // Strength
        put(1, 8); // Speed
        put(12, 9); // Fire Resistance
        put(14, 10); // Invisibility
        put(3, 11); // Haste
        put(13, 12); // Water Breathing
    }};
    public static boolean scoreTeam(EntityPlayer entityPlayer) {
        return mc.thePlayer.isOnSameTeam(entityPlayer);
    }
    public static boolean colorTeam(EntityPlayer sb) {
        String targetName = StringUtils.replace(sb.getDisplayName().getFormattedText(),"§r", "");
        String clientName = mc.thePlayer.getDisplayName().getFormattedText().replace("§r", "");
        return targetName.startsWith("§" + clientName.charAt(1));
    }

    public static boolean armorTeam(EntityPlayer entityPlayer) {
        if (mc.thePlayer.inventory.armorInventory[3] != null && entityPlayer.inventory.armorInventory[3] != null) {
            ItemStack myHead = mc.thePlayer.inventory.armorInventory[3];
            ItemArmor myItemArmor = (ItemArmor) myHead.getItem();
            ItemStack entityHead = entityPlayer.inventory.armorInventory[3];
            ItemArmor entityItemArmor = (ItemArmor) entityHead.getItem();
            if (String.valueOf(entityItemArmor.getColor(entityHead)).equals("10511680")) {
                return true;
            }
            return myItemArmor.getColor(myHead) == entityItemArmor.getColor(entityHead);
        }
        return false;
    }
    public static boolean isBlockUnder(Entity ent) {
        return mc.theWorld.getBlockState(new BlockPos(ent.posX, ent.posY -1, ent.posZ)).getBlock() != Blocks.air && mc.theWorld.getBlockState(new BlockPos(ent.posX, ent.posY -1, ent.posZ)).getBlock().isFullBlock();
    }  public static boolean isBlockUnder(double height, boolean boundingBox) {
        if (boundingBox) {
            int offset = 0;
            while ((double)offset < height) {
                AxisAlignedBB bb = RotationUtil.mc.thePlayer.getEntityBoundingBox().offset(0.0, -offset, 0.0);
                if (!RotationUtil.mc.theWorld.getCollidingBoundingBoxes(RotationUtil.mc.thePlayer, bb).isEmpty()) {
                    return true;
                }
                offset += 2;
            }
        } else {
            int offset = 0;
            while ((double)offset < height) {
                if (PlayerUtil.blockRelativeToPlayer(0.0, -offset, 0.0).isFullBlock()) {
                    return true;
                }
                ++offset;
            }
        }
        return false;
    }
    public static int findTool(final BlockPos blockPos) {
        float bestSpeed = 1;
        int bestSlot = -1;

        final IBlockState blockState = mc.theWorld.getBlockState(blockPos);

        for (int i = 0; i < 9; i++) {
            final ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);

            if (itemStack != null) {

                final float speed = itemStack.getStrVsBlock(blockState.getBlock());

                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = i;
                }
            }
        }

        return bestSlot;
    }

    public static boolean isInTeam(Entity entity) {
        if (mc.thePlayer.getDisplayName() != null && entity.getDisplayName() != null) {
            String targetName = entity.getDisplayName().getFormattedText().replace("§r", "");
            String clientName = mc.thePlayer.getDisplayName().getFormattedText().replace("§r", "");
            return targetName.startsWith("§" + clientName.charAt(1));
        }
        return false;
    }
    public static int potionRanking(final int id) {
        return GOOD_POTIONS.getOrDefault(id, -1);
    }
    public static Vec3 getPredictedPos(float forward, float strafe) {
        strafe *= 0.98F;
        forward *= 0.98F;
        float f4 = 0.91F;
        double motionX = mc.thePlayer.motionX;
        double motionZ = mc.thePlayer.motionZ;
        double motionY = mc.thePlayer.motionY;
        boolean isSprinting = mc.thePlayer.isSprinting();

        if (mc.thePlayer.isJumping && mc.thePlayer.onGround) {
            motionY = mc.thePlayer.getJumpUpwardsMotion();
            if (mc.thePlayer.isPotionActive(Potion.jump)) {
                motionY += (float)(mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
            }

            if (isSprinting) {
                float f = mc.thePlayer.rotationYaw * (float) (Math.PI / 180.0);
                motionX -= MathHelper.sin(f) * 0.2F;
                motionZ += MathHelper.cos(f) * 0.2F;
            }
        }

        if (mc.thePlayer.onGround) {
            f4 = mc.thePlayer
                    .worldObj
                    .getBlockState(
                            new BlockPos(
                                    MathHelper.floor_double(mc.thePlayer.posX),
                                    MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minY) - 1,
                                    MathHelper.floor_double(mc.thePlayer.posZ)
                            )
                    )
                    .getBlock()
                    .slipperiness
                    * 0.91F;
        }

        float f3 = 0.16277136F / (f4 * f4 * f4);
        float friction;
        if (mc.thePlayer.onGround) {
            friction = mc.thePlayer.getAIMoveSpeed() * f3;
            if (mc.thePlayer == Minecraft.getMinecraft().thePlayer
                    && mc.thePlayer.isSprinting()) {
                friction = 0.12999998F;
            }
        } else {
            friction = mc.thePlayer.jumpMovementFactor;
        }

        float f = strafe * strafe + forward * forward;
        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt_float(f);
            if (f < 1.0F) {
                f = 1.0F;
            }

            f = friction / f;
            strafe *= f;
            forward *= f;
            float f1 = MathHelper.sin(mc.thePlayer.rotationYaw * (float) Math.PI / 180.0F);
            float f2 = MathHelper.cos(mc.thePlayer.rotationYaw * (float) Math.PI / 180.0F);
            motionX += strafe * f2 - forward * f1;
            motionZ += forward * f2 + strafe * f1;
        }

        f4 = 0.91F;
        if (mc.thePlayer.onGround) {
            f4 = mc.thePlayer
                    .worldObj
                    .getBlockState(
                            new BlockPos(
                                    MathHelper.floor_double(mc.thePlayer.posX),
                                    MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minY) - 1,
                                    MathHelper.floor_double(mc.thePlayer.posZ)
                            )
                    )
                    .getBlock()
                    .slipperiness
                    * 0.91F;
        }

        motionY *= 0.98F;
        motionX *= f4;
        motionZ *= f4;
        return new Vec3(motionX, motionY, motionZ);
    }
    public static Block blockRelativeToPlayer(double d, double d2, double d3) {
        return block(mc.thePlayer.posX + d, mc.thePlayer.posY + d2, mc.thePlayer.posZ + d3);
    }

    public static Block block(double d, double d2, double d3) {
        return mc.theWorld.getBlockState(new BlockPos(d, d2, d3)).getBlock();
    }

    public static boolean isMob(Entity entity) {
        return entity instanceof EntityMob
                || entity instanceof EntityVillager
                || entity instanceof EntitySlime
                || entity instanceof EntityGhast
                || entity instanceof EntityDragon;
    }

    public static Block getBlock(BlockPos blockPos) {
        return mc.theWorld.getBlockState(blockPos).getBlock();
    }

    public static boolean isAnimal(Entity entity) {
        return entity instanceof EntityAnimal
                || entity instanceof EntitySquid
                || entity instanceof EntityGolem
                || entity instanceof EntityBat;
    }
}
