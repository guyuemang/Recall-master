package qwq.arcane.gui.mcgui;

import com.google.common.collect.Lists;
import de.florianmichael.viamcp.gui.GuiProtocolSelector;
import lombok.Getter;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.network.OldServerPinger;
import net.minecraft.client.resources.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.utils.render.shader.MainMenu;
import qwq.arcane.utils.time.TimerUtil;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class GuiMultiplayer extends GuiScreen implements GuiYesNoCallback {
    private static final Logger logger = LogManager.getLogger();
    private ServerData selectedServer;
    @Getter
    private ServerList savedServerList;
    private boolean deletingServer;
    private boolean addingServer;
    private boolean editingServer;
    private boolean directConnect;
    private LanServerDetector.LanServerList lanServerList;
    private LanServerDetector.ThreadLanServerFind lanServerDetector;
    @Getter
    private final OldServerPinger oldServerPinger = new OldServerPinger();
    private final List<ServerListEntry> serverListDisplay = Lists.newArrayList();
    private final List<ServerListEntry> serverListInternet = Lists.newArrayList();
    private final TimerUtil timer = new TimerUtil();
    private final ScrollContainer scrollContainer = new ScrollContainer();
    private int selectedIndex = -1;

    private final GuiButton join = new GuiButton(I18n.format("selectServer.select"), () -> {
        if (selectedServer == null) return;
        connectToSelected();
    }, new Color(200, 200, 200, 120), new Color(InterFace.mainColor.get().darker().getRGB()));

    private final GuiButton connect = new GuiButton(I18n.format("selectServer.direct"), () -> {
        this.directConnect = true;
        this.mc.displayGuiScreen(new GuiScreenServerList(this, this.selectedServer = new ServerData(I18n.format("selectServer.defaultName"), "", false)));
    }, new Color(200, 200, 200, 120), new Color(InterFace.mainColor.get().darker().getRGB()));

    private final GuiButton add = new GuiButton(I18n.format("selectServer.add"), () -> {
        this.addingServer = true;
        this.mc.displayGuiScreen(new GuiScreenAddServer(this, this.selectedServer = new ServerData(I18n.format("selectServer.defaultName"), "", false)));
    }, new Color(200, 200, 200, 120), new Color(InterFace.mainColor.get().darker().getRGB()));

    private final GuiButton edit = new GuiButton(I18n.format("selectServer.edit"), () -> {
        if (selectedServer == null) return;
        this.editingServer = true;
        ServerData serverdata = selectedServer;
        this.selectedServer = new ServerData(serverdata.serverName, serverdata.serverIP, false);
        this.selectedServer.copyFrom(serverdata);
        mc.displayGuiScreen(new GuiScreenAddServer(this, this.selectedServer));
    }, new Color(200, 200, 200, 120), new Color(InterFace.mainColor.get().darker().getRGB()));

    private final GuiButton remove = new GuiButton(I18n.format("selectServer.delete"), () -> {
        if (selectedServer == null) return;
        this.deletingServer = true;
        String s4 = selectedServer.serverName;
        if (s4 != null) {
            String s = I18n.format("selectServer.deleteQuestion");
            String s1 = "'" + s4 + "' " + I18n.format("selectServer.deleteWarning");
            String s2 = I18n.format("selectServer.deleteButton");
            String s3 = I18n.format("gui.cancel");
            GuiYesNo guiyesno = new GuiYesNo(this, s, s1, s2, s3, getSelectedServerIndex());
            this.mc.displayGuiScreen(guiyesno);
        }
    }, new Color(200, 200, 200, 120), new Color(InterFace.mainColor.get().darker().getRGB()));

    private final GuiButton refresh = new GuiButton(I18n.format("selectServer.refresh"), () -> mc.displayGuiScreen(new GuiMultiplayer()), new Color(200, 200, 200, 120), new Color(InterFace.mainColor.get().darker().getRGB()));
    private final GuiButton back = new GuiButton(I18n.format("gui.cancel"), () -> mc.displayGuiScreen(new qwq.arcane.gui.MainMenu()), new Color(200, 200, 200, 120), new Color(InterFace.mainColor.get().darker().getRGB()));
    private final GuiButton viaversion = new GuiButton("Via Version", () -> mc.displayGuiScreen(new GuiProtocolSelector(this)), new Color(200, 200, 200, 120), new Color(InterFace.mainColor.get().darker().getRGB()));

    @Override
    public void initGui() {
        super.initGui();
        this.savedServerList = new ServerList(mc);
        this.savedServerList.loadServerList();
        this.lanServerList = new LanServerDetector.LanServerList();
        try {
            this.lanServerDetector = new LanServerDetector.ThreadLanServerFind(this.lanServerList);
            this.lanServerDetector.start();
        } catch (Exception exception) {
            logger.warn("Unable to start LAN server detection: {}", exception.getMessage());
        }
        refreshServerListDisplay();
        selectedIndex = -1;
        selectedServer = null;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        MainMenu.drawBackground(width, height, mouseX, mouseY);
        FontManager.Bold.get(34).drawCenteredString(I18n.format("selectServer.title"), width / 2f, 20, -1);

            RoundedUtil.drawRound((float) ((width - 360) / 2f), 54f, 360, height - 120, 8, new Color(200, 200, 200, 120));

            RenderUtil.startGlScissor((int) ((width - 360) / 2), 54, 380, height - 120);
            scrollContainer.draw((float) ((width - 390) / 2f), 60, 380, height - 128, mouseX, mouseY, () -> {
                float y = 65 + scrollContainer.getScroll();

                for (int i = 0; i < serverListDisplay.size(); i++) {
                    ServerListEntry server = serverListDisplay.get(i);
                    if (server.getServerData() == null) continue;

                    RoundedUtil.drawRound((float) ((width - 340) / 2f), y, 340, 48, 6, new Color(190, 190, 190, 80));

                    if (RenderUtil.isHovering((float) ((width - 340) / 2f), y, 340, 50, mouseX, mouseY)) {
                        RoundedUtil.drawRound((float) ((width - 340) / 2f), y, 340, 48, 6, new Color(0, 0, 0, 90));
                    }

                    if (selectedIndex == i) {
                        RoundedUtil.drawRound((float) ((width - 340) / 2f), y, 340, 48, 6, new Color(255, 255, 255, 50));
                    }

                    server.drawEntry((int) ((double) (width - 340) / 2), (int) y, 340, 48, mouseX, mouseY, false);
                    y += 58;
                }

                scrollContainer.setHeight(y - 50 - scrollContainer.getScroll());
            });

            RenderUtil.stopGlScissor();

            float spacing = 4f;
            float buttonHeight = 23.5f;

            int topCount = 3;
            float topTotalWidth = 360f;
            float topButtonWidth = (topTotalWidth - spacing * (topCount - 1)) / topCount;
            float topStartX = (float) ((width - topTotalWidth) / 2f);
            float topY = height - 58f;

            int bottomCount = 4;
            float bottomTotalWidth = 360f;
            float bottomButtonWidth = (bottomTotalWidth - spacing * (bottomCount - 1)) / bottomCount;
            float bottomStartX = (float) ((width - bottomTotalWidth) / 2f);
            float bottomY = height - 31;

            join.render(topStartX, topY, topButtonWidth, buttonHeight, mouseX, mouseY);
            connect.render(topStartX + (topButtonWidth + spacing) * 1, topY, topButtonWidth, buttonHeight, mouseX, mouseY);
            add.render(topStartX + (topButtonWidth + spacing) * 2, topY, topButtonWidth, buttonHeight, mouseX, mouseY);

            edit.render(bottomStartX, bottomY, bottomButtonWidth, buttonHeight, mouseX, mouseY);
            remove.render(bottomStartX + (bottomButtonWidth + spacing) * 1, bottomY, bottomButtonWidth, buttonHeight, mouseX, mouseY);
            refresh.render(bottomStartX + (bottomButtonWidth + spacing) * 2, bottomY, bottomButtonWidth, buttonHeight, mouseX, mouseY);
            back.render(bottomStartX + (bottomButtonWidth + spacing) * 3, bottomY, bottomButtonWidth, buttonHeight, mouseX, mouseY);

            viaversion.render((float) (8), 8, bottomButtonWidth + 20, buttonHeight, mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        join.mouseClick(mouseX, mouseY, mouseButton);
        connect.mouseClick(mouseX, mouseY, mouseButton);
        add.mouseClick(mouseX, mouseY, mouseButton);
        edit.mouseClick(mouseX, mouseY, mouseButton);
        remove.mouseClick(mouseX, mouseY, mouseButton);
        refresh.mouseClick(mouseX, mouseY, mouseButton);
        back.mouseClick(mouseX, mouseY, mouseButton);
        viaversion.mouseClick(mouseX, mouseY, mouseButton);
        if (RenderUtil.isHovering((float) ((width - 360) / 2f), 54f, 360, height - 120,mouseX,mouseY)) {
            float y = 70 + scrollContainer.getScroll();
            for (int i = 0; i < serverListDisplay.size(); i++) {
                ServerListEntry server = serverListDisplay.get(i);
                if (server.getServerData() == null) continue;
                if (RenderUtil.isHovering((width - 340) / 2f, y, 340, 54, mouseX, mouseY)) {
                    if (selectedIndex != i) {
                        selectServer(i);
                        timer.reset();
                    } else {
                        if (timer.hasTimeElapsed(200)) {
                            selectServer(-1);
                        } else {
                            connectToServer(selectedServer);
                        }
                    }
                }
                y += 58;
            }
        }
    }

    public void selectServer(int index) {
        this.selectedIndex = index;
        this.selectedServer = (index >= 0 && index < serverListDisplay.size()) ?
                serverListDisplay.get(index).getServerData() : null;
        updateButtonStates();
    }

    private void updateButtonStates() {
        join.enabled = false;
        edit.enabled = false;
        remove.enabled = false;
        if (selectedServer != null) {
            join.enabled = true;
            boolean isSavedServer = false;
            for (int i = 0; i < savedServerList.countServers(); i++) {
                if (savedServerList.getServerData(i) == selectedServer) {
                    isSavedServer = true;
                    break;
                }
            }
            if (isSavedServer) {
                edit.enabled = true;
                remove.enabled = true;
            }
        }
    }

    private void refreshServerListDisplay() {
        serverListInternet.clear();
        for (int i = 0; i < this.savedServerList.countServers(); i++) {
            this.serverListInternet.add(new ServerListEntry(this, this.savedServerList.getServerData(i)));
        }
        serverListDisplay.clear();
        serverListDisplay.addAll(serverListInternet);
    }

    private int getSelectedServerIndex() {
        if (selectedServer == null) return -1;
        for (int i = 0; i < savedServerList.countServers(); i++) {
            if (savedServerList.getServerData(i) == selectedServer) {
                return i;
            }
        }
        return -1;
    }

    private int getServerIndexInDisplayList(ServerData server) {
        for (int i = 0; i < serverListDisplay.size(); i++) {
            if (serverListDisplay.get(i).getServerData() == server) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        if (this.deletingServer) {
            this.deletingServer = false;
            if (result && id >= 0 && id < savedServerList.countServers()) {
                savedServerList.removeServerData(id);
                savedServerList.saveServerList();
                selectedServer = null;
                selectedIndex = -1;
                refreshServerListDisplay();
            }

            mc.displayGuiScreen(this);
        } else if (this.directConnect) {
            this.directConnect = false;
            if (result) {
                connectToServer(selectedServer);
            } else {
                mc.displayGuiScreen(this);
            }

        } else if (this.addingServer) {
            this.addingServer = false;
            if (result) {
                savedServerList.addServerData(selectedServer);
                savedServerList.saveServerList();
                selectedServer = null;
                selectedIndex = -1;
                refreshServerListDisplay();
            }

            mc.displayGuiScreen(this);
        } else if (this.editingServer) {
            this.editingServer = false;
            if (result && id >= 0 && id < savedServerList.countServers()) {
                ServerData serverdata = savedServerList.getServerData(id);
                serverdata.serverName = selectedServer.serverName;
                serverdata.serverIP = selectedServer.serverIP;
                serverdata.copyFrom(selectedServer);
                savedServerList.saveServerList();

                refreshServerListDisplay();

                selectedIndex = getServerIndexInDisplayList(serverdata);
                if (selectedIndex != -1) {
                    selectedServer = serverListDisplay.get(selectedIndex).getServerData();
                }
            }
            mc.displayGuiScreen(this);
        }
    }

    public void connectToSelected() {
        if (selectedServer != null) {
            connectToServer(selectedServer);
        }
    }

    private void connectToServer(ServerData server) {
        mc.displayGuiScreen(new GuiConnecting(this, mc, server));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (this.lanServerList.getWasUpdated()) {
            this.lanServerList.setWasNotUpdated();
            refreshServerListDisplay();
        }
        this.oldServerPinger.pingPendingNetworks();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (this.lanServerDetector != null) {
            this.lanServerDetector.interrupt();
            this.lanServerDetector = null;
        }
        this.oldServerPinger.clearPendingNetworks();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_F5) {
            refreshServerListDisplay();
            selectServer(-1);
            return;
        }

        if (selectedIndex == -1 && !serverListDisplay.isEmpty()) {
            if (keyCode == Keyboard.KEY_DOWN) {
                selectServer(0);
                return;
            } else if (keyCode == Keyboard.KEY_UP) {
                selectServer(serverListDisplay.size() - 1);
                return;
            }
        }

        if (selectedIndex >= 0 && selectedIndex < serverListDisplay.size()) {
            ServerListEntry entry = serverListDisplay.get(selectedIndex);
            ServerData serverData = entry.getServerData();

            if (keyCode == Keyboard.KEY_UP) {
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    int serverIndex = getServerIndexInDisplayList(serverData);
                    if (serverIndex > 0) {
                        ServerListEntry temp = serverListDisplay.get(serverIndex);
                        serverListDisplay.set(serverIndex, serverListDisplay.get(serverIndex - 1));
                        serverListDisplay.set(serverIndex - 1, temp);

                        int savedIndex = getSelectedServerIndex();
                        if (savedIndex > 0) {
                            savedServerList.swapServers(savedIndex, savedIndex - 1);
                            savedServerList.saveServerList();
                        }

                        selectServer(selectedIndex - 1);
                    }
                } else {
                    if (selectedIndex > 0) {
                        selectServer(selectedIndex - 1);
                    } else {
                        selectServer(serverListDisplay.size() - 1);
                    }
                }
            }
            else if (keyCode == Keyboard.KEY_DOWN) {
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    int serverIndex = getServerIndexInDisplayList(serverData);
                    if (serverIndex != -1 && serverIndex < serverListDisplay.size() - 1) {
                        ServerListEntry temp = serverListDisplay.get(serverIndex);
                        serverListDisplay.set(serverIndex, serverListDisplay.get(serverIndex + 1));
                        serverListDisplay.set(serverIndex + 1, temp);

                        int savedIndex = getSelectedServerIndex();
                        if (savedIndex != -1 && savedIndex < savedServerList.countServers() - 1) {
                            savedServerList.swapServers(savedIndex, savedIndex + 1);
                            savedServerList.saveServerList();
                        }

                        selectServer(selectedIndex + 1);
                    }
                } else {
                    if (selectedIndex < serverListDisplay.size() - 1) {
                        selectServer(selectedIndex + 1);
                    } else {
                        selectServer(0);
                    }
                }
            } else if (keyCode == Keyboard.KEY_RETURN) {
                connectToSelected();
            } else {
                super.keyTyped(typedChar, keyCode);
            }
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }
}