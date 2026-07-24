package com.xiaoshi2022.tomatocombomod.event;

import com.mrcrayfish.furniture.refurbished.entity.Seat;
import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = "tomatocombomod")
public class SitOnFurnitureListener {

    // 记录玩家状态
    private static final Map<String, Boolean> PREVIOUS_STATE = new ConcurrentHashMap<>();
    private static final Map<String, SoundInstance> ACTIVE_SOUNDS = new ConcurrentHashMap<>();
    private static final Map<String, Integer> LAST_CHECK_TIME = new ConcurrentHashMap<>();

    private static final double DETECTION_RADIUS = 4.0;
    private static final int CHECK_INTERVAL = 5;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();

        // 只在客户端处理
        if (!level.isClientSide) {
            return;
        }

        if (!player.isAlive() || player.isRemoved()) {
            removePlayerData(player);
            return;
        }

        String playerKey = getPlayerKey(player);

        // 限制检查频率
        int currentTick = player.tickCount;
        Integer lastCheck = LAST_CHECK_TIME.get(playerKey);
        if (lastCheck != null && currentTick - lastCheck < CHECK_INTERVAL) {
            return;
        }
        LAST_CHECK_TIME.put(playerKey, currentTick);

        // 检查玩家是否坐在家具上
        Entity vehicle = player.getVehicle();
        boolean isOnFurniture = vehicle instanceof Seat;

        // 获取上一次状态
        Boolean previousState = PREVIOUS_STATE.get(playerKey);

        // 检测状态变化
        if (previousState == null || previousState != isOnFurniture) {
            PREVIOUS_STATE.put(playerKey, isOnFurniture);

            if (isOnFurniture) {
                // 玩家刚坐上去 - 检查是否有电脑并播放音效
                BlockPos seatPos = vehicle.blockPosition();
                boolean hasComputerNearby = checkForComputerNearby(level, seatPos);

                if (hasComputerNearby) {
                    double distance = player.distanceToSqr(seatPos.getX() + 0.5, seatPos.getY() + 0.5, seatPos.getZ() + 0.5);
                    if (distance <= DETECTION_RADIUS * DETECTION_RADIUS) {
//                        TomatoComboMod.LOGGER.info("Player sat down with computer nearby!");
                        playCustomSound(level, seatPos, player);
                    }
                }
            } else {
                // 玩家刚站起来 - 停止音效
//                TomatoComboMod.LOGGER.info("Player stood up, stopping sound");
                stopCustomSound(player);
            }
        }

        // 如果玩家坐在家具上，持续检测电脑是否还在附近
        if (isOnFurniture && ACTIVE_SOUNDS.containsKey(playerKey)) {
            BlockPos seatPos = vehicle.blockPosition();
            boolean hasComputerNearby = checkForComputerNearby(level, seatPos);

            // 如果电脑不再附近，停止音效
            if (!hasComputerNearby) {
//                TomatoComboMod.LOGGER.info("Computer no longer nearby, stopping sound");
                stopCustomSound(player);
            }
        }
    }

    /**
     * 检查附近是否有电脑
     */
    private static boolean checkForComputerNearby(Level level, BlockPos centerPos) {
        int radius = (int) Math.ceil(DETECTION_RADIUS);
        double radiusSq = DETECTION_RADIUS * DETECTION_RADIUS;

        BlockState centerState = level.getBlockState(centerPos);
        if (isComputerBlock(centerState.getBlock())) {
            return true;
        }

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    BlockPos pos = centerPos.offset(x, y, z);
                    if (centerPos.distSqr(pos) <= radiusSq) {
                        BlockState state = level.getBlockState(pos);
                        if (isComputerBlock(state.getBlock())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检查方块是否是电脑
     */
    private static boolean isComputerBlock(Block block) {
        if (block == null) {
            return false;
        }

        String className = block.getClass().getName();
        if (className.contains("ComputerBlock") && className.contains("mrcrayfish")) {
            return true;
        }

        try {
            String registryName = block.builtInRegistryHolder().key().location().toString();
            if (registryName.contains("computer") && registryName.contains("furniture")) {
                return true;
            }
        } catch (Exception ignored) {}

        return false;
    }

    /**
     * 播放自定义音效 - 使用 SoundInstance
     */
    @OnlyIn(Dist.CLIENT)
    private static void playCustomSound(Level level, BlockPos pos, Player player) {
        try {
            if (!level.isClientSide) {
                return;
            }

            String playerKey = getPlayerKey(player);

            if (ACTIVE_SOUNDS.containsKey(playerKey)) {
                stopCustomSound(player);
            }

//            TomatoComboMod.LOGGER.info("Playing sit sound at position: {}", pos);

            // 创建 SoundInstance - 音量调小到 0.3F
            SoundInstance soundInstance = SimpleSoundInstance.forUI(
                    ModSounds.SIT_ON_FURNITURE_WITH_COMPUTER.get(),
                    1.0F, // 音调保持不变
                    0.3F    // 音量从 1.0F 降到 0.3F (30% 音量)
            );

            Minecraft.getInstance().getSoundManager().play(soundInstance);
            ACTIVE_SOUNDS.put(playerKey, soundInstance);

//            TomatoComboMod.LOGGER.info("Sound played successfully!");

        } catch (Exception e) {
            TomatoComboMod.LOGGER.error("Failed to play sound: {}", e.getMessage(), e);
        }
    }

    /**
     * 停止自定义音效 - 使用 1.21.1 的 API
     */
    @OnlyIn(Dist.CLIENT)
    private static void stopCustomSound(Player player) {
        try {
            if (player == null) {
                return;
            }

            String playerKey = getPlayerKey(player);
            SoundInstance soundInstance = ACTIVE_SOUNDS.remove(playerKey);

            if (soundInstance != null) {
                // 使用 1.21.1 的 stop 方法
                Minecraft.getInstance().getSoundManager().stop(soundInstance);
//                TomatoComboMod.LOGGER.info("Sound stopped for player: {}", player.getName().getString());
            } else {
                // 如果没有找到特定的 SoundInstance，尝试停止所有音效
                // 这会停止该玩家所有音效，慎用
                // player.stopSound();
            }

        } catch (Exception e) {
            TomatoComboMod.LOGGER.error("Failed to stop sound: {}", e.getMessage(), e);
        }
    }

    private static String getPlayerKey(Player player) {
        return player.getUUID().toString() + "_" + player.level().dimension().location();
    }

    private static void removePlayerData(Player player) {
        String key = getPlayerKey(player);
        PREVIOUS_STATE.remove(key);
        LAST_CHECK_TIME.remove(key);

        // 清理音效
        SoundInstance sound = ACTIVE_SOUNDS.remove(key);
        if (sound != null) {
            Minecraft.getInstance().getSoundManager().stop(sound);
        }
    }
}