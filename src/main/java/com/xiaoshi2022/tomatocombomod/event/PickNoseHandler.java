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

    // 抠出鼻屎的概率（30%）
    private static final double BOOGER_CHANCE = 0.3;
    // 抠流血的概率（30%）
    private static final double BLEED_CHANCE = 0.3;
    // 动画长度（1.5秒 = 30 tick）
    private static final int ANIMATION_DURATION = 30;
    // 冷却时间（动画结束后需要等待的时间，约2秒 = 40 tick）
    private static final int COOLDOWN = 40;

    // 是否正在播放动画
    private static boolean isAnimating = false;
    // 动画播放计时器
    private static int animationTimer = 0;
    // 按键冷却
    private static int cooldown = 0;
    // 是否已触发效果（防止重复触发）
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

            // ✅ 动画播放完毕时触发效果
            if (animationTimer >= ANIMATION_DURATION) {
                // 动画播放完毕，触发效果
                if (!effectTriggered) {
                    triggerPickNose(mc.player);
                    effectTriggered = true;
                }

                // 进入冷却
                isAnimating = false;
                animationTimer = 0;
                cooldown = COOLDOWN;
                effectTriggered = false;

                mc.player.displayClientMessage(
                        Component.literal("§7抠鼻完毕"),
                        true
                );
            }
            // 动画播放期间不处理任何按键
            return;
        }

        // 处理冷却
        if (cooldown > 0) {
            cooldown--;
            // 冷却期间不处理任何按键
            return;
        }

        // 只有不在动画播放 && 不在冷却时，才检测按键
        if (ModKeyBindings.PICK_NOSE.consumeClick()) {
            // 播放抠鼻动画
            playPinchAnimation(mc.player);
            isAnimating = true;
            animationTimer = 0;
            effectTriggered = false;

            // 显示提示
            mc.player.displayClientMessage(
                    Component.literal("§e抠鼻屎中..."),
                    true
            );
        }
    }

    /**
     * 播放抠鼻屎动画
     */
    private static void playPinchAnimation(Player player) {
        PlayerAnimationManager.playAnimation(player, "pinch");
        TomatoComboMod.LOGGER.debug("🎬 Playing pinch animation for: {}", player.getName().getString());
    }

    /**
     * 触发抠鼻屎动作 - 随机结果（在动画播放完毕时调用）
     */
    private static void triggerPickNose(Player player) {
        Random random = new Random();
        double roll = random.nextDouble();

        if (roll < BOOGER_CHANCE) {
            // 抠出鼻屎
            player.displayClientMessage(Component.literal("§a成功抠出鼻屎！"), true);
//            TomatoComboMod.LOGGER.info("Player {} picked a booger", player.getName().getString());
            PickNosePayload.sendBooger();
        } else if (roll < BOOGER_CHANCE + BLEED_CHANCE) {
            // 抠流血 - 流血效果持续10秒（200 tick）
            player.displayClientMessage(Component.literal("§c抠破鼻子了！仰头止血！"), true);
//            TomatoComboMod.LOGGER.info("Player {} started bleeding", player.getName().getString());
            PickNosePayload.sendBleed();
        } else {
            // 什么都没抠到
            player.displayClientMessage(Component.literal("§7什么都没抠到..."), true);
        }
    }
}