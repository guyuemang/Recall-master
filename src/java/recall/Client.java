package recall;

import de.florianmichael.viamcp.ViaMCP;
import org.lwjgl.opengl.Display;

/**
 * @Author：Guyuemang
 * @Date：2025/6/28 19:42
 */
public class Client {
    public static Client instance = new Client();
    public static String name = "Recall";
    public static String version = "Nextgen";

    public static void Init(){
        ViaMCP.create();
        ViaMCP.INSTANCE.initAsyncSlider();
        Display.setTitle(name + " " + version);
        System.out.println("Client Init");
    }
}
