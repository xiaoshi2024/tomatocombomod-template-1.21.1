//package com.xiaoshi2022.tomatocombomod.data.recipe;
//
//import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
//import com.xiaoshi2022.tomatocombomod.registry.ModItems;
//import net.minecraft.core.HolderLookup;
//import net.minecraft.data.PackOutput;
//import net.minecraft.data.recipes.RecipeOutput;
//import net.minecraft.data.recipes.RecipeProvider;
//import net.minecraft.world.item.Items;
//import net.neoforged.neoforge.common.Tags;
//import vectorwing.farmersdelight.client.recipebook.CookingPotRecipeBookTab;
//import vectorwing.farmersdelight.common.tag.CommonTags;
//import vectorwing.farmersdelight.data.builder.CookingPotRecipeBuilder;
//
//import java.util.concurrent.CompletableFuture;
//
//public class TomatoComboRecipes extends RecipeProvider {
//
//    public static final int FAST_COOKING = 100;
//    public static final int NORMAL_COOKING = 200;
//    public static final int SLOW_COOKING = 400;
//
//    public static final float SMALL_EXP = 0.35F;
//    public static final float MEDIUM_EXP = 1.0F;
//    public static final float LARGE_EXP = 2.0F;
//
//    public TomatoComboRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
//        super(output, lookupProvider);
//    }
//
//    @Override
//    protected void buildRecipes(RecipeOutput output) {
//        buildCookingPotRecipes(output);
//        // 砧板配方已移至手动 JSON 文件
//        // buildCuttingBoardRecipes(output);
//    }
//
//    private void buildCookingPotRecipes(RecipeOutput output) {
//        // 番茄猪肉
//        CookingPotRecipeBuilder.cookingPotRecipe(ModItems.TOMATO_PORK.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
//                .addIngredient(CommonTags.Items.CROPS_TOMATO)
//                .addIngredient(Items.PORKCHOP)
//                .unlockedBy("has_porkchop", has(Items.PORKCHOP))
//                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
//                .save(output);
//
//        // 番茄鸡肉
//        CookingPotRecipeBuilder.cookingPotRecipe(ModItems.TOMATO_CHICKEN.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
//                .addIngredient(CommonTags.Items.CROPS_TOMATO)
//                .addIngredient(Items.CHICKEN)
//                .unlockedBy("has_chicken", has(Items.CHICKEN))
//                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
//                .save(output);
//
//        // 番茄炒蛋
//        CookingPotRecipeBuilder.cookingPotRecipe(ModItems.TOMATO_EGG_FRY.get(), 1, FAST_COOKING, SMALL_EXP)
//                .addIngredient(CommonTags.Items.CROPS_TOMATO)
//                .addIngredient(Tags.Items.EGGS)
//                .addIngredient(Items.TORCH)
//                .unlockedBy("has_egg", has(Items.EGG))
//                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
//                .save(output);
//
//        // 番茄鸡蛋
//        CookingPotRecipeBuilder.cookingPotRecipe(ModItems.TOMATO_EGG.get(), 1, FAST_COOKING, SMALL_EXP)
//                .addIngredient(CommonTags.Items.CROPS_TOMATO)
//                .addIngredient(Tags.Items.EGGS)
//                .unlockedBy("has_egg", has(Items.EGG))
//                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
//                .save(output);
//
//        // 番茄饭
//        CookingPotRecipeBuilder.cookingPotRecipe(ModItems.TOMATO_RICE.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
//                .addIngredient(CommonTags.Items.CROPS_TOMATO)
//                .addIngredient(CommonTags.Items.CROPS_RICE)
//                .unlockedBy("has_rice", has(Items.WHEAT))
//                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
//                .save(output);
//
//        // 番茄河粉
//        CookingPotRecipeBuilder.cookingPotRecipe(ModItems.TOMATO_RIVER_NOODLE.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
//                .addIngredient(CommonTags.Items.CROPS_TOMATO)
//                .addIngredient(Tags.Items.CROPS_WHEAT)
//                .unlockedBy("has_wheat", has(Items.WHEAT))
//                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
//                .save(output);
//
//        // 番茄米粉
//        CookingPotRecipeBuilder.cookingPotRecipe(ModItems.TOMATO_RICE_NOODLE.get(), 1, NORMAL_COOKING, MEDIUM_EXP)
//                .addIngredient(CommonTags.Items.CROPS_TOMATO)
//                .addIngredient(CommonTags.Items.CROPS_RICE)
//                .unlockedBy("has_rice", has(Items.WHEAT))
//                .setRecipeBookTab(CookingPotRecipeBookTab.MEALS)
//                .save(output);
//
//        // 最终番茄
//        CookingPotRecipeBuilder.cookingPotRecipe(ModItems.FINAL_TOMATO.get(), 1, SLOW_COOKING, LARGE_EXP)
//                .addIngredient(ModItems.TOMATO_BOOGER.get())
//                .addIngredient(ModItems.TOMATO_PORK.get())
//                .addIngredient(ModItems.TOMATO_CHICKEN.get())
//                .addIngredient(ModItems.TOMATO_EGG_FRY.get())
//                .addIngredient(ModItems.TOMATO_EGG.get())
//                .addIngredient(ModItems.TOMATO_SMASH.get())
//                .addIngredient(ModItems.TOMATO_RICE.get())
//                .addIngredient(ModItems.TOMATO_RIVER_NOODLE.get())
//                .addIngredient(ModItems.TOMATO_RICE_NOODLE.get())
//                .unlockedBy("has_base_tomato", has(ModItems.TOMATO_BOOGER.get()))
//                .setRecipeBookTab(CookingPotRecipeBookTab.MISC)
//                .save(output);
//    }
//}