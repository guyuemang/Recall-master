package qwq.arcane.utils.chats;

import qwq.arcane.module.Mine;
import net.minecraft.util.EnumChatFormatting;

/**
 * @Author: Guyuemang
 * 2025/5/25
 */
public class ChatUtils {
    private static final Mine mc = Mine.getMinecraft();

    public static void sendMessage(String message) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(EnumChatFormatting.RED + "[Arcane] " + EnumChatFormatting.RESET + message));
        }
    }
}
