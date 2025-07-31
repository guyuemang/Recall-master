package qwq.arcane.module.impl.misc;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketReceiveEvent;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.ModuleManager;
import qwq.arcane.module.impl.combat.KillAura;
import qwq.arcane.module.impl.player.InvManager;
import qwq.arcane.module.impl.player.Stealer;
import qwq.arcane.module.impl.world.PlayerTracker;
import qwq.arcane.utils.time.TimerUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.NumberValue;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:03 AM
 */
public class AutoPlay extends Module {
    public AutoPlay() {
        super("AutoPlay", Category.Misc);
    }
    private final BoolValue toggleModule = new BoolValue("Toggle Module", true);
    public boolean display = false;
    private static final Pattern PATTERN_BEHAVIOR_EXCEPTION = Pattern.compile("\u73a9\u5bb6(.*?)\u5728\u672c\u5c40\u6e38\u620f\u4e2d\u884c\u4e3a\u5f02\u5e38");
    private static final Pattern PATTERN_WIN_MESSAGE = Pattern.compile("\u4f60\u5728\u5730\u56fe(.*?)\u4e2d\u8d62\u5f97\u4e86(.*?)");
    private static final String TEXT_LIKE_OPTIONS = "      \u559c\u6b22      \u4e00\u822c      \u4e0d\u559c\u6b22";
    private static final String TEXT_BEDWARS_GAME_END = "[\u8d77\u5e8a\u6218\u4e89] Game \u7ed3\u675f\uff01\u611f\u8c22\u60a8\u7684\u53c2\u4e0e\uff01";
    private static final String TEXT_COUNTDOWN = "\u5f00\u59cb\u5012\u8ba1\u65f6: 1 \u79d2";

    @EventTarget
    public void onMotion(MotionEvent event) {
        ItemStack itemStack;
        if (event.isPost()) {
            return;
        }
        if ((itemStack = AutoPlay.mc.thePlayer.inventoryContainer.getSlot(44).getStack()) == null || itemStack.getDisplayName() == null) {
            return;
        }
        if (!itemStack.getDisplayName().contains("\u9000\u51fa\u89c2\u6218")) {
            return;
        }
    }

    @EventTarget
    public void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (AutoPlay.mc.thePlayer == null || AutoPlay.mc.theWorld == null) {
            return;
        }
        Packet packet = event.getPacket();
        if (packet instanceof S02PacketChat) {
            String text = ((S02PacketChat)packet).getChatComponent().getUnformattedText();
            if (PATTERN_BEHAVIOR_EXCEPTION.matcher(text).find()) {
            } else if (PATTERN_WIN_MESSAGE.matcher(text).find() || AutoPlay.mc.thePlayer.isSpectator() && this.toggleModule.getValue().booleanValue()) {
                this.toggleOffensiveModules(false);
            } else if (text.contains(TEXT_LIKE_OPTIONS) || text.contains(TEXT_BEDWARS_GAME_END)) {
            } else if (text.contains(TEXT_COUNTDOWN)) {
                this.checkAndTogglePlayerTracker();
            }
        }
    }

    private void toggleOffensiveModules(boolean state) {
        ModuleManager moduleManager = Client.Instance.getModuleManager();
        moduleManager.getModule(InvManager.class).setState(state);
        moduleManager.getModule(Stealer.class).setState(state);
        moduleManager.getModule(KillAura.class).setState(state);
    }

    private void checkAndTogglePlayerTracker() {
        if (this.toggleModule.getValue().booleanValue()) {
            this.toggleOffensiveModules(true);
        }
    }

    public void drop(int slot) {
        AutoPlay.mc.playerController.windowClick(AutoPlay.mc.thePlayer.inventoryContainer.windowId, slot, 1, 4, AutoPlay.mc.thePlayer);
    }
}
