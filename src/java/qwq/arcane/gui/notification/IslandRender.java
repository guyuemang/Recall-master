package qwq.arcane.gui.notification;

import net.minecraft.block.Block;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import qwq.arcane.Client;
import qwq.arcane.module.ClientApplication;
import qwq.arcane.module.Mine;
import qwq.arcane.module.impl.display.IsLand;
import qwq.arcane.module.impl.player.Stealer;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.module.impl.world.BlockFly;
import qwq.arcane.module.impl.world.Scaffold;
import qwq.arcane.utils.Instance;
import qwq.arcane.utils.animations.AnimationUtils;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.ContinualAnimation;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.player.ScaffoldUtil;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;

import java.awt.*;
import java.util.Deque;
import java.util.List;

/**
 * @Author: Guyuemang
 * 2025/5/23
 */
public class IslandRender implements Instance {
    public static IslandRender INSTANCE = new IslandRender();

    public ContinualAnimation animatedX = new ContinualAnimation();
    public ContinualAnimation animatedY = new ContinualAnimation();
    public ContinualAnimation animatedw = new ContinualAnimation();
    public ContinualAnimation animatedh = new ContinualAnimation();
    public float x, y,y2, width, height,radius;
    private ScaledResolution sr;

    public String title, description;

    public IslandRender() {
        this.sr = new ScaledResolution(mc);
        if (mc.theWorld == null) {
            resetDisplay();
        }
    }

    public void rendershader(ScaledResolution sr){
        this.sr = sr;
        if (Client.Instance.getModuleManager().getModule(Scaffold.class).getState() || Client.Instance.getModuleManager().getModule(BlockFly.class).getState()){
            drawBackgroundAuto(50);
            renderScaffold();
            return;
        }
        List<Notification> notifications = Client.Instance.getNotification().getNotifications();
        if (!notifications.isEmpty()) {
            int yOffset = 0;
            int totalHeight = (40 * (notifications.size()));
            drawBackgroundAuto(totalHeight);
            for (Notification notification : notifications) {
                renderNotification(notification, yOffset);
                yOffset += 40; // 根据实际需要调整这个值
            }
            return;
        }
        renderPersistentInfo();
    }
    public void render(ScaledResolution sr){
        this.sr = sr;
        if (Client.Instance.getModuleManager().getModule(Stealer.class).getState() && mc.thePlayer.openContainer instanceof ContainerChest){
            drawBackgroundAuto(50);
            renderstealer();
            return;
        }
        if (Client.Instance.getModuleManager().getModule(Scaffold.class).getState() || Client.Instance.getModuleManager().getModule(BlockFly.class).getState()){
            drawBackgroundAuto(50);
            renderScaffold();
            return;
        }
        List<Notification> notifications = Client.Instance.getNotification().getNotifications();
        if (!notifications.isEmpty()) {
            int yOffset = 0;
            int totalHeight = (40 * (notifications.size()));
            drawBackgroundAuto(totalHeight);
            for (Notification notification : notifications) {
                if (notifications.size() > 3){
                    notifications.remove(0);
                }
                renderNotification(notification, yOffset);
                yOffset += 40; // 根据实际需要调整这个值
            }
            return;
        }
        renderPersistentInfo();
    }

    private void renderstealer() {
        x = sr.getScaledWidth() / 2f;
        y = 30;
        width = 140;
        height = 25;
        radius = 11;
        runToXy(x, y);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        float itemWidth = 14;
        float y1 = 17.0F;
        float x1 = 0.7F;
        for (int i = 9; i < 36; ++i) {
            ItemStack slot = mc.thePlayer.inventory.getStackInSlot(i);
            RenderUtil.renderItemStack(slot, x - 70, y - 10, 0.80F);
            x += itemWidth;
            x += x1;
            if (i == 17) {
                y += y1 - 1;
                x -= itemWidth * 9.0F;
                x -= x1 * 8.5F;
            }

            if (i == 26) {
                y += y1 - 1;
                x -= itemWidth * 9.0F;
                x -= x1 * 9.0F;
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
    }

    private void renderScaffold() {
        x = sr.getScaledWidth() / 2f;
        y = 30;
        width = 180;
        height = 25;
        radius = 11;
        runToXy(x, y);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        int currentBlocks = ScaffoldUtil.getBlockCount();
        int maxBlocks = ScaffoldUtil.getBlockCount(); // 假设热键栏有9个槽位，每个最多64个
        float progress = (float) currentBlocks / maxBlocks;
          FontManager.Icon.get(60).drawString("B",x - 80,y - 5,-1);
        FontManager.Bold.get(22).drawString("Scaffold Block Count",x - 50,y - 6,-1);
        FontManager.Bold.get(22).drawString( "remainder: " + String.valueOf(ScaffoldUtil.getBlockCount()),x - 50,y + 7,-1);
        RoundedUtil.drawRound(x - 80, animatedY.getOutput() + ((y - animatedY.getOutput()) + 22),
                160, 5f, 2,InterFace.secondColor.get().darker());
        int progressBarWidth = (int) (160 * progress);
        RoundedUtil.drawRound(x - 80, animatedY.getOutput() + ((y - animatedY.getOutput()) + 22),
                progressBarWidth, 5f, 2, InterFace.color(1).darker());
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
    }

    private void renderNotification(Notification notification,int ys) {
        title = notification.getMessage();
        description = notification.getTitle();
        width = FontManager.Bold.get(18).getStringWidth(title) + 35 + 20;
        height = 34;
        radius = 11;
        x = sr.getScaledWidth() / 2f;
        y = 30;
        y2 = 30 + ys;
        runToXy(x, y);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        notification.animations = AnimationUtils.animate(notification.animations,notification.getType() == notification.getType().SUCCESS? 10 : 9,0.9f);
        float progress = Math.min(notification.getTimer().getTime2() / notification.getTime(), 1);
        RoundedUtil.drawRound(animatedX.getOutput() + 6, animatedY.getOutput() + ((y2 - animatedY.getOutput()) + 15),
                width - 12, 5f, 2,InterFace.secondColor.get().darker());
        RoundedUtil.drawRound(animatedX.getOutput() + 6, animatedY.getOutput() + ((y2 - animatedY.getOutput()) + 15),
                (width - 12) * progress, 5f, 2, InterFace.color(1).darker());
        if (notification.getType() == notification.getType().SUCCESS){
            RoundedUtil.drawRound(x - width / 2 + 5,y2 - 9,35,18,8,new Color(70,70,70,190));
            RenderUtil.drawCircleCGUI(x - width / 2 + 22 + notification.animations,y2,14,-1);
        }else {
            RoundedUtil.drawRound(x - width / 2 + 5,y2 - 9,35,18,8,new Color(70,70,70,190));
            RenderUtil.drawCircleCGUI(x - width / 2 + 22 - notification.animations,y2,14,-1);
        }
        FontManager.Bold.get(18).drawString(title,x - width / 2 + 46,y2 + 2,-1);
        FontManager.Bold.get(18).drawString(description,x - width / 2 + 46,y2 - 8,Client.Instance.getModuleManager().getModule(InterFace.class).color());
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
    }
    public static String getCurrentConnectionInfo() {
        if (mc.isSingleplayer()) {
            return "SinglePlayer";
        } else if (mc.getCurrentServerData() != null) {
            return mc.getCurrentServerData().serverIP;
        } else {
            return "null";
        }
    }
    private void renderPersistentInfo() {
        x = sr.getScaledWidth() / 2f;
        y = 20;
        String sb = " - " + ClientApplication.usernameField.getText() + " - " + (Client.Instance.getModuleManager().getModule(IsLand.class).island.get() ? getCurrentConnectionInfo() : " - " + "") + "§2"+ InterFace.getPing() + "ms" + "§f - §7" + Mine.getDebugFPS() + "fps";
        width = FontManager.Regular.get(20).getStringWidth(sb) + Bold.get(22).getStringWidth("Arcane") + Icon.get(46).getStringWidth("I") + 5;
        height = 10;
        radius = 10;
        runToXy(x, y);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        drawBackgroundAuto((int) ((y - animatedY.getOutput()) * 2 + 10));
        Icon.get(46).drawString("I", x - width / 2 + 6,y - 3,Client.Instance.getModuleManager().getModule(InterFace.class).color());
        Bold.get(22).drawString("Arcane", x - width / 2 + 20,y + 1,Client.Instance.getModuleManager().getModule(InterFace.class).color());
        Regular.get(20).drawString(sb, x + Bold.get(22).getStringWidth("Arcane") - width / 2 + 20,y + 1,new Color(-1).getRGB());
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
    }
    public float getRenderX(float x) {
        return x - width / 2;
    }

    public float getRenderY(float y) {
        return y - height / 2;
    }

    public void runToXy(float realX, float realY) {
        animatedX.animate(getRenderX(realX), 30);
        animatedY.animate(getRenderY(realY), 30);
    }
    public void drawBackgroundAuto(int renderHeight) {
        RenderUtil.scissor(animatedX.getOutput() - 1, animatedY.getOutput() - 1,
                ((x - animatedX.getOutput()) * 2) + 2, renderHeight + 2);
        RoundedUtil.drawRound(animatedX.getOutput(), animatedY.getOutput(),
                (x - animatedX.getOutput()) * 2,renderHeight, radius, new Color(1,1,1,190));
    }

    private void resetDisplay() {
        x = sr.getScaledWidth() / 2f;
        y = 20;
        width = 0;
        height = 0;
        title = "";
    }
}
