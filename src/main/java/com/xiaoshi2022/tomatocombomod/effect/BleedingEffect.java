package com.xiaoshi2022.tomatocombomod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class BleedingEffect extends MobEffect {
    private static final float LOOK_UP_THRESHOLD = (float) Math.toRadians(-60.0);

    public BleedingEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF4444);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            float pitch = player.getXRot();

            // ✅ 只在调试时启用，或者完全移除
            // TomatoComboMod.LOGGER.debug("Bleeding tick - pitch: {}, threshold: {}", pitch, LOOK_UP_THRESHOLD);

            // 如果玩家仰头（向上看），停止流血
            if (pitch < LOOK_UP_THRESHOLD) {
                return false;
            }

            // 每 10 tick 造成 1 点伤害
            if (player.tickCount % 10 == 0) {
                player.hurt(player.damageSources().generic(), 1.0F);
                // 或者使用更精确的日志
                // if (player.tickCount % 100 == 0) { // 每5秒输出一次
                //     TomatoComboMod.LOGGER.info("Player {} is bleeding, taking damage!", player.getName().getString());
                // }
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}