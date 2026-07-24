package com.xiaoshi2022.tomatocombomod;

import com.mojang.logging.LogUtils;
import com.xiaoshi2022.tomatocombomod.event.SitOnFurnitureListener;
import com.xiaoshi2022.tomatocombomod.network.*;
import com.xiaoshi2022.tomatocombomod.registry.ModEntityTypes;
import com.xiaoshi2022.tomatocombomod.registry.ModItems;
import com.xiaoshi2022.tomatocombomod.registry.ModMobEffects;
import com.xiaoshi2022.tomatocombomod.registry.ModSounds;
import com.xiaoshi2022.tomatocombomod.skill.SkillManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(TomatoComboMod.MODID)
public class TomatoComboMod {
    public static final String MODID = "tomatocombomod";
    public static final Logger LOGGER = LogUtils.getLogger();

    // 注册器
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // 创造模式标签页
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tomatocombomod"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModItems.TOMATO_BOOGER.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.BOOGER.get());
                output.accept(ModItems.TOMATO_BOOGER.get());
                output.accept(ModItems.TOMATO_PORK.get());
                output.accept(ModItems.TOMATO_CHICKEN.get());
                output.accept(ModItems.TOMATO_EGG_FRY.get());
                output.accept(ModItems.TOMATO_EGG.get());
                output.accept(ModItems.TOMATO_SMASH.get());
                output.accept(ModItems.TOMATO_RICE.get());
                output.accept(ModItems.TOMATO_RIVER_NOODLE.get());
                output.accept(ModItems.TOMATO_RICE_NOODLE.get());
                output.accept(ModItems.FINAL_TOMATO.get());
            }).build());

    public TomatoComboMod(IEventBus modEventBus, ModContainer modContainer) {
        // 注册物品（使用 ModItems 中的注册器）
        ModItems.ITEMS.register(modEventBus);
        // 注册创造模式标签页
        CREATIVE_MODE_TABS.register(modEventBus);

        // 注册实体、音效和药水效果
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModMobEffects.MOB_EFFECTS.register(modEventBus);

        // 注册事件监听器
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(SitOnFurnitureListener.class);

        // 注册创造模式标签页内容和公共设置
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::commonSetup);

        // 注册网络包处理器 - 使用 modEventBus 而不是 container
        modEventBus.addListener(this::registerPayloadHandlers);
    }

    // 注册网络包处理器
    private void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID);

        // 注册服务端 -> 客户端的包
        registrar.playToClient(
                TomatoJuicePayload.TYPE,
                TomatoJuicePayload.STREAM_CODEC,
                TomatoJuicePayload::handleClient
        );

        registrar.playToClient(
                SkillSyncPayload.TYPE,
                SkillSyncPayload.STREAM_CODEC,
                SkillSyncPayload::handleClient
        );

        registrar.playToClient(
                CooldownSyncPayload.TYPE,
                CooldownSyncPayload.STREAM_CODEC,
                CooldownSyncPayload::handleClient
        );

        registrar.playToClient(
                TomatoHitPayload.TYPE,
                TomatoHitPayload.STREAM_CODEC,
                TomatoHitPayload::handleClient
        );

        registrar.playToClient(
                PlayerAnimationSyncPayload.TYPE,
                PlayerAnimationSyncPayload.STREAM_CODEC,
                PlayerAnimationSyncPayload::handleClient
        );

        // 注册客户端 -> 服务端的包
        registrar.playToServer(
                PickNosePayload.TYPE,
                PickNosePayload.STREAM_CODEC,
                PickNosePayload::handleServer
        );

        registrar.playToServer(
                SkillActivationPayload.TYPE,
                SkillActivationPayload.STREAM_CODEC,
                SkillActivationPayload::handleServer
        );
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Tomato Combo Mod - Common Setup");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // 将番茄变种物品添加到食物和饮品标签页
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.TOMATO_BOOGER.get());
            event.accept(ModItems.TOMATO_PORK.get());
            event.accept(ModItems.TOMATO_CHICKEN.get());
            event.accept(ModItems.TOMATO_EGG_FRY.get());
            event.accept(ModItems.TOMATO_EGG.get());
            event.accept(ModItems.TOMATO_SMASH.get());
            event.accept(ModItems.TOMATO_RICE.get());
            event.accept(ModItems.TOMATO_RIVER_NOODLE.get());
            event.accept(ModItems.TOMATO_RICE_NOODLE.get());
            event.accept(ModItems.FINAL_TOMATO.get());
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Tomato Combo Mod - Server Starting");
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        // 更新技能状态
        SkillManager.getInstance().tick(event.getEntity());
    }
}