package qwq.arcane.module.impl.movement;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.EnumChatFormatting;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.event.impl.events.player.RightClickerEvent;
import qwq.arcane.event.impl.events.player.SlowDownEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.combat.KillAura;
import qwq.arcane.utils.chats.ChatUtils;
import qwq.arcane.utils.pack.PacketUtil;
import qwq.arcane.utils.player.PlayerUtil;
import qwq.arcane.value.impl.BooleanValue;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:00 AM
 */
public class Noslow extends Module {
    public Noslow() {
        super("Noslow",Category.Movement);
    }
    private int offGroundTicks;
    private boolean stop;
    private boolean disable;
    private Packet<?> interactItemPacket;
    private KillAura killAuraModule;
    public final BooleanValue slab = new BooleanValue("Slow down on Slabs",true);;

    @EventTarget
    public void onMotion(MotionEvent event) {
        double d2;
        if (event.getState() == MotionEvent.State.POST) return;
        if (PlayerUtil.blockRelativeToPlayer(0.0d, mc.thePlayer.motionY, 0.0d) != Blocks.air && !mc.thePlayer.isUsingItem() && this.slab.getValue().booleanValue()) {
            this.disable = false;
        }
        if (Math.abs((d2 = event.getY()) - (double)Math.round(d2)) > 0.03 && mc.thePlayer.onGround) {
            this.disable = true;
        }
        if (mc.thePlayer.isUsingItem() && !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) {
            if (mc.thePlayer.onGround) {
                this.offGroundTicks = 0;
            } else {
                this.offGroundTicks++;
            }
            if (this.offGroundTicks >= 2) {
                this.stop = false;
                this.interactItemPacket = null;
            } else if (mc.thePlayer.onGround && !this.disable) {
                event.setY(event.getY() + 0.001d);
            }
        }
        if (!this.disable || mc.thePlayer.onGround || !mc.thePlayer.isUsingItem() || (mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) {
            return;
        }
        mc.thePlayer.motionX *= 0.1d;
        mc.thePlayer.motionZ *= 0.1d;
    }

    @EventTarget
    public void onRightClick(RightClickerEvent event) {
        if (mc.thePlayer.getHeldItem() == null) {
            return;
        }
        if (mc.thePlayer.isUsingItem() || (((mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getMetadata())) || (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) || (mc.thePlayer.getHeldItem().getItem() instanceof ItemBow))) {
            if (mc.thePlayer.offGroundTicks < 2 && mc.thePlayer.offGroundTicks != 0 && !this.disable) {
                event.setCancelled(true);
            } else if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();
                event.setCancelled(true);
            }
        }
    }

    @EventTarget
    public void packetSendEvent(PacketSendEvent event){
        if (this.killAuraModule == null) {
            this.killAuraModule = this.getModule(KillAura.class);
        }
    }

    @EventTarget
    public void OnSlow(SlowDownEvent slowDownEvent) {
        if (!this.disable || mc.thePlayer.onGround) {
            if (mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) {
                slowDownEvent.setCancelled(true);
            }
            if (mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getMetadata())) {
                slowDownEvent.setCancelled(true);
            }
            if (mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) {
                slowDownEvent.setCancelled(true);
            }
        }
        if (mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
            PacketUtil.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
            PacketUtil.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            slowDownEvent.setCancelled(true);
        }
    };
}
