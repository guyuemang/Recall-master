package qwq.arcane.event.impl.events.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import qwq.arcane.event.impl.CancellableEvent;

/**
 * @Author: Guyuemang
 * 2025/4/21
 */
@Getter
@AllArgsConstructor
public class ChatEvent extends CancellableEvent {
    private final String message;
}
