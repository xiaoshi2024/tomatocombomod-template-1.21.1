package com.xiaoshi2022.tomatocombomod.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public interface ITomatoSkill {
    
    String getId();
    
    Component getName();
    
    Component getDescription();
    
    ResourceLocation getIcon();
    
    boolean isActivatable();
    
    int getCooldown();
    
    boolean activate(Player player);
    
    default void tick(Player player) {}
    
    default boolean canActivate(Player player) {
        return true;
    }
    
    default void cleanupPlayerState(Player player) {}
}