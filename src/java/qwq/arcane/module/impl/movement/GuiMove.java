package qwq.arcane.module.impl.movement;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.MoveInputEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.player.MovementUtil;
import qwq.arcane.value.impl.BooleanValue;

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
    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (wdChest.get() && mc.currentScreen instanceof GuiChest)
            event.setJumping(false);
        if (wdInv.get() && mc.currentScreen instanceof GuiInventory)
            event.setJumping(false);
    }

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

            if (wdChest.get() && mc.currentScreen instanceof GuiChest)
                MovementUtil.stopXZ();

            if (wdInv.get() && mc.currentScreen instanceof GuiInventory)
                MovementUtil.stopXZ();

        }
    }
}
