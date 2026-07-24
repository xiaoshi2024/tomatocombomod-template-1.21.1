package com.xiaoshi2022.tomatocombomod.registry;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    public static final String CATEGORY = "key.categories.tomatocombomod";

    // ✅ 使用简单的构造函数：KeyMapping(描述, 键码, 分类)
    public static final KeyMapping PICK_NOSE = new KeyMapping(
            "key.tomatocombomod.pick_nose",
            GLFW.GLFW_KEY_H,  // H 键
            CATEGORY
    );

    public static final KeyMapping ACTIVATE_SKILL = new KeyMapping(
            "key.tomatocombomod.activate_skill",
            GLFW.GLFW_KEY_G,  // G 键
            CATEGORY
    );

    public static void register() {
        // Key bindings are registered via NeoForge's RegisterKeyMappingsEvent
    }
}