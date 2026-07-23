package com.xiaoshi2022.tomatocombomod.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = TomatoComboMod.MODID, value = Dist.CLIENT)
public class TomatoJuiceOverlayHandler {

    // 静态贴图 512x512
    private static final ResourceLocation OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            TomatoComboMod.MODID,
            "textures/gui/tomato_juice_overlay.png"
    );

    private static long startTime = 0;
    private static final long EFFECT_DURATION = 3000; // 3秒
    private static boolean isActive = false;

    public static void triggerTomatoJuiceEffect() {
        startTime = System.currentTimeMillis();
        isActive = true;
        TomatoComboMod.LOGGER.info("🍅 Tomato juice overlay effect triggered!");
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!isActive) return;

        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= EFFECT_DURATION) {
            isActive = false;
            return;
        }

        // 计算透明度：从 0.8 渐变到 0
        float opacity = 0.8f * (1.0f - (float) elapsed / EFFECT_DURATION);
        if (opacity <= 0) return;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        GuiGraphics guiGraphics = event.getGuiGraphics();

        // 计算居中绘制位置（保持比例 512x512，缩放至屏幕大小）
        float scale = Math.min(
                (float) screenWidth / 512.0f,
                (float) screenHeight / 512.0f
        );
        int drawWidth = (int) (512 * scale);
        int drawHeight = (int) (512 * scale);
        int x = (screenWidth - drawWidth) / 2;
        int y = (screenHeight - drawHeight) / 2;

        RenderSystem.setShaderTexture(0, OVERLAY_TEXTURE);

        guiGraphics.pose().pushPose();
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, opacity);

        // 绘制静态贴图
        guiGraphics.blit(
                OVERLAY_TEXTURE,
                x, y,
                drawWidth, drawHeight,
                0, 0,
                512, 512,
                512, 512
        );

        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.pose().popPose();
    }

    public static boolean isEffectActive() {
        return isActive;
    }
}