// RottenTomatoModel.java
package com.xiaoshi2022.tomatocombomod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class RottenTomatoModel extends Model {

    private final ModelPart tomato;

    public RottenTomatoModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.tomato = root.getChild("tomato");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // 创建番茄模型 - 稍微扁平的球体
        PartDefinition tomatoPart = root.addOrReplaceChild(
                "tomato",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -6.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 16)
                        .addBox(-3.0F, -7.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), // 番茄蒂
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, 32, 32);
    }

    // ✅ 新增：直接创建 ModelPart 的静态方法
    public static ModelPart createModelPart() {
        LayerDefinition definition = createLayer();
        return definition.bakeRoot();
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay, int color) {
        tomato.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}