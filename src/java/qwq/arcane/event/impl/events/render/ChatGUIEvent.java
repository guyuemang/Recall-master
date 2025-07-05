package qwq.arcane.event.impl.events.render;

import lombok.Getter;
import qwq.arcane.event.impl.CancellableEvent;

/**
 * @Author: Guyuemang
 * 2025/4/22
 */
@Getter
public class ChatGUIEvent extends CancellableEvent {
    private final int mouseX,mouseY;

    public ChatGUIEvent(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }
}
