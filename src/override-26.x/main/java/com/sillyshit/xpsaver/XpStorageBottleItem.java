package com.sillyshit.xpsaver;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

public class XpStorageBottleItem extends Item {

    private static final String XP_KEY = "stored_xp";

    public XpStorageBottleItem(Properties properties) {
        super(properties);
    }

    public static int getStoredXp(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = data.copyTag();
        return tag.contains(XP_KEY) ? tag.getInt(XP_KEY) : 0;
    }

    public static void setStoredXp(ItemStack stack, int amount) {
        if (amount <= 0) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            CompoundTag tag = new CompoundTag();
            tag.putInt(XP_KEY, amount);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            int playerXp = getPlayerTotalXp(player);
            if (playerXp <= 0) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.translatable("message.xp_saver.no_xp").withStyle(ChatFormatting.RED), true);
                }
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide) {
                int stored = getStoredXp(stack);
                int newTotal = stored + playerXp;
                setStoredXp(stack, newTotal);
                player.totalExperience = 0;
                player.experienceLevel = 0;
                player.experienceProgress = 0.0f;
                level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS, 1.0f, 1.0f);
                player.displayClientMessage(Component.translatable("message.xp_saver.stored", playerXp, newTotal)
                        .withStyle(ChatFormatting.GREEN), true);
            }
            return InteractionResult.SUCCESS;
        }

        int stored = getStoredXp(stack);
        if (stored <= 0) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.xp_saver.empty").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.FAIL;
        }

        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (user instanceof Player player && !level.isClientSide) {
            int stored = getStoredXp(stack);
            if (stored > 0) {
                player.giveExperiencePoints(stored);
                setStoredXp(stack, 0);
                level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                        SoundSource.PLAYERS, 0.5f, 1.0f);
                player.displayClientMessage(Component.translatable("message.xp_saver.drank", stored).withStyle(ChatFormatting.GREEN), true);
            }
        }
        return getDefaultInstance();
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int stored = getStoredXp(stack);
        if (stored > 0) {
            tooltip.add(Component.translatable("tooltip.xp_saver.stored_xp", stored).withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.xp_saver.empty").withStyle(ChatFormatting.GRAY));
        }
    }

    private int getPlayerTotalXp(Player player) {
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
