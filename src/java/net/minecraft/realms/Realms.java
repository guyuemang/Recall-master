package net.minecraft.realms;

import com.google.common.util.concurrent.ListenableFuture;
import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import java.net.Proxy;

import qwq.arcane.module.Mine;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.Session;
import net.minecraft.world.WorldSettings;

public class Realms
{
    public static boolean isTouchScreen()
    {
        return Mine.getMinecraft().gameSettings.touchscreen;
    }

    public static Proxy getProxy()
    {
        return Mine.getMinecraft().getProxy();
    }

    public static String sessionId()
    {
        Session session = Mine.getMinecraft().getSession();
        return session == null ? null : session.getSessionID();
    }

    public static String userName()
    {
        Session session = Mine.getMinecraft().getSession();
        return session == null ? null : session.getUsername();
    }

    public static long currentTimeMillis()
    {
        return Mine.getSystemTime();
    }

    public static String getSessionId()
    {
        return Mine.getMinecraft().getSession().getSessionID();
    }

    public static String getUUID()
    {
        return Mine.getMinecraft().getSession().getPlayerID();
    }

    public static String getName()
    {
        return Mine.getMinecraft().getSession().getUsername();
    }

    public static String uuidToName(String p_uuidToName_0_)
    {
        return Mine.getMinecraft().getSessionService().fillProfileProperties(new GameProfile(UUIDTypeAdapter.fromString(p_uuidToName_0_), (String)null), false).getName();
    }

    public static void setScreen(RealmsScreen p_setScreen_0_)
    {
        Mine.getMinecraft().displayGuiScreen(p_setScreen_0_.getProxy());
    }

    public static String getGameDirectoryPath()
    {
        return Mine.getMinecraft().mcDataDir.getAbsolutePath();
    }

    public static int survivalId()
    {
        return WorldSettings.GameType.SURVIVAL.getID();
    }

    public static int creativeId()
    {
        return WorldSettings.GameType.CREATIVE.getID();
    }

    public static int adventureId()
    {
        return WorldSettings.GameType.ADVENTURE.getID();
    }

    public static int spectatorId()
    {
        return WorldSettings.GameType.SPECTATOR.getID();
    }

    public static void setConnectedToRealms(boolean p_setConnectedToRealms_0_)
    {
        Mine.getMinecraft().setConnectedToRealms(p_setConnectedToRealms_0_);
    }

    public static ListenableFuture<Object> downloadResourcePack(String p_downloadResourcePack_0_, String p_downloadResourcePack_1_)
    {
        ListenableFuture<Object> listenablefuture = Mine.getMinecraft().getResourcePackRepository().downloadResourcePack(p_downloadResourcePack_0_, p_downloadResourcePack_1_);
        return listenablefuture;
    }

    public static void clearResourcePack()
    {
        Mine.getMinecraft().getResourcePackRepository().clearResourcePack();
    }

    public static boolean getRealmsNotificationsEnabled()
    {
        return Mine.getMinecraft().gameSettings.getOptionOrdinalValue(GameSettings.Options.REALMS_NOTIFICATIONS);
    }

    public static boolean inTitleScreen()
    {
        return Mine.getMinecraft().currentScreen != null && Mine.getMinecraft().currentScreen instanceof GuiMainMenu;
    }
}
