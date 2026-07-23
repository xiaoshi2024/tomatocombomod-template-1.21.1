package com.xiaoshi2022.tomatocombomod.network;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.skill.ITomatoSkill;
import com.xiaoshi2022.tomatocombomod.skill.SkillManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SkillActivationPayload(String skillId) implements CustomPacketPayload {

    public static final Type<SkillActivationPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TomatoComboMod.MODID, "skill_activation")
    );

    // ✅ 修复：使用匿名类实现 StreamCodec
    public static final StreamCodec<FriendlyByteBuf, SkillActivationPayload> STREAM_CODEC =
            new StreamCodec<FriendlyByteBuf, SkillActivationPayload>() {
                @Override
                public SkillActivationPayload decode(FriendlyByteBuf buf) {
                    return new SkillActivationPayload(buf.readUtf());
                }

                @Override
                public void encode(FriendlyByteBuf buf, SkillActivationPayload payload) {
                    buf.writeUtf(payload.skillId());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(SkillActivationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null) {
                return;
            }

            SkillManager manager = SkillManager.getInstance();
            ITomatoSkill skill = manager.getSkillById(payload.skillId());

            if (skill != null) {
                boolean success = manager.activateSkill(context.player(), skill);
                TomatoComboMod.LOGGER.info("Skill activation: {} success={}", payload.skillId(), success);
            }
        });
    }
}