package qwq.arcane.module.impl.display;


import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketReceiveEvent;
import qwq.arcane.event.impl.events.player.AttackEvent;
import qwq.arcane.event.impl.events.render.Shader2DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.ModuleWidget;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.value.impl.ModeValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.text.DecimalFormat;

/**
 * @Author：Guyuemang
 * @Date：2025/6/2 11:22
 */

public class Session extends ModuleWidget {
    public ModeValue modeValue = new ModeValue("Mode", "Custom",new String[]{"Custom","Solitude","Normal"});

    public Session() {
        super("Session",Category.Display);
        this.width = 100;
        this.height = 50;
    }
    public int lost = 0, killed = 0, won = 0;
    @EventTarget
    public void onAttackEvent(AttackEvent event){
        if (event.getTargetEntity().isDead){
            ++this.killed;
        }
    }
    @EventTarget
    public void onPacket(PacketReceiveEvent event) {
        Packet<?> packet = event.getPacket();

        if (packet instanceof S02PacketChat) {
            S02PacketChat s02 = (S02PacketChat) event.getPacket();
            String xd = s02.getChatComponent().getUnformattedText();
            if (xd.contains("was killed by " + mc.thePlayer.getName())) {
                ++this.killed;
            }

            if (xd.contains("You Died! Want to play again?")) {
                ++lost;
            }
        }

        if (packet instanceof S45PacketTitle && ((S45PacketTitle) packet).getType().equals(S45PacketTitle.Type.TITLE)) {
            String unformattedText = ((S45PacketTitle) packet).getMessage().getUnformattedText();
            if (unformattedText.contains("VICTORY!")) {
                ++this.won;
            }
            if (unformattedText.contains("GAME OVER!") || unformattedText.contains("DEFEAT!") || unformattedText.contains("YOU DIED!")) {
                ++this.lost;
            }
        }
    }

    @Override
    public void onShader(Shader2DEvent event) {
        int x = (int) renderX;
        int y = (int) renderY;
        switch (modeValue.getValue()) {
            case "Solitude":
                RoundedUtil.drawRound(x, y, 48 + Bold.get(18).getStringWidth("Played Time:" + RenderUtil.sessionTime()), 58, 10, new Color(0, 0, 0, 255));
                break;
            case "Normal":
                RoundedUtil.drawRound(x, y, 48 + Bold.get(18).getStringWidth("Played Time:" + RenderUtil.sessionTime()), 65, INTERFACE.radius.get().intValue(), new Color(0, 0, 0, 255));
                break;
            case "Custom":
                RoundedUtil.drawRound(x, y, 130, 56,6, new Color(255, 255, 255, 255));
                break;
        }
    }

    private final DecimalFormat bpsFormat = new DecimalFormat("0.00");
    @Override
    public void render() {
        int x = (int) renderX;
        int y = (int) renderY;
        switch (modeValue.getValue()) {
            case "Solitude":
                RoundedUtil.drawRound(x, y, 48 + Bold.get(18).getStringWidth("Played Time:" + RenderUtil.sessionTime()), 58, 10, new Color(0, 0, 0, 190));
                RenderUtil.renderPlayer2D(mc.thePlayer, x + 5, y + 17, 35, 12, -1);
                FontManager.Bold.get(20).drawString("Session Info", x + 5, y + 5, -1);
                FontManager.Bold.get(18).drawString("Played Time:" + RenderUtil.sessionTime(), x + 44, y + 20, Color.GRAY.getRGB());
                FontManager.Bold.get(18).drawString("kill:" + killed, x + 44, y + 32, -1);
                FontManager.Bold.get(18).drawString("win:" + won, x + 44, y + 44, -1);
                break;
            case "Custom":
                RoundedUtil.drawRound(x, y, 130, 56,6, new Color(255, 255, 255, 80));
                RenderUtil.startGlScissor(x - 2, y + 42, 134, 20);
                RoundedUtil.drawRound(x, y + 34, 130, 22,6, new Color(255, 255, 255, 100));
                RenderUtil.stopGlScissor();
                Bold.get(18).drawString("Played Time:" + RenderUtil.sessionTime(), x + 4, y + 62 - 16, -1);
                RenderUtil.renderPlayer2D(mc.thePlayer, x + 5, y + 4, 35, 8   , -1);
                Bold.get(18).drawString("kill:" + killed, x + 44, y + 10, -1);
                Bold.get(18).drawString("FPS:" + mc.getDebugFPS(), x + 79, y + 10, -1);
                Bold.get(18).drawString("BPS:" + bpsFormat.format(INTERFACE.getBPS()), x + 44, y + 25, -1);
                Bold.get(18).drawString("win:" + won, x + 44 + Bold.get(18).getStringWidth("BPS:10.0.0") + 4, y + 25, -1);
                break;
            case "Normal":
                RoundedUtil.drawRound(x, y, 48 + Bold.get(18).getStringWidth("Played Time:" + RenderUtil.sessionTime()), 65, INTERFACE.radius.get().intValue(), new Color(0, 0, 0, 89));
                RenderUtil.startGlScissor(x - 2, y - 1, 190, 20);
                RoundedUtil.drawRound(x, y, 48 + Bold.get(18).getStringWidth("Played Time:" + RenderUtil.sessionTime()), 30, INTERFACE.radius.get().intValue(), ColorUtil.applyOpacity(new Color(setting.colors(1)), (float) 0.3f));
                RenderUtil.stopGlScissor();
                RenderUtil.renderPlayer2D(mc.thePlayer, x + 5, y + 25, 35, 14   , -1);
                FontManager.Bold.get(20).drawString("Session", x + 5, y + 5, -1);
                FontManager.Bold.get(18).drawString("Played Time:" + RenderUtil.sessionTime(), x + 44, y + 27, -1);
                FontManager.Bold.get(18).drawString("kill:" + killed, x + 44, y + 39, -1);
                FontManager.Bold.get(18).drawString("win:" + won, x + 44, y + 51, -1);
                break;
        }
        this.width = 48 + Bold.get(18).getStringWidth("Played Time:" + RenderUtil.sessionTime());
        this.height = 65;
    }

    @Override
    public boolean shouldRender() {
        return getState() && setting.getState();
    }
}
