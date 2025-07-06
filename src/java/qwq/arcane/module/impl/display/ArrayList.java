package qwq.arcane.module.impl.display;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.StringUtils;
import qwq.arcane.Client;
import qwq.arcane.event.impl.events.render.Shader2DEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.ModuleWidget;
import qwq.arcane.utils.animations.Animation;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.fontrender.FontManager;
import qwq.arcane.utils.fontrender.FontRenderer;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.value.impl.BooleanValue;
import qwq.arcane.value.impl.ColorValue;
import qwq.arcane.value.impl.ModeValue;
import qwq.arcane.module.Module;
import qwq.arcane.value.impl.NumberValue;

import java.awt.*;
import java.util.Comparator;

/**
 * @Author：Guyuemang
 * @Date：2025/7/4 16:06
 */
public class ArrayList extends ModuleWidget {
    public ArrayList() {
        super("ArrayList",Category.Display);
    }

    public static BooleanValue importantModules = new BooleanValue("Important", false);
    public ModeValue fontmode = new ModeValue("FontMode","Custom",new String[]{"Custom","Bold","Semibold","Regular","Light"});
    public ModeValue textShadow = new ModeValue("Text Shadow","None", new String[]{"Black", "Colored", "None"});
    public BooleanValue suffixColor = new BooleanValue("SuffixColor",false);
    public final ModeValue tags = new ModeValue("Suffix","Bracket", new String[]{"None", "Simple", "Bracket", "Dash"});
    public ModeValue animation = new ModeValue("Animation", "Move In",new String[]{"Move In","Scale In"});
    public final ModeValue color = new ModeValue("Color Setting","Fade", new String[]{"Custom", "Rainbow", "Dynamic","Double","Astolfo","Tenacity"});
    public final NumberValue colorspeed = new NumberValue("ColorSpeed", () -> color.is("Dynamic") || color.is("Fade") || color.is("Tenacity"), 4, 1, 10, 1);
    public final NumberValue colorIndex = new NumberValue("Color Seperation", 1, 1, 50, 1);
    public ColorValue FirstColor = new ColorValue("MainColor", new Color(167, 59, 255));
    public ColorValue SecondColor = new ColorValue("SecondColor", new Color(217, 191, 255));
    public BooleanValue background = new BooleanValue("BackGround",false);
    public ModeValue misc = new ModeValue("Rectangle","None",new String[]{"None", "Top", "Side"});
    public NumberValue radius = new NumberValue("radius",()-> background.get(),3,0,8,1);
    public ModeValue backgroundmod = new ModeValue("BackGroundMod",()-> background.get(),"Rect",new String[]{"Rect","Round"});
    public final NumberValue backgroundAlpha = new NumberValue("Background Alpha", ()-> background.get(), 0.5, 0, 1, .01);
    public NumberValue hight2 = new NumberValue("RectangleHight",12.0,1.0,20.0,0.1);
    public NumberValue hight = new NumberValue("ArrayHight",12.0,1.0,20.0,0.1);
    public NumberValue sb = new NumberValue("FontCount",12.0,-20.0,20.0,0.1);
    public NumberValue count = new NumberValue("ArrayCount",1,1.0,5,0.1);

    @Override
    public void onShader(Shader2DEvent event) {

    }

    @Override
    public void render() {
        FontRenderer fontManager = FontManager.Regular.get(18);
        switch (fontmode.get()){
            case "Bold":
                fontManager = FontManager.Bold.get(18);
                break;
            case "Semibold":
                fontManager = FontManager.Semibold.get(18);
                break;
            case "Regular":
                fontManager = FontManager.Regular.get(18);
                break;
            case "Light":
                fontManager = FontManager.Light.get(18);
        }
        java.util.ArrayList<Module> enabledMods = getModuleArrayList(fontManager);
        int count = 0;
        int counts = 0;
        ScaledResolution sr = new ScaledResolution(mc);
        for (Module module : enabledMods){
            if (importantModules.get()){
                if (module.getCategory() == Category.Visuals) continue;
                if (module.getCategory() == Category.Display) continue;
            }
            Animation moduleAnimation = module.getAnimations();
            moduleAnimation.setDirection(module.getState() ? Direction.FORWARDS : Direction.BACKWARDS);
            if (!module.getState() && moduleAnimation.finished(Direction.BACKWARDS)) continue;
            int renderx = (int) renderX - 2;
            int rendery = (int) renderY + 4;
            boolean flip = renderX + width / 2 <= sr.getScaledWidth() / 2f;
            String displayText = module.getName() + module.getSuffix();
            int x = flip ? (renderx + 4) : (int) (renderx + (this.width - ( fontmode.get().equals("Custom")? mc.fontRendererObj.getStringWidth(displayText)  : fontManager.getStringWidth(displayText))));
            int y = rendery + count + sb.get().intValue();
            switch (animation.get()) {
                case "Move In":
                    if (flip) {
                        x -= (int) Math.abs((moduleAnimation.getOutput() - 1.0) * (12.0 + fontManager.getStringWidth(displayText)));
                    } else {
                        x += (int) Math.abs((moduleAnimation.getOutput() - 1.0) * (12.0 + fontManager.getStringWidth(displayText)));
                    }
                    break;
                case "Scale In":
                    if (flip) {
                        RenderUtil.scaleStart(x, rendery + count + mc.fontRendererObj.FONT_HEIGHT, (float) moduleAnimation.getOutput().floatValue());
                    } else {
                        RenderUtil.scaleStart(x + fontManager.getStringWidth(displayText), rendery + count + mc.fontRendererObj.FONT_HEIGHT, (float) moduleAnimation.getOutput().floatValue());
                    }
                    break;
            }

            int index = (int) (counts * colorIndex.getValue());
            int textcolor = ColorUtil.swapAlpha(color(index), (int) 255);
            if (color.is("Tenacity")) {
                textcolor = ColorUtil.interpolateColorsBackAndForth(colorspeed.getValue().intValue(), index, FirstColor.get(), SecondColor.get(), false).getRGB();
            }
            int w = fontmode.get().equals("Custom")? mc.fontRendererObj.getStringWidth(displayText) + 4 :fontManager.getStringWidth(displayText) + 6;
            switch (misc.getValue()) {
                case "Top":
                    if (count == 0) {
                        RenderUtil.drawRect(x - 2, rendery - 4, w, 2, textcolor);
                    }
                    break;
                case "Side":
                    if (flip) {
                        RenderUtil.drawRect(x - 4, y - 4, 2, hight.get().intValue(),textcolor);
                    }else {
                        RenderUtil.drawRect(x + (fontmode.get().equals("Custom")? mc.fontRendererObj.getStringWidth(displayText) + 2 :fontManager.getStringWidth(displayText) + 2), y - 4.5f, 2, hight2.get().intValue(), textcolor);
                    }
                    break;
                default:
                    break;
            }
            if (background.getValue()) {
                switch (backgroundmod.get()) {
                    case "Rect" :
                        RenderUtil.drawRect(x - 2, rendery + count - 4, w, hight.get().intValue(),ColorUtil.applyOpacity(new Color(1,1,1),backgroundAlpha.get().floatValue()));
                        break;
                    case "Round":
                        RoundedUtil.drawRound(x - 2, rendery + count - 4, w, hight.get().intValue(),radius.get().intValue(),ColorUtil.applyOpacity3(new Color(1,1,1).getRGB(),backgroundAlpha.get().floatValue()));
                        break;
                }
            }
            switch (textShadow.getValue()) {
                case "None":
                    if (fontmode.get().equals("Custom")) {
                        mc.fontRendererObj.drawString(displayText, x + 1, y, textcolor);
                    }else {
                        fontManager.drawString(displayText, x + 1, y, textcolor);
                    }
                    break;
                case "Colored":
                    RenderUtil.resetColor();
                    if (fontmode.get().equals("Custom")) {
                        mc.fontRendererObj.drawString(StringUtils.stripColorCodes(displayText), x + 2, y + 1 - 2, ColorUtil.darker(textcolor, .5f));
                    }else {
                        fontManager.drawString(StringUtils.stripColorCodes(displayText), x + 2, y + 1 - 2, ColorUtil.darker(textcolor, .5f));
                    }
                    RenderUtil.resetColor();
                    if (fontmode.get().equals("Custom")) {
                        mc.fontRendererObj.drawString(displayText, x + 1, y - 2, textcolor);
                    }else {
                        fontManager.drawString(displayText, x + 1, y - 2, textcolor);
                    }
                    break;
                case "Black":
                    float f = 2f;
                    if (fontmode.get().equals("Custom")) {
                        mc.fontRendererObj.drawString(StringUtils.stripColorCodes(displayText), (int) (x + f), (int) (y + f - 2),
                                ColorUtil.applyOpacity(Color.BLACK,1f).getRGB());
                    }else {
                        fontManager.drawString(StringUtils.stripColorCodes(displayText), x + f, y + f - 2,
                                ColorUtil.applyOpacity(Color.BLACK,1f).getRGB());
                    }
                    RenderUtil.resetColor();
                    if (fontmode.get().equals("Custom")) {
                        mc.fontRendererObj.drawString(displayText, x + 1, y - 2, textcolor);
                    }else {
                        fontManager.drawString(displayText, x + 1, y - 2, textcolor);
                    }
                    break;
            }
            if (animation.get().equals("Scale In")) {
                RenderUtil.scaleEnd();
            }
            count += (int) (moduleAnimation.getOutput() * (hight.get() * this.count.get()));
            counts ++;
            this.height = count;
        }
        this.width = 52;
    }
    public int color(int counter) {
        return color(counter, FirstColor.get().getAlpha());
    }
    public int color(int counter, int alpha) {
        int colors = FirstColor.get().getRGB();
        colors = switch (color.get()) {
            case "Rainbow" -> RenderUtil.getRainbow(System.currentTimeMillis(), 2000, counter);
            case "Dynamic" -> ColorUtil.swapAlpha(ColorUtil.colorSwitch(FirstColor.get(), new Color(ColorUtil.darker(FirstColor.get().getRGB(), 0.25F)), 2000.0F, counter, counter * 10, colorspeed.get()).getRGB(), alpha);
            case "Double"->new Color(RenderUtil.colorSwitch(FirstColor.get(), SecondColor.get(), 2000, -counter / 40, 75, 2)).getRGB();
            case "Astolfo" -> ColorUtil.swapAlpha(astolfoRainbow(counter, FirstColor.getSaturation(), FirstColor.getBrightness()), alpha);
            case "Custom" -> ColorUtil.swapAlpha(FirstColor.get().getRGB(), alpha);
            case "Tenacity" -> ColorUtil.interpolateColorsBackAndForth(colorspeed.getValue().intValue(), Client.Instance.getModuleManager().getAllModules().size() * count.get().intValue(), FirstColor.get(), SecondColor.get(), false).getRGB();
            default -> colors;
        };
        return new Color(colors,true).getRGB();
    }
    public int getRainbow(int counter) {
        return Color.HSBtoRGB(getRainbowHSB(counter)[0], getRainbowHSB(counter)[1], getRainbowHSB(counter)[2]);
    }
    public float[] getRainbowHSB(int counter) {
        final int width = 20;

        double rainbowState = Math.ceil(System.currentTimeMillis() - (long) counter * width) / 8;
        rainbowState %= 360;

        float hue = (float) (rainbowState / 360);
        float saturation = FirstColor.getSaturation();
        float brightness = FirstColor.getBrightness();

        return new float[]{hue, saturation, brightness};
    }
    public static int astolfoRainbow(final int offset, final float saturation, final float brightness) {
        double currentColor = Math.ceil((double)(System.currentTimeMillis() + offset * 20L)) / 6.0;
        return Color.getHSBColor(((float)((currentColor %= 360.0) / 360.0) < 0.5) ? (-(float)(currentColor / 360.0)) : ((float)(currentColor / 360.0)), saturation, brightness).getRGB();
    }
    private java.util.ArrayList<Module> getModuleArrayList(FontRenderer string) {
        Comparator<Module> sort = (m1, m2) -> {
            double ab = fontmode.get().equals("Custom")? mc.fontRendererObj.getStringWidth(m1.getName() + m1.getSuffix()) : string.getStringWidth(m1.getName() + m1.getSuffix());
            double bb = fontmode.get().equals("Custom")? mc.fontRendererObj.getStringWidth(m2.getName() + m2.getSuffix()) : string.getStringWidth(m2.getName() + m2.getSuffix());
            return Double.compare(bb, ab);
        };
        java.util.ArrayList<Module> enabledMods = new java.util.ArrayList<>(INSTANCE.getModuleManager().getAllModules());
        enabledMods.sort(sort);
        return enabledMods;
    }
    @Override
    public boolean shouldRender() {
        return getState() && INTERFACE.getState();
    }
}
