package com.xiaoshi2022.tomatocombomod.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobTomatoPasteLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {

    private static final ItemStack ROTTEN_TOMATO = new ItemStack(
            vectorwing.farmersdelight.common.registry.ModItems.ROTTEN_TOMATO.get()
    );

    private static class TomatoPosition {
        final Vec3 offset;
        final float scale;
        final float rotation;

        TomatoPosition(Vec3 offset, float scale, float rotation) {
            this.offset = offset;
            this.scale = scale;
            this.rotation = rotation;
        }
    }

    private final Random random = new Random();

    public MobTomatoPasteLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        MobTomatoPasteDataManager.MobTomatoPasteData data = MobTomatoPasteDataManager.get(entity);
        if (data == null || data.ticksRemaining <= 0) {
            return;
        }

        float baseAlpha = (float) data.ticksRemaining / (float) data.maxTicks;
        if (baseAlpha <= 0.01f) return;

        var entityType = entity.getType();
        var key = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        if (key == null) return;

        String entityName = key.getPath();

        List<TomatoPosition> positions = getTomatoPositions(entityName, entity);

        for (TomatoPosition pos : positions) {
            renderTomatoAtPosition(poseStack, bufferSource, packedLight, entity, pos, baseAlpha);
        }
    }

    private void renderTomatoAtPosition(PoseStack poseStack, MultiBufferSource bufferSource,
                                        int packedLight, T entity, TomatoPosition pos, float baseAlpha) {
        poseStack.pushPose();

        // 获取实体位置
        Vec3 entityPos = entity.position();

        // 获取相机位置
        var renderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        var camera = renderDispatcher.camera;
        Vec3 cameraPos = camera.getPosition();

        // 获取实体的朝向（yaw和pitch）
        float yaw = entity.getYRot();
        float pitch = entity.getXRot();

        // 本地偏移量
        Vec3 localOffset = pos.offset;

        // 将本地偏移转换到世界坐标（考虑实体朝向）
        // 先绕Y轴旋转（偏航）
        Vec3 rotatedOffset = localOffset.yRot((float) Math.toRadians(-yaw));
        // 再绕X轴旋转（俯仰）
        rotatedOffset = rotatedOffset.xRot((float) Math.toRadians(-pitch));

        // 计算相对于相机的位置
        double x = entityPos.x + rotatedOffset.x - cameraPos.x;
        double y = entityPos.y + 1.6 + rotatedOffset.y - cameraPos.y;
        double z = entityPos.z + rotatedOffset.z - cameraPos.z;

        // 重置变换并设置相对位置
        poseStack.setIdentity();
        poseStack.translate(x, y, z);

        // 始终面向玩家（billboard效果）
        poseStack.mulPose(renderDispatcher.cameraOrientation());

        // 随机旋转（让每个番茄看起来自然）
        poseStack.mulPose(Axis.ZP.rotationDegrees(pos.rotation));

        // 缩放（随着时间淡出）- 增大基础尺寸
        float scale = pos.scale * (1.0f + 0.3f * baseAlpha); // 从0.7改为1.0，让番茄更大
        poseStack.scale(scale, scale, scale);

        // 渲染番茄物品
        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(
                ROTTEN_TOMATO,
                ItemDisplayContext.GUI,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                entity.level(),
                0
        );

        poseStack.popPose();
    }

    private List<TomatoPosition> getTomatoPositions(String entityName, T entity) {
        List<TomatoPosition> positions = new ArrayList<>();

        long seed = entity.getId() * 31L + 17L;
        random.setSeed(seed);

        float baseScale = getBaseScale(entityName);

        // 主番茄 - 在实体前方突出，增大尺寸
        positions.add(new TomatoPosition(
                new Vec3(0.0, 0.0, 0.35),
                baseScale * 1.2f, // 从0.8改为1.2
                random.nextFloat() * 360f
        ));

        // 根据不同生物类型添加额外番茄
        switch (entityName) {
            case "player", "villager", "zombie_villager", "zombie", "husk", "drowned",
                 "skeleton", "stray", "wither_skeleton", "pillager", "vindicator",
                 "evoker", "witch", "bogged":
                // 肩膀位置
                positions.add(new TomatoPosition(
                        new Vec3(-0.35, -0.15, 0.20),
                        baseScale * 0.8f, // 从0.5改为0.8
                        random.nextFloat() * 360f
                ));
                positions.add(new TomatoPosition(
                        new Vec3(0.35, -0.15, 0.20),
                        baseScale * 0.8f,
                        random.nextFloat() * 360f
                ));
                // 胸前位置
                positions.add(new TomatoPosition(
                        new Vec3(0.0, -0.35, 0.40),
                        baseScale * 0.9f, // 从0.6改为0.9
                        random.nextFloat() * 360f
                ));
                break;

            case "cow", "mooshroom", "pig", "sheep", "goat":
                // 身体后部
                positions.add(new TomatoPosition(
                        new Vec3(0.0, -0.30, 0.55),
                        baseScale * 0.9f, // 从0.6改为0.9
                        random.nextFloat() * 360f
                ));
                // 身体两侧
                positions.add(new TomatoPosition(
                        new Vec3(-0.30, -0.45, 0.40),
                        baseScale * 0.7f, // 从0.4改为0.7
                        random.nextFloat() * 360f
                ));
                positions.add(new TomatoPosition(
                        new Vec3(0.30, -0.45, 0.40),
                        baseScale * 0.7f,
                        random.nextFloat() * 360f
                ));
                break;

            case "horse", "donkey", "mule", "llama", "trader_llama", "camel":
                positions.add(new TomatoPosition(
                        new Vec3(0.0, -0.20, 0.55),
                        baseScale * 0.9f, // 从0.6改为0.9
                        random.nextFloat() * 360f
                ));
                positions.add(new TomatoPosition(
                        new Vec3(0.0, -0.55, 0.10),
                        baseScale * 0.8f, // 从0.5改为0.8
                        random.nextFloat() * 360f
                ));
                break;

            case "chicken", "rabbit", "parrot":
                positions.add(new TomatoPosition(
                        new Vec3(0.0, -0.20, 0.30),
                        baseScale * 0.8f, // 从0.5改为0.8
                        random.nextFloat() * 360f
                ));
                break;

            case "spider", "cave_spider":
                positions.add(new TomatoPosition(
                        new Vec3(0.0, -0.20, 0.45),
                        baseScale * 0.8f, // 从0.5改为0.8
                        random.nextFloat() * 360f
                ));
                positions.add(new TomatoPosition(
                        new Vec3(0.0, -0.30, -0.30),
                        baseScale * 0.8f,
                        random.nextFloat() * 360f
                ));
                break;

            case "slime", "magma_cube":
                float size = entity.getBbWidth();
                int tomatoCount = size > 2.0f ? 6 : (size > 1.0f ? 4 : 2);
                for (int i = 0; i < tomatoCount; i++) {
                    float angle = (float) (i * 2 * Math.PI / tomatoCount);
                    float radius = size * 0.3f;
                    positions.add(new TomatoPosition(
                            new Vec3(
                                    (float) Math.cos(angle) * radius,
                                    -0.2f + (i % 2) * 0.2f,
                                    (float) Math.sin(angle) * radius + 0.2f
                            ),
                            baseScale * 0.7f, // 从0.4改为0.7
                            random.nextFloat() * 360f
                    ));
                }
                break;

            case "giant":
                positions.add(new TomatoPosition(
                        new Vec3(0.0, -0.20, 0.40),
                        baseScale * 0.9f, // 从0.6改为0.9
                        random.nextFloat() * 360f
                ));
                positions.add(new TomatoPosition(
                        new Vec3(-0.40, -0.40, 0.20),
                        baseScale * 0.7f, // 从0.4改为0.7
                        random.nextFloat() * 360f
                ));
                positions.add(new TomatoPosition(
                        new Vec3(0.40, -0.40, 0.20),
                        baseScale * 0.7f,
                        random.nextFloat() * 360f
                ));
                break;

            default:
                // 其他实体至少显示一个主番茄
                break;
        }

        return positions;
    }

    private float getBaseScale(String entityName) {
        // 所有尺寸整体增大50%
        return switch (entityName) {
            case "player", "villager", "zombie_villager" -> 0.45f; // 从0.30改为0.45
            case "zombie", "husk", "drowned" -> 0.42f; // 从0.28改为0.42
            case "skeleton", "stray", "wither_skeleton" -> 0.42f; // 从0.28改为0.42
            case "pillager", "vindicator", "evoker" -> 0.42f; // 从0.28改为0.42
            case "witch" -> 0.40f; // 从0.26改为0.40
            case "bogged" -> 0.40f; // 从0.26改为0.40
            case "cow", "mooshroom" -> 0.52f; // 从0.35改为0.52
            case "pig" -> 0.48f; // 从0.32改为0.48
            case "sheep" -> 0.45f; // 从0.30改为0.45
            case "goat" -> 0.45f; // 从0.30改为0.45
            case "horse", "donkey", "mule" -> 0.60f; // 从0.40改为0.60
            case "llama", "trader_llama" -> 0.57f; // 从0.38改为0.57
            case "camel" -> 0.60f; // 从0.40改为0.60
            case "chicken" -> 0.45f; // 从0.30改为0.45
            case "rabbit" -> 0.38f; // 从0.25改为0.38
            case "parrot" -> 0.30f; // 从0.20改为0.30
            case "bat" -> 0.27f; // 从0.18改为0.27
            case "spider", "cave_spider" -> 0.38f; // 从0.25改为0.38
            case "slime", "magma_cube" -> 0.60f; // 从0.40改为0.60
            case "giant" -> 0.38f; // 从0.25改为0.38
            default -> 0.38f; // 从0.25改为0.38
        };
    }
}