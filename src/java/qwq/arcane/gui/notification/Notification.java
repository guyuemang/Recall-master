package qwq.arcane.gui.notification;

import qwq.arcane.Client;
import qwq.arcane.module.impl.render.InterFace;
import qwq.arcane.utils.Instance;
import qwq.arcane.utils.animations.Animation;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.DecelerateAnimation;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.utils.time.Timer;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

@Getter
public class Notification implements Instance {
    private ResourceLocation image;
    private final String message;
    private final String title;
    private final Timer timer;

    @Getter
    private final float time;

    private final Type type;

    private double lastY, posY, width, height, animationX;
    private int color;
    private final int imageWidth;
    private final long stayTime;

    private boolean finished;

    public final Animation animation = new DecelerateAnimation(380, 1, Direction.BACKWARDS);
    private final Animation animationY = new DecelerateAnimation(380, 1);
    public float animations;

    public Notification(String title, String message, Type type, long time) {
        image = new ResourceLocation("solitude/notification/" + type.name.toLowerCase() + ".png");
        this.title = title;
        this.time = (long) (1500);
        this.message = message;
        this.type = type;
        timer = new Timer();
        timer.reset();

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        width = Semibold.get(20).getStringWidth(message) + 34;
        animationX = width;
        stayTime = time;
        imageWidth = 9;
        height = 30;
        posY = sr.getScaledHeight() - height;
    }

    public void customshader(double getY, double lastY) {
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        this.lastY = lastY;
        animationY.setDirection(finished ? Direction.BACKWARDS : Direction.FORWARDS);
        animation.setDirection(isFinished() || finished ? Direction.FORWARDS : Direction.BACKWARDS);
        animationX = width * animation.getOutput();
        posY = RenderUtil.animate(posY, getY);
        RenderUtil.drawRect((float) ((resolution.getScaledWidth() / 2) - width / 2), (float) posY - 2, (float) width, 10, new Color(0, 0, 0, 255));

    }
    public void custom(double getY, double lastY) {
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        this.lastY = lastY;
        animationY.setDirection(finished ? Direction.BACKWARDS : Direction.FORWARDS);
        animation.setDirection(isFinished() || finished ? Direction.FORWARDS : Direction.BACKWARDS);
        animationX = width * animation.getOutput();
        posY = RenderUtil.animate(posY, getY);
        RoundedUtil.drawRound((float) ((resolution.getScaledWidth() / 2) - width / 2), (float) posY - 2, (float) width, 10,2, new Color(0, 0, 0, 100));
        Bold.get(18).drawCenteredString(message, resolution.getScaledWidth() / 2, (int) posY, -1);
    }
    public void render(double getY, double lastY) {
        Color scolor = new Color(0xFF171717);
        Color icolor = new Color(23, 3, 46, 100);
        this.lastY = lastY;
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());

        animationY.setDirection(finished ? Direction.BACKWARDS : Direction.FORWARDS);
        animation.setDirection(isFinished() || finished ? Direction.FORWARDS : Direction.BACKWARDS);
        animationX = width * animation.getOutput();
        posY = RenderUtil.animate(posY, getY);
        float progress = 1 - (timer.getTimePassed() / (float)stayTime);

        int x1 = (int) ((resolution.getScaledWidth() - 6) - width + animationX), y1 = (int) posY;

        RoundedUtil.drawRound((float) x1, y1, (float) width, (float) height, 2, new Color(1,1,1,190));
        RoundedUtil.drawRound((float) x1 + 5, (float) (y1 + height - 10), (float) Bold.get(14).getStringWidth(title + " " + message), (float) 2, 1, new Color(100,100,100,100));
        RoundedUtil.drawRound((float) x1 + 5, (float) (y1 + height - 10), (float) Bold.get(14).getStringWidth(title + " " + message) * progress, (float) 2, 1, type.getColor());

        Bold.get(14).drawString(title + " " +  message, (x1 + 5), y1 + 8, -1);
        RenderUtil.stopScissor();
    }
    public void shader(double getY, double lastY) {
        Color scolor = new Color(0xFF171717);
        Color icolor = new Color(23, 3, 46, 100);
        this.lastY = lastY;
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());

        animationY.setDirection(finished ? Direction.BACKWARDS : Direction.FORWARDS);
        animation.setDirection(isFinished() || finished ? Direction.FORWARDS : Direction.BACKWARDS);
        animationX = width * animation.getOutput();
        posY = RenderUtil.animate(posY, getY);

        int x1 = (int) ((resolution.getScaledWidth() - 6) - width + animationX), y1 = (int) posY;

        RoundedUtil.drawRound((float) x1, y1, (float) width, (float) height, 2, new Color(1,1,1,100));

        RenderUtil.startGlScissor(x1 - 4, y1 - 2, (int) width + 8,14);
        RoundedUtil.drawRound((float) x1, y1, (float) width , (float) 50, 2, ColorUtil.applyOpacity(new Color(InterFace.color(1).getRGB()).darker(), (float) 0.3f));
        RenderUtil.stopGlScissor();
        RenderUtil.stopScissor();
    }

    public boolean shouldDelete() {
        return (isFinished() || finished) && animationX >= width - 5;
    }

    private boolean isFinished() {
        return timer.hasReached(stayTime);
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public double getHeight() {
        return height;
    }

    public enum Type {
        SUCCESS("Success", "k",new Color(0xA2DDA5)),
        INFO("Information", "j",new Color(0xB78B72)),
        ERROR("Error", "i",new Color(0xB7777A));
        final String name;
        final String icon;
        final Color color;

        Type(String name, String icon,Color color) {
            this.name = name;
            this.icon = icon;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public Color getColor() {
            return color;
        }

        public String getIcon() {
            return icon;
        }

    }
}
