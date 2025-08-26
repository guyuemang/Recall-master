/*
 * MoonLight Hacked Client
 *
 * A free and open-source hacked client for Minecraft.
 * Developed using Minecraft's resources.
 *
 * Repository: https://github.com/randomguy3725/MoonLight
 *
 * Author(s): [Randumbguy & wxdbie & opZywl & MukjepScarlet & lucas & eonian]
 */
package qwq.arcane.utils.player;

import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import qwq.arcane.module.impl.player.InvManager;
import qwq.arcane.utils.Instance;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class InventoryUtil2 implements Instance {
    public static final int INCLUDE_ARMOR_BEGIN = 5;
    public static final int EXCLUDE_ARMOR_BEGIN = 9;
    public static final int END = 45;


    private static final IntSet BAD_EFFECTS_IDS = IntSet.of(
            Potion.poison.id, Potion.weakness.id, Potion.wither.id, Potion.blindness.id, Potion.digSlowdown.id, Potion.harm.id
    );

    public static void forEachInventorySlot(final int begin, final int end, final SlotConsumer consumer) {
        for (int i = begin; i < end; ++i) {
            final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null) {
                consumer.accept(i, stack);
            }
        }
    }

    public static boolean isValid(ItemStack stack) {
        if (stack == null) return false;
        if (stack.getItem() instanceof ItemBlock) {
            return isGoodBlockStack(stack);
        } else if (stack.getItem() instanceof ItemSword) {
            return isBestSword(stack);
        } else if (stack.getItem() instanceof ItemTool) {
            return isBestTool(stack);
        } else if (stack.getItem() instanceof ItemArmor) {
            return isBestArmor(stack);
        } else if (stack.getItem() instanceof ItemPotion) {
            return isBuffPotion(stack);
        } else if (stack.getItem() instanceof ItemFood) {
            return isGoodFood(stack);
        } else if (stack.getItem() instanceof ItemEnderPearl) {
            return true;
        } else return isGoodItem(stack);
    }

    public static float calculateSwordScore(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemSword sword)) return -1;

        float score = 0;

        score += sword.getDamageVsEntity();

        int sharpness = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
        score += sharpness * 1.25f;

        int fireAspect = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
        if (fireAspect > 0) {
            score += fireAspect * 1.5f;
        }

        int looting = EnchantmentHelper.getEnchantmentLevel(Enchantment.looting.effectId, stack);
        if (looting > 0) {
            score += looting * 0.3f;
        }

        float durabilityRatio = 1.0f - (float) stack.getItemDamage() / stack.getMaxDamage();
        score += durabilityRatio * 5.0f;

        return score;
    }

    public static float mineSpeed(final ItemStack stack) {
        final Item item = stack.getItem();
        int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack);

        level = switch (level) {
            case 1 -> 30;
            case 2 -> 69;
            case 3 -> 120;
            case 4 -> 186;
            case 5 -> 271;
            default -> 0;
        };

        if (item instanceof ItemPickaxe pickaxe) {
            return pickaxe.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        } else if (item instanceof ItemSpade shovel) {
            return shovel.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        } else if (item instanceof ItemAxe axe) {
            return axe.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        }

        return 0;
    }

    public static boolean isGoodFood(final ItemStack stack) {
        ItemFood food = (ItemFood) stack.getItem();
        InvManager invManager = INSTANCE.getModuleManager().getModule(InvManager.class);

        if (food instanceof ItemAppleGold) return true;

        if (invManager.isEnabled() && invManager.keepOtherFood.get()) {
            return true;
        }

        return food.getHealAmount(stack) >= 4 && food.getSaturationModifier(stack) >= 0.3f;
    }

    public static boolean isGoodBlockStack(final ItemStack stack) {
        return stack.stackSize >= 1 && isValidBlock(Block.getBlockFromItem(stack.getItem()), true);
    }

    public static boolean isValidBlock(final Block block, final boolean toPlace) {
        if (block instanceof BlockContainer || block instanceof BlockTNT || !block.isFullBlock() || !block.isFullCube() || (toPlace && block instanceof BlockFalling)) {
            return false;
        }
        final Material material = block.getMaterial();
        return !material.isLiquid() && material.isSolid();
    }

    public static boolean isBestSword(ItemStack itemStack) {
        AtomicDouble damage = new AtomicDouble(0.0);
        AtomicReference<ItemStack> bestStack = new AtomicReference<>(null);

        forEachInventorySlot(EXCLUDE_ARMOR_BEGIN, END, (slot, stack) -> {
            if (stack.getItem() instanceof ItemSword) {
                double newDamage = getItemDamage(stack);

                if (newDamage > damage.get()) {
                    damage.set(newDamage);
                    bestStack.set(stack);
                }
            }
        });

        return bestStack.get() == itemStack || damage.get() < getItemDamage(itemStack);
    }

    public static boolean isBuffPotion(final ItemStack stack) {
        final ItemPotion potion = (ItemPotion) stack.getItem();
        final List<PotionEffect> effects = potion.getEffects(stack);
        if (effects.isEmpty()) {
            return false;
        }
        for (final PotionEffect effect : effects) {
            if (BAD_EFFECTS_IDS.contains(effect.getPotionID())) {
                return false;
            }
        }
        return true;
    }


    public static boolean isGoodItem(ItemStack stack) {
        Item item = stack.getItem();
        InvManager invManager = INSTANCE.getModuleManager().getModule(InvManager.class);

        if (item instanceof ItemBucket) {
            return invManager.isEnabled() && invManager.keepBucket.get();
        }

        return !(item instanceof ItemExpBottle) &&
                (!(item instanceof ItemEgg) && !(item instanceof ItemSnowball) || invManager.keepProjectiles.get()) &&
                (!(item instanceof ItemFishingRod) || invManager.keepFishingRod.get()) &&
                !(item instanceof ItemSkull);
    }

    public static boolean isBestArmor(ItemStack itemStack) {
        ItemArmor itemArmor = (ItemArmor) itemStack.getItem();
        AtomicDouble reduction = new AtomicDouble(0.0);
        AtomicReference<ItemStack> bestStack = new AtomicReference<>(null);
        forEachInventorySlot(InventoryUtil.INCLUDE_ARMOR_BEGIN, InventoryUtil.END, ((slot, stack) -> {
            if (stack.getItem() instanceof ItemArmor stackArmor) {
                if (stackArmor.armorType == itemArmor.armorType) {
                    double newReduction = getDamageReduction(stack);

                    if (newReduction > reduction.get()) {
                        reduction.set(newReduction);
                        bestStack.set(stack);
                    }
                }
            }
        }));

        return bestStack.get() == itemStack ||
                reduction.get() < getDamageReduction(itemStack);
    }

    public static boolean isBestTool(ItemStack itemStack) {
        final int type = getToolType(itemStack);

        AtomicReference<Tool> bestTool = new AtomicReference<>(new Tool(-1, -1, null));

        forEachInventorySlot(InventoryUtil.EXCLUDE_ARMOR_BEGIN, InventoryUtil.END, ((slot, stack) -> {
            if (stack.getItem() instanceof ItemTool && type == getToolType(stack)) {
                double efficiency = getToolEfficiency(stack);
                if (efficiency > bestTool.get().efficiency())
                    bestTool.set(new Tool(slot, efficiency, stack));
            }
        }));

        return bestTool.get().stack() == itemStack ||
                bestTool.get().efficiency() < getToolEfficiency(itemStack);
    }


    public static int getToolType(final ItemStack stack) {
        final ItemTool tool = (ItemTool) stack.getItem();
        if (tool instanceof ItemPickaxe) {
            return 0;
        }
        if (tool instanceof ItemAxe) {
            return 1;
        }
        if (tool instanceof ItemSpade) {
            return 2;
        }
        return -1;
    }

    public static float getToolEfficiency(ItemStack itemStack) {
        float efficiency = 4;

        int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);

        if (lvl > 0)
            efficiency += lvl * lvl + 1;

        return efficiency;
    }

    public static double getItemDamage(final ItemStack stack) {
        double damage = 0.0;
        final Multimap<String, AttributeModifier> attributeModifierMap = stack.getAttributeModifiers();
        for (final String attributeName : attributeModifierMap.keySet()) {
            if (attributeName.equals("generic.attackDamage")) {
                final Iterator<AttributeModifier> attributeModifiers = attributeModifierMap.get(attributeName).iterator();
                if (attributeModifiers.hasNext()) {
                    damage += attributeModifiers.next().getAmount();
                    break;
                }
                break;
            }
        }
        if (stack.isItemEnchanted()) {
            damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
            damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25;
        }
        return damage;
    }

    public static double getDamageReduction(ItemStack stack) {
        double reduction = 0.0;

        ItemArmor armor = (ItemArmor) stack.getItem();
        reduction += armor.damageReduceAmount;
        if (stack.isItemEnchanted()) {
            reduction += EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 0.25D;
        }
        return reduction;
    }

    public static int pickHotarBlock(boolean biggestStack) {
        if (biggestStack) {
            int currentStackSize = 0;
            int currentSlot = 36;
            for (int i = 36; i < 45; i++) {
                ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (itemStack != null && (itemStack.getItem() instanceof ItemBlock) && itemStack.stackSize > currentStackSize) {
                    Block block = ((ItemBlock) itemStack.getItem()).getBlock();
                    if (block.isFullCube() && !ScaffoldUtil.blacklistedBlocks.contains(block)) {
                        currentStackSize = itemStack.stackSize;
                        currentSlot = i;
                    }
                }
            }
            if (currentStackSize > 0) {
                return currentSlot - 36;
            }
            return -1;
        }
        for (int i2 = 36; i2 < 45; i2++) {
            ItemStack itemStack2 = mc.thePlayer.inventoryContainer.getSlot(i2).getStack();
            if (itemStack2 != null && (itemStack2.getItem() instanceof ItemBlock) && itemStack2.stackSize > 0) {
                Block block2 = ((ItemBlock) itemStack2.getItem()).getBlock();
                if (block2.isFullCube() && !ScaffoldUtil.blacklistedBlocks.contains(block2)) {
                    return i2 - 36;
                }
            }
        }
        return -1;
    }

    @FunctionalInterface
    public interface SlotConsumer {
        void accept(final int p0, final ItemStack p1);
    }

    public record Tool(int slot, double efficiency, ItemStack stack) {
    }
}
