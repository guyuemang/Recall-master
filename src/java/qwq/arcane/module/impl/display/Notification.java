package qwq.arcane.module.impl.display;


import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.render.Render2DEvent;
import qwq.arcane.event.impl.events.render.Shader2DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.ModuleWidget;
import qwq.arcane.value.impl.ModeValue;

/**
 * @Author：Guyuemang
 * @Date：2025/6/2 13:38
 */

public class Notification extends ModuleWidget {
    public ModeValue modeValue = new ModeValue("Mode", "Normal",new String[]{"Normal","Custom","Type1"});

    public Notification() {
        super("Notification", Category.Display);
    }

    @Override
    public void onShader(Shader2DEvent event) {
        switch (modeValue.getValue()) {
            case "Custom":
                Client.Instance.getNotification().customshader(sr.getScaledHeight() - 6);
                break;
            case "Normal":
                Client.Instance.getNotification().normalshader(sr.getScaledHeight() - 6);
                break;
            case "Type1":
                Client.Instance.getNotification().type1shader(sr.getScaledHeight() / 2 + 26);
                break;
        }
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        switch (modeValue.getValue()) {
            case "Custom":
                Client.Instance.getNotification().custom(sr.getScaledHeight() - 6);
                break;
            case "Normal":
                Client.Instance.getNotification().normalrender(sr.getScaledHeight() - 6);
                break;
            case "Type1":
                Client.Instance.getNotification().type1render(sr.getScaledHeight() / 2 + 26);
                break;
        }
    }

    @Override
    public void render() {
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
    }

    @Override
    public boolean shouldRender() {
        return getState() && INTERFACE.getState();
    }
}
