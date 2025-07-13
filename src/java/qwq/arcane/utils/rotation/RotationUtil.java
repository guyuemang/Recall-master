package qwq.arcane.utils.rotation;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import qwq.arcane.utils.Instance;
import qwq.arcane.utils.math.MathConst;
import qwq.arcane.utils.math.MathUtils;
import qwq.arcane.utils.math.Vector2f;
import qwq.arcane.utils.math.Vector3d;
import qwq.arcane.utils.player.Rotation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.minecraft.client.entity.EntityPlayerSP.getNearestPointBB;

/**
 * @author Patrick
 * @since 11/17/2021
 */

@UtilityClass
public class RotationUtil implements Instance {

    public Vector2f calculate(final Vector3d from, final Vector3d to) {
        final Vector3d diff = to.subtract(from);
        final double distance = Math.hypot(diff.getX(), diff.getZ());
        final float yaw = (float) (MathHelper.atan2(diff.getZ(), diff.getX()) * MathConst.TO_DEGREES) - 90.0F;
        final float pitch = (float) (-(MathHelper.atan2(diff.getY(), distance) * MathConst.TO_DEGREES));
        return new Vector2f(yaw, pitch);
    }
    public static float[] getRotations(BlockPos blockPos, EnumFacing enumFacing) {
        double d = (double) blockPos.getX() + 0.5 - mc.thePlayer.posX + (double) enumFacing.getFrontOffsetX() * 0.25;
        double d2 = (double) blockPos.getZ() + 0.5 - mc.thePlayer.posZ + (double) enumFacing.getFrontOffsetZ() * 0.25;
        double d3 = mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight() - blockPos.getY() - (double) enumFacing.getFrontOffsetY() * 0.25;
        double d4 = MathHelper.sqrt_double(d * d + d2 * d2);
        float f = (float) (Math.atan2(d2, d) * 180.0 / Math.PI) - 90.0f;
        float f2 = (float) (Math.atan2(d3, d4) * 180.0 / Math.PI);
        return new float[]{MathHelper.wrapAngleTo180_float(f), f2};
    }
    public static float getAngleDifference(final float a, final float b) {
        return ((((a - b) % 360F) + 540F) % 360F) - 180F;
    }

    public static float[] getRotationBlock(BlockPos pos) {
        return getRotationsByVec(mc.thePlayer.getPositionVector().addVector(0.0D, (double)mc.thePlayer.getEyeHeight(), 0.0D), new net.minecraft.util.Vec3((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D));
    }
    public static Vec3 getVectorForRotation(final Rotation rotation) {
        float yawCos = MathHelper.cos(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        float yawSin = MathHelper.sin(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        float pitchCos = -MathHelper.cos(-rotation.getPitch() * 0.017453292F);
        float pitchSin = MathHelper.sin(-rotation.getPitch() * 0.017453292F);
        return new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }
    public static Vec3 getVectorForRotations(final Rotation rotation) {
        float yawCos = MathHelper.cos(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        float yawSin = MathHelper.sin(-rotation.getYaw() * 0.017453292F - (float) Math.PI);
        float pitchCos = -MathHelper.cos(-rotation.getPitch() * 0.017453292F);
        float pitchSin = MathHelper.sin(-rotation.getPitch() * 0.017453292F);
        return new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }
    public static float[] getRotationBlock2(final BlockPos pos) {
        return getRotationsByVec(mc.thePlayer.getPositionVector().addVector(0.0, mc.thePlayer.getEyeHeight(), 0.0), new net.minecraft.util.Vec3(pos.getX() + 0.51, pos.getY() + 0.51, pos.getZ() + 0.51));
    }
    private static float[] getRotationsByVec(net.minecraft.util.Vec3 origin, net.minecraft.util.Vec3 position) {
        net.minecraft.util.Vec3 difference = position.subtract(origin);
        double distance = difference.flat().lengthVector();
        float yaw = (float)Math.toDegrees(Math.atan2(difference.zCoord, difference.xCoord)) - 90.0F;
        float pitch = (float)(-Math.toDegrees(Math.atan2(difference.yCoord, distance)));
        return new float[]{yaw, pitch};
    }
    public static void setVisualRotations(float yaw, float pitch) {
        mc.thePlayer.rotationYawHead = mc.thePlayer.renderYawOffset = yaw;


        mc.thePlayer.renderPitchHead = pitch;
    }
    public static Vector2f toRotation(final net.minecraft.util.Vec3 vec, final boolean predict) {
        final net.minecraft.util.Vec3 eyesPos = new net.minecraft.util.Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY +
                mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);

        if (predict) eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);

        final double diffX = vec.xCoord - eyesPos.xCoord;
        final double diffY = vec.yCoord - eyesPos.yCoord;
        final double diffZ = vec.zCoord - eyesPos.zCoord;

        return new Vector2f(MathHelper.wrapAngleTo180_float(
                (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F
        ), MathHelper.wrapAngleTo180_float(
                (float) (-Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))))
        ));
    }
    public static Vector2f calculateSimple(final Entity entity, double range, double wallRange) {
        AxisAlignedBB aabb = entity.getEntityBoundingBox().contract(-0.05, -0.05, -0.05).contract(0.05, 0.05, 0.05);
        range += 0.05;
        wallRange += 0.05;
        net.minecraft.util.Vec3 eyePos = mc.thePlayer.getPositionEyes(1F);
        net.minecraft.util.Vec3 nearest = new net.minecraft.util.Vec3(
                MathUtils.clamp(eyePos.xCoord, aabb.minX, aabb.maxX),
                MathUtils.clamp(eyePos.yCoord, aabb.minY, aabb.maxY),
                MathUtils.clamp(eyePos.zCoord, aabb.minZ, aabb.maxZ)
        );
        Vector2f rotation = toRotation(nearest, false);
        if (nearest.subtract(eyePos).lengthSquared() <= wallRange * wallRange) {
            return rotation;
        }

        MovingObjectPosition result = RayCastUtil.rayCast(rotation, range, 0F, false);
        final double maxRange = Math.max(wallRange, range);
        if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && result.entityHit == entity && result.hitVec.subtract(eyePos).lengthSquared() <= maxRange * maxRange) {
            return rotation;
        }

        return null;
    }
    private static List<Double> xzPercents = Arrays.asList(0.5, 0.4, 0.3, 0.2, 0.1, 0.0, -0.1, -0.2, -0.3, -0.4, -0.5);
    public static Vector2f calculate(Entity entity) {
        return RotationUtil.calculate(entity.getCustomPositionVector().add(0.0, Math.max(0.0, Math.min(RotationUtil.mc.thePlayer.posY - entity.posY + (double)RotationUtil.mc.thePlayer.getEyeHeight(), (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) * 0.9)), 0.0));
    }
    public static Vector2f calculate(final Entity entity, final boolean adaptive, final double range, final double wallRange, boolean predict, boolean randomCenter) {
        if (mc.thePlayer == null) return null;

        final double rangeSq = range * range;
        final double wallRangeSq = wallRange * wallRange;

        Vector2f simpleRotation = calculateSimple(entity, range, wallRange);
        if (simpleRotation != null) return simpleRotation;

        Vector2f normalRotations = toRotation(getVec(entity), predict);

        if (!randomCenter) {
            MovingObjectPosition normalResult = RayCastUtil.rayCast(normalRotations, range, 0F, false);
            if (normalResult != null && normalResult.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                return normalRotations;
            }
        }

        double yStart = 1, yEnd = 0, yStep = -0.5;
        if (randomCenter && MathUtils.secureRandom.nextBoolean()) {
            yStart = 0;
            yEnd = 1;
            yStep = 0.5;
        }
        for (double yPercent = yStart; Math.abs(yEnd - yPercent) > 1e-3; yPercent += yStep) {
            double xzStart = 0.5, xzEnd = -0.5, xzStep = -0.1;
            if (randomCenter) {
                Collections.shuffle(xzPercents);
            }
            for (double xzPercent : xzPercents) {
                for (int side = 0; side <= 3; side++) {
                    double xPercent = 0F, zPercent = 0F;
                    switch (side) {
                        case 0: {
                            xPercent = xzPercent;
                            zPercent = 0.5F;
                            break;
                        }
                        case 1: {
                            xPercent = xzPercent;
                            zPercent = -0.5F;
                            break;

                        }
                        case 2: {
                            xPercent = 0.5F;
                            zPercent = xzPercent;
                            break;

                        }
                        case 3: {
                            xPercent = -0.5F;
                            zPercent = xzPercent;
                            break;
                        }
                    }
                    net.minecraft.util.Vec3 Vec3 = getVec(entity).add(
                            new net.minecraft.util.Vec3((entity.getEntityBoundingBox().maxX - entity.getEntityBoundingBox().minX) * xPercent,
                                    (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) * yPercent,
                                    (entity.getEntityBoundingBox().maxZ - entity.getEntityBoundingBox().minZ) * zPercent));
                    double distanceSq = Vec3.squareDistanceTo(mc.thePlayer.getPositionEyes(1F));

                    Rotation rotation = toRotationRot(Vec3, predict);
                    rotation.fixedSensitivity(mc.gameSettings.mouseSensitivity);
                    rotation.distanceSq = distanceSq;

                    if (distanceSq <= wallRangeSq) {
                        MovingObjectPosition result = RayCastUtil.rayCast(rotation.toVec2f(), wallRange, 0F, true);
                        if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                            return rotation.toVec2f();
                        }
                    }

                    if (distanceSq <= rangeSq) {
                        MovingObjectPosition result = RayCastUtil.rayCast(rotation.toVec2f(), range, 0F, false);
                        if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                            return rotation.toVec2f();
                        }
                    }
                }
            }
        }

        return null;
    }
    public static net.minecraft.util.Vec3 getVec(Entity entity) {
        return new Vec3(entity.posX, entity.posY, entity.posZ);
    }
    public static Rotation toRotationRot(final net.minecraft.util.Vec3 vec, final boolean predict) {
        final net.minecraft.util.Vec3 eyesPos = new net.minecraft.util.Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY +
                mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);

        if (predict) eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);

        final double diffX = vec.xCoord - eyesPos.xCoord;
        final double diffY = vec.yCoord - eyesPos.yCoord;
        final double diffZ = vec.zCoord - eyesPos.zCoord;

        return new Rotation(MathHelper.wrapAngleTo180_float(
                (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F
        ), MathHelper.wrapAngleTo180_float(
                (float) (-Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))))
        ));
    }
    public static float[] getHVHRotation(Entity entity, double maxRange) {
        if (entity == null) {
            return null;
        } else {
            double diffX = entity.posX - mc.thePlayer.posX;
            double diffZ = entity.posZ - mc.thePlayer.posZ;
            net.minecraft.util.Vec3 BestPos = getNearestPointBB(mc.thePlayer.getPositionEyes(1f), entity.getEntityBoundingBox());
            Location myEyePos = new Location(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY +
                    mc.thePlayer.getEyeHeight(), Minecraft.getMinecraft().thePlayer.posZ);

            double diffY;

            diffY = BestPos.yCoord - myEyePos.getY();
            double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
            float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / 3.141592653589793D) - 90.0F;
            float pitch = (float) (-(Math.atan2(diffY, dist) * 180.0D / 3.141592653589793D));
            return new float[]{yaw, pitch};
        }
    }

    public static Vector2f getNewRotation(Entity target) {
        double yDist = target.posY - mc.thePlayer.posY;
        Vec3 pos = yDist >= 1.7 ? new Vec3(target.posX, target.posY, target.posZ) :
                (yDist <= -1.7 ? new Vec3(target.posX, target.posY + (double)target.getEyeHeight(), target.posZ) :
                        new Vec3(target.posX, target.posY + (double)(target.getEyeHeight() / 2.0f), target.posZ));

        Vec3 vec = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + (double)mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        double xDist = pos.xCoord - vec.xCoord;
        double yDist2 = pos.yCoord - vec.yCoord;
        double zDist = pos.zCoord - vec.zCoord;
        float yaw = (float)Math.toDegrees(Math.atan2(zDist, xDist)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(yDist2, Math.sqrt(xDist * xDist + zDist * zDist))));

        return new Vector2f(yaw, Math.min(Math.max(pitch, -90.0f), 90.0f));
    }

    public Vector2f calculate(final Entity entity, final boolean adaptive, final double range) {
        Vector2f normalRotations = calculate(entity);
        if (!adaptive || RayCastUtil.rayCast(normalRotations, range).typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            return normalRotations;
        }

        for (double yPercent = 1; yPercent >= 0; yPercent -= 0.25) {
            for (double xPercent = 1; xPercent >= -0.5; xPercent -= 0.5) {
                for (double zPercent = 1; zPercent >= -0.5; zPercent -= 0.5) {
                    Vector2f adaptiveRotations = calculate(entity.getCustomPositionVector().add(
                            (entity.getEntityBoundingBox().maxX - entity.getEntityBoundingBox().minX) * xPercent,
                            (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) * yPercent,
                            (entity.getEntityBoundingBox().maxZ - entity.getEntityBoundingBox().minZ) * zPercent));

                    if (RayCastUtil.rayCast(adaptiveRotations, range).typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                        return adaptiveRotations;
                    }
                }
            }
        }

        return normalRotations;
    }

    public Vector2f calculate(final Vec3 to, final EnumFacing enumFacing) {
        return calculate(new Vector3d(to.xCoord, to.yCoord, to.zCoord), enumFacing);
    }

    public Vector2f calculate(final Vec3 to) {
        return calculate(mc.thePlayer.getCustomPositionVector().add(0, mc.thePlayer.getEyeHeight(), 0), new Vector3d(to.xCoord, to.yCoord, to.zCoord));
    }

    public Vector2f calculate(final Vector3d to) {
        return calculate(mc.thePlayer.getCustomPositionVector().add(0, mc.thePlayer.getEyeHeight(), 0), to);
    }

    public Vector2f calculate(final Vector3d position, final EnumFacing enumFacing) {
        double x = position.getX() + 0.5D;
        double y = position.getY() + 0.5D;
        double z = position.getZ() + 0.5D;

        x += (double) enumFacing.getDirectionVec().getX() * 0.5D;
        y += (double) enumFacing.getDirectionVec().getY() * 0.5D;
        z += (double) enumFacing.getDirectionVec().getZ() * 0.5D;
        return calculate(new Vector3d(x, y, z));
    }

    public Vector2f applySensitivityPatch(final Vector2f rotation) {
        final Vector2f previousRotation = mc.thePlayer.getPreviousRotation();
        final float mouseSensitivity = (float) (mc.gameSettings.mouseSensitivity * (1 + Math.random() / 10000000) * 0.6F + 0.2F);
        final double multiplier = mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0F * 0.15D;
        final float yaw = previousRotation.x + (float) (Math.round((rotation.x - previousRotation.x) / multiplier) * multiplier);
        final float pitch = previousRotation.y + (float) (Math.round((rotation.y - previousRotation.y) / multiplier) * multiplier);
        return new Vector2f(yaw, MathHelper.clamp_float(pitch, -90, 90));
    }

    public Vector2f applySensitivityPatch(final Vector2f rotation, final Vector2f previousRotation) {
        final float mouseSensitivity = (float) (mc.gameSettings.mouseSensitivity * (1 + Math.random() / 10000000) * 0.6F + 0.2F);
        final double multiplier = mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0F * 0.15D;
        final float yaw = previousRotation.x + (float) (Math.round((rotation.x - previousRotation.x) / multiplier) * multiplier);
        final float pitch = previousRotation.y + (float) (Math.round((rotation.y - previousRotation.y) / multiplier) * multiplier);
        return new Vector2f(yaw, MathHelper.clamp_float(pitch, -90, 90));
    }

    public static float[] applyGCDFix(float[] prevRotation, float[] currentRotation) {
        final float f = (float) (mc.gameSettings.mouseSensitivity * (1 + Math.random() / 100000) * 0.6F + 0.2F);
        final double gcd = f * f * f * 8.0F * 0.15D;
        final float yaw = prevRotation[0] + (float) (Math.round((currentRotation[0] - prevRotation[0]) / gcd) * gcd);
        final float pitch = prevRotation[1] + (float) (Math.round((currentRotation[1] - prevRotation[1]) / gcd) * gcd);

        return new float[]{yaw, pitch};
    }

    public Vector2f relateToPlayerRotation(final Vector2f rotation) {
        final Vector2f previousRotation = mc.thePlayer.getPreviousRotation();
        final float yaw = previousRotation.x + MathHelper.wrapAngleTo180_float(rotation.x - previousRotation.x);
        final float pitch = MathHelper.clamp_float(rotation.y, -90, 90);
        return new Vector2f(yaw, pitch);
    }

    public Vector2f resetRotation(final Vector2f rotation) {
        if (rotation == null) {
            return null;
        }

        final float yaw = rotation.x + MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - rotation.x);
        final float pitch = mc.thePlayer.rotationPitch;
        return new Vector2f(yaw, pitch);
    }

    public Vector2f smooth(final Vector2f lastRotation, final Vector2f targetRotation, final double speed) {
        float yaw = targetRotation.x;
        float pitch = targetRotation.y;
        final float lastYaw = lastRotation.x;
        final float lastPitch = lastRotation.y;

        if (speed != 0) {
            final float rotationSpeed = (float) speed;

            final double deltaYaw = MathHelper.wrapAngleTo180_float(targetRotation.x - lastRotation.x);
            final double deltaPitch = pitch - lastPitch;

            final double distance = Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch);
            final double distributionYaw = Math.abs(deltaYaw / distance);
            final double distributionPitch = Math.abs(deltaPitch / distance);

            final double maxYaw = rotationSpeed * distributionYaw;
            final double maxPitch = rotationSpeed * distributionPitch;

            final float moveYaw = (float) Math.max(Math.min(deltaYaw, maxYaw), -maxYaw);
            final float movePitch = (float) Math.max(Math.min(deltaPitch, maxPitch), -maxPitch);

            yaw = lastYaw + moveYaw;
            pitch = lastPitch + movePitch;

            for (int i = 1; i <= (int) (Minecraft.getDebugFPS() / 20f + Math.random() * 10); ++i) {

                if (Math.abs(moveYaw) + Math.abs(movePitch) > 1) {
                    yaw += (Math.random() - 0.5) / 1000;
                    pitch -= Math.random() / 200;
                }

                /*
                 * Fixing GCD
                 */
                final Vector2f rotations = new Vector2f(yaw, pitch);
                final Vector2f fixedRotations = RotationUtil.applySensitivityPatch(rotations);

                /*
                 * Setting rotations
                 */
                yaw = fixedRotations.x;
                pitch = Math.max(-90, Math.min(90, fixedRotations.y));
            }
        }

        return new Vector2f(yaw, pitch);
    }
}