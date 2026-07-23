package com.xiaoshi2022.tomatocombomod.registry;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.entity.TomatoVariantEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityTypes {
    
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, TomatoComboMod.MODID);
    
    // 番茄变种投掷实体
    public static final DeferredHolder<EntityType<?>, EntityType<TomatoVariantEntity>> TOMATO_VARIANT = ENTITY_TYPES.register("tomato_variant",
            () -> EntityType.Builder.<TomatoVariantEntity>of(TomatoVariantEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("tomato_variant"));
}
