package qwq.arcane.gui;

import net.minecraft.client.gui.*;
import org.bytedeco.javacv.FrameGrabber;
import qwq.arcane.gui.altmanager.alt.GuiToken;
import qwq.arcane.module.impl.render.InterFace;
import qwq.arcane.utils.animations.Animation;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.DecelerateAnimation;
import qwq.arcane.utils.animations.impl.LayeringAnimation;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @Author：Guyuemang
 * @Date：2025/6/28 23:22
 */
public class MainMenu extends GuiScreen {
    List<Button> buttons = Arrays.asList(
            new Button("Single Player","B"),
            new Button("Multi Player","E"),
            new Button("Alt Manager","D"),
            new Button("Options","O"),
            new Button("Shut down","I")
    );
    // 添加一个动画变量
    private Animation fadeInAnimation = new DecelerateAnimation(4000, 1).setDirection(Direction.FORWARDS);
    private static Animation progress4Anim;
    private Animation Animation = new DecelerateAnimation(1000, 1).setDirection(Direction.FORWARDS);
    int alpha = 0; // 从完全不透明到完全透明
    @Override
    public void initGui() {
        progress4Anim = new DecelerateAnimation(5000, 1).setDirection(Direction.BACKWARDS);
        if (mc.gameSettings.guiScale != 2) {
            mc.gameSettings.guiScale = 2;
            mc.resize(mc.displayWidth - 1, mc.displayHeight);
            mc.resize(mc.displayWidth + 1, mc.displayHeight);
        }
    }
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        try {
            VideoPlayer.render(0, 0, sr.getScaledWidth(), sr.getScaledHeight());
        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
        float aWidth = FontManager.Bold.get(80).getStringWidth("A");
        if (fadeInAnimation.getOutput() >= 0.35f) {
            progress4Anim.setDirection(Direction.FORWARDS);
            Animation.setDirection(Direction.BACKWARDS);
            FontManager.Bold.get(80).drawString("A", 5 + (width / 2 - aWidth / 2 - (FontManager.Bold.get(80).getStringWidth("rcane") / 2)) * Animation.getOutput(), 7 + (height / 2 - 50) * Animation.getOutput(), ColorUtil.applyOpacity(InterFace.color(1),1).getRGB());
            FontManager.Bold.get(80).drawString("rcane", 5 + aWidth + (width / 2 - (FontManager.Bold.get(80).getStringWidth("rcane") / 2) + aWidth / 2) * Animation.getOutput(), 7 + (height / 2 - 50) * Animation.getOutput(), ColorUtil.applyOpacity(-1,1));
        }else {
            FontManager.Bold.get(80).drawString("A", (width / 2 - aWidth / 2 - (FontManager.Bold.get(80).getStringWidth("rcane") / 2)), (height / 2 - 50), ColorUtil.applyOpacity(InterFace.color(1),1).getRGB());
            FontManager.Bold.get(80).drawString("rcane", (width / 2 - (FontManager.Bold.get(80).getStringWidth("rcane") / 2) + aWidth / 2), (height / 2 - 50), ColorUtil.applyOpacity(-1,1));
        }
        float count = 0;
        for (Button button : buttons) {
            button.x = 5;
            button.y = 70 + count;
            button.width = 180;
            button.height = 35;
            button.clickAction = () -> {
                switch (button.name) {
                    case "Single Player": {
                        LayeringAnimation.play(new GuiSelectWorld(this));
                    }
                    break;
                    case "Multi Player": {
                        LayeringAnimation.play(new GuiMultiplayer(this));
                    }
                    break;
                    case "Alt Manager": {
                        LayeringAnimation.play(new GuiToken(this));
                    }
                    break;
                    case "Options": {
                        LayeringAnimation.play(new GuiOptions(this, mc.gameSettings));
                    }
                    break;
                    case "Shut down": {
                        mc.shutdown();
                    }
                    break;
                }
            };
            count += 45;
            button.drawScreen(mouseX, mouseY);
        }
        float progress = fadeInAnimation.getOutput().floatValue();
        alpha = (int)(255 * (1 - progress)); // 从完全不透明到完全透明

            RenderUtil.drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), new Color(0, 0, 0, alpha).getRGB());
    }
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        buttons.forEach(button -> {button.mouseClicked(mouseX, mouseY, mouseButton);});
    }

    class Button {
        String name;
        String icon;
        public float x, y, width, height;
        public Runnable clickAction;
        private Animation hoverAnimation = new DecelerateAnimation(1000, 1);;

        public Button(String name,String icon){
            this.name = name;
            this.icon = icon;
        }

        public void drawScreen(int mouseX, int mouseY) {
            boolean hovered = RenderUtil.isHovering(x,y - 15,width,height, mouseX, mouseY);
            Color rectColor = new Color(35, 37, 43, 150);
            rectColor = ColorUtil.interpolateColorC(rectColor, ColorUtil.brighter(rectColor, 0.4f),this.hoverAnimation.getOutput().floatValue());
            hoverAnimation.setDirection(hovered ? Direction.BACKWARDS : Direction.FORWARDS);
            RoundedUtil.drawRound(x, y - 15, width, height, 3, rectColor);
            RenderUtil.startGlScissor((int) x - 2, (int) (y - 20), 10, 45);
            RoundedUtil.drawGradientVertical(x, y - 15, 10, height, 3, InterFace.color(1),InterFace.color(7));
            RenderUtil.stopGlScissor();
            FontManager.Semibold.get(20).drawString(name,x + 32,y + 1,new Color(-1).getRGB());
            FontManager.Icon.get(30).drawString(icon,x + 13  ,y,new Color(-1).getRGB());
        }

        public void mouseClicked(int mouseX, int mouseY, int button) {
            boolean hovered = RenderUtil.isHovering(x,y - 15,width,height, mouseX, mouseY);
            if (hovered) clickAction.run();
        }
    }
}
