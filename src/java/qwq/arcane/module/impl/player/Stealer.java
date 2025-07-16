package qwq.arcane.module.impl.player;

import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.util.MathHelper;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.WorldLoadEvent;
import qwq.arcane.event.impl.events.packet.PacketReceiveSyncEvent;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.time.StopWatch;
import qwq.arcane.utils.time.TimerUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.MultiBooleanValue;
import qwq.arcane.value.impl.NumberValue;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:02 AM
 */
//这是真自写的方法除了老虎机修复
public class Stealer extends Module {
    public Stealer() {
        super("Stealer", Category.Player);
    }

    private NumberValue delay = new NumberValue("Delay", 3, 0, 15, 1);
    private BoolValue slotMachineFix = new BoolValue("SlotMachine Fix", false);
    private MultiBooleanValue container = new MultiBooleanValue("Container", Arrays.asList(new BoolValue("Chest", true),
            new BoolValue("Furnace", true)));

    public final LinkedBlockingQueue<Packet<INetHandlerPlayClient>> setSlots = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();
    public static final TimerUtil timer = new TimerUtil();
    private int nextDelay = 0;
    private boolean hasItems = false;
    private final StopWatch stopWatch = new StopWatch();
    private boolean action = false;
    private boolean inchest;
    @EventTarget
    public void onWorld(WorldLoadEvent e){
        this.setState(false);
    }
    @EventTarget
    public void onMotion(MotionEvent event) {
        setsuffix(String.valueOf(delay.get()));
        if (mc.thePlayer.openContainer instanceof ContainerFurnace && container.isEnabled("Furnace")) {
            ContainerFurnace furnace = (ContainerFurnace) mc.thePlayer.openContainer;
            hasItems = false;
            for (int i = 0; i < furnace.tileFurnace.getSizeInventory(); ++i) {
                if (furnace.tileFurnace.getStackInSlot(i) != null) {
                    hasItems = true;
                    inchest = true;
                    break;
                }
            }
            for (int i = 0; i < furnace.tileFurnace.getSizeInventory(); ++i) {
                if (furnace.tileFurnace.getStackInSlot(i) != null) {
                    if (timer.delay(nextDelay)) {
                        inchest = true;
                        if (slotMachineFix.get()) {
                            stopWatch.reset();
                            action = true;
                        }
                        mc.playerController.windowClick(furnace.windowId, i, 0, 1, mc.thePlayer);
                        nextDelay = (int) ((delay.get().floatValue() * 10) * MathHelper.getRandomDoubleInRange(0.75, 1.25));
                        timer.reset();
                    }
                }
            }
            if (!hasItems) {
                if (timer.delay(delay.get().floatValue())) {
                    mc.thePlayer.closeScreen();
                    inchest = false;
                }
                return;
            }
        }
        if (mc.thePlayer.openContainer instanceof ContainerChest && container.isEnabled("Chest")) {
            ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;
            hasItems = false;
            for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); ++i) {
                if (container.getLowerChestInventory().getStackInSlot(i) != null) {
                    hasItems = true;
                    inchest = true;
                    break;
                }
            }
            for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); ++i) {
                if (container.getLowerChestInventory().getStackInSlot(i) != null) {
                    if (timer.delay(nextDelay)) {
                        inchest = true;
                        if (slotMachineFix.get()) {
                            stopWatch.reset();
                            action = true;
                        }
                        mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                        nextDelay = (int) ((delay.get().floatValue() * 10) * MathHelper.getRandomDoubleInRange(0.75, 1.25));
                        timer.reset();
                    }
                }
            }
            if (!hasItems) {
                if (timer.delay(delay.get().floatValue())) {
                    mc.thePlayer.closeScreen();
                    inchest = false;
                }
            }
        }
    }

    @EventTarget
    public void onPacketSend(PacketSendEvent event){
        Packet<?> packet = event.getPacket();
        if (packet instanceof C0EPacketClickWindow || packet instanceof C0DPacketCloseWindow) {
            if (inchest){
                event.setCancelled(true);
                packets.add(packet);
            }
        }else {
            packets.forEach(mc.getNetHandler()::addToSendQueueUnregistered);
            packets.clear();
        }
        if (packet instanceof S2DPacketOpenWindow || (packet instanceof C16PacketClientStatus && ((C16PacketClientStatus) packet).getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT)) {
            inchest = true;
        }
        if (packet instanceof S2EPacketCloseWindow || packet instanceof C0DPacketCloseWindow) {
            inchest = false;
        }
    }

    @EventTarget
    public void onPacketReceiveSync(PacketReceiveSyncEvent event) {
        if (slotMachineFix.getValue()) {
            if (action) {
                if (event.getPacket() instanceof S2FPacketSetSlot || event.getPacket() instanceof S30PacketWindowItems || event.getPacket() instanceof C0EPacketClickWindow) {
                    event.setCancelled(true);
                    setSlots.add((Packet<INetHandlerPlayClient>) event.getPacket());
                }
                if (stopWatch.hasTimePassed(delay.get().longValue())) slotMachineReset();
            }

            if (event.getPacket() instanceof S2DPacketOpenWindow || event.getPacket() instanceof S2EPacketCloseWindow)
                slotMachineReset();
        }
    }

    private void slotMachineReset() {
        while (!setSlots.isEmpty()) {
            try {
                setSlots.poll().processPacket(mc.getNetHandler());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        action = false;
    }
}
