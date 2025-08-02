
package qwq.arcane.gui.clickgui.arcane.component.settings;

import com.yumegod.obfuscation.FlowObfuscate;
import com.yumegod.obfuscation.InvokeDynamic;
import com.yumegod.obfuscation.Rename;
import net.minecraft.util.MathHelper;
import qwq.arcane.gui.clickgui.Component;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.animations.Animation;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.DecelerateAnimation;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.value.impl.ColorValue;

import java.awt.*;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 23:16
 */
@Rename
@FlowObfuscate
@InvokeDynamic
public class ColorPickerComponent extends Component {
    private final ColorValue setting;
    private final Animation open = new DecelerateAnimation(250, 1);
    private boolean opened, pickingHue, picking, pickingAlpha;

    public ColorPickerComponent(ColorValue setting) {
        this.setting = setting;
        this.setHeight(22);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        open.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);
        setHeight((float) (24 + 66 * open.getOutput()));

        RoundedUtil.drawRound(getX() + 10, getY() + getHeight() - 4, 145, 1, 0, INSTANCE.getArcaneClickGui().linecolor);
        FontManager.Bold.get(18).drawString(setting.getName(), getX() + 10, getY() + 4, ColorUtil.applyOpacity(INSTANCE.getArcaneClickGui().fontcolor.getRGB(),0.4f));
        RenderUtil.drawCircle(getX() + 149, getY() + 7, 0, 360, 5, 2, true, (setting.isRainbow() ? ColorUtil.getRainbow().getRGB() : setting.get().getRGB()));
        RenderUtil.resetColor2();
        //picker
        if (open.getOutput() > 0) {
            float colorAlpha = 1;
            float gradientWidth = 60;
            float gradientHeight = (float) (60 * open.getOutput());
            float gradientX = getX() + 34;
            float gradientY = getY() + 18;
            float[] hsb = {setting.getHue(), setting.getSaturation(), setting.getBrightness()};

            for (float i = 0; i <= 60 * open.getOutput(); i++) {
                RenderUtil.drawRect(getX() + 21, getY() + 18 + i, 8, 1, Color.getHSBColor((float) (i / 60 * open.getOutput()), 1f, 1f).getRGB());
            }
            RenderUtil.drawRect(getX() + 20, (float) (getY() + 18 + (setting.isRainbow() ? getRainbowHSB(0)[0] : setting.getHue()) * 60 * open.getOutput()),
                    10, 1, Color.WHITE.getRGB());

            for (float i = 0; i <= 60 * open.getOutput(); i++) {
                RenderUtil.drawRect(getX() + 11, getY() + 18 + i, 8, 1, ColorUtil.applyOpacity(new Color(setting.isRainbow() ? (ColorUtil.getRainbow().getRGB()) : (Color.HSBtoRGB(setting.getHue(), setting.getSaturation(), setting.getBrightness()))), setting.getAlpha() - i / 60).getRGB());
            }
            RenderUtil.drawRect(getX() + 10, (float) (getY() + 18 + (1 - setting.getAlpha()) * 60 * open.getOutput()),
                    10, 1, Color.WHITE.getRGB());

            float pickerY = (gradientY + 2) + (gradientHeight * (1 - hsb[2]));
            float pickerX = (gradientX) + (gradientWidth * hsb[1] - 1);
            pickerY = Math.max(Math.min(gradientY + gradientHeight - 2, pickerY), gradientY);
            pickerX = Math.max(Math.min(gradientX + gradientWidth - 2, pickerX), gradientX + 2);

            if (pickingHue) {
                setting.setHue(MathHelper.clamp_float((mouseY - (getY() + 18)) / 60, 0, 1));
            }
            if (pickingAlpha) {
                setting.setAlpha(MathHelper.clamp_float(1 - ((mouseY - (getY() + 18)) / 60), 0, 1));
            }
            if (picking) {
                setting.setBrightness(MathHelper.clamp_float(1 - ((mouseY - gradientY) / 60), 0, 1));
                setting.setSaturation(MathHelper.clamp_float((mouseX - gradientX) / 60, 0, 1));
            }

            Color firstColor = (setting.isRainbow() ? ColorUtil.getRainbow() : ColorUtil.applyOpacity(Color.getHSBColor(hsb[0], 1, 1), colorAlpha));
            RoundedUtil.drawRound(gradientX, gradientY, gradientWidth, gradientHeight, 2,
                    ColorUtil.applyOpacity(firstColor, colorAlpha));
            Color secondColor = Color.getHSBColor(hsb[0], 0, 1);
            RoundedUtil.drawGradientHorizontal(gradientX, gradientY, gradientWidth, gradientHeight, 2 + .5f,
                    ColorUtil.applyOpacity(secondColor, colorAlpha),
                    ColorUtil.applyOpacity(secondColor, 0));
            Color thirdColor = Color.getHSBColor(hsb[0], 1, 0);
            RoundedUtil.drawGradientVertical(gradientX, gradientY, gradientWidth, gradientHeight, 2,
                    ColorUtil.applyOpacity(thirdColor, 0),
                    ColorUtil.applyOpacity(thirdColor, colorAlpha));

            RenderUtil.drawCircle((int) pickerX, (int) pickerY, 0, 360, 2, .1f, false, -1);

        }
        super.drawScreen(mouseX, mouseY);
    }
    public float[] getRainbowHSB(int counter) {
        final int width = 20;

        double rainbowState = Math.ceil(System.currentTimeMillis() - (long) counter * width) / 8;
        rainbowState %= 360;

        float hue = (float) (rainbowState / 360);
        float saturation = InterFace.mainColor.getSaturation();
        float brightness = InterFace.mainColor.getBrightness();

        return new float[]{hue, saturation, brightness};
    }
    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (RenderUtil.isHovering(getX() + 144, getY() + 2, 10, 10, mouseX, mouseY) && mouseButton == 1) {
            opened = !opened;
        }
        if (opened) {
            if (mouseButton == 0) {

                if (RenderUtil.isHovering(getX() + 34, getY() + 18, 60, 60, mouseX, mouseY)) {
                    picking = true;
                }

                if (RenderUtil.isHovering(getX() + 21, getY() + 18, 8, 60, mouseX, mouseY)) {
                    pickingHue = true;
                }

                if (RenderUtil.isHovering(getX() + 11, getY() + 18, 8, 60, mouseX, mouseY)) {
                    pickingAlpha = true;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) {
            pickingHue = false;
            picking = false;
            pickingAlpha = false;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean isVisible() {
        return setting.isAvailable();
    }
}
