package qwq.arcane.utils;

import net.minecraft.client.Minecraft;
import qwq.arcane.Client;
import qwq.arcane.utils.fontrender.FontManager;

/**
 * @Author：Guyuemang
 * @Date：2025/6/29 00:46
 */
public interface Instance {
    Minecraft mc = Minecraft.getMinecraft();
    Client INSTANCE = Client.Instance;
    FontManager Semibold = FontManager.Semibold;
    FontManager Bold = FontManager.Bold;
    FontManager Light = FontManager.Light;
    FontManager Regular = FontManager.Regular;
}
