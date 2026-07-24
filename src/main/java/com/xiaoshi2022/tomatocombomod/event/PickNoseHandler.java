package com.xiaoshi2022.tomatocombomod.event;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.client.PlayerAnimationManager;
import com.xiaoshi2022.tomatocombomod.network.PickNosePayload;
import com.xiaoshi2022.tomatocombomod.registry.ModKeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Random;

@EventBusSubscriber(modid = TomatoComboMod.MODID, value = Dist.CLIENT)
public class PickNoseHandler {

    private static final double BOOGER_CHANCE = 0.3;
    private static final double BLEED_CHANCE = 0.3;
    private static final int ANIMATION_DURATION = 30;
    private static final int COOLDOWN = 40;

    private static boolean isAnimating = false;
    private static int animationTimer = 0;
    private static int cooldown = 0;
    private static boolean effectTriggered = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        // 处理动画播放
        if (isAnimating) {
            animationTimer++;

            if (animationTimer >= ANIMATION_DURATION) {
                if (!effectTriggered) {
                    triggerPickNose(mc.player);
                    effectTriggered = true;
                }

                isAnimating = false;
                animationTimer = 0;
                cooldown = COOLDOWN;
                effectTriggered = false;

                mc.player.displayClientMessage(
                        Component.literal("§7抠鼻完毕"),
                        true
                );
            }
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (ModKeyBindings.PICK_NOSE.consumeClick()) {
            // ✅ 播放本地动画
            playPinchAnimation(mc.player);

            // ✅ 发送动画同步包给其他玩家（只有服务端能发送，所以需要发送到服务端转发）
            // 注意：这里需要发送到服务端，然后由服务端广播给其他玩家
            // 但我们不能直接从客户端发送 PlayerAnimationSyncPayload 给其他客户端
            // 所以需要先发送到服务端，再由服务端广播

            // 发送抠鼻屎动作到服务端（包含动画同步）
            PickNosePayload.sendPickNoseWithAnimation();

            isAnimating = true;
            animationTimer = 0;
            effectTriggered = false;

            mc.player.displayClientMessage(
                    Component.literal("§e抠鼻屎中..."),
                    true
            );
        }
    }

    private static void playPinchAnimation(Player player) {
        PlayerAnimationManager.playAnimation(player, "pinch");
        TomatoComboMod.LOGGER.debug("🎬 Playing pinch animation for: {}", player.getName().getString());
    }

    private static void triggerPickNose(Player player) {
        Random random = new Random();
        double roll = random.nextDouble();

        if (roll < BOOGER_CHANCE) {
            player.displayClientMessage(Component.literal("§a成功抠出鼻屎！"), true);
            PickNosePayload.sendBooger();
        } else if (roll < BOOGER_CHANCE + BLEED_CHANCE) {
            player.displayClientMessage(Component.literal("§c抠破鼻子了！仰头止血！"), true);
            PickNosePayload.sendBleed();
        } else {
            player.displayClientMessage(Component.literal("§7什么都没抠到..."), true);
        }
    }
}