package com.xiaoshi2022.tomatocombomod.mixin;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.client.renderer.layer.MobTomatoPasteLayer;
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

    /**
     * 在构造函数中直接添加到 layers 列表
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(CallbackInfo ci) {
        addTomatoLayer();
    }

    @Unique
    private void addTomatoLayer() {
        if (tomatoLayerAdded) return;

        try {
            boolean alreadyHas = this.layers.stream()
                    .anyMatch(l -> l instanceof MobTomatoPasteLayer);

            if (!alreadyHas) {
                @SuppressWarnings("unchecked")
                LivingEntityRenderer<T, M> renderer = (LivingEntityRenderer<T, M>) (Object) this;
                this.layers.add(new MobTomatoPasteLayer<>(renderer));
                tomatoLayerAdded = true;
//                TomatoComboMod.LOGGER.debug("Added MobTomatoPasteLayer via Mixin to: {}",
//                        renderer.getClass().getSimpleName());
            } else {
                tomatoLayerAdded = true;
            }
        } catch (Exception e) {
            TomatoComboMod.LOGGER.warn("Failed to add MobTomatoPasteLayer: {}", e.getMessage());
        }
    }
}