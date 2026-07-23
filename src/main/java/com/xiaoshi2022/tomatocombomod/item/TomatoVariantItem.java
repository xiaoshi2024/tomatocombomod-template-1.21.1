package com.xiaoshi2022.tomatocombomod.item;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.entity.TomatoVariantEntity;
import com.xiaoshi2022.tomatocombomod.registry.ModItems;
import com.xiaoshi2022.tomatocombomod.registry.ModSounds;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import com.xiaoshi2022.tomatocombomod.skill.SkillManager;

import java.util.Arrays;
import java.util.List;

public class TomatoVariantItem extends Item implements ProjectileItem {
    
    public enum Variant {
        TOMATO_BOOGER("tomato_booger"),
        TOMATO_PORK("tomato_pork"),
        TOMATO_CHICKEN("tomato_chicken"),
        TOMATO_EGG_FRY("tomato_egg_fry"),
        TOMATO_EGG("tomato_egg"),
        TOMATO_SMASH("tomato_smash"),
        TOMATO_RICE("tomato_rice"),
        TOMATO_RIVER_NOODLE("tomato_river_noodle"),
        TOMATO_RICE_NOODLE("tomato_rice_noodle"),
        FINAL_TOMATO("final_tomato");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private final Variant variant;

    public TomatoVariantItem(Variant variant, Properties properties) {
        super(properties);
        this.variant = variant;
    }

    public Variant getVariant() {
        return variant;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        
        // Shift+右键：食用番茄，给反胃效果
        if (player.isShiftKeyDown()) {
            if (player.canEat(false)) {
                player.startUsingItem(hand);
                return InteractionResultHolder.sidedSuccess(heldStack, level.isClientSide());
            }
            return InteractionResultHolder.pass(heldStack);
        }
        
        // 普通右键：最终番茄直接食用，其他变种发射
        if (variant == Variant.FINAL_TOMATO) {
            if (player.canEat(false)) {
                player.startUsingItem(hand);
                return InteractionResultHolder.sidedSuccess(heldStack, level.isClientSide());
            }
            return InteractionResultHolder.pass(heldStack);
        }
        
//        // 其他番茄变种：发射
//        TomatoComboMod.LOGGER.info("TomatoVariantItem.use() called - variant: {}, isClientSide: {}", variant, level.isClientSide());
//        TomatoComboMod.LOGGER.info("  heldStack: {}, count: {}", heldStack.getItem(), heldStack.getCount());
//
        // Play throw sound - 使用FarmersDelight的烂番茄投掷音效
        playTomatoThrowSound(level, player);
        
        if (!level.isClientSide) {
//            TomatoComboMod.LOGGER.info("  Server side: creating projectile");
            TomatoVariantEntity projectile = new TomatoVariantEntity(level, player);
            projectile.setItem(heldStack);
            projectile.setVariant(variant);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(projectile);
//            TomatoComboMod.LOGGER.info("  Projectile created and added to level");
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        
        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(heldStack, level.isClientSide());
    }

    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    // ✅ 正确的重写方法
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        // 先调用父类
        super.finishUsingItem(stack, level, livingEntity);

        if (livingEntity instanceof Player player) {
            if (!level.isClientSide) {
                if (variant == Variant.FINAL_TOMATO) {
                    SkillManager.getInstance().grantSkill(player, "tomato_combo");
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.tomatocombomod.gained_skill"),
                            true
                    );
//                    TomatoComboMod.LOGGER.info("Player {} gained Tomato Combo skill!", player.getName().getString());
                } else {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.CONFUSION,
                            100,
                            0,
                            false,
                            true,
                            true
                    ));
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.tomatocombomod.ate_tomato"),
                            true
                    );
                }
            }

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return stack;
    }

    /**
     * 尝试依次丢出下一个变种番茄
     * 当玩家手中有足够的番茄变种时，可以依次丢出所有变种
     */
    private void tryThrowNextVariant(Player player, InteractionHand hand) {
        List<Variant> variants = Arrays.asList(Variant.values());
        int currentIndex = variants.indexOf(this.variant);
        
        // 如果不是最后一个变种，尝试切换到下一个变种
        if (currentIndex < variants.size() - 1) {
            Variant nextVariant = variants.get(currentIndex + 1);
            Item nextItem = ModItems.getTomatoVariant(nextVariant);
            
            // 在玩家背包中查找下一个变种（包括快捷栏）
            int nextSlot = findItemInInventory(player, nextItem);
            
            if (nextSlot >= 0) {
                if (hand == InteractionHand.MAIN_HAND) {
                    // 如果在快捷栏中（0-8），直接切换
                    if (nextSlot < 9) {
                        player.getInventory().selected = nextSlot;
                    } else {
                        // 如果在背包中，尝试移动到快捷栏空位
                        int emptyHotbarSlot = findEmptyHotbarSlot(player);
                        if (emptyHotbarSlot >= 0) {
                            // 将物品从背包移动到快捷栏
                            ItemStack nextStack = player.getInventory().getItem(nextSlot);
                            player.getInventory().setItem(emptyHotbarSlot, nextStack);
                            player.getInventory().setItem(nextSlot, ItemStack.EMPTY);
                            player.getInventory().selected = emptyHotbarSlot;
                        }
                        // 如果快捷栏没有空位，不切换
                    }
                } else {
                    // 副手处理
                    ItemStack nextStack = player.getInventory().getItem(nextSlot);
                    player.setItemInHand(InteractionHand.OFF_HAND, nextStack.copy());
                    player.getInventory().setItem(nextSlot, ItemStack.EMPTY);
                }
            }
        }
    }

    /**
     * 查找快捷栏中的空位
     * @return 找到的空位索引（0-8），-1表示没有空位
     */
    private int findEmptyHotbarSlot(Player player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 播放番茄投掷音效
     * 使用FarmersDelight的烂番茄投掷音效
     */
    private void playTomatoThrowSound(Level level, Player player) {
        SoundEvent throwSound = BuiltInRegistries.SOUND_EVENT.get(ModSounds.FARMERS_DELIGHT_TOMATO_THROW);
        if (throwSound != null) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    throwSound, SoundSource.NEUTRAL, 0.5F, 
                    0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
        }
    }

    /**
     * 在玩家背包中查找指定物品
     * @return 找到的槽位索引，-1表示未找到
     */
    private int findItemInInventory(Player player, Item item) {
        // 先检查快捷栏（0-8）
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                return i;
            }
        }
        // 检查背包其余部分
        for (int i = 9; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        TomatoVariantEntity tomato = new TomatoVariantEntity(level, position.x(), position.y(), position.z());
        tomato.setItem(itemStack);
        tomato.setVariant(variant);
        return tomato;
    }
}
