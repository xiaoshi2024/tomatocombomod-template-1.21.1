//package com.xiaoshi2022.tomatocombomod.data;
//
//import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
//import com.xiaoshi2022.tomatocombomod.data.recipe.TomatoComboRecipes;
//import net.minecraft.core.HolderLookup;
//import net.minecraft.data.PackOutput;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.data.event.GatherDataEvent;
//
//import java.util.concurrent.CompletableFuture;
//
//@EventBusSubscriber(modid = TomatoComboMod.MODID)
//public class TomatoComboDataGenerator {
//
//    @SubscribeEvent
//    public static void gatherData(GatherDataEvent event) {
//        PackOutput output = event.getGenerator().getPackOutput();
//        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
//
//        // 添加配方生成器
//        event.getGenerator().addProvider(
//                event.includeServer(),
//                new TomatoComboRecipes(output, lookupProvider)
//        );
//    }
//}