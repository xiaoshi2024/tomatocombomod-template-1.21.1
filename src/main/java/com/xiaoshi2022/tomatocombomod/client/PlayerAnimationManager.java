package com.xiaoshi2022.tomatocombomod.client;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerAnimationManager {

    public static final ResourceLocation PINCH_LAYER_ID = ResourceLocation.fromNamespaceAndPath(
            TomatoComboMod.MODID, "pinch_layer");
    public static final ResourceLocation PINCH_ANIMATION_ID = ResourceLocation.fromNamespaceAndPath(
            TomatoComboMod.MODID, "pinch");

    private static final Map<String, Long> lastAnimationTriggerTime = new HashMap<>();
    private static final long DEDUP_DELAY_MS = 200;
    private static final long CLEANUP_THRESHOLD_MS = 30000;

    /**
     * 注册玩家动画控制器 (PAL)
     * 根据文档: https://docs.zigythebird.com/pal/how_to_play_animations
     */
    public static void registerAnimationFactory() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                PINCH_LAYER_ID,
                1500,
                player -> new PlayerAnimationController(player,
                        (controller, state, animSetter) -> {
                            // ✅ 重要：必须返回 PLAY 才能播放动画
                            return PlayState.CONTINUE;
                        }
                )
        );
        TomatoComboMod.LOGGER.info("Registered player animation controller for pinch");
    }

    /**
     * 播放抠鼻屎动画（客户端本地调用）
     */
    public static void playPinchAnimation(Player player) {
        playPinchAnimation(player, false);
    }

    /**
     * 播放抠鼻屎动画
     * @param player 玩家
     * @param fromServer 是否来自服务端（用于去重）
     */
    public static void playPinchAnimation(Player player, boolean fromServer) {
        if (player != null && player.level().isClientSide() && player instanceof AbstractClientPlayer clientPlayer) {
            playAnimation(clientPlayer, PINCH_ANIMATION_ID, fromServer);
        }
    }

    /**
     * 通用动画播放方法
     */
    private static void playAnimation(AbstractClientPlayer player, ResourceLocation animationId, boolean fromServer) {
        String key = player.getUUID() + ":" + animationId.getPath();
        
        if (fromServer) {
            Long lastTime = lastAnimationTriggerTime.get(key);
            if (lastTime != null && System.currentTimeMillis() - lastTime < DEDUP_DELAY_MS) {
                return;
            }
        } else {
            lastAnimationTriggerTime.put(key, System.currentTimeMillis());
            cleanupOldEntries();
        }
        
        try {
            PlayerAnimationController controller = (PlayerAnimationController)
                    PlayerAnimationAccess.getPlayerAnimationLayer(player, PINCH_LAYER_ID);
            if (controller != null) {
                controller.triggerAnimation(animationId);
            }
        } catch (Exception e) {
            TomatoComboMod.LOGGER.warn("Failed to play player animation {}: {}",
                    animationId, e.getMessage());
        }
    }
    
    private static void cleanupOldEntries() {
        long now = System.currentTimeMillis();
        lastAnimationTriggerTime.entrySet().removeIf(entry -> now - entry.getValue() > CLEANUP_THRESHOLD_MS);
    }

    public static void playAnimation(Player player, String animationName) {
        if (player != null && player.level().isClientSide() && player instanceof AbstractClientPlayer clientPlayer) {
            ResourceLocation animId = ResourceLocation.fromNamespaceAndPath(
                    TomatoComboMod.MODID, animationName);
            playAnimation(clientPlayer, animId, false);
        }
    }
}