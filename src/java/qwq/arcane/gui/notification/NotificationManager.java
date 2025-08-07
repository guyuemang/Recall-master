package qwq.arcane.gui.notification;


import qwq.arcane.Client;

import java.util.ArrayList;
import java.util.List;


public class NotificationManager {
    private final List<Notification> notifications;

    public NotificationManager() {
        notifications = new ArrayList<>();
    }

    public void normalrender(double height) {
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
    public void normalshader(double height) {
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
    public void custom(double height) {
        if (notifications.size() > 4)
            notifications.remove(0);

        double startY = height - 36;
        double lastY = startY;

        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            notifications.removeIf(Notification::shouldDelete);

            notification.custom(startY, lastY);
            startY -= 25;
        }
    }
    public void customshader(double height) {
        if (notifications.size() > 4)
            notifications.remove(0);

        double startY = height - 36;
        double lastY = startY;

        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            notifications.removeIf(Notification::shouldDelete);

            notification.customshader(startY, lastY);
            startY -= 25;
        }
    }
    public void type1render(double height) {
        if (notifications.size() > 4)
            notifications.remove(0);

        double startY = height;
        double lastY = startY;

        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            notifications.removeIf(Notification::shouldDelete);

            notification.render1(startY, lastY);
            startY += 18;
        }
    }
    public void type1shader(double height) {
        if (notifications.size() > 4)
            notifications.remove(0);

        double startY = height;
        double lastY = startY;

        for (int i = 0; i < notifications.size(); i++) {
            Notification notification = notifications.get(i);
            notifications.removeIf(Notification::shouldDelete);

            notification.shader1(startY, lastY);
            startY += 18;
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
