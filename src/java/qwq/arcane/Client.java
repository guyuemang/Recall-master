package qwq.arcane;

import de.florianmichael.viamcp.ViaMCP;
import lombok.Getter;
import lombok.Setter;
import qwq.arcane.module.ClientApplication;
import org.lwjgl.opengl.Display;
import qwq.arcane.command.CommandManager;
import qwq.arcane.config.ConfigManager;
import qwq.arcane.event.EventManager;
import qwq.arcane.gui.clickgui.arcane.ArcaneClickGui;
import qwq.arcane.gui.clickgui.dropdown.DropDownClickGui;
import qwq.arcane.gui.notification.NotificationManager;
import qwq.arcane.module.ModuleManager;
import qwq.arcane.utils.Instance;
import qwq.arcane.utils.pack.BlinkComponent;
import qwq.arcane.utils.player.PingerUtils;
import qwq.arcane.utils.player.SlotSpoofComponent;
import qwq.arcane.utils.rotation.RotationManager;

/**
 * @Author：Guyuemang
 * @Date：2025/6/28 19:42
 */

@Getter
@Setter
public class Client implements Instance {
    public static Client Instance = new Client();
    public static String name = "Arcane";
    public static String version = " Release 3.0";

    private EventManager eventManager;
    private ModuleManager moduleManager;
    private ArcaneClickGui arcaneClickGui;
    private DropDownClickGui dropDownClickGui;
    private ConfigManager configManager;
    private NotificationManager notification;
    private CommandManager commandManager;
    private RotationManager rotationManager;
    private PingerUtils pingerUtils;
    public static boolean debug = true;
    int startTime;

    public void Init(){
        startTime = (int) System.currentTimeMillis();
        if (ClientApplication.Hwid){
            System.exit(0);
        }
        if (Client.debug){
            System.exit(0);
        }
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
    }
}
