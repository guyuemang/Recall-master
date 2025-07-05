package qwq.arcane.event.impl.events.packet;

import qwq.arcane.event.impl.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.Packet;

@Getter
@AllArgsConstructor
public class PacketReceiveEvent extends CancellableEvent {
    private Packet<?> packet;
}
