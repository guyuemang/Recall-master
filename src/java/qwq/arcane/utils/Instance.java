package qwq.arcane.utils;

import com.yumegod.obfuscation.FlowObfuscate;
import com.yumegod.obfuscation.InvokeDynamic;
import com.yumegod.obfuscation.Rename;
import net.minecraft.client.Minecraft;
import qwq.arcane.Client;
import qwq.arcane.utils.fontrender.FontManager;

/**
 * @Author：Guyuemang
 * @Date：2025/6/29 00:46
 */
@Rename
@FlowObfuscate
@InvokeDynamic
public interface Instance {
    Minecraft mc = Minecraft.getMinecraft();
    Client INSTANCE = Client.Instance;
    FontManager Semibold = FontManager.Semibold;
    FontManager Bold = FontManager.Bold;
    FontManager Light = FontManager.Light;
    FontManager Regular = FontManager.Regular;
}
