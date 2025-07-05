package qwq.arcane.event.impl.events.packet;

import qwq.arcane.event.impl.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;

@Getter
@AllArgsConstructor
public class HigherPacketEvent implements Event {
    @Setter
    private Packet<?> packet;
}
