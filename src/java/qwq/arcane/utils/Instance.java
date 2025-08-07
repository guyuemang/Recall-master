package qwq.arcane.utils;


import qwq.arcane.module.Mine;
import qwq.arcane.Client;
import qwq.arcane.utils.fontrender.FontManager;

/**
 * @Author：Guyuemang
 * @Date：2025/6/29 00:46
 */

public interface Instance {
    Mine mc = Mine.getMinecraft();
    Client INSTANCE = Client.Instance;
    FontManager Semibold = FontManager.Semibold;
    FontManager Bold = FontManager.Bold;
    FontManager Icon = FontManager.Icon;
    FontManager Light = FontManager.Light;
    FontManager Regular = FontManager.Regular;
}
