package qwq.arcane.gui.notification;

import com.yumegod.obfuscation.FlowObfuscate;
import com.yumegod.obfuscation.InvokeDynamic;
import com.yumegod.obfuscation.Rename;
import qwq.arcane.Client;

import java.util.ArrayList;
import java.util.List;

@Rename
@FlowObfuscate
@InvokeDynamic
public class NotificationManager {
    private final List<Notification> notifications;

    public NotificationManager() {
        notifications = new ArrayList<>();
    }

    public void render(double height) {
        if (notifications.size() > 4)
            notifications.remove(0);

        double startY = height - 36;
        double lastY = startY;

        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            notifications.removeIf(Notification::shouldDelete);

            notification.render(startY, lastY);
            startY -= notification.getHeight() + 3;
        }
    }
    public void custom(double height) {
        if (notifications.size() > 4)
            notifications.remove(0);

        double startY = height / 2 + 40;
        double lastY = startY;

        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            notifications.removeIf(Notification::shouldDelete);

            notification.custom(startY, lastY);
            startY += 13;
        }
    }
    public void shader(double height) {
        if (notifications.size() > 4)
            notifications.remove(0);

        double startY = height - 36;
        double lastY = startY;

        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            notifications.removeIf(Notification::shouldDelete);

            notification.shader(startY, lastY);
            startY -= notification.getHeight() + 3;
        }
    }
    public void customshader(double height) {
        if (notifications.size() > 4)
            notifications.remove(0);

        double startY = height / 2 + 40;
        double lastY = startY;

        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            notifications.removeIf(Notification::shouldDelete);

            notification.customshader(startY, lastY);
            startY += 13;
        }
    }

    public void add(String title, String message, Notification.Type type) {
        if (Client.Instance.getModuleManager().getModule(qwq.arcane.module.impl.display.Notification.class).getState())
            notifications.add(new Notification(title, message, type, 1500));
    }

    public void add(String message, Notification.Type type) {
        if (Client.Instance.getModuleManager().getModule(qwq.arcane.module.impl.display.Notification.class).getState())
            notifications.add(new Notification(type.getName(), message, type, 1500));
    }

    public void add(String title, String message, Notification.Type type, long time) {
        if (Client.Instance.getModuleManager().getModule(qwq.arcane.module.impl.display.Notification.class).getState())
            notifications.add(new Notification(title, message, type, time));
    }

    public void add(String message, Notification.Type type, long time) {
        if (Client.Instance.getModuleManager().getModule(qwq.arcane.module.impl.display.Notification.class).getState())
            notifications.add(new Notification(type.getName(), message, type, time));
    }

    public List<Notification> getNotifications() {
        return notifications;
    }
}
