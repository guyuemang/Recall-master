package recall.utils.animations.impl;

import recall.utils.animations.AnimationUtils;
import recall.utils.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

/**
 * @author cubk
 */
public class LayeringAnimation {
    private static GuiScreen targetScreen;
    private static int progress;
    private static boolean played = false;

    public static void play(GuiScreen target) {
        targetScreen = target;
        progress = 0;
        played = true;
    }
}
