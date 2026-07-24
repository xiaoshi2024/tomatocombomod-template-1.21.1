package com.xiaoshi2022.tomatocombomod.network;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.client.renderer.layer.MobTomatoPasteDataManager;
import com.xiaoshi2022.tomatocombomod.client.renderer.layer.TomatoPasteDataManager;
import com.xiaoshi2022.tomatocombomod.item.TomatoVariantItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// ✅ 使用 int entityId 而不是 UUID
public record TomatoHitPayload(int entityId, int variantId, boolean isPlayer) implements CustomPacketPayload {

    public static final Type<TomatoHitPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TomatoComboMod.MODID, "tomato_hit")
    );

    public static final StreamCodec<FriendlyByteBuf, TomatoHitPayload> STREAM_CODEC =
            new StreamCodec<FriendlyByteBuf, TomatoHitPayload>() {
                @Override
                public TomatoHitPayload decode(FriendlyByteBuf buf) {
                    return new TomatoHitPayload(buf.readInt(), buf.readInt(), buf.readBoolean());
                }

                @Override
                public void encode(FriendlyByteBuf buf, TomatoHitPayload payload) {
                    buf.writeInt(payload.entityId());
                    buf.writeInt(payload.variantId());
                    buf.writeBoolean(payload.isPlayer());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 发送给单个玩家（玩家被击中）
     */
    public static void sendTo(ServerPlayer player, TomatoVariantItem.Variant variant) {
        PacketDistributor.sendToPlayer(player,
                new TomatoHitPayload(player.getId(), variant.ordinal(), true));
    }

    /**
     * 广播给所有跟踪该玩家的客户端（玩家被击中）
     */
    public static void broadcastToTracking(ServerPlayer player, TomatoVariantItem.Variant variant) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new TomatoHitPayload(player.getId(), variant.ordinal(), true));
    }

    /**
     * 广播生物被击中（给所有跟踪该生物的客户端）
     */
    public static void broadcastMobHit(LivingEntity entity, TomatoVariantItem.Variant variant) {
        if (entity.level().isClientSide()) return;

        TomatoHitPayload payload = new TomatoHitPayload(entity.getId(), variant.ordinal(), false);
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
//        TomatoComboMod.LOGGER.info("Broadcast mob tomato hit: {}", entity.getName().getString());
    }

    public static void handleClient(TomatoHitPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null || context.player().level() == null) return;

            // ✅ 使用 entityId (int) 获取实体
            Entity entity = context.player().level().getEntity(payload.entityId());
            if (entity == null) {
                TomatoComboMod.LOGGER.warn("Entity not found with ID: {}", payload.entityId());
                return;
            }

            TomatoVariantItem.Variant variant = TomatoVariantItem.Variant.values()[payload.variantId()];

            if (payload.isPlayer() && entity instanceof Player player) {
                TomatoPasteDataManager.setPlayerHit(player, variant);
//                TomatoComboMod.LOGGER.info("Client: Player {} hit by tomato variant {}",
//                        player.getName().getString(), variant);
            } else if (!payload.isPlayer() && entity instanceof LivingEntity living) {
                MobTomatoPasteDataManager.setMobHit(living, variant);
//                TomatoComboMod.LOGGER.info("Client: Mob {} hit by tomato variant {}",
//                        living.getName().getString(), variant);
            }
        });
    }
}