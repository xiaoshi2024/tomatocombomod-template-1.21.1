package com.xiaoshi2022.tomatocombomod.network;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.client.renderer.layer.TomatoPasteDataManager;
import com.xiaoshi2022.tomatocombomod.item.TomatoVariantItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record TomatoHitPayload(UUID targetPlayerId, int variantId) implements CustomPacketPayload {

    public static final Type<TomatoHitPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TomatoComboMod.MODID, "tomato_hit")
    );

    public static final StreamCodec<FriendlyByteBuf, TomatoHitPayload> STREAM_CODEC =
            new StreamCodec<FriendlyByteBuf, TomatoHitPayload>() {
                @Override
                public TomatoHitPayload decode(FriendlyByteBuf buf) {
                    return new TomatoHitPayload(buf.readUUID(), buf.readInt());
                }

                @Override
                public void encode(FriendlyByteBuf buf, TomatoHitPayload payload) {
                    buf.writeUUID(payload.targetPlayerId());
                    buf.writeInt(payload.variantId());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 发送给单个玩家
     */
    public static void sendTo(ServerPlayer player, TomatoVariantItem.Variant variant) {
        PacketDistributor.sendToPlayer(player, new TomatoHitPayload(player.getUUID(), variant.ordinal()));
    }

    /**
     * 广播给所有跟踪该玩家的客户端（包括玩家自己）
     */
    public static void broadcastToTracking(ServerPlayer player, TomatoVariantItem.Variant variant) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new TomatoHitPayload(player.getUUID(), variant.ordinal()));
    }

    public static void handleClient(TomatoHitPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null || context.player().level() == null) {
                return;
            }

            // ✅ 根据 UUID 找到目标玩家
            var targetPlayer = context.player().level().getPlayerByUUID(payload.targetPlayerId());
            if (targetPlayer == null) {
                TomatoComboMod.LOGGER.warn("Target player not found: {}", payload.targetPlayerId());
                return;
            }

            TomatoVariantItem.Variant variant = TomatoVariantItem.Variant.values()[payload.variantId()];
            TomatoPasteDataManager.setPlayerHit(targetPlayer, variant);

            TomatoComboMod.LOGGER.info("Client: Player {} hit by tomato variant {}",
                    targetPlayer.getName().getString(), variant);
        });
    }
}