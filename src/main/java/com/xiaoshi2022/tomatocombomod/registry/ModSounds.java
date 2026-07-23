package com.xiaoshi2022.tomatocombomod.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, "tomatocombomod");
    
    // 番茄投掷音效 - 使用FarmersDelight自带的烂番茄投掷音效（实际是雪球投掷声）
    public static final ResourceLocation FARMERS_DELIGHT_TOMATO_THROW = ResourceLocation.fromNamespaceAndPath("farmersdelight", "entity.rotten_tomato.throw");
    
    // 番茄命中音效 - 使用FarmersDelight自带的烂番茄命中音效（实际是史莱姆攻击声）
    public static final ResourceLocation FARMERS_DELIGHT_TOMATO_HIT = ResourceLocation.fromNamespaceAndPath("farmersdelight", "entity.rotten_tomato.hit");
    
    // 玩家坐在沙发/椅子上且附近有电脑时的音效 "得~得得"
    public static final DeferredHolder<SoundEvent, SoundEvent> SIT_ON_FURNITURE_WITH_COMPUTER = SOUND_EVENTS.register("entity.player.sit_on_furniture_with_computer", 
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("tomatocombomod", "entity.player.sit_on_furniture_with_computer")));
}
