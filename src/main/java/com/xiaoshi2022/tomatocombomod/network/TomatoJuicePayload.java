package com.xiaoshi2022.tomatocombomod.network;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TomatoJuicePayload() implements CustomPacketPayload {

    public static final Type<TomatoJuicePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TomatoComboMod.MODID, "tomato_juice")
    );

    public static final StreamCodec<FriendlyByteBuf, TomatoJuicePayload> STREAM_CODEC =
            StreamCodec.unit(new TomatoJuicePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 客户端处理器 - 使用反射避免服务端加载客户端类
     * 参考 AffectionSyncPacket.handleClient 的实现方式
     */
    public static void handleClient(TomatoJuicePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                // 使用反射避免服务端加载客户端类
                Class<?> handlerClass = Class.forName("com.xiaoshi2022.tomatocombomod.event.TomatoJuiceOverlayHandler");
                java.lang.reflect.Method method = handlerClass.getMethod("triggerTomatoJuiceEffect");
                method.invoke(null);
//                TomatoComboMod.LOGGER.info("Received tomato juice effect packet");
            } catch (ClassNotFoundException e) {
                // 服务端环境，忽略
                TomatoComboMod.LOGGER.debug("Client-only class not found (server-side)");
            } catch (Exception e) {
                TomatoComboMod.LOGGER.warn("Failed to handle tomato juice effect", e);
            }
        });
    }
}