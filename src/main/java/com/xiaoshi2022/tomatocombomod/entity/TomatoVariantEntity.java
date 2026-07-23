package com.xiaoshi2022.tomatocombomod.entity;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.item.TomatoVariantItem;
import com.xiaoshi2022.tomatocombomod.registry.ModEntityTypes;
import com.xiaoshi2022.tomatocombomod.registry.ModItems;
import com.xiaoshi2022.tomatocombomod.registry.ModSounds;
import com.xiaoshi2022.tomatocombomod.network.TomatoHitPayload;
import com.xiaoshi2022.tomatocombomod.network.TomatoJuicePayload;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;

public class TomatoVariantEntity extends ThrowableItemProjectile {

    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID =
            SynchedEntityData.defineId(TomatoVariantEntity.class, EntityDataSerializers.INT);

    // 默认变种 - 确保不为 null
    private TomatoVariantItem.Variant tempVariant = TomatoVariantItem.Variant.TOMATO_BOOGER;

    public TomatoVariantEntity(EntityType<? extends TomatoVariantEntity> entityType, Level level) {
        super(entityType, level);
        this.tempVariant = TomatoVariantItem.Variant.TOMATO_BOOGER;
    }

    public TomatoVariantEntity(Level level, LivingEntity entity) {
        super(ModEntityTypes.TOMATO_VARIANT.get(), entity, level);
        this.tempVariant = TomatoVariantItem.Variant.TOMATO_BOOGER;
        TomatoComboMod.LOGGER.info("TomatoVariantEntity created with owner: {}", entity);
    }

    public TomatoVariantEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.TOMATO_VARIANT.get(), x, y, z, level);
        this.tempVariant = TomatoVariantItem.Variant.TOMATO_BOOGER;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        // 使用默认值
        builder.define(DATA_VARIANT_ID, TomatoVariantItem.Variant.TOMATO_BOOGER.ordinal());
    }

    public void setVariant(TomatoVariantItem.Variant variant) {
        if (variant == null) {
            TomatoComboMod.LOGGER.warn("Attempted to set null variant, using default");
            variant = TomatoVariantItem.Variant.TOMATO_BOOGER;
        }
        this.tempVariant = variant;
        if (this.entityData != null) {
            this.entityData.set(DATA_VARIANT_ID, variant.ordinal());
        }
    }

    public TomatoVariantItem.Variant getVariant() {
        if (this.entityData != null) {
            try {
                int ordinal = this.entityData.get(DATA_VARIANT_ID);
                TomatoVariantItem.Variant[] variants = TomatoVariantItem.Variant.values();
                if (ordinal >= 0 && ordinal < variants.length) {
                    return variants[ordinal];
                }
            } catch (Exception e) {
                TomatoComboMod.LOGGER.warn("Failed to get variant from entityData, using temp: {}", e.getMessage());
            }
        }
        // 确保 tempVariant 不为 null
        if (this.tempVariant == null) {
            this.tempVariant = TomatoVariantItem.Variant.TOMATO_BOOGER;
        }
        return this.tempVariant;
    }

    @Override
    protected Item getDefaultItem() {
        // 确保返回有效的物品
        TomatoVariantItem.Variant variant = this.getVariant();
        if (variant == null) {
            variant = TomatoVariantItem.Variant.TOMATO_BOOGER;
        }
        Item item = ModItems.getTomatoVariant(variant);
        if (item == null) {
            TomatoComboMod.LOGGER.error("Failed to get item for variant: {}", variant);
            return ModItems.TOMATO_BOOGER.get(); // 回退到默认
        }
        return item;
    }

    @Override
    public void handleEntityEvent(byte id) {
        ItemStack entityStack = new ItemStack(this.getDefaultItem());
        if (id == 3) {
            ParticleOptions iparticledata = new ItemParticleOption(ParticleTypes.ITEM, entityStack);

            for (int i = 0; i < 12; ++i) {
                this.level().addParticle(iparticledata, this.getX(), this.getY(), this.getZ(),
                        ((double) this.random.nextFloat() * 2.0D - 1.0D) * 0.1F,
                        ((double) this.random.nextFloat() * 2.0D - 1.0D) * 0.1F + 0.1F,
                        ((double) this.random.nextFloat() * 2.0D - 1.0D) * 0.1F);
            }
        }
    }

    // 在 onHitEntity 方法中修改触发网络包的部分

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();

        float damage = getDamageForVariant();
        entity.hurt(this.damageSources().thrown(this, this.getOwner()), damage);

        playTomatoHitSound();

        if (entity instanceof LivingEntity) {
            applyVariantEffect((LivingEntity) entity);
        }

        if (!this.level().isClientSide && entity instanceof ServerPlayer serverPlayer) {
            triggerTomatoJuiceOnPlayer(serverPlayer);
        }
    }

    private void triggerTomatoJuiceOnPlayer(ServerPlayer player) {
        try {
            // ✅ 广播给所有跟踪该玩家的客户端
            TomatoHitPayload.broadcastToTracking(player, this.getVariant());

            // 发送番茄汁覆盖效果
            PacketDistributor.sendToPlayer(player, new TomatoJuicePayload());

            TomatoComboMod.LOGGER.info("Broadcast tomato juice effect for player: {}", player.getName().getString());
        } catch (Exception e) {
            TomatoComboMod.LOGGER.error("Failed to send tomato juice effect: {}", e.getMessage(), e);
        }
    }

    private void playTomatoHitSound() {
        SoundEvent hitSound = BuiltInRegistries.SOUND_EVENT.get(ModSounds.FARMERS_DELIGHT_TOMATO_HIT);
        if (hitSound != null) {
            this.playSound(hitSound, 1.0F,
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
        }
    }

    private float getDamageForVariant() {
        TomatoVariantItem.Variant variant = this.getVariant();
        if (variant == null) {
            return 0.5F;
        }
        return switch (variant) {
            case TOMATO_BOOGER -> 0.5F;
            case TOMATO_PORK -> 1.0F;
            case TOMATO_CHICKEN -> 1.0F;
            case TOMATO_EGG_FRY -> 0.5F;
            case TOMATO_EGG -> 0.5F;
            case TOMATO_SMASH -> 2.0F;
            case TOMATO_RICE -> 0.5F;
            case TOMATO_RIVER_NOODLE -> 0.5F;
            case TOMATO_RICE_NOODLE -> 0.5F;
            case FINAL_TOMATO -> 3.0F;
        };
    }

    private void applyVariantEffect(LivingEntity entity) {
        TomatoVariantItem.Variant variant = this.getVariant();
        if (variant == TomatoVariantItem.Variant.TOMATO_SMASH) {
            entity.knockback(0.5F,
                    this.getX() - entity.getX(),
                    this.getZ() - entity.getZ());
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            playTomatoHitSound();
            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        TomatoVariantItem.Variant variant = this.getVariant();
        if (variant != null) {
            tag.putInt("Variant", variant.ordinal());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Variant")) {
            int ordinal = tag.getInt("Variant");
            TomatoVariantItem.Variant[] variants = TomatoVariantItem.Variant.values();
            if (ordinal >= 0 && ordinal < variants.length) {
                this.setVariant(variants[ordinal]);
            }
        }
    }
}