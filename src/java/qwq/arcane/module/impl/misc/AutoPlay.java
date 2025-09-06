package qwq.arcane.module.impl.misc;


import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.util.StringUtils;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketReceiveEvent;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.gui.notification.Notification;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.ModuleManager;
import qwq.arcane.module.impl.combat.KillAura;
import qwq.arcane.module.impl.player.InvManager;
import qwq.arcane.module.impl.player.Stealer;
import qwq.arcane.utils.Multithreading;
import qwq.arcane.utils.chats.ChatUtils;
import qwq.arcane.utils.time.TimerUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.NumberValue;
import qwq.arcane.value.impl.TextValue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static qwq.arcane.module.impl.world.Disabler.isHypixelLobby;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:03 AM
 */

public class AutoPlay extends Module {
    public AutoPlay() {
        super("AutoPlay", Category.Misc);
    }
    private final BoolValue autoGG = new BoolValue("AutoGG", true);
    private final TextValue autoGGMessage = new TextValue("AutoGG Message", "gg");
    private final BoolValue autoPlay = new BoolValue("AutoPlay", true);
    private final NumberValue autoPlayDelay = new NumberValue("Delay", 3.5f, 1, 10, 0.5f);
    private final BoolValue respawnProperty = new BoolValue("On Respawn", true);
    private List<Module> disableOnRespawn;
    private final TimerUtil respawnTimer = new TimerUtil();

    @Override
    public void onEnable() {
        if (this.disableOnRespawn == null) {
            this.disableOnRespawn = Arrays.asList(
                    Client.INSTANCE.getModuleManager().getModule(KillAura.class),
                    Client.INSTANCE.getModuleManager().getModule(Stealer.class),
                    Client.INSTANCE.getModuleManager().getModule(InvManager.class));
        }
    }

    @EventTarget
    private void onPacket(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();

        if (isHypixelLobby()) return;

        if (!event.isCancelled() && event.getPacket() instanceof S02PacketChat s02PacketChat) {
            String message = s02PacketChat.getChatComponent().getUnformattedText(), strippedMessage = StringUtils.stripControlCodes(message);

            String m = s02PacketChat.getChatComponent().toString();
            if (m.contains("ClickEvent{action=RUN_COMMAND, value='/play ")) {
                if (autoGG.get() && !strippedMessage.startsWith("You died!")) {
                    mc.thePlayer.sendChatMessage("/ac " + autoGGMessage.getValue());
                }
                if (autoPlay.get()) {
                    sendToGame(m.split("action=RUN_COMMAND, value='")[1].split("'}")[0]);
                }
            }
        }

        if (this.respawnProperty.get() && packet instanceof S07PacketRespawn) {
            if (this.respawnTimer.hasTimeElapsed(50L)) {
                if (isHypixelLobby()) return;

                boolean msg = false;
                for (Module module : this.disableOnRespawn) {
                    if (!module.isEnabled()) continue;
                    module.toggle();
                    if (msg) continue;
                    msg = true;
                }
                if (msg) {
                    Client.Instance.getNotification().add("Respawn Detected!", "Disabled movement modules/aura.", Notification.Type.INFO);
                }
                this.respawnTimer.reset();
            }
        }
    }

    private void sendToGame(String mode) {
        float delay = autoPlayDelay.get().floatValue();
        String delayText = delay > 0 ? String.format("in %.1f s", delay) : "immediately";
        Client.Instance.getNotification().add("Playing Again!", "Playing again " + delayText + ".", Notification.Type.INFO);
        Multithreading.schedule(() -> mc.thePlayer.sendChatMessage(mode), (long) delay, TimeUnit.SECONDS);
    }
}
