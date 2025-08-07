package qwq.arcane.module.impl.combat;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.AttackEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.pack.PacketUtil;
import qwq.arcane.utils.time.TimerUtil;
import qwq.arcane.value.impl.ModeValue;

public class Wtap extends Module {

    private int ticks;
    private final TimerUtil wtapTimer = new TimerUtil();

    private final ModeValue wtapMode = new ModeValue("Wtap","Wtap", new String[]{"Wtap", "Stap", "Shift tap", "Packet", "Legit"});

    public Wtap() {
        super("Wtap", Category.Combat);
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (wtapTimer.hasReached(500L)) {
            wtapTimer.reset();
            ticks = 2;
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        setsuffix(wtapMode.get());
        switch (ticks) {
            case 2:
                switch (wtapMode.getValue()) {
                    case "Wtap":
                        mc.gameSettings.keyBindForward.pressed = false;
                        break;
                    case "Stap":
                        mc.gameSettings.keyBindForward.pressed = false;
                        mc.gameSettings.keyBindBack.pressed = true;
                        break;
                    case "Shift tap":
                        mc.gameSettings.keyBindSneak.pressed = true;
                        break;
                    case "Packet":
                        PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                        break;
                    case "Legit":
                        mc.thePlayer.setSprinting(false);
                        break;
                }
                ticks--;
                break;
            case 1:
                switch (wtapMode.getValue()) {
                    case "Wtap":
                        mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward);
                        break;
                    case "Stap":
                        mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward);
                        mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack);
                        break;
                    case "Shift tap":
                        mc.gameSettings.keyBindSneak.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSneak);
                        break;
                    case "Packet":
                        PacketUtil.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                        break;
                    case "Legit":
                        mc.thePlayer.setSprinting(true);
                        break;
                }
                ticks--;
                break;
        }
    }
}
