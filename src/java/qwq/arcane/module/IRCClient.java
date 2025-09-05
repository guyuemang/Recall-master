package qwq.arcane.module;

import net.minecraft.entity.player.EntityPlayer;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.ChatEvent;
import qwq.arcane.utils.chats.ChatUtils;
import net.minecraft.client.Minecraft;
import qwq.arcane.utils.chats.IRC;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static qwq.arcane.utils.Instance.mc;

public class IRCClient {
    private static final String SERVER_HOST = "43.248.188.15";
    private static final int SERVER_PORT = 7122;

    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static ExecutorService executor;
    private static boolean connected = false;
    private static ScheduledExecutorService heartbeatScheduler;

    public static void connect() {
        if (connected) return;
        executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                socket = new Socket(SERVER_HOST, SERVER_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // 发送用户名
                out.println(ClientApplication.usernameField.getText());

                // 启动消息接收线程
                new Thread(IRCClient::receiveMessages).start();

                // 启动心跳线程
                heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
                heartbeatScheduler.scheduleAtFixedRate(IRCClient::sendHeartbeat, 0, 30, TimeUnit.SECONDS);

                connected = true;
                ChatUtils.sendMessage("§a[IRC] 已连接到服务器");
            } catch (IOException e) {
                ChatUtils.sendMessage("无法连接到IRC服务器: " + e.getMessage());
            }
        });
    }

    private static void sendHeartbeat() {
        if (connected && out != null) {
            out.println("HEARTBEAT");
        }
    }

    public static void disconnect() {
        if (!connected) return;
        try {
            if (out != null) {
                out.println("/quit");
            }
            if (socket != null) {
                socket.close();
            }
            if (executor != null) {
                executor.shutdown();
            }
            if (heartbeatScheduler != null) {
                heartbeatScheduler.shutdown();
            }
            connected = false;
            IRC.clearUsers();
            ChatUtils.sendMessage("已断开IRC连接");
        } catch (IOException e) {
            // 忽略关闭异常
        }
    }

    @EventTarget
    public void onChat(ChatEvent event) {
        String message = event.getMessage();

        // 处理IRC消息
        if (message.startsWith("-irc ")) {
            event.setCancelled(true);
            String ircMessage = message.substring(5);

            if (connected && out != null) {
                out.println(ircMessage);
                System.out.println("发送IRC消息: " + ircMessage);
            } else {
                ChatUtils.sendMessage("未连接到IRC服务器");
            }
        }
    }

    private static void receiveMessages() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                if (serverMessage.contains("HEARTBEAT") || serverMessage.contains("PING")) {
                    continue;
                }
                final String msg = serverMessage;
                System.out.println("收到IRC消息: " + msg);
                mc.addScheduledTask(() -> {
                    if (msg.startsWith("MESSAGE:")) {
                        String[] parts = msg.split(":", 3);
                        if (parts.length == 3) {
                            String sender = parts[1]; // 原始账户名
                            String content = parts[2];

                            // === 核心修改：获取游戏内显示名 ===
                            String displayName = getDisplayName(sender);

                            // 使用显示名注册用户
                            IRC.addUser(displayName);

                            // 发送消息时使用格式化的显示名
                            ChatUtils.sendIRCMessage(
                                    IRC.getIRCUserDisplayName(displayName + " | " + mc.thePlayer.getName()),
                                    content
                            );
                        }
                    } else if (msg.startsWith("SYSTEM:")) {
                        // 解析系统消息中的用户名（示例格式：SYSTEM:user:message）
                        String[] parts = msg.split(":", 3);
                        if (parts.length == 3) {
                            String user = parts[1];
                            String message = parts[2];

                            // 注册用户并获取显示名
                            String formattedMsg = "§d[IRC] §r" + IRC.getIRCUserDisplayName(user) + "§f: " + message;
                            mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(formattedMsg));
                        } else {
                            // 普通系统消息
                            String systemMsg = "§d[IRC] §f" + msg.substring(7);
                            mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(systemMsg));
                        }
                    } else if (msg.startsWith("USERLIST:")) {
                        // 添加用户列表同步逻辑（可选）
                        String[] users = msg.substring(9).split(",");
                        for (String user : users) {
                            if (!user.isEmpty()) {
                            }
                        }
                    }
                });
            }
        } catch (IOException e) {
            mc.addScheduledTask(() -> {
                if (connected) {
                    ChatUtils.sendMessage("IRC连接已断开");
                    connected = false;
                }
            });
        }
    }
    private static final Map<String, String> displayNameCache = new ConcurrentHashMap<>();
    private static String getDisplayName(String username) {
        // 1. 优先从缓存读取
        if (displayNameCache.containsKey(username)) {
            return displayNameCache.get(username);
        }

        // 2. 查询游戏内玩家实体
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player.getName().equalsIgnoreCase(username)) {
                String displayName = player.getDisplayName().getUnformattedText();
                displayNameCache.put(username, displayName);
                return displayName;
            }
        }

        // 3. 若未找到则使用原始用户名（回退方案）
        return username;
    }

    // 新增：清除缓存的工具方法（可选）
    public static void clearDisplayNameCache() {
        displayNameCache.clear();
    }
}