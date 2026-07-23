package com.xiaoshi2022.tomatocombomod.client.renderer.layer;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.item.TomatoVariantItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class TomatoPasteLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    
    // 番茄汁贴图
    public static final ResourceLocation TOMATO_PASTE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TomatoComboMod.MODID, "textures/entity/tomato_paste.png");
    
    // 不同番茄变种的颜色映射
    private static final int[][] VARIANT_COLORS = {
            {255, 50, 50},    // TOMATO_BOOGER - 红色
            {255, 100, 50},   // TOMATO_PORK - 橙红色
            {255, 150, 50},   // TOMATO_CHICKEN - 橙色
            {255, 200, 0},    // TOMATO_EGG_FRY - 黄色
            {255, 255, 0},    // TOMATO_EGG - 亮黄色
            {180, 50, 50},    // TOMATO_SMASH - 暗红色
            {255, 100, 100},  // TOMATO_RICE - 粉红色
            {150, 200, 255},  // TOMATO_RIVER_NOODLE - 浅蓝色
            {255, 150, 200},  // TOMATO_RICE_NOODLE - 粉紫色
            {255, 0, 0}       // FINAL_TOMATO - 鲜红色
    };
    
    public TomatoPasteLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }
    
    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        
        // 检查玩家是否被番茄砸中
        TomatoPasteData data = TomatoPasteDataManager.get(player);
        if (data == null || data.ticksRemaining <= 0) {
            return;
        }
        
        // 计算透明度（随时间逐渐消失）
        float alpha = (float) data.ticksRemaining / (float) data.maxTicks;
        
        // 获取颜色
        int[] color = getVariantColor(data.variant);
        
        // 转换为 ARGB 格式
        int argb = ((int) (alpha * 200) << 24) | 
                   (color[0] << 16) | 
                   (color[1] << 8) | 
                   color[2];
        
        // 获取渲染缓冲区
        RenderType renderType = RenderType.entityTranslucent(TOMATO_PASTE_TEXTURE);
        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        
        // 复制玩家模型姿势到图层
        PlayerModel<AbstractClientPlayer> parentModel = this.getParentModel();
        
        // 渲染番茄汁叠加层
        parentModel.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, argb);
    }
    
    private int[] getVariantColor(TomatoVariantItem.Variant variant) {
        int index = variant.ordinal();
        if (index >= 0 && index < VARIANT_COLORS.length) {
            return VARIANT_COLORS[index];
        }
        return VARIANT_COLORS[0]; // 默认红色
    }
    
    /**
     * 番茄汁数据 - 存储砸中状态
     */
    public static class TomatoPasteData {
        public final TomatoVariantItem.Variant variant;
        public final int maxTicks;
        public int ticksRemaining;
        
        public TomatoPasteData(TomatoVariantItem.Variant variant, int durationTicks) {
            this.variant = variant;
            this.maxTicks = durationTicks;
            this.ticksRemaining = durationTicks;
        }
    }
}