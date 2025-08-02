package qwq.arcane.utils.rotation;

import com.yumegod.obfuscation.FlowObfuscate;
import com.yumegod.obfuscation.InvokeDynamic;
import com.yumegod.obfuscation.Rename;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import qwq.arcane.event.annotations.EventPriority;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.*;
import qwq.arcane.utils.math.Vector2f;
import qwq.arcane.utils.player.MovementUtil;
import qwq.arcane.utils.player.Rotation;
@Rename
@FlowObfuscate
@InvokeDynamic
public final class RotationManager {
    private Minecraft mc = Minecraft.getMinecraft();
    public static Vector2f rotation;
    public Vector2f lastRotation;
    public Vector2f targetRotation;
    public Vector2f lastServerRotation;
    private float rotationSpeed;
    private boolean modify, smoothed;
    private boolean movementFix, strict;
    private boolean useRandomOffset = false;
    private float randomOffsetRange = 0.0f;

    public RotationManager() {
        this.rotation = new Vector2f(0, 0);
    }

    public Vector2f getRotation() {
        return rotation;
    }

    public void setRandomOffset(boolean enabled, float range) {
        this.useRandomOffset = enabled;
        this.randomOffsetRange = range;
    }

    public MovingObjectPosition rayTrace(double blockReachDistance, float partialTicks) {
        Vec3 vec3 = mc.thePlayer.getPositionEyes(partialTicks);
        Vec3 vec31 = mc.thePlayer.getLookCustom(lastServerRotation.getX(),lastServerRotation.getY());
        Vec3 vec32 = vec3.addVector(vec31.xCoord * blockReachDistance, vec31.yCoord * blockReachDistance, vec31.zCoord * blockReachDistance);
        return mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, false);
    }
    public float snapToHypYaw(final float yaw, final boolean slowdown) {
        final float snappedBase = Math.round(yaw / 45.0f) * 45.0f;
        float lowerOffset;
        float upperOffset;
        if (Math.abs(snappedBase % 90.0f) < 0.001f) {
            lowerOffset = 111.0f;
            upperOffset = 111.0f;
        }
        else {
            lowerOffset = 137.0f;
            upperOffset = 137.0f;
            if (slowdown) {
                MovementUtil.strafe(0.009999999776482582);
            }
        }
        final float lowerCandidate = snappedBase - lowerOffset;
        final float upperCandidate = snappedBase + upperOffset;
        return (Math.abs(yaw - lowerCandidate) <= Math.abs(upperCandidate - yaw)) ? lowerCandidate : upperCandidate;
    }
    public void setRotation(Vector2f rotation, float rotationSpeed, boolean movementFix, boolean strict) {
        this.targetRotation = applyRandomOffset(rotation);
        this.rotationSpeed = rotationSpeed;
        this.movementFix = movementFix;
        this.modify = true;
        this.strict = strict;
        smoothRotation();
    }

    private Vector2f applyRandomOffset(Vector2f rotation) {
        if (!useRandomOffset || randomOffsetRange <= 0) {
            return rotation;
        }

        float randomYaw = (float) ((Math.random() * 2 - 1) * randomOffsetRange);
        float randomPitch = (float) ((Math.random() * 2 - 1) * randomOffsetRange);

        return new Vector2f(
                rotation.getX() + randomYaw,
                MathHelper.clamp_float(rotation.getY() + randomPitch, -90, 90)
        );
    }


    public void setRotation(Vector2f rotation, float rotationSpeed, boolean movementFix) {
        this.targetRotation = rotation;
        this.rotationSpeed = rotationSpeed;
        this.movementFix = movementFix;

        this.modify = true;
        this.strict = false;

        smoothRotation();
    }

    public Vector2f getRotations(double posX, double posY, double posZ) {
        EntityPlayerSP player = mc.thePlayer;
        double x = posX - player.posX;
        double y = posY - (player.posY + (double) player.getEyeHeight());
        double z = posZ - player.posZ;
        double dist = MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float) (Math.atan2(z, x) * 180.0 / 3.141592653589793) - 90.0f;
        float pitch = (float) (-(Math.atan2(y, dist) * 180.0 / 3.141592653589793));
        return new Vector2f(yaw, pitch);
    }


    public void setRotation(Rotation rotation, float rotationSpeed, boolean movementFix) {
        this.targetRotation = rotation.toVec2f();
        this.rotationSpeed = rotationSpeed;
        this.movementFix = movementFix;

        this.modify = true;
        this.strict = false;

        smoothRotation();
    }

    public void setTargetRotation(Rotation rotation, int keepLength) {
        targetRotation = new Vector2f(rotation.yaw, rotation.pitch);
        movementFix = true;
        this.modify = true;
        this.strict = false;
    }

    public double getRotationDifference(final Rotation rotation) {
        return lastServerRotation == null ? 0D : getRotationDifference(rotation, lastServerRotation);
    }

    public float getAngleDifference(final float a, final float b) {
        return ((((a - b) % 360F) + 540F) % 360F) - 180F;
    }

    public double getRotationDifference(final Rotation a, final Vector2f b) {
        return Math.hypot(getAngleDifference(a.getYaw(), b.getX()), a.getPitch() - b.getY());
    }

    @EventTarget
    @EventPriority(8888)
    public void onMotion(UpdateEvent event) {
        if (!modify || rotation == null || lastRotation == null || targetRotation == null) {
            rotation = lastRotation = lastServerRotation = targetRotation = new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        }

        if (modify) {
            smoothRotation();
        }
    }

    @EventTarget
    @EventPriority(8888)
    public void onMovementInput(MoveInputEvent event) {
        if (modify && movementFix && !strict) {
            final float yaw = rotation.getX();
            final float forward = event.getForward();
            final float strafe = event.getStrafe();

            final double angle = MathHelper.wrapAngleTo180_double(Math.toDegrees(getDirection(mc.thePlayer.rotationYaw, forward, strafe)));

            if (forward == 0 && strafe == 0) return;

            float closestForward = 0, closestStrafe = 0, closestDifference = Float.MAX_VALUE;

            for (float predictedForward = -1F; predictedForward <= 1F; predictedForward += 1F) {
                for (float predictedStrafe = -1F; predictedStrafe <= 1F; predictedStrafe += 1F) {
                    if (predictedStrafe == 0 && predictedForward == 0) continue;

                    final double predictedAngle = MathHelper.wrapAngleTo180_double(Math.toDegrees(getDirection(yaw, predictedForward, predictedStrafe)));
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
    }

    public static double getDirection(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    @EventTarget
    @EventPriority(8888)
    public void onLook(LookEvent event) {
        if (modify) {
            event.setRotation(rotation);
        }
    }

    @EventTarget
    @EventPriority(8888)
    public void onStrafe(StrafeEvent event) {
        if (modify && movementFix) {
            event.setYaw(rotation.getX());
        }
    }

    @EventTarget
    @EventPriority(8888)
    public void onJump(JumpEvent event) {
        if (modify && movementFix) {
            event.setYaw(rotation.getX());
        }
    }

    @EventTarget
    @EventPriority(8888)
    public void onUpdate(MotionEvent event) {
        if (event.isPre()) {

            if (modify) {
                event.setYaw(rotation.getX());
                event.setPitch(rotation.getY());


                mc.thePlayer.renderYawOffset = rotation.getX();
                mc.thePlayer.rotationYawHead = rotation.getX();
                mc.thePlayer.renderPitchHead = rotation.getY();
                lastServerRotation = new Vector2f(rotation.getX(), rotation.getY());

                if (Math.abs((rotation.getX() - mc.thePlayer.rotationYaw) % 360) < 1 && Math.abs((rotation.getY() - mc.thePlayer.rotationPitch)) < 1) {
                    modify = false;

                    correctDisabledRotations();
                }

                lastRotation = rotation;
            } else {
                lastRotation = new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
            }

            targetRotation = new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
            smoothed = false;
        }
    }

    private void correctDisabledRotations() {
        final Vector2f rotations = new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        final Vector2f fixedRotations = resetRotation(applySensitivityPatch(rotations, lastRotation));

        mc.thePlayer.rotationYaw = fixedRotations.getX();
        mc.thePlayer.rotationPitch = fixedRotations.getY();
    }

    public Vector2f resetRotation(Vector2f rotation) {
        if (rotation == null) {
            return null;
        }

        final float yaw = rotation.getX() + MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - rotation.getX());
        final float pitch = mc.thePlayer.rotationPitch;
        return new Vector2f(yaw, pitch);
    }

    public Vector2f applySensitivityPatch(final Vector2f rotation, final Vector2f previousRotation) {
        final float mouseSensitivity = (float) (mc.gameSettings.mouseSensitivity * (1 + Math.random() / 10000000) * 0.6F + 0.2F);
        final double multiplier = mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0F * 0.15D;
        final float yaw = previousRotation.getX() + (float) (Math.round((rotation.getX() - previousRotation.getX()) / multiplier) * multiplier);
        final float pitch = previousRotation.getY() + (float) (Math.round((rotation.getY() - previousRotation.getY()) / multiplier) * multiplier);
        return new Vector2f(yaw, MathHelper.clamp_float(pitch, -90, 90));
    }

    private void smoothRotation() {
        if (!smoothed) {
            final float lastYaw = lastRotation.getX();
            final float lastPitch = lastRotation.getY();
            final float targetYaw = targetRotation.getX();
            final float targetPitch = targetRotation.getY();
            rotation = getSmoothRotation(
                    new Vector2f(lastYaw, lastPitch),
                    applyRandomOffset(new Vector2f(targetYaw, targetPitch)),
                    rotationSpeed + Math.random()
            );
            rotation = getSmoothRotation(new Vector2f(lastYaw, lastPitch), new Vector2f(targetYaw, targetPitch),
                    rotationSpeed + Math.random());

            if (movementFix) {
                mc.thePlayer.movementYaw = rotation.getX();
            }

            mc.thePlayer.velocityYaw = rotation.getX();
        }

        smoothed = true;

        mc.entityRenderer.getMouseOver(1);
    }

    public Vector2f getSmoothRotation(Vector2f lastRotation, Vector2f targetRotation, double speed) {
        float yaw = targetRotation.getX();
        float pitch = targetRotation.getY();
        final float lastYaw = lastRotation.getX();
        final float lastPitch = lastRotation.getY();

        if (speed != 0) {
            final float rotationSpeed = (float) speed;

            final double deltaYaw = MathHelper.wrapAngleTo180_float(targetRotation.getX() - lastRotation.getX());
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
        }

        final boolean randomise = Math.random() > 0.8;

        for (int i = 1; i <= (int) (2 + Math.random() * 2); ++i) {

            if (randomise) {
                yaw += (float) ((Math.random() - 0.5) / 100000000);
                pitch -= (float) (Math.random() / 200000000);
            }

            /*
             * Fixing GCD
             */
            final Vector2f rotations = new Vector2f(yaw, pitch);
            final Vector2f fixedRotations = applySensitivityPatch(rotations);

            /*
             * Setting rotations
             */
            yaw = fixedRotations.getX();
            pitch = Math.max(-90, Math.min(90, fixedRotations.getY()));
        }

        return new Vector2f(yaw, pitch);
    }

    public Vector2f applySensitivityPatch(Vector2f rotation) {
        final Vector2f previousRotation = new Vector2f(mc.thePlayer.lastReportedYaw, mc.thePlayer.lastReportedPitch);
        final float mouseSensitivity = (float) (mc.gameSettings.mouseSensitivity * (1 + Math.random() / 10000000) * 0.6F + 0.2F);
        final double multiplier = mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0F * 0.15D;
        final float yaw = previousRotation.getX() + (float) (Math.round((rotation.getX() - previousRotation.getX()) / multiplier) * multiplier);
        final float pitch = previousRotation.getY() + (float) (Math.round((rotation.getY() - previousRotation.getY()) / multiplier) * multiplier);
        return new Vector2f(yaw, MathHelper.clamp_float(pitch, -90, 90));
    }
}