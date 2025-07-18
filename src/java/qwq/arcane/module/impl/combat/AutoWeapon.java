package qwq.arcane.module.impl.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import qwq.arcane.Client;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.player.AttackEvent;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.utils.player.PlayerUtil;
import qwq.arcane.utils.player.SlotSpoofComponent;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.NumberValue;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:01 AM
 */
public class AutoWeapon extends Module {
    private int originalSlot = -1;
    private Entity currentTarget = null;
    private long lastAttackTime = 0;

    public final BoolValue spoof = new BoolValue("Spoof", true);
    public final BoolValue useAxes = new BoolValue("UseAxes", true);
    public final BoolValue preferAxes = new BoolValue("PreferAxes", false);
    public final BoolValue Enchants = new BoolValue("Enchants", true);
    public final NumberValue delay = new NumberValue("Delay", 500, 0, 2000, 50);

    public AutoWeapon() {
        super("AutoWeapon", Category.Combat);
    }

    @Override
    public void onEnable() {
        originalSlot = -1;
        currentTarget = null;
    }

    @Override
    public void onDisable() {
        restoreOriginalSlot();
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (!isEnabled()) return;

        currentTarget = event.getTargetEntity();
        lastAttackTime = System.currentTimeMillis();

        if (originalSlot == -1) {
            originalSlot = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
        }

        int bestSlot = findBestWeaponSlot();
        if (bestSlot != -1) {
            switchToWeapon(bestSlot);
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (!isEnabled() || originalSlot == -1) return;

        if (shouldRestoreOriginalSlot()) {
            restoreOriginalSlot();
        }
    }

    private int findBestWeaponSlot() {
        int bestSlot = -1;
        float bestDamage = 0;
        boolean foundAxe = false;

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(slot);
            if (stack == null) continue;

            Item item = stack.getItem();
            float damage = getWeaponDamage(stack);

            if (item instanceof ItemSword || (useAxes.get() && item instanceof ItemAxe)) {
                boolean isAxe = item instanceof ItemAxe;

                if (foundAxe && !isAxe && preferAxes.get()) continue;

                if (damage > bestDamage || bestSlot == -1) {
                    bestSlot = slot;
                    bestDamage = damage;
                    foundAxe = isAxe;
                }
            }
        }

        return bestSlot;
    }

    private float getWeaponDamage(ItemStack stack) {
        float damage = 0;

        if (stack.getItem() instanceof ItemSword) {
            ItemSword sword = (ItemSword) stack.getItem();
            damage = sword.getDamageVsEntity();
        } else if (stack.getItem() instanceof ItemAxe) {
            ItemAxe axe = (ItemAxe) stack.getItem();
            damage = axe.getMaxDamage();
        }

        if (Enchants.get()) {
            int sharpnessLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
            damage += sharpnessLevel * 1.25F;
        }

        return damage;
    }

    private void switchToWeapon(int slot) {
        if (spoof.get()) {
            SlotSpoofComponent.startSpoofing(slot);
        } else {
            Minecraft.getMinecraft().thePlayer.inventory.currentItem = slot;
        }
    }

    private void restoreOriginalSlot() {
        if (originalSlot == -1) return;

        if (spoof.get()) {
            SlotSpoofComponent.stopSpoofing();
        } else {
            Minecraft.getMinecraft().thePlayer.inventory.currentItem = originalSlot;
        }

        originalSlot = -1;
        currentTarget = null;
    }

    private boolean shouldRestoreOriginalSlot() {
        if (currentTarget == null || currentTarget.isDead) {
            return true;
        }

        double distance = Minecraft.getMinecraft().thePlayer.getDistanceToEntity(currentTarget);
        if (distance > 6.0) {
            return true;
        }

        if (System.currentTimeMillis() - lastAttackTime > delay.get().longValue()) {
            return true;
        }

        return false;
    }
}