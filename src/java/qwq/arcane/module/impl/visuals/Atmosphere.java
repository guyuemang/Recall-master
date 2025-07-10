
package qwq.arcane.module.impl.visuals;

import net.minecraft.network.play.server.S03PacketTimeUpdate;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.value.impl.BooleanValue;
import qwq.arcane.value.impl.ColorValue;
import qwq.arcane.value.impl.ModeValue;
import qwq.arcane.value.impl.NumberValue;

import java.awt.*;

public class Atmosphere extends Module {
    private final BooleanValue time = new BooleanValue("Time Editor", true);
    private final NumberValue timeValue = new NumberValue("Time", time::get, 18000, 0, 24000, 1000);
    private static final BooleanValue weather = new BooleanValue("Weather Editor", true);
    public static final ModeValue weatherValue = new ModeValue("Weather", weather::get, "Clean",
            new String[]{"Clean", "Rain", "Thunder", "Snow", "Blizzard"});
    public static final BooleanValue forceSnow = new BooleanValue("Force Snow", false);
    public final BooleanValue worldColor = new BooleanValue("World Color", true);
    public final ColorValue worldColorRGB = new ColorValue("World Color RGB", worldColor::get, Color.WHITE);
    public final BooleanValue worldFog = new BooleanValue("World Fog", false);
    public final ColorValue worldFogRGB = new ColorValue("World Fog RGB", worldFog::get, Color.WHITE);
    public final NumberValue worldFogDistance = new NumberValue("World Fog Distance", worldFog::get, 0.10F, -1F, 0.9F, 0.1F);

    public Atmosphere() {
        super("Atmosphere",Category.Visuals);
    }

    @EventTarget
    private void onUpdate(UpdateEvent event) {
        if(time.get())
            mc.theWorld.setWorldTime((long) timeValue.get().longValue());
        if (weather.get()) {
            switch (weatherValue.get()) {
                case "Rain":
                    mc.theWorld.setRainStrength(1);
                    mc.theWorld.setThunderStrength(0);
                    break;
                case "Thunder":
                    mc.theWorld.setRainStrength(1);
                    mc.theWorld.setThunderStrength(1);
                    break;
                case "Snow":
                    mc.theWorld.setRainStrength(0.5f);
                    mc.theWorld.setThunderStrength(0);
                    break;
                case "Blizzard":
                    mc.theWorld.setRainStrength(1);
                    mc.theWorld.setThunderStrength(0);
                    break;
                default:
                    mc.theWorld.setRainStrength(0);
                    mc.theWorld.setThunderStrength(0);
            }
        }
    }

    @EventTarget
    private void onPacket(PacketSendEvent event) {
        if (time.get() && event.getPacket() instanceof S03PacketTimeUpdate)
            event.setCancelled(true);
    }

    public static boolean shouldForceSnow() {
        return forceSnow.get() && (weatherValue.get().equals("Snow") || weatherValue.get().equals("Blizzard"));
    }
}
