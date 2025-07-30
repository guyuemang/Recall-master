package qwq.arcane.utils.render;


import qwq.arcane.utils.color.ColorUtil;

import java.awt.*;

public class Particle {
    public float x, y, adjustedX, adjustedY, deltaX, deltaY, size;
    public int opacity;
    public Color color;
    public void render2D() {
        RenderUtil.drawGoodCircle(x + adjustedX + size / 2, y + adjustedY + size / 2, size / 2, ColorUtil.applyOpacity(color, opacity / 255f).getRGB());
    }

    public void updatePosition() {
        for (int i = 1; i <= 2; i++) {
            adjustedX += deltaX;
            adjustedY += deltaY;
            deltaY *= 0.97f;
            deltaX *= 0.97f;
            opacity -= 1f;
            if (opacity < 1) opacity = 1;
        }
    }

    public void init(float x, float y, float deltaX, float deltaY, float size, int color) {
        this.x = x;
        this.y = y;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.size = size;
        this.opacity = 254;
        this.color = new Color(color);
    }
}
