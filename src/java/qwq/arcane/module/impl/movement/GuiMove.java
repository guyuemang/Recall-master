package qwq.arcane.module.impl.movement;


import qwq.arcane.module.Mine;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import org.lwjgl.input.Keyboard;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.event.impl.events.player.MoveInputEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.player.InvManager;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.ModeValue;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:03 AM
 */

public class GuiMove extends Module {
    public GuiMove() {
        super("GuiMove",Category.Movement);
    }
    public final ModeValue mode = new ModeValue("Mode", "Basic", new String[]{"Basic", "Hypixel", "LastPacket"});
    public final BoolValue noChestValue = new BoolValue("Chest", false);
    public final BoolValue noInventoryValue = new BoolValue("Inventory", false);

    int tick = 0;
    double dist = 0;
    private boolean c16 = false;
    private boolean c0d = false;
    private boolean OpenInventory = false;
    C0DPacketCloseWindow pc = null;
    C16PacketClientStatus c16C = null;
    public static List<Packet<?>> InvPacketList = new ArrayList<>();

    @EventTarget
    private void onPacket(PacketSendEvent event) {
        if (mode.is("LastPacket")) {
            if (isHypixelLobby()) return;

            if (event.getPacket() instanceof C16PacketClientStatus p && c16C == null) {
                if (p.getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
                    c16C = p;
                }
            }

            if (event.getPacket() instanceof C0EPacketClickWindow p && pc == null) {
                InvPacketList.add(p);
                event.setCancelled(true);
            }

            if (event.getPacket() instanceof C0DPacketCloseWindow && pc == null) {
                pc = (C0DPacketCloseWindow) event.getPacket();
                event.setCancelled(true);
            }
        }

        if (mode.is("Hypixel")) {
            if (event.getPacket() instanceof C03PacketPlayer) {
                if ((mc.currentScreen instanceof GuiChest) && tick > 0) {
                    InvPacketList.add(event.getPacket());
                    event.setCancelled(true);
                }
            }
            if (event.getPacket() instanceof C16PacketClientStatus p) {
                if (c16 && p.getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
                    event.setCancelled(true);
                }
                c16 = true;
            }

            if (event.getPacket() instanceof C0EPacketClickWindow p && (tick > 0 || OpenInventory)) {
                InvPacketList.add(p);
                event.setCancelled(true);
            }

            if (event.getPacket() instanceof C0DPacketCloseWindow) {
                if (c0d && !(tick > 1) && OpenInventory) {
                    event.setCancelled(true);
                } else {
                    if (!InvPacketList.isEmpty()) {
                        event.setCancelled(true);
                        for (Packet<?> p : InvPacketList) {
                            mc.getNetHandler().addToSendQueueUnregistered(p);

                        }
                        InvPacketList.clear();

                        mc.getNetHandler().addToSendQueueUnregistered(event.getPacket());
                    }
                }
                c0d = true;
            }
        }
    }
    public static boolean isHypixelLobby() {
        String[] strings = new String[]{"CLICK TO PLAY"};
        for (Entity entity : Mine.getMinecraft().theWorld.playerEntities) {
            if (entity.getName().startsWith("§e§l")) {
                for (String string : strings) {
                    if (entity.getName().equals("§e§l" + string)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    @EventTarget()
    public void onUpdate(MotionEvent e) {
        setsuffix(mode.getValue());

        if (mode.is("LastPacket") && e.isPre()) {
            if (isHypixelLobby()) return;

            if (mc.currentScreen == null) {
                if (pc != null) {
                    if (c16C != null && !InvPacketList.isEmpty()) {
                        mc.getNetHandler().addToSendQueueUnregistered(c16C);
                    }
                    if (!InvPacketList.isEmpty()) {
                        for (Packet<?> p : InvPacketList) {
                            mc.getNetHandler().addToSendQueue(p);
                        }
                    }
                    if (c16C == null || !InvPacketList.isEmpty()) {
                        mc.getNetHandler().addToSendQueueUnregistered(pc);
                    }
                }
                InvPacketList.clear();
                c16C = null;
                pc = null;
            }
        }

        if (mode.is("Hypixel") && e.isPre()) {
            c16 = false;
            c0d = false;
            OpenInventory = false;
            if (mc.currentScreen instanceof GuiInventory ||(isEnabled(InvManager.class) && getModule(InvManager.class).isOpen())) {
                double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
                double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
                double lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
                OpenInventory = true;

                if (tick == 1) {
                    mc.getNetHandler().addToSendQueueUnregistered(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                }

                if (dist / tick > 0.00) {
                    if (tick == ((dist / tick > 0.45) ? 2 : 3)) {
                        if (!InvPacketList.isEmpty()) {
                            for (Packet<?> p : InvPacketList) {
                                mc.getNetHandler().addToSendQueueUnregistered(p);

                            }
                            InvPacketList.clear();
                        }
                    }
                    if (tick > ((dist / tick > 0.45) ? 2 : 3)) {
                        mc.getNetHandler().addToSendQueueUnregistered(new C0DPacketCloseWindow());

                        tick = 0;
                        dist = 0;
                    }
                } else if (tick > 0) {
                    if (!InvPacketList.isEmpty()) {
                        for (Packet<?> p : InvPacketList) {
                            mc.getNetHandler().addToSendQueueUnregistered(p);

                        }
                        InvPacketList.clear();
                    }
                    tick = 1;
                    dist = 0;
                }

                tick++;
                dist += lastDist;
            } else if (mc.currentScreen instanceof GuiChest) {
                double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
                double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
                double lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
                tick++;
                dist += lastDist;
            } else {
                tick = 0;
                dist = 0;
            }
        }
    }

    @EventTarget
    public final void onUpdate(UpdateEvent event) {
        if (mc.currentScreen == null){
            return;
        }
        if (mc.currentScreen.GuiInvMove()) return;

        KeyBinding[] moveKeys = new KeyBinding[]{
                mc.gameSettings.keyBindForward,
                mc.gameSettings.keyBindBack,
                mc.gameSettings.keyBindLeft,
                mc.gameSettings.keyBindRight,
                mc.gameSettings.keyBindJump
        };

        for (KeyBinding bind : moveKeys) {
            KeyBinding.setKeyBindState(bind.getKeyCode(), Keyboard.isKeyDown(bind.getKeyCode()));
        }
    }

    @EventTarget
    public final void onMoveInput(MoveInputEvent event) {
        if (mc.currentScreen == null){
            return;
        }
        if (mc.currentScreen.GuiInvMove()) return;

        float moveStrafe = 0.0F;
        float moveForward = 0.0F;

        if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
            ++moveForward;
        }

        if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
            --moveForward;
        }

        if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode())) {
            ++moveStrafe;
        }

        if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode())) {
            --moveStrafe;
        }

        if (mode.is("Hypixel")) {
            if (mc.currentScreen instanceof GuiChest) {
                if (dist / tick > 0.05) {
                    if (!InvPacketList.isEmpty()) {
                        for (Packet<?> p : InvPacketList) {
                            mc.getNetHandler().addToSendQueueUnregistered(p);

                        }
                        InvPacketList.clear();
                    }

                    tick = 0;
                    dist = 0;
                } else if (tick > 0) {
                    if (!InvPacketList.isEmpty()) {
                        for (Packet<?> p : InvPacketList) {
                            mc.getNetHandler().addToSendQueueUnregistered(p);

                        }
                        InvPacketList.clear();
                    }
                    tick = 1;
                    dist = 0;
                }
            }
        }
        event.setForward(moveForward);
        event.setStrafe(moveStrafe);
    }
}
