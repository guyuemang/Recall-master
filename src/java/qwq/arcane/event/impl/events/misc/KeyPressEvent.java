
package qwq.arcane.event.impl.events.misc;

import lombok.Getter;
import qwq.arcane.event.impl.CancellableEvent;

@Getter
public class KeyPressEvent extends CancellableEvent {
    private final int key;

    public KeyPressEvent(int key) {
        this.key = key;
    }
}
