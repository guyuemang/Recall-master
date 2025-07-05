package qwq.arcane.event.impl.events.packet;

import qwq.arcane.event.impl.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;

@Getter@Setter
@AllArgsConstructor
public class PacketSendEvent extends CancellableEvent {
    private Packet<?> packet;
}
