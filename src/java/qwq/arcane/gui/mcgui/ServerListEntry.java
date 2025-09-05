package qwq.arcane.gui.mcgui;

import com.google.common.base.Charsets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.fontrender.FontRenderer;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.utils.render.StencilUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import static qwq.arcane.utils.Instance.mc;

public class ServerListEntry {
    private static final Logger logger = LogManager.getLogger();
    private static final ThreadPoolExecutor field_148302_b = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());
    private static final ResourceLocation UNKNOWN_SERVER = new ResourceLocation("textures/misc/unknown_server.png");
    private static final ResourceLocation SERVER_SELECTION_BUTTONS = new ResourceLocation("textures/gui/server_selection.png");
    private final ServerData server;
    private final ResourceLocation serverIcon;
    private String field_148299_g;
    private DynamicTexture field_148305_h;

    int x, y, width, height;

    GuiMultiplayer owner;

    protected ServerListEntry(GuiMultiplayer multiplayer, ServerData p_i45048_2_) {
        this.owner = multiplayer;
        this.server = p_i45048_2_;
        this.serverIcon = new ResourceLocation("servers/" + p_i45048_2_.serverIP + "/icon");
        this.field_148305_h = (DynamicTexture) mc.getTextureManager().getTexture(this.serverIcon);
    }
    public void tryPing() {
        if (!server.field_78841_f) {
            server.field_78841_f = true;
            server.pingToServer = -2L;
            server.serverMOTD = "";
            server.populationInfo = "";

            field_148302_b.submit(() -> {
                try {
                    owner.getOldServerPinger().ping(server);
                } catch (UnknownHostException e) {
                    server.pingToServer = -1L;
                    server.serverMOTD = EnumChatFormatting.DARK_RED + "Can't resolve hostname";
                } catch (Exception e) {
                    server.pingToServer = -1L;
                    server.serverMOTD = EnumChatFormatting.DARK_RED + "Can't connect to server.";
                }
            });
        }
    }
    public void drawEntry(int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
        this.x = x;
        this.y = y;
        this.width = listWidth;
        this.height = slotHeight;

        if (!server.field_78841_f) {
            server.field_78841_f = true;
            server.pingToServer = -2L;
            server.serverMOTD = "";
            server.populationInfo = "";

            field_148302_b.submit(() -> {
                try {
                    owner.getOldServerPinger().ping(server);
                } catch (UnknownHostException e) {
                    server.pingToServer = -1L;
                    server.serverMOTD = EnumChatFormatting.DARK_RED + "Can't resolve hostname";
                } catch (Exception e) {
                    server.pingToServer = -1L;
                    server.serverMOTD = EnumChatFormatting.DARK_RED + "Can't connect to server.";
                }
            });
        }

        FontRenderer title = FontManager.Bold.get(18);
        FontRenderer text = FontManager.Bold.get(16);

        title.drawString(server.serverName, x + 32 + 3 + 12, y + 1 + 10, -1);

        List<String> lines = mc.fontRendererObj.listFormattedStringToWidth(server.serverMOTD, listWidth - 48 - 2);
        for (int i = 0; i < Math.min(lines.size(), 2); i++) {
            String line = lines.get(i);
            float lineWidth = text.getStringWidth(line);
            float drawX;

            if (line.contains("Can't resolve hostname") || line.contains("Can't connect to server")) {
                drawX = x + 32 + 3 + 12;
            } else {
                drawX = x + (listWidth - lineWidth) / 2.0f;
            }

            text.drawString(line, drawX, y + 12 + 12 + mc.fontRendererObj.FONT_HEIGHT * i, -1);
        }

        boolean incompatible = server.version != 47;
        String info = incompatible ? EnumChatFormatting.DARK_RED + server.gameVersion : server.populationInfo;
        int infoWidth = text.getStringWidth(info);
        text.drawString(info, x + listWidth - infoWidth - 4, y + 1 + 5, -1);

        GlStateManager.color(1f, 1f, 1f, 1f);
        if (server.getBase64EncodedIconData() != null && !server.getBase64EncodedIconData().equals(field_148299_g)) {
            field_148299_g = server.getBase64EncodedIconData();
            prepareServerIcon();
            owner.getSavedServerList().saveServerList();
        }

        RoundedUtil.drawRound(x + 3, y + 4, 38, 38, 6, getServerIconColor());

        if (field_148305_h != null) {
            drawTextureAt(x + 8, y + 10, serverIcon);
        } else {
            drawTextureAt(x + 8, y + 10, UNKNOWN_SERVER);
        }

        if (mc.gameSettings.touchscreen || isSelected) {
            mc.getTextureManager().bindTexture(SERVER_SELECTION_BUTTONS);
            Gui.drawRect(x, y, x + 32, y + 32, 0xA0000000);
            GlStateManager.color(1f, 1f, 1f, 1f);

            int k1 = mouseX - x;
            if (func_178013_b()) {
                if (k1 < 32 && k1 > 16) {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 0f, 32f, 32, 32, 256f, 256f);
                } else {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, 32, 32, 256f, 256f);
                }
            }
        }
    }

    private Color getServerIconColor() {
        if (this.field_148305_h == null) {
            return new Color(255, 255, 255, 50);
        }

        int[] textureData = this.field_148305_h.getTextureData();
        if (textureData == null || textureData.length == 0) {
            return new Color(255, 255, 255, 125);
        }

        int r = 0, g = 0, b = 0, count = 0;
        int step = Math.max(1, textureData.length / 16);

        for (int i = 0; i < textureData.length; i += step) {
            int color = textureData[i];
            r += (color >> 16) & 0xFF;
            g += (color >> 8) & 0xFF;
            b += color & 0xFF;
            count++;
        }

        if (count > 0) {
            r /= count;
            g /= count;
            b /= count;
            return new Color(r, g, b, 125);
        }

        return new Color(255, 255, 255, 125);
    }

    protected void drawTextureAt(int x, int y, ResourceLocation resourceLocation) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();

        StencilUtils.initStencilToWrite();

        RoundedUtil.drawRound((float) x - 2, (float) y - 2, 32, 32, 13, Color.WHITE);

        StencilUtils.readStencilBuffer(1);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        mc.getTextureManager().bindTexture(resourceLocation);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 28, 28, 28.0F, 28.0F);

        StencilUtils.uninitStencilBuffer();
        GlStateManager.disableBlend();
    }

    private boolean func_178013_b() {
        return true;
    }

    private void prepareServerIcon() {
        if (this.server.getBase64EncodedIconData() == null) {
            mc.getTextureManager().deleteTexture(this.serverIcon);
            this.field_148305_h = null;
        } else {
            ByteBuf bytebuf = Unpooled.copiedBuffer(this.server.getBase64EncodedIconData(), Charsets.UTF_8);
            ByteBuf bytebuf1 = Base64.decode(bytebuf);

            BufferedImage bufferedimage;
            label80:

            {
                try {
                    bufferedimage = TextureUtil.readBufferedImage(new ByteBufInputStream(bytebuf1));
                    Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
                    Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
                    break label80;
                } catch (Throwable throwable) {
                    logger.error("Invalid icon for server {} ({})", this.server.serverName, this.server.serverIP, throwable);
                    this.server.setBase64EncodedIconData(null);
                } finally {
                    bytebuf.release();
                    bytebuf1.release();
                }

                return;
            }

            if (this.field_148305_h == null) {
                this.field_148305_h = new DynamicTexture(bufferedimage.getWidth(), bufferedimage.getHeight());
                mc.getTextureManager().loadTexture(this.serverIcon, this.field_148305_h);
            }

            bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), this.field_148305_h.getTextureData(), 0, bufferedimage.getWidth());
            this.field_148305_h.updateDynamicTexture();
        }

    }

    public ServerData getServerData() {
        return this.server;
    }
}
