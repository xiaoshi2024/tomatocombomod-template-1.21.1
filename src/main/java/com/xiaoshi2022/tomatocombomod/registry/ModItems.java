package com.xiaoshi2022.tomatocombomod.registry;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.item.TomatoVariantItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, TomatoComboMod.MODID);
    
    // 鼻屎 - 单独的合成材料，不能投掷
    public static final DeferredHolder<Item, Item> BOOGER = ITEMS.register("booger",
            () -> new Item(new Item.Properties()));
    
    // 番茄变种物品注册（9种可投掷的番茄）
    public static final DeferredHolder<Item, Item> TOMATO_BOOGER = ITEMS.register("tomato_booger",
            () -> new TomatoVariantItem(TomatoVariantItem.Variant.TOMATO_BOOGER, new Item.Properties()));
    
    public static final DeferredHolder<Item, Item> TOMATO_PORK = ITEMS.register("tomato_pork",
            () -> new TomatoVariantItem(TomatoVariantItem.Variant.TOMATO_PORK, new Item.Properties()));
    
    public static final DeferredHolder<Item, Item> TOMATO_CHICKEN = ITEMS.register("tomato_chicken",
            () -> new TomatoVariantItem(TomatoVariantItem.Variant.TOMATO_CHICKEN, new Item.Properties()));
    
    public static final DeferredHolder<Item, Item> TOMATO_EGG_FRY = ITEMS.register("tomato_egg_fry",
            () -> new TomatoVariantItem(TomatoVariantItem.Variant.TOMATO_EGG_FRY, new Item.Properties()));
    
    public static final DeferredHolder<Item, Item> TOMATO_EGG = ITEMS.register("tomato_egg",
            () -> new TomatoVariantItem(TomatoVariantItem.Variant.TOMATO_EGG, new Item.Properties()));
    
    public static final DeferredHolder<Item, Item> TOMATO_SMASH = ITEMS.register("tomato_smash",
            () -> new TomatoVariantItem(TomatoVariantItem.Variant.TOMATO_SMASH, new Item.Properties()));
    
    public static final DeferredHolder<Item, Item> TOMATO_RICE = ITEMS.register("tomato_rice",
            () -> new TomatoVariantItem(TomatoVariantItem.Variant.TOMATO_RICE, new Item.Properties()));
    
    public static final DeferredHolder<Item, Item> TOMATO_RIVER_NOODLE = ITEMS.register("tomato_river_noodle",
            () -> new TomatoVariantItem(TomatoVariantItem.Variant.TOMATO_RIVER_NOODLE, new Item.Properties()));
    
    public static final DeferredHolder<Item, Item> TOMATO_RICE_NOODLE = ITEMS.register("tomato_rice_noodle",
            () -> new TomatoVariantItem(TomatoVariantItem.Variant.TOMATO_RICE_NOODLE, new Item.Properties()));

    // 最终番茄 - 可食用，食用后获得番茄连招技能
    public static final FoodProperties FINAL_TOMATO_FOOD = new FoodProperties.Builder()
            .nutrition(4)
            .saturationModifier(0.8f)
            .alwaysEdible()  // ✅ 修复：alwaysEat() → alwaysEdible()
            .build();
    
    public static final DeferredHolder<Item, Item> FINAL_TOMATO = ITEMS.register("final_tomato",
            () -> new TomatoVariantItem(TomatoVariantItem.Variant.FINAL_TOMATO, new Item.Properties().food(FINAL_TOMATO_FOOD)));
    
    /**
     * 根据变种获取对应的物品
     */
    public static Item getTomatoVariant(TomatoVariantItem.Variant variant) {
        return switch (variant) {
            case TOMATO_BOOGER -> TOMATO_BOOGER.get();
            case TOMATO_PORK -> TOMATO_PORK.get();
            case TOMATO_CHICKEN -> TOMATO_CHICKEN.get();
            case TOMATO_EGG_FRY -> TOMATO_EGG_FRY.get();
            case TOMATO_EGG -> TOMATO_EGG.get();
            case TOMATO_SMASH -> TOMATO_SMASH.get();
            case TOMATO_RICE -> TOMATO_RICE.get();
            case TOMATO_RIVER_NOODLE -> TOMATO_RIVER_NOODLE.get();
            case TOMATO_RICE_NOODLE -> TOMATO_RICE_NOODLE.get();
            case FINAL_TOMATO -> FINAL_TOMATO.get();
        };
    }
}
