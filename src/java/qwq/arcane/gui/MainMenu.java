package qwq.arcane.gui;

import com.yumegod.obfuscation.FlowObfuscate;
import com.yumegod.obfuscation.InvokeDynamic;
import com.yumegod.obfuscation.Rename;
import com.yumegod.obfuscation.StringObfuscate;
import net.minecraft.client.gui.*;
import org.bytedeco.javacv.FrameGrabber;
import qwq.arcane.Client;
import qwq.arcane.gui.alt.GuiAccountManager;
import qwq.arcane.module.impl.visuals.InterFace;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * @Author：Guyuemang
 * @Date：2025/6/28 23:22
 */
@Rename
@FlowObfuscate
@InvokeDynamic
@StringObfuscate
public class MainMenu extends GuiScreen {
    List<Button> buttons = Arrays.asList(
            new Button("Single Player","B"),
            new Button("Multi Player","E"),
            new Button("Alt Manager","D"),
            new Button("Options","O"),
            new Button("Shut down","I")
    );
    List<Button2> buttons2 = Arrays.asList(
            new Button2("Discord","V"),
            new Button2("Kook","W"),
            new Button2("Bilibili","X"),
            new Button2("YouTube","Y"),
            new Button2("Shop","Z")
    );
    private Animation fadeInAnimation = new DecelerateAnimation(4000, 1).setDirection(Direction.FORWARDS);
    private static Animation progress4Anim;
    private Animation Animation = new DecelerateAnimation(1000, 1).setDirection(Direction.FORWARDS);
    int alpha = 0;
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
        RoundedUtil.drawRound(width - 200, height - 50 , 180, 30, 5, new Color(35, 37, 43, 150));
        LocalTime currentTime1 = LocalTime.now();
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime1 = currentTime1.format(formatter1);
        FontManager.Bold.get(50).drawCenteredString(formattedTime1, width - 85, 10, -1);
        RoundedUtil.drawGradientHorizontal(width - 160, 40 , 160, 1, 3,  InterFace.color(1).brighter(),InterFace.color(7).brighter());

        FontManager.Bold.get(18).drawString("Arcane-Client", 10, height - 70, -1);
        FontManager.Bold.get(18).drawString("Made by Guyuemang", 10, height - 60, -1);
        FontManager.Bold.get(18).drawString("Love Nothing Team", 10, height - 50, -1);
        FontManager.Bold.get(18).drawString("Version:" + Client.version, 10, height - 40, -1);

        float count = 0;
        float count2 = 0;
        for (Button2 buttons2 : buttons2) {
            buttons2.x = width - 200 + count2;
            buttons2.y = height - 42;
            buttons2.width = FontManager.Icon.get(40).getStringWidth(buttons2.icon);
            buttons2.height = 15;
            buttons2.clickAction = () -> {
                switch (buttons2.name){
                    case "Discord": {
                        URI uri = null;
                        try {
                            uri = new URI("https://discord.gg/yh3nqbnupB");
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            Desktop.getDesktop().browse(uri);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                    case "Kook":{
                        URI uri = null;
                        try {
                            uri = new URI("https://kook.vip/Adb58B");
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            Desktop.getDesktop().browse(uri);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                    case "Bilibili":{
                        URI uri = null;
                        try {
                            uri = new URI("https://space.bilibili.com/1068486349?spm_id_from=333.1007.0.0");
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            Desktop.getDesktop().browse(uri);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                    case "YouTube":
                    {
                        URI uri = null;
                        try {
                            uri = new URI("https://www.youtube.com/channel/UCZCkwH9GU1u-t34jma9XrlA");
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            Desktop.getDesktop().browse(uri);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                    case "Shop":{
                        URI uri = null;
                        try {
                            uri = new URI("https://kw.atrishop.top/item?id=156");
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            Desktop.getDesktop().browse(uri);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            };
            count2 += FontManager.Icon.get(40).getStringWidth(buttons2.icon) + 15;
            buttons2.drawScreen(mouseX, mouseY);
        }
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
                        LayeringAnimation.play(new GuiAccountManager(this));
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
        buttons2.forEach(button -> {button.mouseClicked(mouseX, mouseY, mouseButton);});
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
    class Button2 {
        String name;
        String icon;
        public float x, y, width, height;
        public Runnable clickAction;
        private Animation hoverAnimation = new DecelerateAnimation(1000, 1);;

        public Button2(String name,String icon){
            this.name = name;
            this.icon = icon;
        }

        public void drawScreen(int mouseX, int mouseY) {
            boolean hovered = RenderUtil.isHovering(x + 11 - FontManager.Icon.get(40).getStringWidth(icon) / 2 + width / 2, y - 1, width, height, mouseX, mouseY);
            Color rectColor = new Color(35, 37, 43, 150);
            rectColor = ColorUtil.interpolateColorC(rectColor, ColorUtil.brighter(rectColor, 0.4f), this.hoverAnimation.getOutput().floatValue());
            hoverAnimation.setDirection(hovered ? Direction.BACKWARDS : Direction.FORWARDS);
            if (hovered){
                RoundedUtil.drawRound(x + 11 - FontManager.Icon.get(40).getStringWidth(icon) / 2 + width / 2, y - 1, width, height, 3, rectColor);
            }
            if (hovered) {
                FontManager.Semibold.get(20).drawCenteredString(name, x + 13 + width / 2, y - 15, rectColor.brighter().brighter().brighter().brighter().getRGB());
            }
            FontManager.Icon.get(40).drawString(icon,x + 13  ,y,new Color(-1).getRGB());
        }

        public void mouseClicked(int mouseX, int mouseY, int button) {
            boolean hovered = RenderUtil.isHovering(x + 11 - FontManager.Icon.get(40).getStringWidth(icon) / 2 + width / 2, y - 1, width, height, mouseX, mouseY);
            if (hovered) clickAction.run();
        }
    }
}
