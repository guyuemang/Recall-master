package qwq.arcane.utils.player;

import lombok.Getter;
import qwq.arcane.module.Mine;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.network.OldServerPinger;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.TickEvent;
import qwq.arcane.utils.Instance;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static qwq.arcane.module.impl.visuals.InterFace.isOnHypixel;

public class PingerUtils implements Instance {

    public static long SERVER_UPDATE_TIME = 30000;

    private final OldServerPinger serverPinger;
    private final Map<String, Long> serverUpdateTime;
    private final Map<String, Boolean> serverUpdateStatus;

    @Getter
    private Long serverPing;

    @EventTarget
    public void onTickEvent(TickEvent event) {
        updateManually(Mine.getMinecraft().getCurrentServerData());
    }

    public PingerUtils() {
        this.serverPinger = new OldServerPinger();
        this.serverUpdateTime = new HashMap<>();
        this.serverUpdateStatus = new HashMap<>();
        this.serverPing = null;
        Client.Instance.getEventManager().register(this);
    }

    public static String getPing() {
        int latency = 0;
        if (!mc.isSingleplayer()) {
            NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
            if (info != null) latency = info.getResponseTime();

            if (isOnHypixel() && latency == 1) {
                int temp = Client.INSTANCE.getPingerUtils().getServerPing().intValue();
                if (temp != -1) {
                    latency = temp;
                }
            }
        } else {
            return "SinglePlayer";
        }

        return latency == 0 ? "?" : String.valueOf(latency);
    }

    public void updateManually(ServerData server) {
        if (server != null) {
            Long updateTime = serverUpdateTime.get(server.serverIP);
            if ((updateTime == null || updateTime + SERVER_UPDATE_TIME <= System.currentTimeMillis()) && !serverUpdateStatus.getOrDefault(server.serverIP, false)) {
                serverUpdateStatus.put(server.serverIP, true);

                new Thread(() -> {
                    try {
                        serverPinger.ping(server);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }

                    serverUpdateStatus.put(server.serverIP, false);
                    serverUpdateTime.put(server.serverIP, System.currentTimeMillis());
                }).start();
            }

            if (!isOnHypixel() || server.pingToServer != 1) {
                serverPing = server.pingToServer;
            }
        }
    }

}
