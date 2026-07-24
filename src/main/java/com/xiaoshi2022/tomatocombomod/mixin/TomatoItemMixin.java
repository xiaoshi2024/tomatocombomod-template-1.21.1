package com.xiaoshi2022.tomatocombomod.mixin;

import com.xiaoshi2022.tomatocombomod.entity.TomatoVariantEntity;
import com.xiaoshi2022.tomatocombomod.item.TomatoVariantItem;
import com.xiaoshi2022.tomatocombomod.registry.ModSounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class TomatoItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(Level level, Player player, InteractionHand hand,
                       CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack heldStack = player.getItemInHand(hand);

        // 只处理农夫乐事的番茄
        if (!isFarmersDelightTomato(heldStack)) {
            return;
        }

        // Shift+右键：食用番茄（原版行为，不添加额外效果）
        if (player.isShiftKeyDown()) {
            if (player.canEat(false)) {
                player.startUsingItem(hand);
                cir.setReturnValue(InteractionResultHolder.sidedSuccess(heldStack, level.isClientSide()));
            } else {
                cir.setReturnValue(InteractionResultHolder.pass(heldStack));
            }
            return;
        }

        // 普通右键：投掷番茄
        playTomatoThrowSound(level, player);

        if (!level.isClientSide) {
            TomatoVariantEntity projectile = new TomatoVariantEntity(level, player);
            projectile.setItem(heldStack.copy());
            projectile.setVariant(TomatoVariantItem.Variant.TOMATO_BOOGER);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(projectile);
        }

        player.awardStat(Stats.ITEM_USED.get(heldStack.getItem()));

        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        cir.setReturnValue(InteractionResultHolder.sidedSuccess(heldStack, level.isClientSide()));
    }

    // ✅ 已移除 finishUsingItem 注入，普通番茄食用不再有反胃效果

    @Unique
    private boolean isFarmersDelightTomato(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null &&
                "farmersdelight".equals(id.getNamespace()) &&
                "tomato".equals(id.getPath());
    }

    @Unique
    private void playTomatoThrowSound(Level level, Player player) {
        SoundEvent throwSound = BuiltInRegistries.SOUND_EVENT.get(ModSounds.FARMERS_DELIGHT_TOMATO_THROW);
        if (throwSound != null) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    throwSound, net.minecraft.sounds.SoundSource.NEUTRAL, 0.5F,
                    0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
        }
    }
}