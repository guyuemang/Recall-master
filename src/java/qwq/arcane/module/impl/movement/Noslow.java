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
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S19PacketEntityStatus;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:00 AM
 */
public class Noslow extends Module {
    public Noslow() {
        super("Noslow",Category.Movement);
    }
    private final ModeValue mode = new ModeValue("Mode", "Blink", new String[]{"Blink","Grim"});
    private final BoolValue bedWarsFood = new BoolValue("Food (Bed Wars)",() -> this.mode.is("Grim"), false);
    private final BoolValue food = new BoolValue("Food",() -> this.mode.is("Grim"), true);
    public final BoolValue bow = new BoolValue("Bow",() -> this.mode.is("Grim"), true);
    private final BoolValue potions = new BoolValue("Potions",() -> this.mode.is("Grim"), true);
    private final LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();
    boolean usingItem;
    private boolean lastUsingRestItem = false;
    private static final int currentSlot = 0;
    public static boolean hasDroppedFood = false;
    private boolean sent = false;
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
        setsuffix(String.valueOf(mode.get()));
        if (this.isGapple()) {
            return;
        }
        if (mode.is("Blink")) {
            if (mc.thePlayer.isUsingItem() && usingItem){
                mc.thePlayer.setJumping(true);
            }
            if (mc.thePlayer.getCurrentEquippedItem() == null) return;

            final Item item = mc.thePlayer.getCurrentEquippedItem().getItem();

            if (mc.thePlayer.isUsingItem() && item instanceof ItemFood || item instanceof ItemPotion || item instanceof ItemBow || item instanceof ItemSword) {
                BlinkComponent.blinking = true;
                BlinkComponent.dispatch();
            }else {
                usingItem = true;
                BlinkComponent.blinking = false;
            }
        }
        if (mode.is("Grim")){
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
                        com.viaversion.viarewind.utils.PacketUtil.sendToServer(useItem, Protocol1_8To1_9.class, true, true);
                        PacketWrapper useItem2 = PacketWrapper.create(29, null, Via.getManager().getConnectionManager().getConnections().iterator().next());
                        useItem2.write(Type.VAR_INT, 0);
                        PacketUtil.sendToServer(useItem2, Protocol1_8To1_9.class, true, true);
                    }
                }
            }
        }
    }
    @EventTarget
    public void onSlowDown(SlowDownEvent event) {
        if (this.isGapple()) {
            return;
        }
        switch (mode.get()) {
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
            case "Watchdog":
                final Item item = mc.thePlayer.getCurrentEquippedItem().getItem();

                if (mc.thePlayer.isUsingItem() && usingItem) {
                    if (item instanceof ItemFood || item instanceof ItemPotion || item instanceof ItemBow || item instanceof ItemSword) {
                        event.setCancelled();
                        event.setForward(1);
                        event.setStrafe(1);
                        event.setSprinting(false);
                    }
                }
                break;
        }
    }
    @EventTarget
    public void onReceive(PacketReceiveEvent event) {
        if (this.isGapple()) {
            return;
        }
        if (mode.is("Grim")) {
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
    public void onPacketSend(PacketSendEvent event) {
        if (this.isGapple()) {
            return;
        }
        Packet<?> packet = event.getPacket();

        if (mc.thePlayer == null) return;

        if (mode.is("Blink")) {
            if (packet instanceof C08PacketPlayerBlockPlacement status){
                if (status.getPlacedBlockDirection() >= 4) {
                     usingItem = true;
                    packets.add(status);
                }
            }else if (!packets.isEmpty()){;
                packets.forEach(mc.getNetHandler()::addToSendQueueUnregistered);
                packets.clear();
            }
        }
        if (mode.is("Grim")){
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
    }
}
