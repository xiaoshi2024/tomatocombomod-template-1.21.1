package com.xiaoshi2022.tomatocombomod.client.renderer.layer;

import com.xiaoshi2022.tomatocombomod.item.TomatoVariantItem;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = "tomatocombomod", value = Dist.CLIENT)
public class TomatoPasteDataManager {
    
    // 存储玩家的番茄汁状态（客户端）
    private static final ConcurrentHashMap<UUID, TomatoPasteLayer.TomatoPasteData> playerData = new ConcurrentHashMap<>();
    
    // 番茄汁持续时间（秒 * 20）
    private static final int PASTE_DURATION = 120; // 6秒
    
    /**
     * 设置玩家被番茄砸中
     */
    public static void setPlayerHit(Player player, TomatoVariantItem.Variant variant) {
        if (player == null) return;
        playerData.put(player.getUUID(), new TomatoPasteLayer.TomatoPasteData(variant, PASTE_DURATION));
//        TomatoComboMod.LOGGER.info("🍅 Set tomato paste data for player: {}, variant: {}, total players: {}",
//                player.getName().getString(), variant, playerData.size());
    }
    
    /**
     * 获取玩家的番茄汁数据
     */
    public static TomatoPasteLayer.TomatoPasteData get(Player player) {
        return playerData.get(player.getUUID());
    }
    
    /**
     * 更新玩家番茄汁状态
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        // 双重检查：确保只在客户端运行
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) {
            return;
        }
        
        if (!player.level().isClientSide()) {
            return;
        }
        
        TomatoPasteLayer.TomatoPasteData data = playerData.get(player.getUUID());
        if (data != null) {
            data.ticksRemaining--;
            if (data.ticksRemaining <= 0) {
                playerData.remove(player.getUUID());
            }
        }
    }
    
    /**
     * 清理玩家数据
     */
    public static void cleanup(Player player) {
        playerData.remove(player.getUUID());
    }
}