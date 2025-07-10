
package qwq.arcane.gui.clickgui.dropdown.setting.impl;

import qwq.arcane.gui.clickgui.Component;
import qwq.arcane.module.impl.visuals.InterFace;
import qwq.arcane.utils.animations.Animation;
import qwq.arcane.utils.animations.Direction;
import qwq.arcane.utils.animations.impl.DecelerateAnimation;
import qwq.arcane.utils.color.ColorUtil;
import qwq.arcane.utils.render.RenderUtil;
import qwq.arcane.utils.render.RoundedUtil;
import qwq.arcane.value.impl.TextValue;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author：Guyuemang
 * @Date：2025/7/3 12:31
 */
public class StringComponent extends Component {
    private final TextValue setting;
    private final Animation input = new DecelerateAnimation(250, 1);
    private boolean inputting;
    private String text = "";
    public StringComponent(TextValue setting) {
        this.setting = setting;
        setHeight(Bold.get(14).getHeight() * 2 + 6);
        input.setDirection(Direction.BACKWARDS);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        input.setDirection(inputting ? Direction.FORWARDS : Direction.BACKWARDS);
        text = setting.get();
        if (setting.isOnlyNumber() && !NumberUtils.isNumber(text)) {
            text = text.replaceAll("[a-zA-Z]", "");
        }
        String textToDraw = setting.get().isEmpty() && !inputting ? "Empty..." : setting.getText();
        RoundedUtil.drawRound(getX(),getY() + Bold.get(14).getHeight() - 2,getWidth() ,Bold.get(14).getHeight() + 4,2, InterFace.mainColor.get());
        Bold.get(14).drawString(setting.getName(),getX() + 4,getY(),-1);
        drawTextWithLineBreaks(textToDraw + (inputting && text.length() < 59 && System.currentTimeMillis() % 1000 > 500 ? "|" : ""),getX() + 6,getY() + Bold.get(14).getHeight() + 2,getWidth() - 12);
        super.drawScreen(mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (RenderUtil.isHovering(getX(),getY() + Bold.get(14).getHeight() + 4,getWidth(),4,mouseX,mouseY) && mouseButton == 0){
            inputting = !inputting;
        } else {
            inputting = false;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (setting.isOnlyNumber() && !NumberUtils.isNumber(String.valueOf(typedChar))) {
            return;
        }
        if (inputting){
            if (keyCode == Keyboard.KEY_BACK) {
                deleteLastCharacter();
            }

            if (text.length() < 18 && (Character.isLetterOrDigit(typedChar) || keyCode == Keyboard.KEY_SPACE)) {
                text += typedChar;
                setting.setText(text);
            }
        }
        super.keyTyped(typedChar, keyCode);
    }
    private void drawTextWithLineBreaks(String text, float x, float y, float maxWidth) {
        String[] lines = text.split("\n");
        float currentY = y;

        for (String line : lines) {
            List<String> wrappedLines = wrapText(line, 0, maxWidth);
            for (String wrappedLine : wrappedLines) {

                Bold.get(14).drawString(wrappedLine, x, currentY, ColorUtil.interpolateColor2(Color.GRAY,Color.WHITE, (float) input.getOutput().floatValue()));
                currentY += Bold.get(14).getHeight();
            }
        }
    }

    private List<String> wrapText(String text, float size, float maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (Bold.get(14).getStringWidth(word) <= maxWidth) {
                if (Bold.get(14).getStringWidth(currentLine.toString() + word) <= maxWidth) {
                    currentLine.append(word).append(" ");
                } else {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word).append(" ");
                }
            } else {
                if (!currentLine.toString().isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                currentLine = breakAndAddWord(word, currentLine, size, lines);
            }
        }

        if (!currentLine.toString().isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
    private void deleteLastCharacter() {
        if (!text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
            setting.setText(text);
        }
    }
    private StringBuilder breakAndAddWord(String word, StringBuilder currentLine, float maxWidth, List<String> lines) {
        int wordLength = word.length();
        for (int i = 0; i < wordLength; i++) {
            char c = word.charAt(i);
            String nextPart = currentLine.toString() + c;
            if (Bold.get(14).getStringWidth(nextPart) <= maxWidth) {
                currentLine.append(c);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(String.valueOf(c));
            }
        }
        return currentLine;
    }
    @Override
    public boolean isVisible() {
        return this.setting.isAvailable();
    }
}
