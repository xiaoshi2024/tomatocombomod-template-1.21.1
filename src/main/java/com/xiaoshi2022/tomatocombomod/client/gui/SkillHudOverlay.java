package com.xiaoshi2022.tomatocombomod.client.gui;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.skill.ITomatoSkill;
import com.xiaoshi2022.tomatocombomod.skill.SkillManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.List;

@EventBusSubscriber(modid = TomatoComboMod.MODID, value = Dist.CLIENT)
public class SkillHudOverlay {

    private static final int HUD_EDGE_OFFSET = 8;
    private static final int HUD_SLOT_SPACING = 22;
    private static final int HUD_ICON_SIZE = 16;
    private static final int HUD_BG_PADDING = 3;

    private static ITomatoSkill selectedSkill = null;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        SkillManager skillManager = SkillManager.getInstance();
        List<ITomatoSkill> skills = skillManager.getPlayerSkills(minecraft.player);

        if (skills.isEmpty()) {
            return;
        }

        int x = HUD_EDGE_OFFSET;
        int startY = calculateStartY(screenHeight, skills.size());

        renderSkills(event.getGuiGraphics(), minecraft, skills, skillManager, x, startY);
    }

    private static int calculateStartY(int screenHeight, int skillCount) {
        int totalHeight = skillCount * HUD_SLOT_SPACING;
        return Math.max(10, Math.min((screenHeight - totalHeight) / 2, screenHeight - totalHeight - 10));
    }

    private static void renderSkills(GuiGraphics graphics, Minecraft minecraft,
                                     List<ITomatoSkill> skills, SkillManager skillManager,
                                     int baseX, int baseY) {
        int index = 0;
        for (ITomatoSkill skill : skills) {
            int y = baseY + index * HUD_SLOT_SPACING;

            boolean isSelected = selectedSkill != null && selectedSkill.getId().equals(skill.getId());
            boolean onCooldown = skillManager.isOnCooldown(minecraft.player, skill);

            // 绘制背景框
            int bgColor = (isSelected && !onCooldown) ? 0xAA4488FF : 0xAA2A2A3E;
            int bgX = baseX - HUD_BG_PADDING;
            int bgY = y - HUD_BG_PADDING;
            int bgSize = HUD_ICON_SIZE + HUD_BG_PADDING * 2;
            graphics.fill(bgX, bgY, bgX + bgSize, bgY + bgSize, bgColor);

            // 绘制技能图标
            graphics.pose().pushPose();
            graphics.pose().translate(baseX, y, 100);

            ResourceLocation iconPath = skill.getIcon();
            float colorMultiplier = onCooldown ? 0.4f : 1.0f;
            graphics.setColor(colorMultiplier, colorMultiplier, colorMultiplier, 1.0f);

            if (iconPath != null) {
                graphics.blit(iconPath, 0, 0, 0, 0, HUD_ICON_SIZE, HUD_ICON_SIZE, HUD_ICON_SIZE, HUD_ICON_SIZE);
            } else {
                graphics.renderItem(getDefaultIcon(), 0, 0);
            }

            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            graphics.pose().popPose();

            // 冷却遮罩
            if (onCooldown) {
                renderCooldownOverlay(graphics, minecraft, baseX, y, skillManager, skill);
            }

            // 边框
            int borderColor = isSelected && !onCooldown ? 0xFFFFFFFF :
                             onCooldown ? 0xFF666666 : 0xFFAAAAAA;
            graphics.pose().pushPose();
            graphics.pose().translate(bgX, bgY, 250);
            graphics.renderOutline(0, 0, bgSize, bgSize, borderColor);
            graphics.pose().popPose();

            index++;
        }
    }

    private static void renderCooldownOverlay(GuiGraphics graphics, Minecraft minecraft,
                                              int x, int y, SkillManager skillManager,
                                              ITomatoSkill skill) {
        Player player = minecraft.player;
        if (player == null) return;

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 150);

        int remaining = skillManager.getCooldownRemaining(player, skill);
        if (remaining <= 0) {
            graphics.pose().popPose();
            return;
        }

        graphics.fill(0, 0, HUD_ICON_SIZE, HUD_ICON_SIZE, 0x88000000);

        int seconds = (remaining + 19) / 20;
        String cooldownText = String.valueOf(seconds);
        int textWidth = minecraft.font.width(cooldownText);
        graphics.drawString(minecraft.font, cooldownText,
                HUD_ICON_SIZE / 2 - textWidth / 2, HUD_ICON_SIZE / 2 - 4, 0xFFAAAAAA, true);

        graphics.pose().popPose();
    }

    private static ItemStack getDefaultIcon() {
        return new ItemStack(Items.LIGHTNING_ROD);
    }

    public static void setSelectedSkill(ITomatoSkill skill) {
        selectedSkill = skill;
    }

    public static ITomatoSkill getSelectedSkill() {
        return selectedSkill;
    }
}