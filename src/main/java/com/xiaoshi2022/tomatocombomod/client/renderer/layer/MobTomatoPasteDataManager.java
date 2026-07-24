// MobTomatoPasteDataManager.java - 优化番茄位置生成

package com.xiaoshi2022.tomatocombomod.client.renderer.layer;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.item.TomatoVariantItem;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = TomatoComboMod.MODID, value = Dist.CLIENT)
public class MobTomatoPasteDataManager {

    private static final ConcurrentHashMap<UUID, MobTomatoPasteData> mobData = new ConcurrentHashMap<>();
    private static final int PASTE_DURATION = 120; // 6秒
    private static final int MAX_TOMATOES_PER_MOB = 20;

    public static void setMobHit(LivingEntity entity, TomatoVariantItem.Variant variant) {
        if (entity == null) return;

        UUID uuid = entity.getUUID();
        MobTomatoPasteData data = mobData.get(uuid);

        if (data == null) {
            data = new MobTomatoPasteData(variant, PASTE_DURATION);
            data.addTomato(generateRandomOffset(entity));
            mobData.put(uuid, data);
//            TomatoComboMod.LOGGER.info("🍅 First tomato paste on mob: {}", entity.getName().getString());
        } else {
            data.ticksRemaining = PASTE_DURATION;
            if (data.getTomatoCount() < MAX_TOMATOES_PER_MOB) {
                data.addTomato(generateRandomOffset(entity));
//                TomatoComboMod.LOGGER.info("🍅 Added tomato #{} on mob: {}",
//                        data.getTomatoCount(), entity.getName().getString());
            }
        }
    }

    /**
     * 生成随机偏移位置 - 在头部周围分布
     * 注意：这些偏移是相对于头部部件的局部坐标
     */
    private static TomatoPosition generateRandomOffset(LivingEntity entity) {
        Random random = new Random();

        // 在头部周围球状分布
        double radius = 0.35;
        double theta = random.nextDouble() * 2 * Math.PI;
        double phi = random.nextDouble() * Math.PI;
        double r = 0.15 + random.nextDouble() * (radius - 0.15);

        double x = r * Math.sin(phi) * Math.cos(theta);
        double y = r * Math.cos(phi) * 0.8 - 0.05;
        double z = r * Math.sin(phi) * Math.sin(theta);

        float scale = 0.85f + random.nextFloat() * 0.3f;

        return new TomatoPosition(x, y, z, scale);
    }

    public static MobTomatoPasteData get(LivingEntity entity) {
        return mobData.get(entity.getUUID());
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!entity.level().isClientSide()) return;

        MobTomatoPasteData data = mobData.get(entity.getUUID());
        if (data != null) {
            data.ticksRemaining--;
            if (data.ticksRemaining <= 0) {
                mobData.remove(entity.getUUID());
                TomatoComboMod.LOGGER.debug("Removed tomato paste from mob: {}", entity.getName().getString());
            }
        }
    }

    public static void cleanup(LivingEntity entity) {
        mobData.remove(entity.getUUID());
    }

    /**
     * 番茄位置数据
     */
    public static class TomatoPosition {
        public final double x;
        public final double y;
        public final double z;
        public final float scale;

        public TomatoPosition(double x, double y, double z, float scale) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.scale = scale;
        }
    }

    public static class MobTomatoPasteData {
        public final TomatoVariantItem.Variant variant;
        public final int maxTicks;
        public int ticksRemaining;
        private final List<TomatoPosition> tomatoPositions;

        public MobTomatoPasteData(TomatoVariantItem.Variant variant, int durationTicks) {
            this.variant = variant;
            this.maxTicks = durationTicks;
            this.ticksRemaining = durationTicks;
            this.tomatoPositions = new ArrayList<>();
        }

        public void addTomato(TomatoPosition position) {
            tomatoPositions.add(position);
        }

        public List<TomatoPosition> getTomatoPositions() {
            return tomatoPositions;
        }

        public int getTomatoCount() {
            return tomatoPositions.size();
        }

        public TomatoPosition getTomatoPosition(int index) {
            if (index >= 0 && index < tomatoPositions.size()) {
                return tomatoPositions.get(index);
            }
            return null;
        }
    }
}