package qwq.arcane.module.impl.movement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.GameSettings;
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
import qwq.arcane.utils.pack.BlinkComponent;
import qwq.arcane.utils.player.MovementUtil;
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
}
