// LivingEntityRendererMixin.java
package com.xiaoshi2022.tomatocombomod.mixin;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.TomatoComboModClient;
import com.xiaoshi2022.tomatocombomod.client.renderer.layer.MobTomatoAttachmentLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends net.minecraft.client.model.EntityModel<T>> {

    @Shadow
    protected List<RenderLayer<T, M>> layers;

    @Unique
    private boolean tomatoLayerAdded = false;

    @Unique
    private static ModelPart tomatoModelPart = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(CallbackInfo ci) {
        addTomatoLayer();
    }

    @Unique
    private void addTomatoLayer() {
        if (tomatoLayerAdded) return;

        try {
            boolean alreadyHas = this.layers.stream()
                    .anyMatch(l -> l instanceof MobTomatoAttachmentLayer);

            if (!alreadyHas) {
                @SuppressWarnings("unchecked")
                LivingEntityRenderer<T, M> renderer = (LivingEntityRenderer<T, M>) (Object) this;

                // ✅ 从模型管理器获取已注册的 ModelPart
                ModelPart modelPart = getOrCreateTomatoModel();
                if (modelPart != null) {
                    this.layers.add(new MobTomatoAttachmentLayer<>(renderer, modelPart));
                    tomatoLayerAdded = true;
                    TomatoComboMod.LOGGER.debug("Added MobTomatoAttachmentLayer to: {}",
                            renderer.getClass().getSimpleName());
                }
            } else {
                tomatoLayerAdded = true;
            }
        } catch (Exception e) {
            TomatoComboMod.LOGGER.warn("Failed to add MobTomatoAttachmentLayer: {}", e.getMessage());
        }
    }

    @Unique
    private static ModelPart getOrCreateTomatoModel() {
        if (tomatoModelPart != null) {
            return tomatoModelPart;
        }

        try {
            // ✅ 从模型管理器获取已注册的模型
            var models = Minecraft.getInstance().getEntityModels();
            if (models == null) {
                TomatoComboMod.LOGGER.warn("EntityModels is null");
                return null;
            }

            tomatoModelPart = models.bakeLayer(TomatoComboModClient.ROTTEN_TOMATO_LAYER);
            TomatoComboMod.LOGGER.debug("Baked tomato model from registry");
            return tomatoModelPart;
        } catch (Exception e) {
            TomatoComboMod.LOGGER.warn("Failed to bake tomato model: {}", e.getMessage());
            // 如果从注册表获取失败，直接创建
            try {
                tomatoModelPart = com.xiaoshi2022.tomatocombomod.client.model.RottenTomatoModel.createModelPart();
                TomatoComboMod.LOGGER.debug("Created tomato model directly");
                return tomatoModelPart;
            } catch (Exception e2) {
                TomatoComboMod.LOGGER.warn("Failed to create tomato model directly: {}", e2.getMessage());
                return null;
            }
        }
    }
}