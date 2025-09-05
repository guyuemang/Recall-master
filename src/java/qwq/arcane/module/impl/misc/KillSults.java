/*
 * MoonLight Hacked Client
 *
 * A free and open-source hacked client for Minecraft.
 * Developed using Minecraft's resources.
 *
 * Repository: https://github.com/randomguy3725/MoonLight
 *
 * Author(s): [Randumbguy & wxdbie & opZywl & MukjepScarlet & lucas & eonian]
 */
package qwq.arcane.module.impl.misc;

import net.minecraft.entity.player.EntityPlayer;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.WorldLoadEvent;
import qwq.arcane.event.impl.events.player.AttackEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.value.impl.TextValue;

import java.util.concurrent.ThreadLocalRandom;

public class KillSults extends Module {
    private EntityPlayer currentTarget;
    public final TextValue stringV = new TextValue("Name"," 你已被Arcane击败");

    public KillSults() {
        super("KillSults",Category.Misc);
    }

    @EventTarget
    private void onUpdate(UpdateEvent event) {
        if (currentTarget.isDead && !mc.thePlayer.isDead && !mc.thePlayer.isSpectator()) {
            sendMessage(currentTarget.getName());
            currentTarget = null;
        }
    }

    @EventTarget
    private void onWorld(WorldLoadEvent event) {
        currentTarget = null;
    }

    @EventTarget
    private void onAttack(AttackEvent event) {
        if (event.getTargetEntity() instanceof EntityPlayer)
            currentTarget = (EntityPlayer) event.getTargetEntity();
    }

    public void sendMessage(String name) {
        final String[] text = {"Nobe，"};
        final int randomIndex = ThreadLocalRandom.current().nextInt(0, text.length);
        mc.thePlayer.sendChatMessage("@" + name + " " + text[randomIndex] + stringV.getValue());
    }
}
