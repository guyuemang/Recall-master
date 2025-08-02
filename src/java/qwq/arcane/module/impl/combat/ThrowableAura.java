package qwq.arcane.module.impl.combat;

import com.yumegod.obfuscation.FlowObfuscate;
import com.yumegod.obfuscation.InvokeDynamic;
import com.yumegod.obfuscation.Rename;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemSnowball;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.WorldLoadEvent;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.misc.Teams;
import qwq.arcane.module.impl.player.Blink;
import qwq.arcane.module.impl.world.Scaffold;
import qwq.arcane.utils.math.Vector2f;
import qwq.arcane.utils.pack.PacketUtil;
import qwq.arcane.utils.rotation.RayCastUtil;
import qwq.arcane.utils.rotation.RotationUtil;
import qwq.arcane.utils.time.TimerUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.NumberValue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Rename
@FlowObfuscate
@InvokeDynamic
public class ThrowableAura extends Module {
    private final NumberValue dealy = new NumberValue("Delay", 8, 0, 1000, 1);
    private final NumberValue range = new NumberValue("Range", 5, 1, 8, 1);
    private final NumberValue Fov = new NumberValue("Fov", 90, 0, 360, 1);
    public BoolValue playersValue = new BoolValue("Players", true);
    public BoolValue animalsValue = new BoolValue("Animals", true);
    public BoolValue mobsValue = new BoolValue("Mobs", false);
    public BoolValue invisibleValue = new BoolValue("Invisible", false);
    private final TimerUtil attackTimer = new TimerUtil();
    private final TimerUtil timer = new TimerUtil();
    private int index;

    public static final List<EntityLivingBase> targets = new ArrayList<>();
    public static EntityLivingBase target;

    public ThrowableAura() {
        super("ThrowableAura", Category.Combat);
    }

    @Override
    public void onEnable() {
        index = 0;
        targets.clear();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        index = 0;
        targets.clear();
        super.onDisable();
    }

    @EventTarget
    public void onWorldEvent(WorldLoadEvent event) {
        index = 0;
        targets.clear();
    }

    @EventTarget
    public void onUpdateEvent(MotionEvent e) {
        if (e.isPost()) {
            if (Objects.requireNonNull(Client.Instance.getModuleManager().getModule(Scaffold.class).getState() ||
                    isGapple()
                    || Client.Instance.getModuleManager().getModule(Gapple.class).getState()
                    || (Client.Instance.getModuleManager().getModule(KillAura.class).getState() && KillAura.target != null))) {
                return;
            }
            int slot = -1;
            if (getEggSlot() != -1) {
                slot = getEggSlot();
            } else if (getSnowballSlot() != -1) {
                slot = getSnowballSlot();
            }

            if (slot == -1) return;

            findTarget();
            /*if (switchTimer.hasTimeElapsed(dealy.getValue().longValue(), true)) {
                index++;
            }*/
            if (index >= targets.size()) {
                index = 0;
            }

            if (targets.isEmpty()) return;

            target = targets.get(index);

            if (target == null) return;
            if (!mc.thePlayer.canEntityBeSeen(target)) return;

            float[] rotation = RotationUtil.getRotationsNeededBall(target);

            Client.Instance.rotationManager.setRotation(new Vector2f(rotation[0], rotation[1]), 360f, true, false);

            if (RayCastUtil.rayCast(Client.Instance.rotationManager.rotation, range.getValue()).entityHit == null || !attackTimer.hasTimeElapsed(100, true))
                return;
            final int prevSlot = mc.thePlayer.inventory.currentItem;
            if (timer.delay((long) (dealy.getValue() * 10L))) {
                PacketUtil.sendPacket(new C09PacketHeldItemChange(slot));
                PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                PacketUtil.sendPacket(new C09PacketHeldItemChange(prevSlot));
                mc.thePlayer.inventory.currentItem = prevSlot;
                timer.reset();
            }
        }
    }

    private void findTarget() {
        targets.clear();
        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase entityLivingBase = (EntityLivingBase) entity;

                if (mc.thePlayer.getDistanceToEntity(entity) <= range.getValue() && shouldAdd(entity) && mc.thePlayer != entityLivingBase) {
                    targets.add(entityLivingBase);
                }
            }
        }
        targets.sort(Comparator.comparingDouble(mc.thePlayer::getDistanceToEntity));
    }

    public int getEggSlot() {
        for (int i = 0; i < 9; ++i) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i + 36).getHasStack() || !(mc.thePlayer.inventoryContainer.getSlot(i + 36).getStack().getItem() instanceof ItemEgg))
                continue;
            return i;
        }
        return -1;
    }

    public int getSnowballSlot() {
        for (int i = 0; i < 9; ++i) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i + 36).getHasStack() || !(mc.thePlayer.inventoryContainer.getSlot(i + 36).getStack().getItem() instanceof ItemSnowball))
                continue;
            return i;
        }
        return -1;
    }

    public boolean shouldAdd(Entity target) {
        float entityFov = (float) RotationUtil.getRotationDifference(target);
        float fov = this.Fov.getValue().floatValue();
        Blink blink = Client.Instance.getModuleManager().getModule(Blink.class);
        double d2 = mc.thePlayer.getDistanceToEntity(target);
        double d3 = this.range.getValue();
        if (d2 > d3) {
            return false;
        }
        if (target.isInvisible() && !this.invisibleValue.getValue()) {
            return false;
        }
        if (!target.isEntityAlive()) {
            return false;
        }
        if (fov != 360.0f && !(entityFov <= fov)) {
            return false;
        }
        if (target == Minecraft.getMinecraft().thePlayer || target.isDead || Minecraft.getMinecraft().thePlayer.getHealth() == 0.0f) {
            return false;
        }
        if ((target instanceof EntityMob || target instanceof EntityGhast || target instanceof EntityGolem || target instanceof EntityDragon || target instanceof EntitySlime) && this.mobsValue.getValue().booleanValue()) {
            return true;
        }
        if ((target instanceof EntitySquid || target instanceof EntityBat || target instanceof EntityVillager) && this.animalsValue.getValue()) {
            return true;
        }
        if (target instanceof EntityAnimal && this.animalsValue.getValue()) {
            return true;
        }
        if (blink.getState()) {
            return false;
        }
        if (Teams.isSameTeam(target)) {
            return false;
        }
        return target instanceof EntityPlayer && this.playersValue.getValue();
    }
}

