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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PickNosePayload(Action action) implements CustomPacketPayload {
    public static final Type<PickNosePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TomatoComboMod.MODID, "pick_nose")
    );

    public static final StreamCodec<FriendlyByteBuf, PickNosePayload> STREAM_CODEC =
            new StreamCodec<FriendlyByteBuf, PickNosePayload>() {
                @Override
                public PickNosePayload decode(FriendlyByteBuf buf) {
                    return new PickNosePayload(buf.readEnum(Action.class));
                }

                @Override
                public void encode(FriendlyByteBuf buf, PickNosePayload payload) {
                    buf.writeEnum(payload.action());
                }
            };

    public enum Action {
        BOOGER,
        BLEED
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void sendBooger() {
        PacketDistributor.sendToServer(new PickNosePayload(Action.BOOGER));
    }

    public static void sendBleed() {
        PacketDistributor.sendToServer(new PickNosePayload(Action.BLEED));
    }

    public static void handleServer(PickNosePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            switch (payload.action()) {
                case BOOGER -> {
                    ItemStack boogerStack = new ItemStack(ModItems.BOOGER.get(), 1);
                    player.getInventory().add(boogerStack);
                    TomatoComboMod.LOGGER.info("Server: Player {} received booger", player.getName().getString());
                }
                case BLEED -> {
                    // ✅ 直接使用 DeferredHolder，它实现了 Holder 接口
                    player.addEffect(new MobEffectInstance(
                            ModMobEffects.BLEEDING,  // DeferredHolder<MobEffect> 本身就是 Holder<MobEffect>
                            120, // 6秒 = 120 tick
                            0,
                            false,
                            true,
                            true
                    ));
                    TomatoComboMod.LOGGER.info("Server: Player {} started bleeding", player.getName().getString());
                }
            }
        });
    }
}