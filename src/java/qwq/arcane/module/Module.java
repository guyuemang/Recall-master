package qwq.arcane.module;


import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import qwq.arcane.Client;
import qwq.arcane.gui.notification.Notification;
import qwq.arcane.module.impl.combat.Gapple;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.Instance;
import qwq.arcane.utils.animations.Animation;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.DecelerateAnimation;
import qwq.arcane.utils.render.SoundUtil;
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

    public static <M extends Module> M getModule(Class<M> clazz) {
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
            } if (getModule(qwq.arcane.module.impl.display.ArrayList.class).style.is("Suffix")){
                switch (tagStyle) {
                    case "simple":
                        suffix = "§9 " + tag;
                        break;
                    case "dash":
                        suffix = "§9 - " + tag;
                        break;
                    case "bracket":
                        suffix = "§9 [" + tag + "]";
                        break;
                    default:
                        suffix = "";
                }
            } else {
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
    private void playClickSound(float volume) {
        if (mc.thePlayer != null) {
            switch (Client.INSTANCE.getModuleManager().getModule(InterFace.class).soundMode.getValue()) {
                case "Default":
                    mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("random.click"), volume));
                    break;
                case "Sigma":
                    if (State) {
                        SoundUtil.playSound(new ResourceLocation("nothing/sounds/jello/activate.wav"), 1);
                    } else {
                        SoundUtil.playSound(new ResourceLocation("nothing/sounds/jello/deactivate.wav"), 1);
                    }
                    break;
                case "Augustus":
                    if (State) {
                        SoundUtil.playSound(new ResourceLocation("nothing/sounds/augustus/enable.wav"), 1);
                    } else {
                        SoundUtil.playSound(new ResourceLocation("nothing/sounds/augustus/disable.wav"), 1);
                    }
                    break;
            }
        }
    }

    public void setState(boolean state) {
        if (this.State != state) {
            this.State = state;
            if (state) {
                Client.Instance.getEventManager().register(this);
                qwq.arcane.module.impl.display.Notification notificationModule = Client.Instance.getModuleManager().getModuleW(qwq.arcane.module.impl.display.Notification.class);
                if (notificationModule != null) {
                    Client.Instance.getNotification().add("Module Toggle", "Module " + this.name + "§2 Enabled", Notification.Type.SUCCESS);
                }
                playClickSound(1.0F);
                onEnable();
            } else {
                Client.Instance.getEventManager().unregister(this);
                qwq.arcane.module.impl.display.Notification notificationModule = Client.Instance.getModuleManager().getModuleW(qwq.arcane.module.impl.display.Notification.class);
                if (notificationModule != null) {
                    Client.Instance.getNotification().add("Module Toggle", "Module " + this.name + "§c Disabled", Notification.Type.ERROR);
                }
                playClickSound(1.0F);
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
