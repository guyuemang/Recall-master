package qwq.arcane.module.impl.movement;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.event.impl.events.player.MoveInputEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.pack.BlinkComponent;
import qwq.arcane.utils.pack.PacketUtil;
import qwq.arcane.utils.player.MovementUtil;
import qwq.arcane.value.impl.BooleanValue;

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

    private final BooleanValue cancelInventory = new BooleanValue("NoInv", false);
    private final BooleanValue cancelChest = new BooleanValue("No Chest", false);
    private final BooleanValue wdChest = new BooleanValue("Watchdog Chest", false);
    private final BooleanValue wdInv = new BooleanValue("Watchdog Inv", false);
    private final KeyBinding[] keyBindings = new KeyBinding[]{mc.gameSettings.keyBindForward, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindJump};

    @Override
    public void onDisable() {
        for (KeyBinding keyBinding : this.keyBindings) {
            KeyBinding.setKeyBindState(keyBinding.getKeyCode(), false);
        }
    }
    boolean sb;

    @EventTarget
    private void onUpdate(UpdateEvent event) {
        if (!(mc.currentScreen instanceof GuiChat) && !(mc.currentScreen instanceof GuiIngameMenu)) {
            if (cancelInventory.get() && (mc.currentScreen instanceof GuiContainer))
                return;

            if (cancelChest.get() && mc.currentScreen instanceof GuiChest)
                return;

            for (KeyBinding keyBinding : this.keyBindings) {
                KeyBinding.setKeyBindState(keyBinding.getKeyCode(), GameSettings.isKeyDown(keyBinding));
            }
            if (mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChest) {
                if (MovementUtil.isMoving()) {
                    sb = true;
                    BlinkComponent.blinking = true;
                }
            }else if (sb){
                sb = false;
                BlinkComponent.blinking = false;
            }

            if (wdChest.get() && mc.currentScreen instanceof GuiChest)
                mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;

            if (wdInv.get() && mc.currentScreen instanceof GuiInventory)
                mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
        }
    }
}
