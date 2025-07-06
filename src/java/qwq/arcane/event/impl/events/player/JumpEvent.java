package qwq.arcane.event.impl.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import qwq.arcane.event.impl.CancellableEvent;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:50 AM
 */
@Getter
@Setter
@AllArgsConstructor
public class JumpEvent extends CancellableEvent {
    private float motionY;
    private float yaw;
}
