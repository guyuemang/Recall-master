package recall.gui.clickgui.fac;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import recall.gui.clickgui.fac.panels.CategoryPanel;
import recall.module.Category;
import recall.utils.fontrender.FontManager;
import recall.utils.render.RenderUtil;
import recall.utils.render.RoundedUtil;
import recall.utils.render.StencilUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * @Author：Guyuemang
 * @Date：2025/6/29 13:28
 */
@Getter
public class FatalityClickGui extends GuiScreen {
    private final List<CategoryPanel> categoryPanels = new ArrayList<>();
    public int x;
    public int y;
    public int w = 740;
    public final int h = 420;
    private int dragX;
    private int dragY;
    private boolean dragging = false;

    public static Color backgroundcolor;
    public static Color rectcolor;
    public static Color linecolor;
    public static Color categorycolor;
    public static Color font1color;
    public static Color font2color;

    List<Button> buttons = Arrays.asList(
            new Button("J"),
            new Button("K"),
            new Button("L"),
            new Button("M"),
            new Button("O"),
            new Button("N")
    );

    public FatalityClickGui() {
        Arrays.stream(Category.values()).forEach(moduleCategory -> categoryPanels.add(new CategoryPanel(moduleCategory)));
        x = 40;
        y = 40;
        backgroundcolor = new Color(0xD3D7DA);
        rectcolor = new Color(0xE2E4E8);
        linecolor = new Color(0x9D2645);
        categorycolor = new Color(0x6D6F73);
        font1color = new Color(1);
        font2color = new Color(0x9D2645);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (dragging) {
            x = mouseX + dragX;
            y = mouseY + dragY;
        }

        RoundedUtil.drawRound(x,y,w,h,3,backgroundcolor);

        RoundedUtil.drawRound(x,y,w,40,3,rectcolor);
        RoundedUtil.drawRound(x,y + 39,w,1,0,linecolor);

        RoundedUtil.drawRound(x,y + h - 25,w,25,3,rectcolor);
        RoundedUtil.drawRound(x,y + h - 25,w,1,0,linecolor);

        FontManager.Bold.get(32).drawString("Fatality".toUpperCase(Locale.ROOT),x + 15,y + 12,font1color.getRGB());

        QQAvatarUtil.drawRoundedQQAvatar("122227762", x + w - 35, y + 5, 30, 15);
        FontManager.Bold.get(18).drawString("Dev".toUpperCase(Locale.ROOT),x + w - 45 - FontManager.Bold.get(16).getStringWidth("Dev".toUpperCase(Locale.ROOT))
                ,y + 12,font2color.getRGB());

        FontManager.Bold.get(18).drawString("剩余订阅时间:".toUpperCase(Locale.ROOT),x + w - 45 - FontManager.Bold.get(16).getStringWidth("剩余订阅时间:".toUpperCase(Locale.ROOT))
                ,y + 24,1);

        float count = 0;
        for (Button button : buttons) {
            button.x = x + count + 8;
            button.y = y + h - 15;
            button.width = 160;
            button.height = 35;
            switch (button.name){
                case "N":
                    button.x += w - 155;
                    break;
            }
            count += 25;
            button.drawScreen(mouseX, mouseY);
        }

        Color sb = categorycolor;

        for (CategoryPanel categoryPanel : categoryPanels) {
            categoryPanel.drawScreen(mouseX, mouseY);
            if (categoryPanel.isSelected()) {
                RoundedUtil.drawRound((categoryPanel.getCategory().ordinal() >= 6 ? x + 150 + categoryPanel.getCategory().ordinal() * 70 :
                        categoryPanel.getCategory().ordinal() >= 5 ? x + 150 + categoryPanel.getCategory().ordinal() * 70
                                : categoryPanel.getCategory().ordinal() >= 4 ? x + 150 + categoryPanel.getCategory().ordinal() * 70
                                : categoryPanel.getCategory().ordinal() >= 3 ? x + 155 + categoryPanel.getCategory().ordinal() * 70
                                : categoryPanel.getCategory().ordinal() >= 2 ? x + 170 + categoryPanel.getCategory().ordinal() * 70
                                : categoryPanel.getCategory().ordinal() >= 1 ? x + 150 + categoryPanel.getCategory().ordinal() * 70
                                : x + 140 + categoryPanel.getCategory().ordinal() * 70) - 25, y + 10, 10 + FontManager.Icon.get(34).getStringWidth(categoryPanel.getCategory().icon) + FontManager.Bold.get(22).getStringWidth(categoryPanel.getCategory().name()), 19, 3, new Color(0xFFC5C5C5));
                FontManager.Icon.get(32).drawCenteredString(categoryPanel.getCategory().icon,
                        (categoryPanel.getCategory().ordinal() >= 6 ? x + 150 + categoryPanel.getCategory().ordinal() * 70 :
                                categoryPanel.getCategory().ordinal() >= 5 ? x + 150 + categoryPanel.getCategory().ordinal() * 70
                                        : categoryPanel.getCategory().ordinal() >= 4 ? x + 150 + categoryPanel.getCategory().ordinal() * 70
                                        : categoryPanel.getCategory().ordinal() >= 3 ? x + 155 + categoryPanel.getCategory().ordinal() * 70
                                        : categoryPanel.getCategory().ordinal() >= 2 ? x + 170 + categoryPanel.getCategory().ordinal() * 70
                                        : categoryPanel.getCategory().ordinal() >= 1 ? x + 150 + categoryPanel.getCategory().ordinal() * 70
                                        : x + 140 + categoryPanel.getCategory().ordinal() * 70) - 15, y + 15, linecolor.getRGB());
            }else {
                FontManager.Icon.get(32).drawCenteredString(categoryPanel.getCategory().icon,
                        (categoryPanel.getCategory().ordinal() >= 6 ? x + 150 + categoryPanel.getCategory().ordinal() * 70 :
                                categoryPanel.getCategory().ordinal() >= 5 ? x + 150 + categoryPanel.getCategory().ordinal() * 70
                                        : categoryPanel.getCategory().ordinal() >= 4 ? x + 150 + categoryPanel.getCategory().ordinal() * 70
                                        : categoryPanel.getCategory().ordinal() >= 3 ? x + 155 + categoryPanel.getCategory().ordinal() * 70
                                        : categoryPanel.getCategory().ordinal() >= 2 ? x + 170 + categoryPanel.getCategory().ordinal() * 70
                                        : categoryPanel.getCategory().ordinal() >= 1 ? x + 150 + categoryPanel.getCategory().ordinal() * 70
                                        : x + 140 + categoryPanel.getCategory().ordinal() * 70) - 15, y + 15, sb.getRGB());
            }
            FontManager.Bold.get(22).drawString(categoryPanel.getCategory().name(),
                    (categoryPanel.getCategory().ordinal() >= 6 ? x + 150 + categoryPanel.getCategory().ordinal() * 70 :
                            categoryPanel.getCategory().ordinal() >= 5 ? x + 150 + categoryPanel.getCategory().ordinal() * 70
                                    : categoryPanel.getCategory().ordinal() >= 4 ? x + 150 + categoryPanel.getCategory().ordinal() * 70
                                    : categoryPanel.getCategory().ordinal() >= 3 ? x + 155 + categoryPanel.getCategory().ordinal() * 70
                                    : categoryPanel.getCategory().ordinal() >= 2 ? x + 170 + categoryPanel.getCategory().ordinal() * 70
                                    : categoryPanel.getCategory().ordinal() >= 1 ? x + 150 + categoryPanel.getCategory().ordinal() * 70
                                    : x + 140 + categoryPanel.getCategory().ordinal() * 70) - 5, y + 15, categorycolor.getRGB());

        }

    }

    class Button {
        String name;
        public float x, y, width, height;
        public Button(String name){
            this.name = name;
        }
        public void drawScreen(int mouseX, int mouseY) {
            FontManager.Icon.get(30).drawString(name,x,y,categorycolor.getRGB());
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (RenderUtil.isHovering(x,y,100,40, mouseX, mouseY)) {
            dragging = true;
            dragX = x - mouseX;
            dragY = y - mouseY;
        }
        if (mouseButton == 0){
            for (CategoryPanel panel : categoryPanels) {
                if (handleCategoryPanel(panel, mouseX, mouseY)) {
                    break;
                }
            }
            if (RenderUtil.isHovering(x,y,136,42, mouseX, mouseY)) {
                dragging = true;
                dragX = x - mouseX;
                dragY = y - mouseY;
            }
        }
        CategoryPanel selected = getSelected();
        if (selected != null) {
            if (!RenderUtil.isHovering(x + 120, y + 39, w - 120, h,mouseX,mouseY)) return;
            selected.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0){
            dragging = false;
        }
        CategoryPanel selected = getSelected();
        if (selected != null) {
            selected.mouseReleased(mouseX, mouseY, state);
        }
    }
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        CategoryPanel selected = getSelected();
        if (selected != null) {
            selected.keyTyped(typedChar, keyCode);
        }
        super.keyTyped(typedChar, keyCode);
    }
    private boolean handleCategoryPanel(CategoryPanel panel, int mouseX, int mouseY) {
        if (RenderUtil.isHovering((panel.getCategory().ordinal() >= 6 ? x + 150 + panel.getCategory().ordinal() * 70 :
                panel.getCategory().ordinal() >= 5 ? x + 150 + panel.getCategory().ordinal() * 70
                        : panel.getCategory().ordinal() >= 4 ? x + 150 + panel.getCategory().ordinal() * 70
                        : panel.getCategory().ordinal() >= 3 ? x + 155 + panel.getCategory().ordinal() * 70
                        : panel.getCategory().ordinal() >= 2 ? x + 170 + panel.getCategory().ordinal() * 70
                        : panel.getCategory().ordinal() >= 1 ? x + 150 + panel.getCategory().ordinal() * 70
                        : x + 140 + panel.getCategory().ordinal() * 70) - 25, y + 10, 10 + FontManager.Icon.get(34).getStringWidth(panel.getCategory().icon) + FontManager.Bold.get(22).getStringWidth(panel.getCategory().name()), 19, mouseX, mouseY)) {
            for (CategoryPanel p : categoryPanels) {
                p.setSelected(false);
            }
            panel.setSelected(true);
            return true;
        }
        return false;
    }
    public CategoryPanel getSelected() {
        return categoryPanels.stream().filter(CategoryPanel::isSelected).findAny().orElse(null);
    }

    private static class QQAvatarUtil {
        private static final Map<String, ResourceLocation> avatarCache = new HashMap<>();

        public static void drawRoundedQQAvatar(String qqNumber, float x, float y, float size, float radius) {
            try {
                // 检查缓存
                ResourceLocation cached = avatarCache.get(qqNumber);
                if (cached != null) {
                    drawTextureWithRoundCorners(cached, x, y, size, size, radius);
                    return;
                }

                // 下载头像
                String avatarUrl = "http://q1.qlogo.cn/g?b=qq&nk=" + qqNumber + "&s=640";
                BufferedImage original = ImageIO.read(new URL(avatarUrl));

                // 转换为Minecraft纹理
                DynamicTexture dynamicTexture = new DynamicTexture(original);
                ResourceLocation avatarTexture = Minecraft.getMinecraft().getTextureManager()
                        .getDynamicTextureLocation("qq_avatar_" + qqNumber, dynamicTexture);

                // 缓存纹理
                avatarCache.put(qqNumber, avatarTexture);

                // 绘制圆角头像
                mc.getTextureManager().bindTexture(avatarTexture);
                Gui.drawScaledCustomSizeModalRect((int) x, (int) y, (float) 8.0, (float) 8.0, 8, 8, (int) size, (int) size, 64.0F, 64.0F);

            } catch (Exception e) {
                // 绘制默认头像
                RoundedUtil.drawRound(x, y, size, size, radius, Color.LIGHT_GRAY);
                FontManager.Semibold.get(12).drawCenteredString("QQ", x + size/2, y + size/2 - 6, Color.DARK_GRAY.getRGB());
            }
        }

        private static void drawTextureWithRoundCorners(ResourceLocation texture, float x, float y, float width, float height, float radius) {
            // 使用模板缓冲实现圆角
            StencilUtils.initStencilToWrite();
            RoundedUtil.drawRound(x, y, width, height, radius, Color.WHITE);
            StencilUtils.readStencilBuffer(1);

            // 绘制纹理
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
            Gui.drawModalRectWithCustomSizedTexture((int)x, (int)y, 0, 0, (int)width, (int)height, (int)width, (int)height);

            GL11.glDisable(GL11.GL_BLEND);
            StencilUtils.uninitStencilBuffer();
        }
    }
}
