package qwq.arcane.gui;


import net.minecraft.client.gui.*;
import qwq.arcane.Client;
import qwq.arcane.gui.alt.GuiAccountManager;
import qwq.arcane.gui.mcgui.GuiMultiplayer;
import qwq.arcane.module.ClientApplication;
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
            new Button("Shut down","R")
    );
    List<Button2> buttons2 = Arrays.asList(
            new Button2("Discord","V"),
            new Button2("Kook","W"),
            new Button2("Bilibili","X"),
            new Button2("YouTube","Y"),
            new Button2("Shop","Z")
    );
    private Animation fadeInAnimation = new DecelerateAnimation(3000, 1).setDirection(Direction.FORWARDS);
    private static Animation progress4Anim;
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
        qwq.arcane.utils.render.shader.MainMenu.drawBackground(width, height, mouseX, mouseY);
        FontManager.Bold.get(80).drawStringDynamic(Client.name, sr.getScaledWidth() / 2 - FontManager.Bold.get(80).getStringWidth(Client.name) / 2, sr.getScaledHeight() / 2 - 110, InterFace.mainColor.get().getRGB(), InterFace.secondColor.get().getRGB());
        FontManager.Bold.get(22).drawStringDynamic(Client.version, sr.getScaledWidth() / 2 + 38 - FontManager.Bold.get(22).getStringWidth(Client.version) / 2, sr.getScaledHeight() / 2 - 110, InterFace.mainColor.get().getRGB(), InterFace.secondColor.get().getRGB());
//        RoundedUtil.drawRound(sr.getScaledWidth() / 2 - 130, sr.getScaledHeight() - 45, 260, 40, 6, new Color(0, 0, 0, 120));
//        FontManager.Bold.get(18).drawCenteredString(Client.name + " " + Client.version, sr.getScaledWidth() / 2, sr.getScaledHeight() - 38, new Color(255, 255, 255).getRGB());
//        FontManager.Bold.get(18).drawCenteredString("OptiFine_1.8.9_HD_U_M6_pre2", sr.getScaledWidth() / 2, sr.getScaledHeight() - 28, new Color(255, 255, 255).getRGB());
//        FontManager.Bold.get(18).drawCenteredString("Made by Guyuemang", sr.getScaledWidth() / 2, sr.getScaledHeight() - 18, new Color(255, 255, 255).getRGB());
//        //按钮专属背景
        RoundedUtil.drawRound(sr.getScaledWidth() / 2 - 80, sr.getScaledHeight() / 2 - 55, 160, 175, 14, new Color(255, 255, 255, 120));
        RoundedUtil.drawRound(width / 2 - 90, height - 120, 180, 30, 11, new Color(255, 255, 255, 120));
        float count = 0;
        for (Button button : buttons) {
            button.x = sr.getScaledWidth() / 2;
            button.y = sr.getScaledHeight() / 2 - 40 + count;
            button.width = 160;
            button.height = 35;
            button.clickAction = () -> {
                switch (button.name) {
                    case "Single Player": {
                        LayeringAnimation.play(new GuiSelectWorld(this));
                    }
                    break;
                    case "Multi Player": {
                        LayeringAnimation.play(new GuiMultiplayer());
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
            count += 35;
            button.drawScreen(mouseX, mouseY);
        }
        float count2 = 0;
        for (Button2 buttons2 : buttons2) {
            buttons2.x = width / 2 - 90 + count2;
            buttons2.y = height - 112;
            buttons2.width = FontManager.Icon.get(40).getStringWidth(buttons2.icon);
            buttons2.height = 15;
            buttons2.clickAction = () -> {
                switch (buttons2.name) {
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
                    case "Kook": {
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
                    case "Bilibili": {
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
                    case "YouTube": {
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
                    case "Shop": {
                        URI uri = null;
                        try {
                            uri = new URI("https://guyuem.xyz/");
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

        float progress = fadeInAnimation.getOutput().floatValue();
        alpha = (int) (255 * (1 - progress)); // 从完全不透明到完全透明

        RenderUtil.drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), new Color(0, 0, 0, alpha).getRGB());
        if (fadeInAnimation.getOutput() <= 0.9) {
            FontManager.Bold.get(60).drawString("Arcane " + Client.version, sr.getScaledWidth() / 2 - FontManager.Bold.get(60).getStringWidth("Arcane " + Client.version) / 2, sr.getScaledHeight() / 2 - 20, ColorUtil.applyOpacity(InterFace.color(1).getRGB(),fadeInAnimation.getOutput().floatValue()));
            FontManager.Bold.get(30).drawString("Welcome!! " + ClientApplication.usernameField.getText(), sr.getScaledWidth() / 2 - FontManager.Bold.get(30).getStringWidth("Welcome!! " + ClientApplication.usernameField.getText()) / 2, sr.getScaledHeight() / 2 + 20, ColorUtil.applyOpacity(InterFace.color(17).getRGB(),fadeInAnimation.getOutput().floatValue()));
        }
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
            boolean hovered = RenderUtil.isHovering(x - 80,y - 15,width,35, mouseX, mouseY);
            Color rectColor = new Color(200, 200, 200, 100);
            rectColor = ColorUtil.interpolateColorC(rectColor, ColorUtil.brighter(rectColor, 0.4f),this.hoverAnimation.getOutput().floatValue());
            hoverAnimation.setDirection(hovered ? Direction.BACKWARDS : Direction.FORWARDS);
            if (hovered) {
                RoundedUtil.drawRound(x - 80, y - 15, width, 35, 14, rectColor);
            }
            FontManager.Bold.get(20).drawCenteredString(name,x + 5,y,InterFace.color(1).getRGB());
            FontManager.Icon.get(30).drawCenteredString(icon,x - 5 - FontManager.Bold.get(20).getStringWidth(name) / 2  ,y,InterFace.color(1).getRGB());
        }

        public void mouseClicked(int mouseX, int mouseY, int button) {
            boolean hovered = RenderUtil.isHovering(x - 80,y - 15,width,35, mouseX, mouseY);
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
            Color rectColor = new Color(190, 190, 190, 150);
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
