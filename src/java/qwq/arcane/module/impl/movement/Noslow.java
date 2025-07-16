package qwq.arcane.module.impl.movement;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketReceiveEvent;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.event.impl.events.player.SlowDownEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.pack.BlinkComponent;
import qwq.arcane.utils.player.MovementUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.ModeValue;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:00 AM
 */
public class Noslow extends Module {
    public Noslow() {
        super("Noslow",Category.Movement);
    }
    public final ModeValue mode = new ModeValue("Mode", "Blink", new String[]{"Blink","Grim"});
    public final BoolValue sprint = new BoolValue("Sprint",() -> this.mode.is("Blink"), false);
    public final BoolValue foodValue = new BoolValue("Food",() -> this.mode.is("Blink"), false);
    public final BoolValue potionValue = new BoolValue("Potion",() -> this.mode.is("Blink"), false);
    public final BoolValue swordValue = new BoolValue("Sword",() -> this.mode.is("Blink"), false);
    public final BoolValue bowValue = new BoolValue("Bow",() -> this.mode.is("Blink"), false);
    private final BoolValue bedWarsFood = new BoolValue("Food (Bed Wars)", false);
    private final BoolValue food = new BoolValue("Food",() -> this.mode.is("Grim"), true);
    private final BoolValue bow = new BoolValue("Bow",() -> this.mode.is("Grim"), true);
    boolean usingItem;
    private final LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();
    private boolean lastUsingRestItem = false;
    private boolean sent = false;
    private static final int currentSlot = 0;
    public static boolean hasDroppedFood = false;

    @Override
    public void onEnable() {
        sent = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        packets.forEach(packet -> mc.getNetHandler().addToSendQueueUnregistered(packet));
        packets.clear();
        lastUsingRestItem = false;
        super.onDisable();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        setsuffix(mode.getValue());
        if (event.isPre()) {
            switch (mode.get()) {
                case "Grim":
                if (!mc.isSingleplayer()) {
                    if (event.isPre()) {
                        if (mc.thePlayer == null || mc.theWorld == null || mc.thePlayer.getHeldItem() == null) return;
                        //F00d N0Sl0w
                        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && food.getValue()) {
                            Minecraft.getMinecraft().rightClickDelayTimer = 4;
                            if (mc.thePlayer.isUsingItem() && !hasDroppedFood && mc.thePlayer.getHeldItem().stackSize > 1) {
                                mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                                hasDroppedFood = true;
                            } else {
                                hasDroppedFood = false;
                            }
                        }
                        if (Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem() != null) {
                            if (mc.thePlayer.isBlocking() || mc.thePlayer.isUsingItem() && hasSword()) {
                                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                                mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("germmod-netease", new PacketBuffer(Unpooled.buffer())));
                                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                            }
                            if (mc.thePlayer.getHeldItem().getItem() instanceof ItemBow && mc.thePlayer.isUsingItem() && bow.getValue() && !mc.thePlayer.isSneaking()) {
                                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                                mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("germmod-netease", new PacketBuffer(Unpooled.buffer())));
                                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                            }
                        }
                    }
                    if (event.isPost()) {
                        if (mc.thePlayer.getHeldItem() == null) return;
                        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && mc.thePlayer.isUsingItem()) {
                            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                        }
                        if (mc.thePlayer.getHeldItem().getItem() instanceof ItemBow && mc.thePlayer.isUsingItem() && bow.getValue()) {
                            PacketWrapper useItem = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                            useItem.write(Type.VAR_INT, 1);
                            PacketUtil.sendToServer(useItem, Protocol1_8To1_9.class, true, true);
                            PacketWrapper useItem2 = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                            useItem2.write(Type.VAR_INT, 0);
                            PacketUtil.sendToServer(useItem2, Protocol1_8To1_9.class, true, true);
                        }
                    }
                }
                break;
                case "Blink":
                if (mc.thePlayer.getCurrentEquippedItem() == null) return;

                final Item item = mc.thePlayer.getCurrentEquippedItem().getItem();

                if (mc.thePlayer.isUsingItem()) {
                    if (item instanceof ItemFood && foodValue.get() || item instanceof ItemPotion && potionValue.get() || item instanceof ItemBow && bowValue.get()) {
                        BlinkComponent.blinking = true;
                    }

                    usingItem = true;
                } else if (usingItem) {
                    usingItem = false;

                    BlinkComponent.blinking = false;
                }
                break;
            }
        }
    }

    @EventTarget
    public void onReceive(PacketReceiveEvent event) {
        if (mode.get().equals("Grim")) {
            Packet<?> packet = event.getPacket();
            if (packet instanceof C08PacketPlayerBlockPlacement) {
                C08PacketPlayerBlockPlacement wrapped = (C08PacketPlayerBlockPlacement) packet;

                if (wrapped.getPlacedBlockDirection() == 255 && wrapped.getPosition().equals(new BlockPos(-1, -1, -1))) {
                    if (!sent) {
                        mc.thePlayer.sendChatMessage("/lizi open");
                        sent = true;
                    }
                }
            }
        }
    }

    @EventTarget
    public void onPacketSend(PacketSendEvent event){
        Packet<?> packet = event.getPacket();
        if (mode.is("Grim") && bedWarsFood.getValue()) {
            if (mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || (mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getMetadata())))) {
                if (packet instanceof C08PacketPlayerBlockPlacement) {
                    C08PacketPlayerBlockPlacement wrapped = (C08PacketPlayerBlockPlacement) packet;

                    if (wrapped.getPlacedBlockDirection() == 255 && wrapped.getPosition().equals(new BlockPos(-1, -1, -1))) {
                        if (!sent) {
                            mc.thePlayer.sendChatMessage("/lizi open");
                            sent = true;
                        }
                    }
                }
            } else {
                sent = false;
            }

        }
    }

    @EventTarget
    public void onSlowDown(SlowDownEvent event) {
        switch (mode.get()) {
            case "Blink":
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
            break;
            case "Grim":
                if (mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || (mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getMetadata())))) {
                    if (!sent) {
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
                if (mc.thePlayer == null || mc.theWorld == null || mc.thePlayer.getHeldItem() == null || mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && food.getValue())
                    return;
                if (mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemSword || (mc.thePlayer.getHeldItem().getItem() instanceof ItemBow && bow.getValue())) && mc.thePlayer.isUsingItem())
                    event.setCancelled(true);
                if (!mc.thePlayer.isSprinting() && !mc.thePlayer.isSneaking() && MovementUtil.isMoving()) {
                    mc.thePlayer.setSprinting(true);
                }
                break;
        }
    }

    public static boolean hasSword() {
        return Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }

    public static boolean isRest(Item item) {
        return item instanceof ItemFood || item instanceof ItemPotion;
    }

    public static ItemStack getHeldItem() {
        final InventoryPlayer inventory = mc.thePlayer.inventory;
        if (currentSlot != 0)
            return currentSlot < 9 && currentSlot >= 0 ? inventory.mainInventory[currentSlot] : null;
        return getRenderHeldItem();
    }

    public static ItemStack getRenderHeldItem() {
        final InventoryPlayer inventory = mc.thePlayer.inventory;
        return inventory.currentItem < 9 && inventory.currentItem >= 0 ? inventory.mainInventory[inventory.currentItem] : null;
    }

    public static int getCurrentSlot() {
        if (currentSlot != 0)
            return currentSlot;
        return mc.thePlayer.inventory.currentItem;
    }
}
