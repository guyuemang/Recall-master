package qwq.arcane.module.impl.movement;

import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.event.impl.events.player.RightClickerEvent;
import qwq.arcane.event.impl.events.player.SlowDownEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.combat.KillAura;
import qwq.arcane.utils.pack.BlinkComponent;
import qwq.arcane.utils.pack.PacketUtil;
import qwq.arcane.utils.player.MovementUtil;
import qwq.arcane.utils.player.PlayerUtil;
import qwq.arcane.value.impl.BooleanValue;
import qwq.arcane.value.impl.ModeValue;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:00 AM
 */
public class Noslow extends Module {
    public Noslow() {
        super("Noslow",Category.Movement);
    }
    public final ModeValue mode = new ModeValue("Mode", "Blink", new String[]{"Blink"});
    public final BooleanValue sprint = new BooleanValue("Sprint", false);
    public final BooleanValue foodValue = new BooleanValue("Food", false);
    public final BooleanValue potionValue = new BooleanValue("Potion", false);
    public final BooleanValue swordValue = new BooleanValue("Sword", false);
    public final BooleanValue bowValue = new BooleanValue("Bow", false);
    boolean usingItem;

    @EventTarget
    public void onMotion(MotionEvent event) {
        setsuffix(mode.get());

        if (event.isPre()) {
            if (mc.thePlayer.getCurrentEquippedItem() == null) return;

            final Item item = mc.thePlayer.getCurrentEquippedItem().getItem();

            if (mc.thePlayer.isUsingItem()) {
                if (item instanceof ItemSword && swordValue.get()) {
                    BlinkComponent.blinking = true;

                    if (mc.thePlayer.ticksExisted % 5 == 0) {
                        PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getCurrentEquippedItem()));
                    }
                } else if (item instanceof ItemFood && foodValue.get() || item instanceof ItemBow && bowValue.get()) {
                    BlinkComponent.blinking = true;
                }

                usingItem = true;
            } else if (usingItem) {
                usingItem = false;

                BlinkComponent.blinking = false;
            }
        }
    }

    @EventTarget
    public void onSlowDown(SlowDownEvent event) {
        event.setSprinting(sprint.get());

        if (foodValue.get() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) {
            event.setForward(1);
            event.setStrafe(1);
        }
        if (potionValue.get() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) {
            event.setForward(1);
            event.setStrafe(1);
        }
        if (swordValue.get() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
            event.setForward(1);
            event.setStrafe(1);
        }
        if (bowValue.get() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) {
            event.setForward(1);
            event.setStrafe(1);
        }
    }
}
