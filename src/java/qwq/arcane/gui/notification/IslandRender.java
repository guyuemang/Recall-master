package qwq.arcane.gui.notification;

import net.minecraft.block.Block;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
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

import static net.minecraft.client.gui.GuiPlayerTabOverlay.field_175252_a;

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
        if (this.mc.gameSettings.keyBindPlayerList.isKeyDown() && (!this.mc.isIntegratedServerRunning() || this.mc.thePlayer.sendQueue.getPlayerInfoMap().size() > 1)){
            drawBackgroundAuto(50 + mc.theWorld.playerEntities.size() * 15);
            renderList(true);
            return;
        }
        if (mc.thePlayer.openContainer instanceof ContainerChest){
            renderstealer(true);
            drawBackgroundAuto(50);
            return;
        }
        if (Client.Instance.getModuleManager().getModule(Scaffold.class).getState() || Client.Instance.getModuleManager().getModule(BlockFly.class).getState()){
            if (ScaffoldUtil.getBlockSlot() != -1){
                drawBackgroundAuto(50);
                renderScaffold();
                return;
            }
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
    public void render(ScaledResolution sr){
        this.sr = sr;
        if (this.mc.gameSettings.keyBindPlayerList.isKeyDown() && (!this.mc.isIntegratedServerRunning() || this.mc.thePlayer.sendQueue.getPlayerInfoMap().size() > 1)){
            drawBackgroundAuto(50 + mc.theWorld.playerEntities.size() * 15);
            renderList(true);
            return;
        }

        if (mc.thePlayer.openContainer instanceof ContainerChest){
            drawBackgroundAuto(50);
            renderstealer(false);
            return;
        }
        if (Client.Instance.getModuleManager().getModule(Scaffold.class).getState() || Client.Instance.getModuleManager().getModule(BlockFly.class).getState()){
            if (ScaffoldUtil.getBlockSlot() != -1){
                drawBackgroundAuto(50);
                renderScaffold();
                return;
            }
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

    private void renderList(boolean b) {
        x = sr.getScaledWidth() / 2f;
        y = 30;
        width = 180;
        height = 30;
        radius = 11;
        int y1 = 25;
        runToXy(x, y);
        int count = 0;
        int counts = 0;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        if (b) {
            Scoreboard scoreboard = this.mc.theWorld.getScoreboard();
            ScoreObjective scoreobjective = null;
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(this.mc.thePlayer.getName());
            if (scoreplayerteam != null) {
                int i1 = scoreplayerteam.getChatFormat().getColorIndex();

                if (i1 >= 0) {
                    scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + i1);
                }
            }
            ScoreObjective scoreobjective1 = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);
            scoreobjective1 = scoreboard.getObjectiveInDisplaySlot(0);
            NetHandlerPlayClient nethandlerplayclient = this.mc.thePlayer.sendQueue;
            List<NetworkPlayerInfo> list = field_175252_a.<NetworkPlayerInfo>sortedCopy(nethandlerplayclient.getPlayerInfoMap());
            for (EntityPlayer entityPlayer : mc.theWorld.playerEntities) {
                mc.fontRendererObj.drawString(entityPlayer.getName() + " " + scoreboard.getValueFromObjective(entityPlayer.getGameProfile().getName(), scoreobjective1).getScorePoints() + EnumChatFormatting.GREEN + " ms", (int) (x - width / 2 + 10), y1 + count, -1);

                count += 15;
                counts += 1;
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
    }
    private void renderstealer(boolean shader) {
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
        if (!shader) {
            for (int i = 9; i < 36; ++i) {
                ItemStack slot = mc.thePlayer.inventory.getStackInSlot(i);
                RenderUtil.renderItemStack(slot, x - 65, y - 10, 0.80F);
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
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
    }

    private final ContinualAnimation animation = new ContinualAnimation();
    private void renderScaffold() {
        x = sr.getScaledWidth() / 2f;
        y = 30;
        width = 180;
        height = 25;
        radius = 11;
        runToXy(x, y);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

          FontManager.Icon.get(60).drawString("B",x - 80,y - 5,-1);
        FontManager.Bold.get(22).drawString("Scaffold Block Count",x - 50,y - 6,-1);
        FontManager.Bold.get(22).drawString( "remainder: " + String.valueOf(ScaffoldUtil.getBlockCount()),x - 50,y + 7,-1);
        float percentage = Math.min(1, ScaffoldUtil.getBlockCount() / 128.0F);
        animation.animate(percentage, 18);
        float percentageWidth = animation.getOutput();
        RoundedUtil.drawRound(x - 80, animatedY.getOutput() + ((y - animatedY.getOutput()) + 22),
                160, 5f, 2,InterFace.secondColor.get().darker());
        int progressBarWidth = (int) (percentageWidth * 160);
        RoundedUtil.drawRound(x - 80, animatedY.getOutput() + ((y - animatedY.getOutput()) + 22),
                progressBarWidth, 5f, 2, InterFace.color(1).darker());
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
    }

    private void renderNotification(Notification notification,int ys) {
        title = notification.getMessage();
        description = notification.getTitle();
        width = 200;
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
                width - 12, 5f, 2,new Color(255,255,255,190));
        RoundedUtil.drawGradientHorizontal(animatedX.getOutput() + 6, animatedY.getOutput() + ((y2 - animatedY.getOutput()) + 15),
                (width - 12) * progress, 5f, 2, InterFace.color(1), InterFace.color(10));
        if (notification.getType() == notification.getType().SUCCESS){
            RoundedUtil.drawRound(x - width / 2 + 5,y2 - 9,35,18,8,new Color(255,255,255,100));
            RenderUtil.drawCircleCGUI(x - width / 2 + 22 + notification.animations,y2,14,-1);
        }else if (notification.getType() == notification.getType().ERROR){
            RoundedUtil.drawRound(x - width / 2 + 5,y2 - 9,35,18,8,new Color(255,255,255,100));
            RenderUtil.drawCircleCGUI(x - width / 2 + 22 - notification.animations,y2,14,-1);
        }else {
            RoundedUtil.drawRound(x - width / 2 + 13,y2 - 9,20,18,8,new Color(255,255,255,100));
            FontManager.Icon.get(40).drawString("N",x - width / 2 + 14,y2 - 5.5f,new Color(250, 102, 102, 226).getRGB());
        }
        FontManager.Bold.get(18).drawString(title,x - width / 2 + 46,y2 + 2,-1);
        FontManager.Bold.get(18).drawString(description,x - width / 2 + 46,y2 - 8,notification.getType().getColor().brighter().getRGB());
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
        String sb = " - " + ClientApplication.usernameField.getText() + (Client.Instance.getModuleManager().getModule(IsLand.class).island.get() ? " - " : "") + (Client.Instance.getModuleManager().getModule(IsLand.class).island.get() ? getCurrentConnectionInfo() : "" + "") + " - §2"+ InterFace.getPing() + "ms" + "§f - §8" + Mine.getDebugFPS() + "fps";
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
                (x - animatedX.getOutput()) * 2,renderHeight, radius, ColorUtil.applyOpacity(IsLand.colorValue.get(),1));
    }

    private void resetDisplay() {
        x = sr.getScaledWidth() / 2f;
        y = 20;
        width = 0;
        height = 0;
        title = "";
    }
}
