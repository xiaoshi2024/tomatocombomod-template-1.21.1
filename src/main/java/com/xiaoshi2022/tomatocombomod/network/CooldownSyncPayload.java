package com.xiaoshi2022.tomatocombomod.network;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.skill.SkillManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CooldownSyncPayload(String skillId, int ticks) implements CustomPacketPayload {

    public static final Type<CooldownSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TomatoComboMod.MODID, "cooldown_sync")
    );

    // ✅ 修复：使用匿名类实现 StreamCodec
    public static final StreamCodec<FriendlyByteBuf, CooldownSyncPayload> STREAM_CODEC =
            new StreamCodec<FriendlyByteBuf, CooldownSyncPayload>() {
                @Override
                public CooldownSyncPayload decode(FriendlyByteBuf buf) {
                    return new CooldownSyncPayload(buf.readUtf(), buf.readInt());
                }

                @Override
                public void encode(FriendlyByteBuf buf, CooldownSyncPayload payload) {
                    buf.writeUtf(payload.skillId());
                    buf.writeInt(payload.ticks());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(CooldownSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            SkillManager.getInstance().handleCooldownSync(payload.skillId(), payload.ticks());
        });
    }
}