// TomatoComboModClient.java
package com.xiaoshi2022.tomatocombomod;

import com.xiaoshi2022.tomatocombomod.client.PlayerAnimationManager;
import com.xiaoshi2022.tomatocombomod.client.model.RottenTomatoModel;
import com.xiaoshi2022.tomatocombomod.client.renderer.item.TomatoVariantItemRenderer;
import com.xiaoshi2022.tomatocombomod.client.renderer.layer.TomatoPasteLayer;
import com.xiaoshi2022.tomatocombomod.registry.ModEntityTypes;
import com.xiaoshi2022.tomatocombomod.registry.ModItems;
import com.xiaoshi2022.tomatocombomod.registry.ModKeyBindings;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

@Mod(value = TomatoComboMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = TomatoComboMod.MODID, value = Dist.CLIENT)
public class TomatoComboModClient {

    // ✅ 定义 ModelLayerLocation 常量
    public static final ModelLayerLocation ROTTEN_TOMATO_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(TomatoComboMod.MODID, "rotten_tomato"),
            "main"
    );

    public TomatoComboModClient(ModContainer container) {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        TomatoComboMod.LOGGER.info("Tomato Combo Mod - Client Setup");

        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntityTypes.TOMATO_VARIANT.get(),
                    context -> new ThrownItemRenderer<>(context));

            PlayerAnimationManager.registerAnimationFactory();
        });
    }

    // ✅ 修复：使用 ModelLayerLocation
    @SubscribeEvent
    static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                ROTTEN_TOMATO_LAYER,  // 使用 ModelLayerLocation
                RottenTomatoModel::createLayer
        );
    }

    @SubscribeEvent
    static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        IClientItemExtensions renderer = new TomatoVariantItemRenderer();

        event.registerItem(renderer,
                ModItems.TOMATO_BOOGER.get(),
                ModItems.TOMATO_PORK.get(),
                ModItems.TOMATO_CHICKEN.get(),
                ModItems.TOMATO_EGG_FRY.get(),
                ModItems.TOMATO_EGG.get(),
                ModItems.TOMATO_SMASH.get(),
                ModItems.TOMATO_RICE.get(),
                ModItems.TOMATO_RIVER_NOODLE.get(),
                ModItems.TOMATO_RICE_NOODLE.get(),
                ModItems.FINAL_TOMATO.get()
        );
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.TOMATO_VARIANT.get(),
                context -> new ThrownItemRenderer<>(context));
    }

    @SubscribeEvent
    static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBindings.PICK_NOSE);
        event.register(ModKeyBindings.ACTIVATE_SKILL);
    }

    /**
     * 添加番茄汁渲染层到所有玩家模型
     */
    @SubscribeEvent
    static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        PlayerSkin.Model[] skinModels = {PlayerSkin.Model.SLIM, PlayerSkin.Model.WIDE};

        for (PlayerSkin.Model skinModel : skinModels) {
            var renderer = event.getSkin(skinModel);
            if (renderer instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new TomatoPasteLayer(playerRenderer));
                TomatoComboMod.LOGGER.debug("Added TomatoPasteLayer to skin: {}", skinModel.id());
            }
        }
    }
}