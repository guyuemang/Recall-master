package qwq.arcane;

import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viarewind.protocol.v1_9to1_8.storage.PlayerPositionTracker;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;
import de.florianmichael.viamcp.ViaMCP;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.lwjgl.opengl.Display;
import qwq.arcane.command.CommandManager;
import qwq.arcane.config.ConfigManager;
import qwq.arcane.event.EventManager;
import qwq.arcane.gui.VideoComponent;
import qwq.arcane.gui.VideoPlayer;
import qwq.arcane.gui.clickgui.arcane.ArcaneClickGui;
import qwq.arcane.gui.clickgui.dropdown.DropDownClickGui;
import qwq.arcane.gui.notification.NotificationManager;
import qwq.arcane.module.ModuleManager;
import qwq.arcane.utils.Instance;

import java.io.File;

/**
 * @Author：Guyuemang
 * @Date：2025/6/28 19:42
 */
@Getter
@Setter
public class Client implements Instance {
    public static Client Instance = new Client();
    public static String name = "Arcane";
    public static String version = "Nextgen";

    private EventManager eventManager;
    private ModuleManager moduleManager;
    private ArcaneClickGui arcaneClickGui;
    private DropDownClickGui dropDownClickGui;
    private ConfigManager configManager;
    private VideoComponent videoComponent;
    private NotificationManager notification;
    private CommandManager commandManager;

    public void Init(){
        ViaMCP.create();
        ViaMCP.INSTANCE.initAsyncSlider();
        fixviamcp();
        Display.setTitle(name + " " + version);
        eventManager = new EventManager();
        eventManager.register(this);
        moduleManager = new ModuleManager();
        moduleManager.Init();
        notification = new NotificationManager();
        configManager = new ConfigManager();
        configManager.loadConfig("config",moduleManager);
        arcaneClickGui = new ArcaneClickGui();
        dropDownClickGui = new DropDownClickGui();
        this.videoComponent = new VideoComponent();
        commandManager = new CommandManager(moduleManager);
        try {
            VideoPlayer.init(new File(Minecraft.getMinecraft().mcDataDir, "background.mp4"));
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void fixviamcp() {
        Protocol1_9To1_8 protocol = Via.getManager().getProtocolManager().getProtocol(Protocol1_9To1_8.class);
        if (protocol != null) {
            (protocol).registerClientbound(ClientboundPackets1_9.PLAYER_POSITION, new PacketHandlers() {
                public void register() {
                    this.map(Types.DOUBLE);
                    this.map(Types.DOUBLE);
                    this.map(Types.DOUBLE);
                    this.map(Types.FLOAT);
                    this.map(Types.FLOAT);
                    this.map(Types.BYTE);
                    this.handler((wrapper) -> {
                        PlayerPositionTracker pos = (PlayerPositionTracker)wrapper.user().get(PlayerPositionTracker.class);
                        int teleportId = wrapper.read(Types.VAR_INT);
                        pos.setConfirmId(teleportId);
                        byte flags = wrapper.get(Types.BYTE, 0);
                        double x = wrapper.get(Types.DOUBLE, 0);
                        double y = wrapper.get(Types.DOUBLE, 1);
                        double z = wrapper.get(Types.DOUBLE, 2);
                        float yaw = wrapper.get(Types.FLOAT, 0);
                        float pitch = wrapper.get(Types.FLOAT, 1);
                        UserConnection userConnection = wrapper.user();
                        try {
                            PacketWrapper acceptTeleport = PacketWrapper.create(ServerboundPackets1_9.ACCEPT_TELEPORTATION, userConnection);
                            acceptTeleport.write(Types.VAR_INT, teleportId);
                            acceptTeleport.sendToServer(Protocol1_9To1_8.class);
                        } catch (Exception e) {
                        }
                        wrapper.set(Types.BYTE, 0, (byte)0);
                        if (flags != 0) {
                            if ((flags & 1) != 0) {
                                x += pos.getPosX();
                                wrapper.set(Types.DOUBLE, 0, x);
                            }

                            if ((flags & 2) != 0) {
                                y += pos.getPosY();
                                wrapper.set(Types.DOUBLE, 1, y);
                            }

                            if ((flags & 4) != 0) {
                                z += pos.getPosZ();
                                wrapper.set(Types.DOUBLE, 2, z);
                            }

                            if ((flags & 8) != 0) {
                                yaw += pos.getYaw();
                                wrapper.set(Types.FLOAT, 0, yaw);
                            }

                            if ((flags & 16) != 0) {
                                pitch += pos.getPitch();
                                wrapper.set(Types.FLOAT, 1, pitch);
                            }
                        }

                        pos.setPos(x, y, z);
                        pos.setYaw(yaw);
                        pos.setPitch(pitch);
                    });
                }
            });
            System.out.println("Registered CustomPlayerPacketRewriter for Protocol1_9To1_8");
        } else {
            System.err.println("Failed to find Protocol1_9To1_8");
        }
    }
}
