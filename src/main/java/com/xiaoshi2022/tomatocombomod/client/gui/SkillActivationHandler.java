package com.xiaoshi2022.tomatocombomod.client.gui;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.network.SkillActivationPayload;
import com.xiaoshi2022.tomatocombomod.registry.ModKeyBindings;
import com.xiaoshi2022.tomatocombomod.skill.ITomatoSkill;
import com.xiaoshi2022.tomatocombomod.skill.SkillManager;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber(modid = TomatoComboMod.MODID, value = Dist.CLIENT)
public class SkillActivationHandler {
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        
        // 检查技能键是否被按下
        if (ModKeyBindings.ACTIVATE_SKILL.consumeClick()) {
            activateSkill(minecraft.player);
        }
    }
    
    private static void activateSkill(net.minecraft.world.entity.player.Player player) {
        SkillManager manager = SkillManager.getInstance();
        List<ITomatoSkill> skills = manager.getPlayerSkills(player);
        
        if (skills.isEmpty()) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("message.tomatocombomod.no_skills"),
                    true
            );
            return;
        }
        
        // 使用选中的技能，如果没有选中则使用第一个技能
        ITomatoSkill selected = SkillHudOverlay.getSelectedSkill();
        ITomatoSkill skillToActivate = selected != null ? selected : skills.get(0);
        
        // 检查冷却
        if (manager.isOnCooldown(player, skillToActivate)) {
            int remaining = manager.getCooldownRemaining(player, skillToActivate);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("message.tomatocombomod.skill_on_cooldown", remaining / 20),
                    true
            );
            return;
        }
        
        // 发送技能激活包到服务端
        PacketDistributor.sendToServer(new SkillActivationPayload(skillToActivate.getId()));
//        TomatoComboMod.LOGGER.info("Sent skill activation: {}", skillToActivate.getId());
    }
}