package qwq.arcane.module.impl.movement;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import qwq.arcane.event.annotations.EventPriority;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.JumpEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.combat.KillAura;
import qwq.arcane.utils.player.MovementUtil;
import qwq.arcane.utils.player.PlayerUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.NumberValue;

public class TargetStrafe
        extends Module {
    private final NumberValue range = new NumberValue("Range", 1.0, 0.1, 6.0, 0.1);
    private final BoolValue jump = new BoolValue("Jump", true);
    private final BoolValue behind = new BoolValue("behind", false);
    public float yaw;
    private boolean left;
    private boolean colliding;
    public boolean active;
    public EntityLivingBase target;

    public TargetStrafe() {
        super("TargetStrafe", Category.Movement);
    }

    @EventTarget
    @EventPriority(value=3)
    public void onJump(JumpEvent eventJump) {
        if (this.active && this.target != null) {
            eventJump.setYaw(this.yaw);
        }
    }

    @EventTarget
    @EventPriority(value=3)
    public void onUpdate(UpdateEvent eventUpdate) {
        if (((Boolean)this.jump.getValue()).booleanValue() && TargetStrafe.mc.gameSettings.keyBindJump.isPressed() || TargetStrafe.mc.gameSettings.keyBindForward.isKeyDown()) {
            this.active = false;
            this.target = null;
            return;
        }
        this.target = KillAura.target;
        if (this.target == null) {
            this.active = false;
            return;
        }
        if (TargetStrafe.mc.thePlayer.isCollidedVertically || PlayerUtil.isBlockUnder(5.0, false)) {
            if (!this.colliding) {
                MovementUtil.strafe(0.45);
                this.left = !this.left;
            }
            this.colliding = true;
        }
        this.colliding = false;
        this.active = true;
        float f = (Boolean)this.behind.getValue() != false ? this.target.rotationYaw + 180.0f : TargetStrafe.getYaw(TargetStrafe.mc.thePlayer, new Vec3(this.target.posX, this.target.posY, this.target.posZ)) + (float)(135 * (this.left ? -1 : 1));
        double d = (double)(this.range.getValue()).floatValue() + Math.random() / 100.0;
        double d2 = (double)(-MathHelper.sin((float)Math.toRadians(f))) * d + this.target.posX;
        double d3 = (double)MathHelper.cos((float)Math.toRadians(f)) * d + this.target.posZ;
        this.yaw = f = TargetStrafe.getYaw(TargetStrafe.mc.thePlayer, new Vec3(d2, this.target.posY, d3));
    }

    public static float getYaw(EntityPlayer entityPlayer, Vec3 vec3) {
        return entityPlayer.rotationYaw + MathHelper.wrapAngleTo180_float((float)Math.toDegrees(Math.atan2(vec3.zCoord - entityPlayer.posZ, vec3.xCoord - entityPlayer.posX)) - 90.0f - entityPlayer.rotationYaw);
    }
}
