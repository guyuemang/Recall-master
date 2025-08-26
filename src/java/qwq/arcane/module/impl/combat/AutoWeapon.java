package qwq.arcane.module.impl.combat;


import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C02PacketUseEntity;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.gui.notification.Notification;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.player.InventoryUtil;

import static qwq.arcane.utils.pack.PacketUtil.sendPacketNoEvent;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:01 AM
 */

public class AutoWeapon extends Module {
    public AutoWeapon() {
        super("AutoWeapon", Category.Combat);
    }
    @EventTarget
    public void onPacket(PacketSendEvent event) {
        if (event.getPacket() instanceof C02PacketUseEntity packet && packet.getAction() == C02PacketUseEntity.Action.ATTACK) {
            int slot = -1;
            double maxDamage = -1.0;

            for (int i = 0; i <= 8; i++) {
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                if (stack != null && stack.getItem() instanceof ItemSword) {
                    double damage = stack.getAttributeModifiers().get("generic.attackDamage").stream().findFirst().map(AttributeModifier::getAmount).orElse(0.0)
                            + 1.25 * InventoryUtil.getEnchantment(stack, Enchantment.sharpness);
                    if (damage > maxDamage) {
                        maxDamage = damage;
                        slot = i;
                    }
                }
            }
            if (mc.thePlayer.getHeldItem().getItem() instanceof ItemSword){
            }else {
                Client.Instance.getNotification().add("Module Info", "Successfully switch weapons", Notification.Type.INFO);
            }
            if (slot == -1 || slot == mc.thePlayer.inventory.currentItem)
                return;

            mc.thePlayer.inventory.currentItem = slot;
            mc.playerController.updateController();
            Entity entity = packet.getEntityFromWorld(mc.theWorld);
            event.setCancelled(true);
            sendPacketNoEvent(new C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK));
        }
    }
}