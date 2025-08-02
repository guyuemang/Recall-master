package qwq.arcane;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
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
import qwq.arcane.utils.AuthClient;
import qwq.arcane.utils.Instance;
import qwq.arcane.utils.pack.BlinkComponent;
import qwq.arcane.utils.player.PingerUtils;
import qwq.arcane.utils.player.SlotSpoofComponent;
import qwq.arcane.utils.rotation.RotationManager;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

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
    private PingerUtils pingerUtils;
    public RotationManager rotationManager;
    int startTime;

    public void Init(){
        startTime = (int) System.currentTimeMillis();
        ViaMCP.create();
        ViaMCP.INSTANCE.initAsyncSlider();
        Display.setTitle(name + " " + version);
        eventManager = new EventManager();
        eventManager.register(this);
        eventManager.register(new BlinkComponent());
        eventManager.register(new SlotSpoofComponent());
        rotationManager = new RotationManager();
        eventManager.register(rotationManager);
        pingerUtils = new PingerUtils();
        eventManager.register(pingerUtils);

        moduleManager = new ModuleManager();
        moduleManager.Init();

        notification = new NotificationManager();

        configManager = new ConfigManager();
        configManager.loadConfig("config",moduleManager);

        commandManager = new CommandManager(moduleManager);

        arcaneClickGui = new ArcaneClickGui();
        dropDownClickGui = new DropDownClickGui();

        this.videoComponent = new VideoComponent();

        try {
            VideoPlayer.init(new File(Minecraft.getMinecraft().mcDataDir, "background.mp4"));
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }
}
