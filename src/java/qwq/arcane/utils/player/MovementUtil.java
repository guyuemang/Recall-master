package qwq.arcane.utils.player;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import qwq.arcane.event.impl.events.player.MoveEvent;
import qwq.arcane.event.impl.events.player.MoveInputEvent;
import qwq.arcane.utils.Instance;
import qwq.arcane.utils.math.Vector2f;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:51 AM
 */
public class MovementUtil implements Instance {
    public static boolean isMoving() {
        return isMoving(mc.thePlayer);
    }

    public static boolean isMoving(EntityLivingBase player) {
        return player != null && (player.moveForward != 0F || player.moveStrafing != 0F);
    }
    public static double predictedMotion(final double motion, final int ticks) {
        if (ticks == 0) return motion;
        double predicted = motion;

        for (int i = 0; i < ticks; i++) {
            predicted = (predicted - 0.08) * 0.98F;
        }

        return predicted;
    }

    public static int getSpeedEffect() {
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed))
            return mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1;
        else return 0;
    }
    public static void setSpeed(double moveSpeed, float yaw, double strafe, double forward) {
        if (forward != 0.0D) {
            if (strafe > 0.0D) {
                yaw += (float)(forward > 0.0D ? -45 : 45);
            } else if (strafe < 0.0D) {
                yaw += (float)(forward > 0.0D ? 45 : -45);
            }

            strafe = 0.0D;
            if (forward > 0.0D) {
                forward = 1.0D;
            } else if (forward < 0.0D) {
                forward = -1.0D;
            }
        }

        if (strafe > 0.0D) {
            strafe = 1.0D;
        } else if (strafe < 0.0D) {
            strafe = -1.0D;
        }

        double mx = Math.cos(Math.toRadians((double)(yaw + 90.0F)));
        double mz = Math.sin(Math.toRadians((double)(yaw + 90.0F)));
        mc.thePlayer.motionX = forward * moveSpeed * mx + strafe * moveSpeed * mz;
        mc.thePlayer.motionZ = forward * moveSpeed * mz - strafe * moveSpeed * mx;
    }

    public static void setSpeed(double moveSpeed) {
        setSpeed(moveSpeed, mc.thePlayer.rotationYaw, (double) mc.thePlayer.movementInput.getMoveStrafe(), (double) mc.thePlayer.movementInput.getMoveForward());
    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = mc.thePlayer.capabilities.getWalkSpeed() * 2.873;
        if (mc.thePlayer.isPotionActive(Potion.moveSlowdown)) {
            baseSpeed /= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(Potion.moveSlowdown).getAmplifier() + 1);
        }
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            baseSpeed *= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        }
        return baseSpeed;
    }
    public static double strafe(final double d) {
        if (!isMoving())
            return mc.thePlayer.rotationYaw;

        final double yaw = getDirection1();
        mc.thePlayer.motionX = -MathHelper.sin((float) yaw) * d;
        mc.thePlayer.motionZ = MathHelper.cos((float) yaw) * d;

        return yaw;
    }
    public static boolean isOnGround(double height) {
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0D, -height, 0.0D)).isEmpty();
    }
    public static float getBindsDirection(float rotationYaw) {
        int moveForward = 0;
        if (GameSettings.isKeyDown(mc.gameSettings.keyBindForward)) moveForward++;
        if (GameSettings.isKeyDown(mc.gameSettings.keyBindBack)) moveForward--;

        int moveStrafing = 0;
        if (GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) moveStrafing++;
        if (GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) moveStrafing--;

        boolean reversed = moveForward < 0;
        double strafingYaw = 90 * (moveForward > 0 ? .5 : reversed ? -.5 : 1);

        if (reversed)
            rotationYaw += 180.f;
        if (moveStrafing > 0)
            rotationYaw += strafingYaw;
        else if (moveStrafing < 0)
            rotationYaw -= strafingYaw;

        return rotationYaw;
    }
    public static boolean isMovingStraight() {
        float direction = getRawDirection() + 180;
        float movingYaw = Math.round(direction / 45) * 45;
        return movingYaw % 90 == 0f;
    }
    public static void setMotion2(double d, float f) {
        mc.thePlayer.motionX = -Math.sin(Math.toRadians(f)) * d;
        mc.thePlayer.motionZ = Math.cos(Math.toRadians(f)) * d;
    }
    public static float getMoveYaw(float yaw) {
        Vector2f from = new Vector2f((float) mc.thePlayer.lastTickPosX, (float) mc.thePlayer.lastTickPosZ),
                to = new Vector2f((float) mc.thePlayer.posX, (float) mc.thePlayer.posZ),
                diff = new Vector2f(to.x - from.x, to.y - from.y);

        double x = diff.x, z = diff.y;
        if (x != 0 && z != 0) {
            yaw = (float) Math.toDegrees((Math.atan2(-x, z) + MathHelper.PI2) % MathHelper.PI2);
        }
        return yaw;
    }
    public static double speed() {
        return Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ);
    }
    public static double getDirection1() {
        float yaw = mc.thePlayer.rotationYaw;

        if (mc.thePlayer.moveForward < 0.0F) {
            yaw += 180F;
        }

        float forward = 1.0F;
        if (mc.thePlayer.moveForward < 0.0F) {
            forward = -0.5F;
        } else if (mc.thePlayer.moveForward > 0.0F) {
            forward = 0.5F;
        }

        if (mc.thePlayer.moveStrafing > 0.0F) {
            yaw -= 90F * forward;
        } else if (mc.thePlayer.moveStrafing < 0.0F) {
            yaw += 90F * forward;
        }

        return Math.toRadians(yaw);
    }
    public static void stop() {
        mc.thePlayer.motionX = 0;
        mc.thePlayer.motionZ = 0;
    }
    public static float getDirection(final float yaw) {
        return getDirection(yaw, mc.thePlayer.movementInput.moveForward, mc.thePlayer.movementInput.moveStrafe);
    }
    public static float getDirection(float yaw, final float forward, final float strafe) {
        if (forward != 0) {
            if (strafe < 0) {
                yaw += forward < 0 ? 135 : 45;
            } else if (strafe > 0) {
                yaw -= forward < 0 ? 135 : 45;
            } else if (strafe == 0 && forward < 0) {
                yaw -= 180;
            }
        } else {
            if (strafe < 0) {
                yaw += 90;
            } else if (strafe > 0) {
                yaw -= 90;
            }
        }

        return yaw;
    }
    public static boolean canSprint(final boolean legit) {
        return (legit ? mc.thePlayer.moveForward >= 0.8F
                && !mc.thePlayer.isCollidedHorizontally
                && (mc.thePlayer.getFoodStats().getFoodLevel() > 6 || mc.thePlayer.capabilities.allowFlying)
                && !mc.thePlayer.isPotionActive(Potion.blindness)
                //&& !mc.thePlayer.isUsingItem()
                && !mc.thePlayer.isSneaking()
                : enoughMovementForSprinting());
    }

    public static boolean enoughMovementForSprinting() {
        return Math.abs(mc.thePlayer.moveForward) >= 0.8F || Math.abs(mc.thePlayer.moveStrafing) >= 0.8F;
    }

    public static void stopXZ() {
        mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
    }

    public static void fixMovement(final MoveInputEvent event, final float yaw) {
        final float forward = event.getForward();
        final float strafe = event.getStrafe();

        final double angle = MathHelper.wrapAngleTo180_double(Math.toDegrees(MovementUtil.direction(mc.thePlayer.rotationYaw, forward, strafe)));

        if (forward == 0 && strafe == 0) {
            return;
        }

        float closestForward = 0, closestStrafe = 0, closestDifference = Float.MAX_VALUE;

        for (float predictedForward = -1F; predictedForward <= 1F; predictedForward += 1F) {
            for (float predictedStrafe = -1F; predictedStrafe <= 1F; predictedStrafe += 1F) {
                if (predictedStrafe == 0 && predictedForward == 0) continue;

                final double predictedAngle = MathHelper.wrapAngleTo180_double(Math.toDegrees(MovementUtil.direction(yaw, predictedForward, predictedStrafe)));
                final double difference = Math.abs(angle - predictedAngle);

                if (difference < closestDifference) {
                    closestDifference = (float) difference;
                    closestForward = predictedForward;
                    closestStrafe = predictedStrafe;
                }
            }
        }

        event.setForward(closestForward);
        event.setStrafe(closestStrafe);
    }

    public static double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    public static double direction() {
        float rotationYaw = mc.thePlayer.rotationYaw;

        if (mc.thePlayer.moveForward < 0) {
            rotationYaw += 180;
        }

        float forward = 1;

        if (mc.thePlayer.moveForward < 0) {
            forward = -0.5F;
        } else if (mc.thePlayer.moveForward > 0) {
            forward = 0.5F;
        }

        if (mc.thePlayer.moveStrafing > 0) {
            rotationYaw -= 70 * forward;
        }

        if (mc.thePlayer.moveStrafing < 0) {
            rotationYaw += 70 * forward;
        }

        return Math.toRadians(rotationYaw);
    }


    public static void strafe(final double speed, double yaw) {
        if (!isMoving())
            return;

        mc.thePlayer.motionX = -Math.sin(yaw) * speed;
        mc.thePlayer.motionZ = Math.cos(yaw) * speed;
    }
    public static void strafe(final MoveEvent moveEvent, final double speed, final double direction) {
        if (!isMoving()) return;
        moveEvent.setX(mc.thePlayer.motionX = -Math.sin(direction) * speed);
        moveEvent.setZ(mc.thePlayer.motionZ = Math.cos(direction) * speed);
    }
    public static float getRawDirection() {
        return getRawDirectionRotation(mc.thePlayer.rotationYaw, mc.thePlayer.keyMovementInput.moveStrafe, mc.thePlayer.keyMovementInput.moveForward);
    }

    public static float getRawDirectionRotation(float yaw, float pStrafe, float pForward) {
        float rotationYaw = yaw;

        if (pForward < 0F)
            rotationYaw += 180F;

        float forward = 1F;
        if (pForward < 0F)
            forward = -0.5F;
        else if (pForward > 0F)
            forward = 0.5F;

        if (pStrafe > 0F)
            rotationYaw -= 90F * forward;

        if (pStrafe < 0F)
            rotationYaw += 90F * forward;

        return rotationYaw;
    }

}
