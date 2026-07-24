package com.xiaoshi2022.tomatocombomod.skill;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.entity.TomatoVariantEntity;
import com.xiaoshi2022.tomatocombomod.item.TomatoVariantItem;
import com.xiaoshi2022.tomatocombomod.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TomatoComboSkill implements ITomatoSkill {
    
    public static final String ID = "tomato_combo";
    
    private static final int SHOT_INTERVAL = 8;  // 每8tick发射一次
    private static final int HUNGER_COST_INTERVAL = 20;  // 每秒消耗一次饱食度
    
    private final Map<UUID, ComboState> activePlayers = new HashMap<>();

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Component getName() {
        return Component.translatable("skill.tomatocombomod.tomato_combo");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("skill.tomatocombomod.tomato_combo.desc");
    }

    @Override
    public ResourceLocation getIcon() {
        return ResourceLocation.fromNamespaceAndPath(TomatoComboMod.MODID, "textures/item/skill/tomato_combo.png");
    }

    @Override
    public boolean isActivatable() {
        return true;
    }

    @Override
    public int getCooldown() {
        return 0;
    }

    @Override
    public boolean canActivate(Player player) {
        return player.getFoodData().getFoodLevel() >= 3;
    }

    @Override
    public boolean activate(Player player) {
        if (player.level().isClientSide()) {
            return true;
        }
        
        if (player.getFoodData().getFoodLevel() < 3) {
            player.displayClientMessage(
                    Component.translatable("message.tomatocombomod.hungry_cannot_use"),
                    true
            );
            return false;
        }
        
        ComboState state = activePlayers.get(player.getUUID());
        if (state == null) {
            state = new ComboState();
            activePlayers.put(player.getUUID(), state);
        }
        
        state.active = !state.active;
        
        if (state.active) {
            state.shotCounter = 0;
            state.hungerCounter = 0;
            state.variantIndex = 0;
            player.displayClientMessage(
                    Component.translatable("message.tomatocombomod.skill_activated"),
                    true
            );
        } else {
            player.displayClientMessage(
                    Component.translatable("message.tomatocombomod.skill_deactivated"),
                    true
            );
        }
        
        return true;
    }

    @Override
    public void tick(Player player) {
        // 只在服务端运行
        if (player.level().isClientSide()) {
            return;
        }
        
        ComboState state = activePlayers.get(player.getUUID());
        
        if (state == null || !state.active) {
            return;
        }
        
        state.shotCounter++;
        state.hungerCounter++;
        
        // 持续消耗饱食度
        if (state.hungerCounter >= HUNGER_COST_INTERVAL) {
            state.hungerCounter = 0;
            int currentFood = player.getFoodData().getFoodLevel();
            player.getFoodData().setFoodLevel(Math.max(0, currentFood - 1));
            
            if (player.getFoodData().getFoodLevel() <= 0) {
                state.active = false;
                player.displayClientMessage(
                        Component.translatable("message.tomatocombomod.hungry_cannot_use"),
                        true
                );
                return;
            }
        }
        
        // 每8tick发射一个番茄
        if (state.shotCounter >= SHOT_INTERVAL) {
            state.shotCounter = 0;
            
            // 循环使用9种番茄变种（排除最终番茄）
            TomatoVariantItem.Variant[] variants = TomatoVariantItem.Variant.values();
            
            TomatoVariantItem.Variant variant = variants[state.variantIndex];
            
            TomatoVariantEntity projectile = new TomatoVariantEntity(player.level(), player);
            projectile.setItem(new ItemStack(ModItems.getTomatoVariant(variant)));
            projectile.setVariant(variant);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            player.level().addFreshEntity(projectile);
            
            // 切换到下一个变种（只循环前9种，排除最终番茄）
            state.variantIndex = (state.variantIndex + 1) % (variants.length - 1);
        }
    }

    @Override
    public void cleanupPlayerState(Player player) {
        activePlayers.remove(player.getUUID());
    }

    private static class ComboState {
        boolean active = false;
        int shotCounter = 0;
        int hungerCounter = 0;
        int variantIndex = 0;
    }
}