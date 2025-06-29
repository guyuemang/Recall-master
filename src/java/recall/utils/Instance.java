package recall.utils;

import net.minecraft.client.Minecraft;
import recall.Client;

/**
 * @Author：Guyuemang
 * @Date：2025/6/29 00:46
 */
public interface Instance {
    Minecraft mc = Minecraft.getMinecraft();
    Client INSTANCE = Client.Instance;
}
