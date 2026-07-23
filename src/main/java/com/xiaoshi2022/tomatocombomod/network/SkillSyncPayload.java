package com.xiaoshi2022.tomatocombomod.network;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.skill.SkillManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SkillSyncPayload(String skillId, boolean granted) implements CustomPacketPayload {

    public static final Type<SkillSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TomatoComboMod.MODID, "skill_sync")
    );

    // ✅ 修复：使用匿名类实现 StreamCodec
    public static final StreamCodec<FriendlyByteBuf, SkillSyncPayload> STREAM_CODEC =
            new StreamCodec<FriendlyByteBuf, SkillSyncPayload>() {
                @Override
                public SkillSyncPayload decode(FriendlyByteBuf buf) {
                    return new SkillSyncPayload(buf.readUtf(), buf.readBoolean());
                }

                @Override
                public void encode(FriendlyByteBuf buf, SkillSyncPayload payload) {
                    buf.writeUtf(payload.skillId());
                    buf.writeBoolean(payload.granted());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(SkillSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                SkillManager.getInstance().handleSkillSync(context.player(), payload.skillId(), payload.granted());
//                TomatoComboMod.LOGGER.info("Synced skill: {} granted={}", payload.skillId(), payload.granted());
            }
        });
    }
}