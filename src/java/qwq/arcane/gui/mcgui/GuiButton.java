package qwq.arcane.gui.mcgui;

import lombok.Setter;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import qwq.arcane.utils.animations.ColorAnimation;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;

import java.awt.*;

import static qwq.arcane.utils.Instance.mc;

public class GuiButton {
    private final String text;
    private final Runnable runnable;
    private float x = 0f;
    private float y = 0f;
    private float width = 0f;
    private float height = 0f;
    private final Color color;
    private final Color hoverColor;
    private final Color disabledColor = new Color(100, 100, 100, 140);
    private final ColorAnimation btnColor = new ColorAnimation(new Color(113, 127, 254));
    @Setter
    public boolean enabled = true;
    @Setter
    public boolean visible = true;
    protected boolean hovered;
    public int id;

    public GuiButton(String text, Runnable runnable, Color color, Color hoverColor) {
        this.text = text;
        this.runnable = runnable;
        this.color = color;
        this.hoverColor = hoverColor;
    }

    public GuiButton(String text, Runnable runnable) {
        this(text, runnable, new Color(113, 127, 254), new Color(135, 147, 255));
    }

    public void render(float x, float y, float width, float height, float mouseX, float mouseY) {
        if (!visible) return;

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hovered = RenderUtil.isHovering(x, y, width, height, (int) mouseX, (int) mouseY);

        if (!enabled) {
            btnColor.base(disabledColor);
        } else if (hovered) {
            btnColor.base(hoverColor);
        } else {
            btnColor.base(color);
        }

        RoundedUtil.drawRound(x, y, width, height, 5, btnColor.getColor());

        int textColor = enabled ?
                (hovered ? new Color(255, 255, 255).brighter().getRGB() : new Color(255, 255, 255).getRGB()) :
                new Color(150, 150, 150).getRGB();

        float textHeight = FontManager.Bold.get(18).getMiddleOfBox(height);
        FontManager.Bold.get(18).drawCenteredString(
                text,
                x + width / 2,
                y + (height - textHeight) / 2,
                textColor
        );
    }

    public void mouseClick(float mouseX, float mouseY, int btn) {
        if (visible && enabled && btn == 0 && RenderUtil.isHovering(x, y, width, height, (int) mouseX, (int) mouseY)) {
            runnable.run();
            playPressSound(mc.getSoundHandler());
        }
    }

    public void playPressSound(SoundHandler soundHandlerIn) {
        soundHandlerIn.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }
}