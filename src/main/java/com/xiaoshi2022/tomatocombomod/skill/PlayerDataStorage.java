package com.xiaoshi2022.tomatocombomod.skill;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataStorage {

    private static final String SKILLS_KEY = "TomatoComboSkills";

    // In-memory cache for quick access
    private static final ConcurrentHashMap<UUID, Set<String>> playerSkillsCache = new ConcurrentHashMap<>();

    public static void init() {
        NeoForge.EVENT_BUS.addListener(PlayerDataStorage::onPlayerJoin);
        NeoForge.EVENT_BUS.addListener(PlayerDataStorage::onPlayerLeave);
        NeoForge.EVENT_BUS.addListener(PlayerDataStorage::onPlayerClone);
    }

    /**
     * 玩家登录时加载技能数据
     */
    private static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            loadPlayerSkills(player);

            // 同步技能到客户端
            SkillManager manager = SkillManager.getInstance();
            for (String skillId : getPlayerSkills(player)) {
                manager.handleSkillSync(player, skillId, true);
            }
        }
    }

    /**
     * 玩家登出时清理缓存
     */
    private static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        playerSkillsCache.remove(uuid);
        SkillManager.getInstance().cleanupPlayerState(event.getEntity());
    }

    /**
     * 玩家死亡/重生时复制数据
     */
    private static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && event.getOriginal() instanceof ServerPlayer originalPlayer) {
            ServerPlayer newPlayer = (ServerPlayer) event.getEntity();

            // 复制技能数据
            Set<String> skills = getPlayerSkills(originalPlayer);
            savePlayerSkills(newPlayer, skills);

            // 同步到客户端
            SkillManager manager = SkillManager.getInstance();
            for (String skillId : skills) {
                manager.handleSkillSync(newPlayer, skillId, true);
            }
        }
    }

    // ✅ 添加这个缺失的方法
    /**
     * 加载玩家的技能数据到缓存
     */
    private static void loadPlayerSkills(ServerPlayer player) {
        UUID uuid = player.getUUID();

        // 从NBT加载
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag modData = persistentData.getCompound(TomatoComboMod.MODID);

        Set<String> skills = new HashSet<>();
        if (modData.contains(SKILLS_KEY)) {
            String[] skillArray = modData.getString(SKILLS_KEY).split(",");
            for (String skillId : skillArray) {
                if (!skillId.isEmpty()) {
                    skills.add(skillId);
                }
            }
        }

        // 更新缓存
        playerSkillsCache.put(uuid, skills);
        TomatoComboMod.LOGGER.debug("Loaded skills for player {}: {}", player.getName().getString(), skills);
    }

    /**
     * 获取玩家的技能列表
     */
    public static Set<String> getPlayerSkills(ServerPlayer player) {
        UUID uuid = player.getUUID();

        // 先从缓存获取
        Set<String> cached = playerSkillsCache.get(uuid);
        if (cached != null) {
            return cached;
        }

        // 从NBT加载
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag modData = persistentData.getCompound(TomatoComboMod.MODID);

        Set<String> skills = new HashSet<>();
        if (modData.contains(SKILLS_KEY)) {
            String[] skillArray = modData.getString(SKILLS_KEY).split(",");
            for (String skillId : skillArray) {
                if (!skillId.isEmpty()) {
                    skills.add(skillId);
                }
            }
        }

        // 更新缓存
        playerSkillsCache.put(uuid, skills);
        return skills;
    }

    /**
     * 保存玩家技能数据
     */
    public static void savePlayerSkills(ServerPlayer player, Set<String> skills) {
        UUID uuid = player.getUUID();

        // 更新缓存
        playerSkillsCache.put(uuid, skills);

        // 保存到NBT
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag modData = persistentData.getCompound(TomatoComboMod.MODID);

        String skillsString = String.join(",", skills);
        modData.putString(SKILLS_KEY, skillsString);
        persistentData.put(TomatoComboMod.MODID, modData);
    }

    /**
     * 添加技能给玩家
     */
    public static void addSkill(ServerPlayer player, String skillId) {
        Set<String> skills = getPlayerSkills(player);
        if (skills.add(skillId)) {
            savePlayerSkills(player, skills);
//            TomatoComboMod.LOGGER.info("Added skill {} to player {}", skillId, player.getName().getString());
        }
    }

    /**
     * 移除玩家技能
     */
    public static void removeSkill(ServerPlayer player, String skillId) {
        Set<String> skills = getPlayerSkills(player);
        if (skills.remove(skillId)) {
            savePlayerSkills(player, skills);
//            TomatoComboMod.LOGGER.info("Removed skill {} from player {}", skillId, player.getName().getString());
        }
    }

    /**
     * 检查玩家是否有某个技能
     */
    public static boolean hasSkill(ServerPlayer player, String skillId) {
        return getPlayerSkills(player).contains(skillId);
    }
}