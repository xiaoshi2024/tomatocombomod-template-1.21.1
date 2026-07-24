package com.xiaoshi2022.tomatocombomod.network;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.registry.ModItems;
import com.xiaoshi2022.tomatocombomod.registry.ModMobEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PickNosePayload(int actionId) implements CustomPacketPayload {
    public static final Type<PickNosePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TomatoComboMod.MODID, "pick_nose")
    );

    public static final StreamCodec<FriendlyByteBuf, PickNosePayload> STREAM_CODEC =
            new StreamCodec<FriendlyByteBuf, PickNosePayload>() {
                @Override
                public PickNosePayload decode(FriendlyByteBuf buf) {
                    return new PickNosePayload(buf.readInt());
                }

                @Override
                public void encode(FriendlyByteBuf buf, PickNosePayload payload) {
                    buf.writeInt(payload.actionId());
                }
            };

    public enum Action {
        BOOGER,
        BLEED,
        ANIMATION_ONLY  // ✅ 新增：只播放动画，不产生效果
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void sendBooger() {
        PacketDistributor.sendToServer(new PickNosePayload(Action.BOOGER.ordinal()));
    }

    public static void sendBleed() {
        PacketDistributor.sendToServer(new PickNosePayload(Action.BLEED.ordinal()));
    }

    // ✅ 新增：发送仅动画同步
    public static void sendPickNoseWithAnimation() {
        PacketDistributor.sendToServer(new PickNosePayload(Action.ANIMATION_ONLY.ordinal()));
    }

    public static void handleServer(PickNosePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            Action action;
            try {
                action = Action.values()[payload.actionId()];
            } catch (ArrayIndexOutOfBoundsException e) {
                TomatoComboMod.LOGGER.warn("Invalid action id: {}", payload.actionId());
                return;
            }

            // ✅ 无论是哪种操作，都广播动画给其他玩家
            PlayerAnimationSyncPayload.broadcast(player, "pinch");

            switch (action) {
                case BOOGER -> {
                    ItemStack boogerStack = new ItemStack(ModItems.BOOGER.get(), 1);
                    player.getInventory().add(boogerStack);
                    TomatoComboMod.LOGGER.debug("Server: Player {} received booger", player.getName().getString());
                }
                case BLEED -> {
                    // ✅ DeferredHolder 实现了 Holder 接口，可以直接使用
                    player.addEffect(new MobEffectInstance(
                            ModMobEffects.BLEEDING,  // DeferredHolder 本身就是 Holder
                            120,
                            0,
                            false,
                            true,
                            true
                    ));
                    TomatoComboMod.LOGGER.debug("Server: Player {} started bleeding", player.getName().getString());
                }
                case ANIMATION_ONLY -> {
                    // 只播放动画，不做其他事情
                    TomatoComboMod.LOGGER.debug("Server: Player {} played animation only", player.getName().getString());
                }
            }
        });
    }
}