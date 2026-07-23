package com.xiaoshi2022.tomatocombomod.network;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.client.PlayerAnimationManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record PlayerAnimationSyncPayload(
        UUID playerUuid,
        String animationName
) implements CustomPacketPayload {

    public static final Type<PlayerAnimationSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TomatoComboMod.MODID, "player_animation_sync"));

    public static final String ANIMATION_PINCH = "pinch";

    public static final StreamCodec<FriendlyByteBuf, PlayerAnimationSyncPayload> STREAM_CODEC =
            StreamCodec.ofMember(PlayerAnimationSyncPayload::write, PlayerAnimationSyncPayload::read);

    private static PlayerAnimationSyncPayload read(FriendlyByteBuf buf) {
        return new PlayerAnimationSyncPayload(
                buf.readUUID(),
                buf.readUtf()
        );
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(playerUuid);
        buf.writeUtf(animationName);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 服务端调用：向目标玩家周围的所有玩家广播动画同步包
     */
    public static void broadcast(ServerPlayer sourcePlayer, String animationName) {
        if (sourcePlayer == null) {
            return;
        }
        PlayerAnimationSyncPayload payload = new PlayerAnimationSyncPayload(sourcePlayer.getUUID(), animationName);

        // ✅ NeoForge 1.21.1 的正确写法
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(sourcePlayer, payload);
    }

    /**
     * 客户端处理动画同步包
     */
    public static void handleClient(PlayerAnimationSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player target = context.player().level().getPlayerByUUID(payload.playerUuid());
            if (target == null) return;

            String anim = payload.animationName();
            if (ANIMATION_PINCH.equals(anim)) {
                PlayerAnimationManager.playPinchAnimation(target, true);
            }
        });
    }
}