
package qwq.arcane.event.impl.events.player;

import qwq.arcane.event.impl.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;

@Getter
@AllArgsConstructor
public final class AttackEvent extends CancellableEvent {
    private final Entity targetEntity;
}
