package com.sillyshit.xpsaver;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.consume.UseAction;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class XpStorageBottleItem extends Item {

    private static final String XP_KEY = "stored_xp";

    public XpStorageBottleItem(Settings settings) {
        super(settings);
    }

    public static int getStoredXp(ItemStack stack) {
        NbtComponent data = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound tag = data.copyNbt();
        return tag.getInt(XP_KEY, 0);
    }

    public static void setStoredXp(ItemStack stack, int amount) {
        if (amount <= 0) {
            stack.remove(DataComponentTypes.CUSTOM_DATA);
        } else {
            NbtCompound tag = new NbtCompound();
            tag.putInt(XP_KEY, amount);
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
        }
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (player.isSneaking()) {
            int playerXp = getPlayerTotalXp(player);
            if (playerXp <= 0) {
                if (!world.isClient()) {
                    player.sendMessage(Text.translatable("message.xp_saver.no_xp").formatted(Formatting.RED), true);
                }
                return ActionResult.FAIL;
            }

            if (!world.isClient()) {
                int stored = getStoredXp(stack);
                int newTotal = stored + playerXp;
                setStoredXp(stack, newTotal);
                player.totalExperience = 0;
                player.experienceLevel = 0;
                player.experienceProgress = 0.0f;
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                        SoundCategory.PLAYERS, 1.0f, 1.0f);
                player.sendMessage(Text.translatable("message.xp_saver.stored", playerXp, newTotal)
                        .formatted(Formatting.GREEN), true);
            }
            return ActionResult.SUCCESS;
        }

        int stored = getStoredXp(stack);
        if (stored <= 0) {
            if (!world.isClient()) {
                player.sendMessage(Text.translatable("message.xp_saver.empty").formatted(Formatting.RED), true);
            }
            return ActionResult.FAIL;
        }

        return ItemUsage.consumeHeldItem(world, player, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity player && !world.isClient()) {
            int stored = getStoredXp(stack);
            if (stored > 0) {
                player.addExperience(stored);
                setStoredXp(stack, 0);
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP,
                        SoundCategory.PLAYERS, 0.5f, 1.0f);
                player.sendMessage(Text.translatable("message.xp_saver.drank", stored).formatted(Formatting.GREEN), true);
            }
        }
        return getDefaultStack();
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        int stored = getStoredXp(stack);
        if (stored > 0) {
            textConsumer.accept(Text.translatable("tooltip.xp_saver.stored_xp", stored).formatted(Formatting.AQUA));
        } else {
            textConsumer.accept(Text.translatable("tooltip.xp_saver.empty").formatted(Formatting.GRAY));
        }
    }

    private int getPlayerTotalXp(PlayerEntity player) {
        int level = player.experienceLevel;
        float progress = player.experienceProgress;
        int total = 0;
        for (int i = 0; i < level; i++) {
            total += getXpRequiredForLevel(i);
        }
        total += (int) (progress * getXpRequiredForLevel(level));
        return total;
    }

    private int getXpRequiredForLevel(int level) {
        if (level >= 30) {
            return 62 + (level - 30) * 7;
        } else if (level >= 15) {
            return 17 + (level - 15) * 3;
        } else {
            return 2 * level + 7;
        }
    }
}