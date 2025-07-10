package qwq.arcane.module.impl.combat;

import de.florianmichael.viamcp.fixes.AttackOrder;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.MovingObjectPosition;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.AttackEvent;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.event.impl.events.render.Render3DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.world.Scaffold;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.animations.Animation;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.DecelerateAnimation;
import qwq.arcane.utils.math.MathUtils;
import qwq.arcane.utils.math.Vector2f;
import qwq.arcane.utils.pack.PacketUtil;
import qwq.arcane.utils.player.PlayerUtil;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.rotation.MovementFix;
import qwq.arcane.utils.rotation.RotationComponent;
import qwq.arcane.utils.rotation.RotationUtil;
import qwq.arcane.utils.time.TimerUtil;
import qwq.arcane.value.impl.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 23:53
 */
public class KillAura extends Module {
    public KillAura() {
        super("KillAura",Category.Combat);
    }
    private final ModeValue mode = new ModeValue("AttackMode","Single",new String[]{"Single","Switch"});
    public final NumberValue switchDelayValue = new NumberValue("SwitchDelay", () -> mode.is("Switch"),9, 0, 20, 1);
    private final NumberValue maxCPS = new NumberValue("Max CPS", 12, 1, 20, 1);
    private final NumberValue minCPS = new NumberValue("Min CPS", 6, 1, 20, 1);
    public static NumberValue range = new NumberValue("Range", 3.0,  0.0, 5.0, 0.1);
    private final ModeValue priority = new ModeValue("Priority", "Health", new String[]{"Range", "Armor", "Health", "HurtTime"});
    private final BooleanValue raycase = new BooleanValue("RayCase",true);
    public static BooleanValue autoblock = new BooleanValue("AutoBlock",true);
    public static ModeValue autoblockmode = new ModeValue("AutoBlockMode", autoblock::getValue,"Off",new String[]{"Grim","Watchdog","Off"});
    private final MultiBooleanValue targetOption = new MultiBooleanValue("Targets", Arrays.asList(new BooleanValue("Players", true), new BooleanValue("Mobs", false),
            new BooleanValue("Animals", false), new BooleanValue("Invisible", true), new BooleanValue("Dead", false)));
    public final MultiBooleanValue filter = new MultiBooleanValue("Filter", Arrays.asList(new BooleanValue("Teams", true), new BooleanValue("Friends", true)));
    private final MultiBooleanValue auraESP = new MultiBooleanValue("TargetHUD ESP", Arrays.asList(
            new BooleanValue("Circle", true),
            new BooleanValue("Tracer", false),
            new BooleanValue("Box", false),
            new BooleanValue("Custom Color", false)));
    private final ColorValue customColor = new ColorValue("Custom Color", Color.WHITE);
    public List<EntityLivingBase> targets = new ArrayList<>();
    public static EntityLivingBase target;
    public boolean blocking;
    private final TimerUtil switchTimer = new TimerUtil();
    private final TimerUtil attackTimer = new TimerUtil();
    private int index;
    private final Animation auraESPAnim = new DecelerateAnimation(300, 1);
    private Entity auraESPTarget;
    int cps = 0;

    @Override
    public void onEnable() {
        targets.clear();
        target = null;
        blocking = false;
        switchTimer.reset();
        attackTimer.reset();
        index = 0;
        cps = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        targets.clear();
        target = null;
        attackTimer.reset();
        switchTimer.reset();
        index = 0;
        cps = 0;
        StopAutoBlock();
        blocking = false;
        super.onDisable();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        setsuffix(mode.get());
        targets = gettargets();
        if (!targets.isEmpty()) {
            if (switchTimer.hasTimeElapsed((long) (switchDelayValue.get() * 100L)) && targets.size() > 1) {
                ++index;
                switchTimer.reset();
            }
            if (index >= targets.size()) {
                index = 0;
                switchTimer.reset();
            }

            if (attackTimer.hasTimeElapsed(cps)) {
                switch (mode.getValue()) {
                    case "Single":
                        target = targets.get(0);
                        attack(target);

                        break;
                    case "Switch":
                        target = targets.get(index);
                        attack(target);
                        break;
                }
                final int maxValue = (int) ((minCPS.getMax() - maxCPS.getValue()) * 20);
                final int minValue = (int) ((minCPS.getMax() - minCPS.getValue()) * 20);
                cps = MathUtils.getRandomInRange(minValue, maxValue);
                attackTimer.reset();
            }

            onAutoBlock();

            if (targets.size() > 1) {
                switch (priority.get()) {
                    case "Armor":
                        targets.sort(Comparator.comparingInt(EntityLivingBase::getTotalArmorValue));
                        break;
                    case "Range":
                        targets.sort(Comparator.comparingDouble(mc.thePlayer::getDistanceToEntity));
                        break;
                    case "Health":
                        targets.sort(Comparator.comparingDouble(EntityLivingBase::getHealth));
                        break;
                    case "HurtTime":
                        targets.sort(Comparator.comparingInt(entity -> entity.hurtTime));
                        break;
                }
            }

            onRotation();

        } else {
            targets.clear();
            target = null;
            attackTimer.reset();
            switchTimer.reset();
            index = 0;
            cps = 0;
            StopAutoBlock();
        }
    }

    public void onRotation(){
        float[] rotation = RotationUtil.getHVHRotation(target, range.getValue());;
        RotationComponent.setRotations(new Vector2f(rotation[0], rotation[1]), 180, MovementFix.NORMAL);
    }

    public void onAutoBlock(){
        if (autoblock.get()) {
            if (isSword()) {
                switch (autoblockmode.getValue()) {
                    case "Watchdog":
                        blocking = true;
                        break;
                    case "Off":
                        blocking = true;
                        break;
                }
            }
        }
    }
    public void StopAutoBlock(){
        if (autoblock.get() && blocking) {
            if (isSword()) {
                switch (autoblockmode.getValue()) {
                    case "Watchdog":
                        blocking = false;
                        break;
                    case "Off":
                        blocking = false;
                        break;
                }
            }
        }
    }

    public boolean shouldAttack() {
        if (raycase.get()) {
            final MovingObjectPosition movingObjectPosition = mc.objectMouseOver;
            if (Client.Instance.getModuleManager().getModule(Scaffold.class).getState()) return false;
            return (mc.thePlayer.getClosestDistanceToEntity(target) <= range.get()) && (movingObjectPosition != null && movingObjectPosition.entityHit == target);
        } else {
            return (double) (mc.thePlayer.canEntityBeSeen(target) ? mc.thePlayer.getClosestDistanceToEntity(target) : mc.thePlayer.getDistanceToEntity(target)) <= range.get();
        }
    }

    public List<EntityLivingBase> gettargets(){
        targets.clear();
        final List<EntityLivingBase> entities = new ArrayList<>();
        for (Entity entity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase target = (EntityLivingBase) entity;
                if (target.getDistanceToEntity(Minecraft.getMinecraft().thePlayer) <= range.get() && !target.equals(Minecraft.getMinecraft().thePlayer) && isValid(target)) {
                    entities.add(target);
                }else entities.remove(target);
            }
        }
        return entities;
    }

    public void attack(Entity entity){
        if (shouldAttack()) {
            AttackEvent attackEvent = new AttackEvent(entity);
            Client.Instance.getEventManager().call(attackEvent);
            PacketUtil.sendPacket(new C0APacketAnimation());
            AttackOrder.sendFixedAttackByPacket(entity);
        }
    }

    public boolean isValid(Entity entity) {
        if ((filter.isEnabled("Teams") && PlayerUtil.isInTeam(entity))) {
            return false;
        }
        if (entity instanceof EntityLivingBase && (targetOption.isEnabled("Dead") || entity.isEntityAlive()) && entity != mc.thePlayer) {
            if (targetOption.isEnabled("Invisible") || !entity.isInvisible()) {
                if (targetOption.isEnabled("Players") && entity instanceof EntityPlayer) {
                    return !isEnabled(AntiBot.class) || !getModule(AntiBot.class).isBot((EntityPlayer) entity);
                }
            }
            return (targetOption.isEnabled("Mobs") && PlayerUtil.isMob(entity)) || (targetOption.isEnabled("Animals") && PlayerUtil.isAnimal(entity));
        }
        return false;
    }

    @EventTarget
    public void onRender3DEvent(Render3DEvent event) {
        auraESPAnim.setDirection(KillAura.target != null ? Direction.FORWARDS : Direction.BACKWARDS);
        if (KillAura.target != null) {
            auraESPTarget = KillAura.target;
        }

        if (auraESPAnim.finished(Direction.BACKWARDS)) {
            auraESPTarget = null;
        }

        Color color = Client.Instance.getModuleManager().getModule(InterFace.class).color(1);

        if (auraESP.isEnabled("Custom Color")) {
            color = customColor.get();
        }
        if (auraESPTarget != null) {
            if (auraESP.isEnabled("Box")) {
                RenderUtil.renderBoundingBox((EntityLivingBase) auraESPTarget, color, auraESPAnim.getOutput().floatValue());
            }
            if (auraESP.isEnabled("Circle")) {
                RenderUtil.drawCircle(this.auraESPTarget, event.partialTicks(), 0.75, color.getRGB(), this.auraESPAnim.getOutput().floatValue());
            }
            if (auraESP.isEnabled("Tracer")) {
                RenderUtil.drawTracerLine(auraESPTarget, 4f, Color.BLACK, auraESPAnim.getOutput().floatValue());
                RenderUtil.drawTracerLine(auraESPTarget, 2.5f, color, auraESPAnim.getOutput().floatValue());
            }
        }
    }

    private int getItemIndex() {
        final InventoryPlayer inventoryPlayer = mc.thePlayer.inventory;
        return inventoryPlayer.currentItem;
    }

    public ItemStack getItemStack() {
        return (mc.thePlayer == null || mc.thePlayer.inventoryContainer == null ? null : mc.thePlayer.inventoryContainer.getSlot(getItemIndex() + 36).getStack());
    }
    public void interact(MovingObjectPosition mouse) {
        if (!mc.playerController.isPlayerRightClickingOnEntity(mc.thePlayer, mouse.entityHit, mouse)) {
            mc.playerController.interactWithEntitySendPacket(mc.thePlayer, mouse.entityHit);
        }
    }
    public boolean isSword() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }
}
