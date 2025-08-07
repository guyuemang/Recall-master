package qwq.arcane.module.impl.misc;


import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.player.PlayerUtil;
import qwq.arcane.value.impl.BoolValue;

import java.util.Objects;

public class Teams extends Module {
    private static final BoolValue armorValue = new BoolValue("ArmorColor", true);
    private static final BoolValue colorValue = new BoolValue("Color", true);
    private static final BoolValue scoreboardValue = new BoolValue("ScoreboardTeam", true);


    public Teams() {
        super("Teams", Category.Misc);
    }
    @EventTarget
    public void onSuffix(UpdateEvent event){
        setsuffix(armorValue.getName().toString());
    }
    public static boolean isSameTeam(Entity entity) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) entity;
            if (Objects.requireNonNull(Client.Instance.getModuleManager().getModule(Teams.class)).getState()) {
                return (armorValue.getValue() && PlayerUtil.armorTeam(entityPlayer)) ||
                        (colorValue.getValue() && PlayerUtil.colorTeam(entityPlayer)) ||
                        (scoreboardValue.getValue() && PlayerUtil.scoreTeam(entityPlayer));
            }
            return false;
        }
        return false;
    }


}
