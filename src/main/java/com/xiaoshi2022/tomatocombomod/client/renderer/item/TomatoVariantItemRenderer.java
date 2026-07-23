package com.xiaoshi2022.tomatocombomod.client.renderer.item;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.item.TomatoVariantItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class TomatoVariantItemRenderer implements IClientItemExtensions {
    
    private static final ResourceLocation BASE_TOMATO_TEXTURE = 
            ResourceLocation.fromNamespaceAndPath("farmersdelight", "item/tomato");
    
    // 不同番茄变种的颜色映射（RGBA）
    private static final int[][] VARIANT_COLORS = {
            {255, 100, 100, 255},    // TOMATO_BOOGER - 粉红色
            {255, 100, 50, 255},     // TOMATO_PORK - 橙红色
            {255, 150, 50, 255},     // TOMATO_CHICKEN - 橙色
            {255, 200, 0, 255},      // TOMATO_EGG_FRY - 金黄色
            {255, 255, 0, 255},      // TOMATO_EGG - 亮黄色
            {180, 50, 50, 255},      // TOMATO_SMASH - 暗红色
            {255, 150, 150, 255},    // TOMATO_RICE - 浅红色
            {150, 200, 255, 255},    // TOMATO_RIVER_NOODLE - 浅蓝色
            {255, 150, 200, 255},    // TOMATO_RICE_NOODLE - 粉紫色
            {255, 0, 0, 255},        // FINAL_TOMATO - 鲜红色
            {150, 150, 150, 255}     // BOOGER - 灰色（鼻屎）
    };
    
    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return new TomatoVariantBER();
    }
    
    private static class TomatoVariantBER extends BlockEntityWithoutLevelRenderer {
        
        public TomatoVariantBER() {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        }

        // 替换第60-70行
        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                                 MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
            if (!(stack.getItem() instanceof TomatoVariantItem tomatoItem)) {
                Minecraft.getInstance().getItemRenderer().renderStatic(stack, context, packedLight, packedOverlay, poseStack, bufferSource, null, 0);
                return;
            }

            TomatoVariantItem.Variant variant = tomatoItem.getVariant();
            int[] color = getVariantColor(variant);

            poseStack.pushPose();

            var itemRenderer = Minecraft.getInstance().getItemRenderer();
            var model = itemRenderer.getModel(stack, null, null, 0);

            // ✅ 修复：使用正确的渲染方法
            var consumer = bufferSource.getBuffer(RenderType.entityTranslucent(BASE_TOMATO_TEXTURE));
            itemRenderer.render(stack, context, false, poseStack, (MultiBufferSource) consumer, packedLight, packedOverlay, model);

            poseStack.popPose();
        }
        
        private int[] getVariantColor(TomatoVariantItem.Variant variant) {
            int index = variant.ordinal();
            if (index >= 0 && index < VARIANT_COLORS.length) {
                return VARIANT_COLORS[index];
            }
            return VARIANT_COLORS[0]; // 默认粉红色
        }
    }
}