package qwq.arcane.event.impl.events.player;

import lombok.Getter;
import lombok.Setter;
import qwq.arcane.event.impl.CancellableEvent;

/**
 * @author FuMeng
 * @since 2024/6/1 1:14
 */
@Setter
@Getter
public class PlaceEvent extends CancellableEvent {
    private boolean shouldRightClick;
    private int slot;

    public PlaceEvent(int slot) {
        this.slot = slot;
    }

}
