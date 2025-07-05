/*
 * MoonLight Hacked Client
 *
 * A free and open-source hacked client for Minecraft.
 * Developed using Minecraft's resources.
 *
 * Repository: https://github.com/randomguy3725/MoonLight
 *
 * Author(s): [Randumbguy & opZywl & lucas]
 */
package qwq.arcane.gui.clickgui;

import qwq.arcane.utils.Instance;

/**
 * @Author：Guyuemang
 * @Date：2025/6/29 16:44
 */
public interface IComponent extends Instance {
    default void drawScreen(int mouseX, int mouseY) {
    }

    default void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    default void mouseReleased(int mouseX, int mouseY, int state) {
    }

    default void keyTyped(char typedChar, int keyCode) {
    }
}
