package qwq.arcane.utils.chats;

import net.minecraft.entity.player.EntityPlayer;
import java.util.HashMap;
import java.util.Map;

public class IRC {
    private static final Map<String, String> ircUsers = new HashMap<>(); // 用户名 -> 显示名

    // 添加IRC用户
    public static void addUser(String username) {
        ircUsers.put(username.toLowerCase(), username);
    }

    // 检查是否为IRC用户
    public static boolean isIRCUser(EntityPlayer player) {
        if (player == null) return false;
        String playerName = player.getName();
        return ircUsers.containsKey(playerName.toLowerCase());
    }

    // 获取IRC用户的显示名
    public static String getIRCUserDisplayName(String username) {
        return ircUsers.getOrDefault(username.toLowerCase(), username);
    }

    // 清空用户列表
    public static void clearUsers() {
        ircUsers.clear();
    }
}