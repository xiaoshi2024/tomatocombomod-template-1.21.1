// MobTomatoAttachmentLayer.java - 为不同生物单独调整

package com.xiaoshi2022.tomatocombomod.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.client.model.RottenTomatoModel;
import com.xiaoshi2022.tomatocombomod.item.TomatoVariantItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Zoglin;

import java.lang.reflect.Field;

public class MobTomatoAttachmentLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {

    private static final ResourceLocation TOMATO_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TomatoComboMod.MODID, "textures/entity/rotten_tomato.png");

    private final RottenTomatoModel tomatoModel;

    public MobTomatoAttachmentLayer(RenderLayerParent<T, M> renderer, ModelPart root) {
        super(renderer);
        this.tomatoModel = new RottenTomatoModel(root);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        MobTomatoPasteDataManager.MobTomatoPasteData data = MobTomatoPasteDataManager.get(entity);
        if (data == null || data.ticksRemaining <= 0) {
            return;
        }

        java.util.List<MobTomatoPasteDataManager.TomatoPosition> positions = data.getTomatoPositions();
        if (positions.isEmpty()) return;

        float alpha = (float) data.ticksRemaining / (float) data.maxTicks;
        if (alpha <= 0.01f) return;

        int[] color = getVariantColor(data.variant);
        int argb = ((int) (alpha * 200) << 24) | (color[0] << 16) | (color[1] << 8) | color[2];

        // 获取生物类型和对应的偏移
        EntityOffsets offsets = getEntityOffsets(entity);
        float scaleConfig = offsets.scale;

        var entityPos = entity.getPosition(partialTick);

        for (int i = 0; i < positions.size(); i++) {
            MobTomatoPasteDataManager.TomatoPosition pos = positions.get(i);
            if (pos == null) continue;

            poseStack.pushPose();

            ModelPart headPart = getHeadPart(entity);
            if (headPart != null) {
                float headX = headPart.x / 16.0F;
                float headY = headPart.y / 16.0F;
                float headZ = headPart.z / 16.0F;

                double x = headX + pos.x + offsets.offsetX;
                double y = headY + pos.y + offsets.offsetY;
                double z = headZ + pos.z + offsets.offsetZ;

                poseStack.translate(x, y, z);
            } else {
                float eyeHeight = entity.getEyeHeight();
                poseStack.translate(pos.x + offsets.offsetX,
                        eyeHeight + pos.y + offsets.offsetY,
                        pos.z + offsets.offsetZ);
            }

            // 随机旋转（小幅度）
            float rotX = (entity.tickCount * 0.5f + entity.getId() + i * 137) % 360;
            float rotZ = (entity.tickCount * 0.3f + entity.getId() * 2 + i * 197) % 360;
            poseStack.mulPose(Axis.XP.rotationDegrees(rotX * 0.1f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ * 0.1f));

            // 缩放
            float scale = scaleConfig * (0.7f + 0.3f * alpha) * pos.scale;
            poseStack.scale(scale, scale, scale);

            tomatoModel.renderToBuffer(
                    poseStack,
                    bufferSource.getBuffer(tomatoModel.renderType(TOMATO_TEXTURE)),
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    argb
            );

            poseStack.popPose();
        }
    }

    /**
     * 🎯 为不同生物设置偏移值
     * 正数 = 向前/向上，负数 = 向后/向下
     */
    private EntityOffsets getEntityOffsets(T entity) {
        // 默认值（人形生物）
        double offsetX = 0.0;
        double offsetY = 0.0;
        double offsetZ = 0.0;
        float scale = 0.4f;

        if (entity instanceof Pig) {
            // 🐷 猪
            offsetX = 0.0;
            offsetY = -0.1;   // 向下一点
            offsetZ = -0.5;    // 向前一点
            scale = 0.4f;
        } else if (entity instanceof Cow) {
            // 🐄 牛 / 蘑菇牛
            offsetX = 0.0;
            offsetY = -0.9;
            offsetZ = -0.5;
            scale = 0.45f;
        } else if (entity instanceof Sheep) {
            // 🐑 羊
            offsetX = 0.0;
            offsetY = -0.9;
            offsetZ = -0.5;
            scale = 0.4f;
        } else if (entity instanceof Goat) {
            // 🐐 山羊
            offsetX = 0.0;
            offsetY = -0.9;
            offsetZ = -0.5;
            scale = 0.4f;
        } else if (entity instanceof Horse) {
            // 🐴 马
            offsetX = 0.0;
            offsetY = -1.1;
            offsetZ = -0.5;
            scale = 0.5f;
        } else if (entity instanceof Wolf) {
            // 🐺 狼
            offsetX = 0.0;
            offsetY = 0.0;
            offsetZ = 0.5;
            scale = 0.4f;
        } else if (entity instanceof Fox) {
            // 🦊 狐狸
            offsetX = 0.0;
            offsetY = 0.0;
            offsetZ = 0.4;
            scale = 0.35f;
        } else if (entity instanceof Rabbit) {
            // 🐰 兔子
            offsetX = 0.0;
            offsetY = 0.1;
            offsetZ = 0.3;
            scale = 0.35f;
        } else if (entity instanceof Chicken) {
            // 🐔 鸡
            offsetX = 0.0;
            offsetY = -0.1;
            offsetZ = 0.2;
            scale = 0.35f;
        } else if (entity instanceof PolarBear) {
            // 🐻‍❄️ 北极熊
            offsetX = 0.0;
            offsetY = -0.2;
            offsetZ = 0.6;
            scale = 0.5f;
        } else if (entity instanceof Panda) {
            // 🐼 熊猫
            offsetX = 0.0;
            offsetY = -0.9;
            offsetZ = -0.5;
            scale = 0.45f;
        } else if (entity instanceof Turtle) {
            // 🐢 龟
            offsetX = 0.0;
            offsetY = 0.6;
            offsetZ = 0.3;
            scale = 0.35f;
        } else if (entity instanceof Dolphin) {
            // 🐬 海豚
            offsetX = 0.0;
            offsetY = 0.7;
            offsetZ = 0.5;
            scale = 0.4f;
        } else if (entity instanceof Strider) {
            // 炽足兽
            offsetX = 0.0;
            offsetY = -1.7;
            offsetZ = 0.4;
            scale = 0.4f;
        } else if (entity instanceof net.minecraft.world.entity.monster.hoglin.Hoglin) {
            // 疣猪兽
            offsetX = 0.0;
            offsetY = -0.2;
            offsetZ = 0.7;
            scale = 0.5f;
        } else if (entity instanceof Zoglin) {
            // 僵尸疣猪兽
            offsetX = 0.0;
            offsetY = -0.2;
            offsetZ = 0.7;
            scale = 0.5f;
        }
        // 其他生物（包括人形生物）使用默认值

        return new EntityOffsets(offsetX, offsetY, offsetZ, scale);
    }

    /**
     * 偏移数据类
     */
    private static class EntityOffsets {
        double offsetX;
        double offsetY;
        double offsetZ;
        float scale;

        EntityOffsets(double offsetX, double offsetY, double offsetZ, float scale) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.scale = scale;
        }
    }

    /**
     * 检测是否是四足动物
     */
    private boolean isQuadruped(T entity) {
        return entity instanceof Cow ||
                entity instanceof Pig ||
                entity instanceof Sheep ||
                entity instanceof Goat ||
                entity instanceof Horse ||
                entity instanceof net.minecraft.world.entity.animal.horse.Donkey ||
                entity instanceof net.minecraft.world.entity.animal.horse.Mule ||
                entity instanceof net.minecraft.world.entity.animal.horse.Llama ||
                entity instanceof net.minecraft.world.entity.animal.camel.Camel ||
                entity instanceof Wolf ||
                entity instanceof Fox ||
                entity instanceof PolarBear ||
                entity instanceof Panda ||
                entity instanceof Rabbit ||
                entity instanceof Chicken ||
                entity instanceof Turtle ||
                entity instanceof Dolphin ||
                entity instanceof Strider ||
                entity instanceof net.minecraft.world.entity.monster.hoglin.Hoglin ||
                entity instanceof Zoglin;
    }

    /**
     * 获取头部模型部件
     */
    private ModelPart getHeadPart(T entity) {
        M model = this.getParentModel();
        if (model == null) return null;

        if (model instanceof HeadedModel headedModel) {
            try {
                ModelPart head = headedModel.getHead();
                if (head != null) {
                    return head;
                }
            } catch (Exception e) {
                // 忽略
            }
        }

        try {
            Field field = model.getClass().getDeclaredField("head");
            field.setAccessible(true);
            Object value = field.get(model);
            if (value instanceof ModelPart) {
                return (ModelPart) value;
            }
        } catch (Exception ignored) {}

        try {
            Field rootField = null;
            try {
                rootField = model.getClass().getDeclaredField("root");
            } catch (NoSuchFieldException e) {
                try {
                    rootField = model.getClass().getDeclaredField("model");
                } catch (NoSuchFieldException ex) {
                    // 忽略
                }
            }

            if (rootField != null) {
                rootField.setAccessible(true);
                Object rootObj = rootField.get(model);
                if (rootObj instanceof ModelPart) {
                    ModelPart root = (ModelPart) rootObj;
                    ModelPart head = root.getChild("head");
                    if (head != null) {
                        return head;
                    }
                }
            }
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * 获取变体颜色
     */
    private int[] getVariantColor(TomatoVariantItem.Variant variant) {
        int[][] colors = {
                {255, 50, 50},   // RED
                {255, 100, 50},  // ORANGE
                {255, 150, 50},  // YELLOW_ORANGE
                {255, 200, 0},   // YELLOW
                {255, 255, 0},   // LIME
                {180, 50, 50},   // DARK_RED
                {255, 100, 100}, // PINK
                {150, 200, 255}, // BLUE
                {255, 150, 200}, // PURPLE
                {255, 0, 0}      // BLOOD_RED
        };
        int index = variant.ordinal();
        return index < colors.length ? colors[index] : colors[0];
    }
}