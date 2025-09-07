package qwq.arcane.module.impl.player;


import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.util.DamageSource;
import qwq.arcane.event.annotations.EventTarget;
import qwq.arcane.event.impl.events.misc.WorldLoadEvent;
import qwq.arcane.event.impl.events.packet.PacketReceiveSyncEvent;
import qwq.arcane.event.impl.events.packet.PacketSendEvent;
import qwq.arcane.event.impl.events.player.AttackEvent;
import qwq.arcane.event.impl.events.player.MotionEvent;
import qwq.arcane.event.impl.events.player.UpdateEvent;
import qwq.arcane.module.Category;
import qwq.arcane.module.Module;
import qwq.arcane.module.impl.combat.KillAura;
import qwq.arcane.module.impl.movement.GuiMove;
import qwq.arcane.module.impl.world.Scaffold;
import qwq.arcane.utils.math.MathUtils;
import qwq.arcane.utils.pack.PacketUtil;
import qwq.arcane.utils.player.InventoryUtil;
import qwq.arcane.utils.player.InventoryUtil2;
import qwq.arcane.utils.player.PlayerUtil;
import qwq.arcane.utils.player.SelectorDetectionComponent;
import qwq.arcane.utils.time.StopWatch;
import qwq.arcane.utils.time.TimerUtil;
import qwq.arcane.value.impl.BoolValue;
import qwq.arcane.value.impl.ModeValue;
import qwq.arcane.value.impl.NumberValue;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

import static qwq.arcane.utils.pack.PacketUtil.sendPacket;

/**
 * @Author：Guyuemang
 * @Date：7/7/2025 12:02 AM
 */

//依旧Solitude老鼠搬新家
public class InvManager extends Module {
    private final ModeValue modeValue = new ModeValue("Mode",  "Basic", new String[]{"Basic", "OpenInv"});
    private final NumberValue delay = new NumberValue("Delay", 150, 0, 500,1);

    private final BoolValue autoArmor = new BoolValue("AutoArmor", true);
    private final BoolValue dropItems = new BoolValue("Drop Items", true);

    public final BoolValue keepBucket = new BoolValue("Keep Bucket", true);
    public final BoolValue keepOtherFood = new BoolValue("Keep Other Food", false);
    public final BoolValue keepProjectiles = new BoolValue("Keep Projectiles", false);
    public final BoolValue keepFishingRod = new BoolValue("Keep Fishing Rod", false);

    private final NumberValue swordSlot = new NumberValue("Sword Slot", 1, 0, 9,1);
    private final NumberValue throwableSlot = new NumberValue("Throwable Slot", 2, 0, 9,1);
    private final NumberValue gappleSlot = new NumberValue("Gapple Slot", 3, 0, 9,1);
    private final NumberValue blockSlot = new NumberValue("Block Slot", 4, 0, 9,1);
    private final NumberValue bucketSlot = new NumberValue("Bucket Slot", 7, 0, 9,1);
    private final NumberValue potionSlot = new NumberValue("Potion Slot", 8, 0, 9,1);
    private final NumberValue pickaxeSlot = new NumberValue("Pickaxe Slot", 8, 0, 9,1);
    private final NumberValue axeSlot = new NumberValue("Axe Slot", 9, 0, 9,1);

    @Getter
    private boolean moved, open;
    private long nextClick;
    public short action;

    public final TimerUtil timerUtil = new TimerUtil();
    private int chestTicks, attackTicks, placeTicks;

    public InvManager() {
        super("Manager",Category.Player);
    }

    @Override
    public void onDisable() {
        if (this.canOpenInventory()) {
            this.closeInventory();
        }
        super.onDisable();
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        this.attackTicks = 0;
    }

    @EventTarget
    public void onPacketSend(PacketSendEvent event) {
        if (KillAura.target != null || isEnabled(Scaffold.class)){
            return;
        }
        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            this.placeTicks = 0;
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (event.isPre()) {
            if (KillAura.target != null || mc.thePlayer.isUsingItem() || isEnabled(Scaffold.class)){
                return;
            }
            if (mc.thePlayer.ticksExisted <= 40) return;

            if (mc.currentScreen instanceof GuiChest) {
                this.chestTicks = 0;
            } else {
                this.chestTicks++;
            }

            this.moved = false;

            this.attackTicks++;
            this.placeTicks++;

            if (!this.timerUtil.hasTimeElapsed(this.nextClick) || this.chestTicks < 10 || this.attackTicks < 10 || this.placeTicks < 10) {
                this.closeInventory();
                return;
            }

            if (modeValue.is("OpenInv") && !(mc.currentScreen instanceof GuiInventory)) {
                return;
            }

            int INVENTORY_SLOTS = 4 * 9 + 4;
            int throwable = -1, bucket = -1;
            int helmet = -1, chestplate = -1, leggings = -1, boots = -1;
            int sword = -1, pickaxe = -1, axe = -1, block = -1, potion = -1, food = -1;

            Set<Integer> keepSlots = new HashSet<>();

            for (int i = 0; i < INVENTORY_SLOTS; i++) {
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                if (stack == null) continue;

                Item item = stack.getItem();

                if (!InventoryUtil2.isValid(stack)) continue;

                if (autoArmor.get() && item instanceof ItemArmor armor) {
                    int reduction = armorReduction(stack);
                    switch (armor.armorType) {
                        case 0:
                            if (helmet == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(helmet))) helmet = i;
                            break;
                        case 1:
                            if (chestplate == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(chestplate))) chestplate = i;
                            break;
                        case 2:
                            if (leggings == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(leggings))) leggings = i;
                            break;
                        case 3:
                            if (boots == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(boots))) boots = i;
                            break;
                    }
                    continue;
                }

                if (item instanceof ItemSpade) continue;

                if (item instanceof ItemSword) {
                    float swordScore = InventoryUtil2.calculateSwordScore(stack);
                    int fireAspect = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);

                    if (sword == -1) {
                        sword = i;
                    } else {
                        ItemStack currentBest = mc.thePlayer.inventory.getStackInSlot(sword);
                        float bestScore = InventoryUtil2.calculateSwordScore(currentBest);
                        int currentFireAspect = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, currentBest);

                        if (fireAspect > 0 && currentFireAspect == 0) {
                            sword = i;
                        } else if (fireAspect > 0 && currentFireAspect > 0 && swordScore > bestScore) {
                            sword = i;
                        } else if (fireAspect == 0 && currentFireAspect == 0 && swordScore > bestScore) {
                            sword = i;
                        }
                    }
                    continue;
                }

                if (item instanceof ItemPickaxe) {
                    if (pickaxe == -1 || InventoryUtil2.mineSpeed(stack) > InventoryUtil2.mineSpeed(mc.thePlayer.inventory.getStackInSlot(pickaxe))) pickaxe = i;
                    continue;
                }

                if (item instanceof ItemAxe) {
                    if (axe == -1 || InventoryUtil2.mineSpeed(stack) > InventoryUtil2.mineSpeed(mc.thePlayer.inventory.getStackInSlot(axe))) axe = i;
                    continue;
                }

                if (item instanceof ItemBlock) {
                    keepSlots.add(i);

                    if (block == -1) {
                        block = i;
                    } else {
                        ItemStack currentBlock = mc.thePlayer.inventory.getStackInSlot(block);
                        if (stack.stackSize > currentBlock.stackSize) {
                            block = i;
                        } else if (stack.stackSize == currentBlock.stackSize) {
                            if (Item.getIdFromItem(stack.getItem()) > Item.getIdFromItem(currentBlock.getItem())) {
                                block = i;
                            }
                        }
                    }
                    continue;
                }

                if (item instanceof ItemPotion potionItem) {
                    if (potion == -1) potion = i;
                    else {
                        int curRank = PlayerUtil.potionRanking(((ItemPotion) mc.thePlayer.inventory.getStackInSlot(potion).getItem()).getEffects(mc.thePlayer.inventory.getStackInSlot(potion)).get(0).getPotionID());
                        int newRank = PlayerUtil.potionRanking(potionItem.getEffects(stack).get(0).getPotionID());
                        if (newRank > curRank) potion = i;
                    }
                    continue;
                }

                if (item instanceof ItemFood itemFood) {
                    boolean isGoldenApple = item == Item.getItemById(322) || item == Item.getItemById(466);

                    if (isGoldenApple || keepOtherFood.get()) {
                        keepSlots.add(i);

                        if (food == -1) {
                            food = i;
                        } else {
                            ItemStack currentBestStack = mc.thePlayer.inventory.getStackInSlot(food);
                            float curSat = ((ItemFood) currentBestStack.getItem()).getSaturationModifier(currentBestStack);
                            float newSat = itemFood.getSaturationModifier(stack);

                            if (newSat > curSat) {
                                food = i;
                            }
                        }
                    }
                }
            }

            Stream.of(helmet, chestplate, leggings, boots, sword, pickaxe, axe, block, potion, food).filter(slot -> slot != -1).forEach(keepSlots::add);

            for (int i = 0; i < INVENTORY_SLOTS; i++) {
                if (!keepSlots.contains(i)) {
                    ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                    if (stack == null) continue;
                    Item item = stack.getItem();

                    if (item instanceof ItemBucket) {
                        if (bucket == -1) {
                            bucket = i;
                            throwItem(bucket);
                        }
                        continue;
                    }

                    if (item instanceof ItemSnowball || item instanceof ItemEgg || item instanceof ItemEnderPearl) {
                        if (throwable == -1 || stack.stackSize > mc.thePlayer.inventory.getStackInSlot(throwable).stackSize) {
                            throwable = i;
                        }
                        continue;
                    }

                    if (item instanceof ItemSword) {
                        int durability = stack.getMaxDamage() - stack.getItemDamage();
                        int fireAspect = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);

                        if (durability < 30 && fireAspect == 0) {
                            throwItem(i);
                        } else if (i != sword) {
                            throwItem(i);
                        }
                        continue;
                    }

                    if (item instanceof ItemSpade || !InventoryUtil2.isValid(stack)) {
                        throwItem(i);
                    } else if (item instanceof ItemFood && item != Item.getItemById(322)
                            && item != Item.getItemById(466)
                            && !keepOtherFood.get()) {
                        throwItem(i);
                    }
                }
            }

            if (autoArmor.get()) {
                if (helmet != -1 && helmet != 39) equipItem(helmet);
                if (chestplate != -1 && chestplate != 38) equipItem(chestplate);
                if (leggings != -1 && leggings != 37) equipItem(leggings);
                if (boots != -1 && boots != 36) equipItem(boots);
            }

            if (sword != -1) this.moveItemToSlot(sword, swordSlot);
            if (pickaxe != -1) this.moveItemToSlot(pickaxe, pickaxeSlot);
            if (axe != -1) this.moveItemToSlot(axe, axeSlot);
            if (potion != -1) this.moveItemToSlot(potion, potionSlot);
            if (food != -1) this.moveItemToSlot(food, gappleSlot);
            if (throwable != -1) this.moveItemToSlot(throwable, throwableSlot);
            if (bucket != -1) this.moveItemToSlot(bucket, bucketSlot);

            if (block != -1 && blockSlot.getValue() > 0 && block != blockSlot.getValue()-1 && !isEnabled(Scaffold.class)) {
                ItemStack currentSlot = mc.thePlayer.inventory.getStackInSlot((int)(blockSlot.getValue()-1));
                if (currentSlot == null || !ItemStack.areItemStacksEqual(
                        mc.thePlayer.inventory.getStackInSlot(block),
                        currentSlot)) {
                    moveItem(block, (int)(blockSlot.getValue()-37));
                }
            }

            if (canOpenInventory() && !moved) closeInventory();
        }
    }

    private boolean canOpenInventory() {
        return isEnabled(GuiMove.class) && !(mc.currentScreen instanceof GuiInventory);
    }

    private void moveItemToSlot(int itemIndex, NumberValue slotSetting) {
        if (slotSetting.getValue() > 0 && itemIndex != slotSetting.getValue()-1) {
            moveItem(itemIndex, (int)(slotSetting.getValue()-37));
        }
    }

    private void openInventory() {
        if (!this.open) {
            PacketUtil.sendPacket(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            this.open = true;
        }
    }

    private void throwItem(final int slot) {
        if ((!this.moved || this.nextClick <= 0) && !SelectorDetectionComponent.selector(slot) && dropItems.get()) {
            if (this.canOpenInventory()) openInventory();
            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 1, 4, mc.thePlayer);
            this.updateNextClick();
        }
    }

    private void moveItem(int slot, int destination) {
        if ((!this.moved || this.nextClick <= 0) && !SelectorDetectionComponent.selector(slot)) {
            if (this.canOpenInventory()) openInventory();
            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), this.slot(destination), 2, mc.thePlayer);
            this.updateNextClick();
        }
    }

    private void equipItem(int slot) {
        if ((!this.moved || this.nextClick <= 0) && !SelectorDetectionComponent.selector(slot) && autoArmor.get()) {
            if (this.canOpenInventory()) openInventory();
            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 0, 1, mc.thePlayer);
            this.updateNextClick();
        }
    }

    private void updateNextClick() {
        this.nextClick = Math.round((float) MathUtils.getRandom(this.delay.getValue().intValue(), this.delay.getValue().intValue()));
        this.timerUtil.reset();
        this.moved = true;
    }
    @EventTarget
    public void onWorld(WorldLoadEvent e){
        this.setState(false);
    }
    private void closeInventory() {
        if (this.open) {
            PacketUtil.sendPacket(new C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId));
            this.open = false;
        }
    }

    private int armorReduction(ItemStack stack) {
        ItemArmor armor = (ItemArmor) stack.getItem();
        return armor.damageReduceAmount + EnchantmentHelper.getEnchantmentModifierDamage(new ItemStack[]{stack}, DamageSource.generic);
    }

    private int slot(final int slot) {
        if (slot >= 36) {
            return 8 - (slot - 36);
        }

        if (slot < 9) {
            return slot + 36;
        }

        return slot;
    }
}
