package qwq.arcane.module.impl.combat;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.viamcp.fixes.AttackOrder;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import org.apache.commons.lang3.RandomUtils;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.WorldLoadEvent;
import qwq.arcane.event.impl.events.player.*;
import qwq.arcane.event.impl.events.render.Render3DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.movement.Sprint;
import qwq.arcane.module.impl.player.Blink;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.module.impl.world.BlockFly;
import qwq.arcane.module.impl.world.Scaffold;
import qwq.arcane.utils.animations.Animation;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.DecelerateAnimation;
import qwq.arcane.utils.math.MathUtils;
import qwq.arcane.utils.math.Vector2f;
import qwq.arcane.utils.pack.BlinkComponent;
import qwq.arcane.utils.player.BlinkUtils;
import qwq.arcane.utils.player.MovementUtil;
import qwq.arcane.utils.player.PlayerUtil;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.rotation.RayCastUtil;
import qwq.arcane.utils.rotation.RotationUtil;
import qwq.arcane.utils.time.TimerUtil;
import qwq.arcane.value.impl.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static qwq.arcane.utils.pack.PacketUtil.sendPacket;


public class KillAura extends Module {
    public KillAura() {
        super("KillAura",Category.Combat);
    }
    public ModeValue modeValue = new ModeValue("AttackMode","Switch",new String[]{"Single","Switch"});
    public NumberValue switchdelay = new NumberValue("SwitchDelay",()-> modeValue.getValue().equals("Switch"),10,1,20,1);
    public NumberValue max = new NumberValue("MaxDelay",10,1,20,1);
    public NumberValue min = new NumberValue("MinDelay",10,1,20,1);
    public static NumberValue range = new NumberValue("Range",3.0,1.0,6.0,0.1);
    public BoolValue keepsprint = new BoolValue("KeepSprint",false);
    public BoolValue autoblock = new BoolValue("AutoBlock",false);
    public NumberValue blockrange = new NumberValue("BlockRange",()->autoblock.get(), 3.0,1.0,6.0,0.1);
    private final ModeValue blockmode = new ModeValue("BlockMode",()->autoblock.get(), "Fake", new String[]{"Fake", "Grim","Blink","LEGIT", "Interact"});
    public BoolValue rotation = new BoolValue("Rotation",false);
    public NumberValue Rotationrange = new NumberValue("RotationRange",()->rotation.get(),3.0,1.0,6.0,0.1);
    public NumberValue rotationspeed = new NumberValue("RotationSpeed",()->rotation.get(),180.0,1.0,180.0,1);
    private final ModeValue rotationmode = new ModeValue("RotationMode",()->rotation.get(), "Normal", new String[]{"Normal", "HvH", "Smart"});
    public static BoolValue rayCastValue = new BoolValue("RayCast", false);
    public BoolValue movefix = new BoolValue("MoveFix",false);
    public BoolValue strictValue = new BoolValue("FollowTarget", () -> movefix.getValue(), false);
    private final ModeValue priority = new ModeValue("Priority", "Range", new String[]{"Range", "Armor", "Health", "HurtTime"});
    public static BoolValue noscaffold = new BoolValue("NoScaffold", false);
    public MultiBooleanValue sorttargets = new MultiBooleanValue("Targets",Arrays.asList(
            new BoolValue("Animals",false)
            ,new BoolValue("Players",true),
            new BoolValue("Mobs",false),
            new BoolValue("Dead",false),
            new BoolValue("Invisible",false),
            new BoolValue("Teams",false)
    ));
    private final MultiBooleanValue auraESP = new MultiBooleanValue("TargetHUD ESP", Arrays.asList(
            new BoolValue("Circle", true),
            new BoolValue("Tracer", false),
            new BoolValue("Box", false),
            new BoolValue("Custom Color", false)));
    private final ColorValue customColor = new ColorValue("Custom Color", Color.WHITE);
    public List<EntityLivingBase> targets = new ArrayList<>();
    public static EntityLivingBase target;
    public EntityLivingBase blockTarget;
    public EntityLivingBase rotationTarget;
    public boolean blocking;
    public TimerUtil switchTimer = new TimerUtil();
    public TimerUtil attacktimer = new TimerUtil();
    private int index;
    private int cps;
    private Entity auraESPTarget;
    public boolean blink;
    public boolean blinkab = false;
    private boolean swapped = false;
    private int serverSlot = -1;
    @Override
    public void onEnable() {
        StopAutoBlock();
        BlinkComponent.dispatch();
        blocking = false;
        index = 0;
        cps = 0;
        switchTimer.reset();
        attacktimer.reset();
        targets.clear();
        target = null;
        blockTarget = null;
        rotationTarget = null;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        StopAutoBlock();
        BlinkComponent.dispatch();
        blocking = false;
        index = 0;
        cps = 0;
        switchTimer.reset();
        attacktimer.reset();
        targets.clear();
        target = null;
        blockTarget = null;
        rotationTarget = null;
        super.onDisable();
    }

    @EventTarget
    public void onWorldLoad(WorldLoadEvent event){
        setState(false);
    }

    public void Attack(){
        if (!targets.isEmpty()) {
            if (attacktimer.delay(cps)) {
                if (keepsprint.get()) {
                    Sprint.keepSprinting = true;
                }
                switch (modeValue.get()) {
                    case "Single":
                        target = targets.get(0);
                        attack(target);
                        break;
                    case "Switch":
                        target = targets.get(index);
                        attack(target);
                        break;
                }
            }
            final int maxValue = (int) ((min.getMax() - max.getValue()) * 20);
            final int minValue = (int) ((min.getMin() - min.getValue()) * 20);
            cps = MathUtils.getRandomInRange(minValue, maxValue);
            attacktimer.reset();
        }
    }
    /**
     * @Author：22666j
     * @Description：别抄，抄了死全家
     */
    @EventTarget
    public void PreUpdate(PreUpdateEvent e){
        if (!targets.isEmpty()) {
            if (switchTimer.hasTimeElapsed((long) (switchdelay.get() * 100L)) && targets.size() > 1) {
                ++index;
                switchTimer.reset();
            }
            if (index >= targets.size()) {
                index = 0;
                switchTimer.reset();
            }
            switch (modeValue.get()) {
                case "Single":
                    target = targets.get(0);
                    break;
                case "Switch":
                    target = targets.get(index);
                    break;
            }
        } else {
            Sprint.keepSprinting = false;
            index = 0;
            cps = 0;
            switchTimer.reset();
            attacktimer.reset();
            targets.clear();
            target = null;
        }
        if (target == null) return;
        if (blockmode.is("Blink")){
            preBlinktick();
            return;
        }
        Attack();
    }
    @EventTarget
    public void onSlowDown(SlowDownEvent e) {
        if (blockmode.is("Blink")) {
            if (blocking) {
                mc.thePlayer.movementInput.moveStrafe *= 0.2f;
                mc.thePlayer.movementInput.moveForward *= 0.2f;
                mc.thePlayer.sprintToggleTimer = 0;
            }
        }
    }
    @EventTarget
    public void UpdateEvent(UpdateEvent event){
        setsuffix(modeValue.get());
        targets = setTargets();
        if (rotation.get()) {
            rotationTarget = findClosestEntity(Rotationrange.get());
            if (rotationTarget != null) {
                onRotation(rotationTarget);
            }
        }
    }

    @EventTarget
    public void onPostMotion(MotionEvent event){
        if (event.isPre()){
            return;
        }
        if (keepsprint.get()) {
            Sprint.keepSprinting = false;
        }
        if (autoblock.get()) {
            blockTarget = findClosestEntity(blockrange.get());
            if (blockTarget != null) {
                onAutoBlock();
            } else {
                StopAutoBlock();
                BlinkComponent.dispatch();
            }
        }
        if (event.isPost() && !targets.isEmpty() && target != null){
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
        }
    }

    public EntityLivingBase findClosestEntity(double range) {
        EntityLivingBase closest = null;
        double minDistance = Double.MAX_VALUE;
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase living = (EntityLivingBase) entity;
                if (setTarget(living)) {
                    double distance = mc.thePlayer.getDistanceToEntity(living);
                    if (distance <= range && distance < minDistance) {
                        minDistance = distance;
                        closest = living;
                    }
                }
            }
        }
        return closest;
    }

    public void attack(Entity entity){
        if (shouldAttack()){
            AttackEvent event = new AttackEvent(entity);
            Client.Instance.getEventManager().call(event);
            AttackOrder.sendFixedAttack(mc.thePlayer,entity);
        }
    }

    public void onRotation(Entity entity){
        float[] rotaiton = new float[0];
        if (shouldRotation(entity)){
            switch (rotationmode.get()){
                case "Smart":
                    rotaiton = RotationUtil.getAngles(target);
                    break;
                case "Normal":
                    Vector2f vec = RotationUtil.calculate(target, true, range.getValue() + 0.2f, range.getValue() + 0.2, true, true);
                    rotaiton = new float[]{vec.x, vec.y};
                    break;
                case "HvH":
                    rotaiton = RotationUtil.getHVHRotation(entity, Rotationrange.getValue());
                    break;
            }
            Client.Instance.getRotationManager().setRotation(new Vector2f(rotaiton[0],rotaiton[1]),rotationspeed.get().intValue(), movefix.get(),strictValue.getValue());
        }
    }

    public void onAutoBlock(){
        if (shouldAutoBlock(blockTarget)) {
            switch (blockmode.get()) {
                case "Grim":
                    PacketWrapper useItem = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                    useItem.write(Type.VAR_INT, 1);
                    PacketUtil.sendToServer(useItem, Protocol1_8To1_9.class, true, true);
                    PacketWrapper useItem2 = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                    useItem2.write(Type.VAR_INT, 0);
                    PacketUtil.sendToServer(useItem2, Protocol1_8To1_9.class, true, true);
                    mc.gameSettings.keyBindUseItem.pressed = true;
                    blocking = true;
                    break;
                case "Fake":
                    blocking = true;
                    break;
                case "LEGIT":
                    if (mc.thePlayer.ticksExisted % 4 == 0) {
                        mc.gameSettings.keyBindUseItem.setPressed(true);
                    } else {
                        mc.gameSettings.keyBindUseItem.setPressed(false);
                    }
                    blocking = true;
                    break;
                case "Interact":
                    break;
            }
        }
    }

    public void preBlinktick(){
        if (blinkab) {
            BlinkComponent.blinking = true;
            blink = true;
            if (blocking) {
                mc.playerController.onStoppedUsingItem(mc.thePlayer);
                blocking = false;
            }
            qwq.arcane.utils.pack.PacketUtil.sendPacket(new C17PacketCustomPayload("喵喵喵我是可爱猫猫226", new PacketBuffer(Unpooled.buffer())));
            blinkab = false;
        } else {
            Attack();
            MovingObjectPosition result = RayCastUtil.rayCast(Client.Instance.getRotationManager().lastRotation ,range.getValue().floatValue());
            if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && result.entityHit == target) {
                if (!mc.playerController.isPlayerRightClickingOnEntity(mc.thePlayer, result.entityHit, result)) {
                    mc.playerController.interactWithEntitySendPacket(mc.thePlayer, result.entityHit);
                }
                if (!blocking) {
                    blocking = true;
                    qwq.arcane.utils.pack.PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                }
            }
            blinkab = true;
            BlinkComponent.dispatch();
            blink = false;
        }
    }

    public void StopAutoBlock(){
        if (blocking){
            switch (blockmode.get()) {
                case "Grim":
                    mc.gameSettings.keyBindUseItem.pressed = false;
                    mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(
                            C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    if (target != null) {
                        mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
                    }
                    blocking = false;
                    break;
                case "Interact":
                    break;
                case "LEGIT":
                    mc.gameSettings.keyBindUseItem.setPressed(false);
                    blocking = false;
                case "Blink":
                    if (this.swapped) {
                        int currentSlot = mc.thePlayer.inventory.currentItem;
                        if (this.serverSlot != currentSlot) {
                            qwq.arcane.utils.pack.PacketUtil.sendPacket(new C09PacketHeldItemChange(this.serverSlot = currentSlot));
                        }
                        this.swapped = false;
                    }
                    sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    blocking = false;
                    break;
                case "Fake":
                    blocking = false;
                    break;
            }
        }
    }

    public static boolean shouldAttack(){
        if (target == null) return false;
        if (rayCastValue.get()) {
            final MovingObjectPosition movingObjectPosition = mc.objectMouseOver;
            if (Client.Instance.getModuleManager().getModule(Scaffold.class).getState() && noscaffold.get()) return false;
            return (mc.thePlayer.getClosestDistanceToEntity(target) <= range.get()) && (movingObjectPosition != null && movingObjectPosition.entityHit == target);
        } else {
            return (double) (mc.thePlayer.canEntityBeSeen(target) ? mc.thePlayer.getClosestDistanceToEntity(target) : mc.thePlayer.getDistanceToEntity(target)) <= range.get();
        }
    }

    public boolean shouldAutoBlock(EntityLivingBase target){
        return autoblock.get() && target != null && target.getDistanceToEntity(mc.thePlayer) <= blockrange.get() && isSword();
    }

    public boolean shouldRotation(Entity entity){
        return rotation.get() && entity != null && entity.getDistanceToEntity(mc.thePlayer) <= Rotationrange.get();
    }

    public boolean isSword() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }

    public List<EntityLivingBase> setTargets(){
        targets.clear();
        final List<EntityLivingBase> entities = new ArrayList<>();
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase target = (EntityLivingBase) entity;
                if (!target.equals(mc.thePlayer) && setTarget(target) && mc.thePlayer.getDistanceToEntity(target) <= range.get()) {
                    entities.add(target);
                }else entities.remove(target);
            }
        }
        return entities;
    }

    public boolean setTarget(Entity entity){
        if (Client.Instance.getModuleManager().getModule(Blink.class).getState()){
            return false;
        }
        if ((sorttargets.isEnabled("Teams") && PlayerUtil.isInTeam(entity))) {
            return false;
        }
        if (entity instanceof EntityLivingBase && (sorttargets.isEnabled("Dead") || entity.isEntityAlive()) && entity != mc.thePlayer) {
            if (sorttargets.isEnabled("Invisible") || !entity.isInvisible()) {
                if (sorttargets.isEnabled("Players") && entity instanceof EntityPlayer) {
                    return !isEnabled(AntiBot.class) || !getModule(AntiBot.class).isBot((EntityPlayer) entity);
                }
            }
            return (sorttargets.isEnabled("Mobs") && PlayerUtil.isMob(entity)) || (sorttargets.isEnabled("Animals") && PlayerUtil.isAnimal(entity));
        }
        return false;
    }

    private final Animation auraESPAnim = new DecelerateAnimation(300, 1);

    private final ScaledResolution sr = new ScaledResolution(mc);
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
                RenderUtil.resetColor();
                RenderUtil.resetColor2();
            }
            if (auraESP.isEnabled("Circle")) {
                RenderUtil.drawCircle(this.auraESPTarget, event.partialTicks(), 0.75, color.getRGB(), this.auraESPAnim.getOutput().floatValue());
                RenderUtil.resetColor();
                RenderUtil.resetColor2();
            }
            if (auraESP.isEnabled("Tracer")) {
                RenderUtil.drawTracerLine(auraESPTarget, 4f, Color.BLACK, auraESPAnim.getOutput().floatValue());
                RenderUtil.drawTracerLine(auraESPTarget, 2.5f, color, auraESPAnim.getOutput().floatValue());
                RenderUtil.resetColor();
                RenderUtil.resetColor2();
            }
        }
    }
}