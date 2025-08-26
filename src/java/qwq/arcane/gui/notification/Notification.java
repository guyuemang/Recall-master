package qwq.arcane.gui.notification;


import qwq.arcane.Client;
import qwq.arcane.module.impl.display.IsLand;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.Instance;
import qwq.arcane.utils.animations.Animation;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.DecelerateAnimation;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.utils.time.Timer;
import lombok.Getter;
import qwq.arcane.module.Mine;
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

        ScaledResolution sr = new ScaledResolution(Mine.getMinecraft());
        width = Bold.get(20).getStringWidth(message) + 10;
        animationX = width;
        stayTime = time;
        imageWidth = 9;
        height = 30;
        posY = sr.getScaledHeight() - height;
    }

    public void customshader(double getY, double lastY) {
        this.lastY = lastY;
        ScaledResolution resolution = new ScaledResolution(Mine.getMinecraft());

        animationY.setDirection(finished ? Direction.BACKWARDS : Direction.FORWARDS);
        animation.setDirection(isFinished() || finished ? Direction.FORWARDS : Direction.BACKWARDS);
        animationX = width * animation.getOutput();
        posY = RenderUtil.animate(posY, getY);
        float progress = 1 - (timer.getTimePassed() / (float)stayTime);

        int x1 = (int) ((resolution.getScaledWidth() - 6) - width + animationX), y1 = (int) posY;
        RenderUtil.drawRect((float) x1 + Bold.get(14).getStringWidth(message) / 2 - 15, (float) y1, (float) Bold.get(14).getStringWidth(message) + 20, (float) 20,new Color(1,1,1,255).getRGB());
    }
    public void render1(double getY, double lastY) {
        this.lastY = lastY;
        ScaledResolution resolution = new ScaledResolution(Mine.getMinecraft());

        animationY.setDirection(finished ? Direction.BACKWARDS : Direction.FORWARDS);
        animation.setDirection(isFinished() || finished ? Direction.FORWARDS : Direction.BACKWARDS);
        animationX = width * animation.getOutput();
        posY = RenderUtil.animate(posY, getY);
        float progress = 1 - (timer.getTimePassed() / (float)stayTime);

        int x1 = resolution.getScaledWidth() / 2 - Bold.get(14).getStringWidth(message) + 30, y1 = (int) posY;
        ScaledResolution sr = new ScaledResolution(Mine.getMinecraft());
        RoundedUtil.drawRound(x1, y1,Bold.get(14).getStringWidth(message) + 30 ,15,3,new Color(255,255,255,40));
        Icon.get(24).drawString(type.icon, x1 + 4,y1 + 5,type.getColor().getRGB());
        Bold.get(14).drawString(message, x1 + 18, y1 + 6, -1);
    }
    public void shader1(double getY, double lastY) {
        this.lastY = lastY;
        ScaledResolution resolution = new ScaledResolution(Mine.getMinecraft());

        animationY.setDirection(finished ? Direction.BACKWARDS : Direction.FORWARDS);
        animation.setDirection(isFinished() || finished ? Direction.FORWARDS : Direction.BACKWARDS);
        animationX = width * animation.getOutput();
        posY = RenderUtil.animate(posY, getY);
        float progress = 1 - (timer.getTimePassed() / (float)stayTime);

        int x1 = resolution.getScaledWidth() / 2 - Bold.get(14).getStringWidth(message) + 30, y1 = (int) posY;
        ScaledResolution sr = new ScaledResolution(Mine.getMinecraft());
        RoundedUtil.drawRound(x1, y1,Bold.get(14).getStringWidth(message) + 30 ,15,3,new Color(255,255,255,190));
        Icon.get(24).drawString(type.icon, x1 + 4,y1 + 5,type.getColor().getRGB());
    }
    public void custom(double getY, double lastY) {
        this.lastY = lastY;
        ScaledResolution resolution = new ScaledResolution(Mine.getMinecraft());

        animationY.setDirection(finished ? Direction.BACKWARDS : Direction.FORWARDS);
        animation.setDirection(isFinished() || finished ? Direction.FORWARDS : Direction.BACKWARDS);
        animationX = width * animation.getOutput();
        posY = RenderUtil.animate(posY, getY);
        float progress = 1 - (timer.getTimePassed() / (float)stayTime);

        int x1 = (int) ((resolution.getScaledWidth() - 6) - width + animationX), y1 = (int) posY;
        RenderUtil.drawRect((float) x1 + Bold.get(14).getStringWidth(message) / 2 - 15, (float) y1, (float) Bold.get(14).getStringWidth(message) + 20, (float) 20,new Color(1,1,1,100).getRGB());
        RoundedUtil.drawGradientVertical(x1 + Bold.get(14).getStringWidth(message) / 2 - 15 + 4,y1 + 3,2.5f,14,2,type.color,type.color);
        Bold.get(14).drawString(message, (x1 + Bold.get(14).getStringWidth(message) / 2) - 4, y1 + 8, -1);
    }
    public void render(double getY, double lastY) {
        this.lastY = lastY;
        ScaledResolution resolution = new ScaledResolution(Mine.getMinecraft());

        animationY.setDirection(finished ? Direction.BACKWARDS : Direction.FORWARDS);
        animation.setDirection(isFinished() || finished ? Direction.FORWARDS : Direction.BACKWARDS);
        animationX = width * animation.getOutput();
        posY = RenderUtil.animate(posY, getY);
        float progress = 1 - (timer.getTimePassed() / (float)stayTime);

        int x1 = (int) ((resolution.getScaledWidth() - 6) - width + animationX), y1 = (int) posY;

        RoundedUtil.drawRound((float) x1, y1, (float) width, (float) height, 6, new Color(1,1,1,100));
        RoundedUtil.drawRound((float) x1, y1, (float) width, (float) 12, 6, new Color(1,1,1,100));
        RenderUtil.drawCircleCGUI(x1 + 6, y1 + 6, 8, type.color.getRGB());
        Bold.get(14).drawString(title, (x1 + 12), y1 + 4, -1);
        Icon.get(24).drawString(type.icon, x1 + 4,y1 + 19,type.getColor().getRGB());
        Bold.get(14).drawString(message, (x1 + 18), y1 + 20, -1);
        RenderUtil.stopScissor();
    }
    public void shader(double getY, double lastY) {
        ScaledResolution resolution = new ScaledResolution(Mine.getMinecraft());

        animationY.setDirection(finished ? Direction.BACKWARDS : Direction.FORWARDS);
        animation.setDirection(isFinished() || finished ? Direction.FORWARDS : Direction.BACKWARDS);
        animationX = width * animation.getOutput();
        posY = RenderUtil.animate(posY, getY);
        float progress = 1 - (timer.getTimePassed() / (float)stayTime);

        int x1 = (int) ((resolution.getScaledWidth() - 6) - width + animationX), y1 = (int) posY;

        RoundedUtil.drawRound((float) x1, y1, (float) width, (float) height, 6, new Color(1,1,1,255));
        RoundedUtil.drawRound((float) x1, y1, (float) width, (float) 12, 6, new Color(1,1,1,255));
        RenderUtil.drawCircleCGUI(x1 + 6, y1 + 6, 8, type.color.getRGB());
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
        SUCCESS("Success", "U",new Color(0xA2DDA5)),
        INFO("Information", "N",new Color(0xB78B72)),
        ERROR("Error", "T",new Color(0xB7777A));
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
