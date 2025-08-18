package qwq.arcane.gui.mcgui;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.input.Mouse;
import qwq.arcane.utils.animations.AnimationUtils;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;

import java.awt.*;

public class ScrollContainer {
    private float wheel = 0f;
    private float wheel_anim = 0f;
    @Getter @Setter
    private float height = 0f;
    private float scrollStart = 0f;
    private boolean isScrolling = false;

    public float getScroll() {
        return wheel;
    }

    public float getRealScroll() {
        return wheel_anim;
    }

    public void draw(float x, float y, float width, float height, int mouseX, int mouseY, Runnable runnable) {
        runnable.run();

        if (this.height > height) {
            float percent = (height / this.height);
            float sHeight = percent * height;
            float scrollPercent = (getScroll() / (this.height - height));
            float sY = y - scrollPercent * (height - sHeight) + 6;
            float sX = x + width + 1;

            RoundedUtil.drawRound(sX, y - 5, 4.5f, height + 6, 2, new Color(0, 0, 0, 120));
            RoundedUtil.drawRound(sX, sY - 11, 4.5f, sHeight + 6, 2, isScrolling ? new Color(255, 255, 255, 185) : new Color(255, 255, 255, 100));

            if (RenderUtil.isHovering(sX, sY, 5f, height, mouseX, mouseY)) {
                if (Mouse.isButtonDown(0)) {
                    if (!isScrolling) {
                        isScrolling = true;
                        scrollStart = mouseY - sY;
                    }
                }
            }

            if (isScrolling) {
                if (Mouse.isButtonDown(0)) {
                    wheel_anim = -((mouseY - scrollStart - y) / height) * this.height;
                } else {
                    isScrolling = false;
                }
            }
        } else {
            wheel_anim = 0f;
        }

        if (RenderUtil.isHovering(x,y,width,height, mouseX, mouseY)) {
            if (this.height > height) {
                int mouseDWheel = Mouse.getDWheel();
                if (mouseDWheel > 0) {
                    wheel_anim += 20f;
                } else if (mouseDWheel < 0) {
                    wheel_anim -= 20f;
                }
            }
        }

        float maxUp = this.height - height;
        wheel_anim = Math.min(Math.max(wheel_anim, -maxUp), 0f);
        wheel = (float) AnimationUtils.base(wheel, wheel_anim, 0.2);
    }
}