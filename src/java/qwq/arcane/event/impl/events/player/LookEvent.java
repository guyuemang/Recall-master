package qwq.arcane.event.impl.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import qwq.arcane.event.impl.Event;
import qwq.arcane.utils.math.Vector2f;

/**
 * @Author：Guyuemang
 * @Date：2025/7/9 23:55
 */
@Getter
@Setter
@AllArgsConstructor
public class LookEvent implements Event {
    private Vector2f rotation;
}
