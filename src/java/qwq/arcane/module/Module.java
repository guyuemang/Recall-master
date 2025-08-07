package qwq.arcane.module;


import lombok.Getter;
import lombok.Setter;
import org.lwjgl.input.Keyboard;
import qwq.arcane.Client;
import qwq.arcane.gui.notification.Notification;
import qwq.arcane.module.impl.combat.Gapple;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.Instance;
import qwq.arcane.utils.animations.Animation;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.DecelerateAnimation;
import qwq.arcane.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @Author：Guyuemang
 * @Date：2025/6/1 00:47
 */
@Getter
@Setter

public class Module implements Instance {
    public String name;
    public Category category;
    private String suffix = "";
    public boolean State;
    private int key = Keyboard.KEY_NONE;
    private final List<Value<?>> settings = new ArrayList<>();
    private final Animation animations = new DecelerateAnimation(250, 1).setDirection(Direction.BACKWARDS);

    public Module(String name,Category category){
        this.name = name;
        this.category = category;
    }

    public void onEnable(){
    }
    public void onDisable(){
    }

    public <M extends Module> M getModule(Class<M> clazz) {
        return Client.Instance.getModuleManager().getModule(clazz);
    }
    public boolean isGapple() {
        Gapple gapple = Client.Instance.getModuleManager().getModule(Gapple.class);
        if (gapple.getState()){
            return true;
        }
        else {
            return false;
        }
    }
    public <M extends Module> boolean isEnabled(Class<M> module) {
        Module mod = Client.Instance.getModuleManager().getModule(module);
        return mod != null && mod.isEnabled();
    }
    public void setsuffix(String tag) {
        if (tag != null && !tag.isEmpty()) {
            String tagStyle = Optional.ofNullable(getModule(qwq.arcane.module.impl.display.ArrayList.class)).map(m -> m.tags.get()).orElse("").toLowerCase();
            if (getModule(qwq.arcane.module.impl.display.ArrayList.class).suffixColor.getValue()){
                switch (tagStyle) {
                    case "simple":
                        suffix = " " + tag;
                        break;
                    case "dash":
                        suffix = " - " + tag;
                        break;
                    case "bracket":
                        suffix = " [" + tag + "]";
                        break;
                    default:
                        suffix = "";
                }
            }else {
                switch (tagStyle) {
                    case "simple":
                        suffix = "§f " + tag;
                        break;
                    case "dash":
                        suffix = "§f - " + tag;
                        break;
                    case "bracket":
                        suffix = "§f [" + tag + "]";
                        break;
                    default:
                        suffix = "";
                }
            }
        } else {
            suffix = "";
        }
    }
    public boolean isEnabled() {
        return State;
    }

    public void setState(boolean state) {
        if (mc.theWorld != null) {
            mc.theWorld.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, "random.click", 0.5f, state ? 0.6f : 0.5f, false);
        }
        if (this.State != state) {
            this.State = state;
            if (state) {
                Client.Instance.getEventManager().register(this);
                qwq.arcane.module.impl.display.Notification notificationModule = Client.Instance.getModuleManager().getModuleW(qwq.arcane.module.impl.display.Notification.class);
                if (notificationModule != null) {
                    Client.Instance.getNotification().add("Module Toggle", "Module " + this.name + " Enabled", Notification.Type.SUCCESS);
                }
                onEnable();
            } else {
                Client.Instance.getEventManager().unregister(this);
                qwq.arcane.module.impl.display.Notification notificationModule = Client.Instance.getModuleManager().getModuleW(qwq.arcane.module.impl.display.Notification.class);
                if (notificationModule != null) {
                    Client.Instance.getNotification().add("Module Toggle", "Module " + this.name + " Disabled", Notification.Type.ERROR);
                }
                onDisable();
            }
        }
    }

    public void toggle() {
        setState(!State);
    }

    public boolean getState() {
        return State;
    }
}
