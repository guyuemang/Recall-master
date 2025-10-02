package qwq.arcane.utils.pack;

import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import qwq.arcane.event.annotations.EventPriority;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.WorldLoadEvent;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.utils.time.TimerUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import static qwq.arcane.utils.Instance.mc;

public class BlinkComponent {

}
