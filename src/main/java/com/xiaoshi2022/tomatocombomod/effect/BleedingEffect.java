package com.xiaoshi2022.tomatocombomod.effect;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class BleedingEffect extends MobEffect {
    // 仰头恢复角度阈值（弧度）
    private static final float LOOK_UP_THRESHOLD = (float) Math.toRadians(-60.0);

    public BleedingEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF4444);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            float pitch = player.getXRot();

            // ✅ 添加调试日志
            TomatoComboMod.LOGGER.debug("Bleeding tick - pitch: {}, threshold: {}", pitch, LOOK_UP_THRESHOLD);

            if (pitch < LOOK_UP_THRESHOLD) {
                TomatoComboMod.LOGGER.debug("Player is looking up, no damage");
                return false;
            }

            if (player.tickCount % 10 == 0) {
                player.hurt(player.damageSources().generic(), 1.0F);
//                TomatoComboMod.LOGGER.info("Player {} is bleeding, taking damage!", player.getName().getString());
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 返回 true 表示每 tick 都执行 applyEffectTick
        return true;
    }
}