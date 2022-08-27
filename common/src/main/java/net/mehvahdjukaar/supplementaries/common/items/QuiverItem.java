package net.mehvahdjukaar.supplementaries.common.items;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.supplementaries.ForgeHelper;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class QuiverItem extends Item implements DyeableLeatherItem {

    private static final int BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public QuiverItem(Properties properties) {
        super(properties);
    }
    //TODO: quark arrow preview

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack quiver, Slot pSlot, ClickAction pAction, Player pPlayer) {
        if (pAction != ClickAction.SECONDARY) {
            return false;
        } else {
            ItemStack itemstack = pSlot.getItem();
            //place into slot
            if (itemstack.isEmpty()) {
                IQuiverData data = getQuiverData(quiver);
                if (data != null) {
                    this.playRemoveOneSound(pPlayer);
                    data.removeOne().ifPresent((p_150740_) -> {
                        data.add(pSlot.safeInsert(p_150740_));
                    });
                }
            }
            //add
            else if (itemstack.getItem().canFitInsideContainerItems()) {
                IQuiverData data = getQuiverData(quiver);
                if (data != null) {
                    ItemStack i = data.add(pSlot.safeTake(itemstack.getCount(), 64, pPlayer));
                    if (!i.equals(itemstack)) {
                        this.playInsertSound(pPlayer);
                        pSlot.set(i);
                        return true;
                    }
                }
            }
            return true;
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack quiver, ItemStack pOther, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess) {
        if (pAction == ClickAction.SECONDARY && pSlot.allowModification(pPlayer)) {
            IQuiverData data = getQuiverData(quiver);
            if (data != null) {
                if (pOther.isEmpty()) {
                    data.removeOne().ifPresent((removed) -> {
                        this.playRemoveOneSound(pPlayer);
                        pAccess.set(removed);
                    });
                    return true;
                } else {
                    ItemStack i = data.add(pOther);
                    if (!i.equals(pOther)) {
                        this.playInsertSound(pPlayer);
                        pAccess.set(i);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player player, InteractionHand pUsedHand) {
        ItemStack stack = player.getItemInHand(pUsedHand);
        if (player.isSecondaryUseActive()) {

            IQuiverData data = getQuiverData(stack);
            if (data != null) {
                data.cycle();
            }
        } else {
            //same as startUsingItem but client only so it does not slow
            if (pLevel.isClientSide) {
                toggleQuiverGUI(true);
            }
            this.playRemoveOneSound(player);
            player.startUsingItem(pUsedHand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        return super.finishUsingItem(stack, level, livingEntity);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (level.isClientSide) {
            toggleQuiverGUI(false);
        }
        this.playInsertSound(livingEntity);
        livingEntity.swing(livingEntity.getUsedItemHand());
        super.releaseUsing(stack, level, livingEntity, timeCharged);
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        IQuiverData data = getQuiverData(pStack);
        if (data != null) {
            return data.getSelected().getCount() > 0;
        }
        return false;
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        IQuiverData data = getQuiverData(pStack);
        if (data != null) {
            return Math.min(1 + 12 * data.getSelectedArrowCount() / (64 * CommonConfigs.Items.QUIVER_SLOTS.get()), 13);
        }
        return 0;
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return BAR_COLOR;
    }


    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack pStack) {
        IQuiverData data = getQuiverData(pStack);
        if (data != null) {
            NonNullList<ItemStack> list = NonNullList.create();
            boolean isEmpty = true;
            for (var v : data.getContent()) {
                if (!v.isEmpty()) isEmpty = false;
                list.add(v);
            }
            if (!isEmpty) {
                return Optional.of(new QuiverTooltip(new ArrayList<>(data.getContent()), data.getSelectedSlot()));
            }
        }
        return Optional.empty();
    }


    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        IQuiverData data = getQuiverData(pStack);
        if (data != null) {
            int c = data.getSelectedArrowCount();
            if (c != 0) {
                pTooltipComponents.add(Component.translatable("message.supplementaries.quiver.tooltip",
                        data.getSelected(null).getItem().getDescription(), c).withStyle(ChatFormatting.GRAY));
            }
        }
    }


    @Override
    public void onDestroyed(ItemEntity pItemEntity) {
        IQuiverData data = getQuiverData(pItemEntity.getItem());
        if (data != null) {
            ItemUtils.onContainerDestroyed(pItemEntity, data.getContent().stream());
        }
    }

    private void playRemoveOneSound(Entity pEntity) {
        pEntity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + pEntity.getLevel().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity pEntity) {
        pEntity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + pEntity.getLevel().getRandom().nextFloat() * 0.4F);
    }

    private void playDropContentsSound(Entity pEntity) {
        pEntity.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + pEntity.getLevel().getRandom().nextFloat() * 0.4F);
    }

    @Nullable
    @ExpectPlatform
    public static IQuiverData getQuiverData(ItemStack stack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ItemStack getQuiver(LivingEntity entity) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    protected static void toggleQuiverGUI(boolean on) {
        throw new AssertionError();
    }

    public record QuiverTooltip(List<ItemStack> stacks, int selected) implements TooltipComponent {
    }

    //this is cap, cap provider
    public interface IQuiverData {

        int getSelectedSlot();

        void setSelectedSlot(int selectedSlot);

        List<ItemStack> getContent();

        default ItemStack getSelected() {
            return getSelected(null);
        }

        ItemStack getSelected(@Nullable Predicate<ItemStack> supporterArrows);

        default void cycle() {
            cycle(1);
        }

        default void cycle(boolean clockWise){
            cycle(clockWise ? 1 : -1);
        }

        void cycle(int slotsMoved);

        /**
         * Adds one item. returns the item that is remaining and has not been added
         */
        ItemStack add(ItemStack pInsertedStack);

        Optional<ItemStack> removeOne();

        default int getSelectedArrowCount() {
            ItemStack selected = this.getSelected(null);
            int amount = 0;
            for (var item : this.getContent()) {
                if (ForgeHelper.canItemStack(selected, item)) {
                    amount += item.getCount();
                }
            }
            return amount;
        }
    }

    //if local player is using gui
    public static boolean isUsingGUI = false;
    public static Integer slot  = null;

}
