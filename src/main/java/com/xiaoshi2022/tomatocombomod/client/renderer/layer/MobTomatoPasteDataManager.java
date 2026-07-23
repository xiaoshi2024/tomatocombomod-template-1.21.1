package com.xiaoshi2022.tomatocombomod.client.renderer.layer;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.item.TomatoVariantItem;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = TomatoComboMod.MODID, value = Dist.CLIENT)
public class MobTomatoPasteDataManager {

    private static final ConcurrentHashMap<UUID, MobTomatoPasteData> mobData = new ConcurrentHashMap<>();
    private static final int PASTE_DURATION = 120; // 6秒

    public static void setMobHit(LivingEntity entity, TomatoVariantItem.Variant variant) {
        if (entity == null) return;
        mobData.put(entity.getUUID(), new MobTomatoPasteData(variant, PASTE_DURATION));
        TomatoComboMod.LOGGER.info("🍅 Set tomato paste for mob: {}, variant: {}",
                entity.getName().getString(), variant);
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
            }
        }
    }

    public static void cleanup(LivingEntity entity) {
        mobData.remove(entity.getUUID());
    }

    // ✅ 将这个类改为 public static 并暴露给外部
    public static class MobTomatoPasteData {
        public final TomatoVariantItem.Variant variant;
        public final int maxTicks;
        public int ticksRemaining;

        public MobTomatoPasteData(TomatoVariantItem.Variant variant, int durationTicks) {
            this.variant = variant;
            this.maxTicks = durationTicks;
            this.ticksRemaining = durationTicks;
        }
    }
}