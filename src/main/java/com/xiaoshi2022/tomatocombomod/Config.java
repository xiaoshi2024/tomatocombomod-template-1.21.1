//package com.xiaoshi2022.tomatocombomod;
//
//import java.util.List;
//
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.resources.ResourceLocation;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.fml.event.config.ModConfigEvent;
//import net.neoforged.neoforge.common.ModConfigSpec;
//
//// 番茄组合模组配置文件
//@EventBusSubscriber(modid = TomatoComboMod.MODID)
//public class Config {
//    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
//
//    // 番茄投掷物伤害倍率配置
//    public static final ModConfigSpec.DoubleValue TOMATO_DAMAGE_MULTIPLIER = BUILDER
//            .comment("全局番茄投掷物伤害倍率 (默认: 1.0)")
//            .defineInRange("tomatoDamageMultiplier", 1.0D, 0.0D, 10.0D);
//
//    // 番茄投掷物击退力度配置
//    public static final ModConfigSpec.DoubleValue TOMATO_KNOCKBACK_STRENGTH = BUILDER
//            .comment("番茄投掷物击退力度 (默认: 0.5)")
//            .defineInRange("tomatoKnockbackStrength", 0.5D, 0.0D, 5.0D);
//
//    // 是否启用番茄粉碎变种的额外击退效果
//    public static final ModConfigSpec.BooleanValue TOMATO_SMASH_KNOCKBACK_ENABLED = BUILDER
//            .comment("是否启用番茄粉碎(Tomato Smash)变种的额外击退效果 (默认: true)")
//            .define("tomatoSmashKnockbackEnabled", true);
//
//    // 是否启用番茄投掷物命中音效
//    public static final ModConfigSpec.BooleanValue TOMATO_HIT_SOUND_ENABLED = BUILDER
//            .comment("是否启用番茄投掷物命中音效 (默认: true)")
//            .define("tomatoHitSoundEnabled", true);
//
//    // 番茄投掷物粒子效果数量
//    public static final ModConfigSpec.IntValue TOMATO_PARTICLE_COUNT = BUILDER
//            .comment("番茄投掷物破碎时的粒子数量 (默认: 12)")
//            .defineInRange("tomatoParticleCount", 12, 1, 50);
//
//    static final ModConfigSpec SPEC = BUILDER.build();
//
//    // 在配置加载时记录配置信息
//    @SubscribeEvent
//    static void onLoad(final ModConfigEvent.Loading configEvent) {
//        // 配置加载时的日志记录
//        TomatoComboMod.LOGGER.debug("番茄组合模组配置已加载!");
//        TomatoComboMod.LOGGER.debug("番茄伤害倍率: {}", TOMATO_DAMAGE_MULTIPLIER.get());
//        TomatoComboMod.LOGGER.debug("番茄击退力度: {}", TOMATO_KNOCKBACK_STRENGTH.get());
//        TomatoComboMod.LOGGER.debug("番茄粉碎击退启用: {}", TOMATO_SMASH_KNOCKBACK_ENABLED.get());
//        TomatoComboMod.LOGGER.debug("番茄命中音效启用: {}", TOMATO_HIT_SOUND_ENABLED.get());
//        TomatoComboMod.LOGGER.debug("番茄粒子数量: {}", TOMATO_PARTICLE_COUNT.get());
//    }
//}