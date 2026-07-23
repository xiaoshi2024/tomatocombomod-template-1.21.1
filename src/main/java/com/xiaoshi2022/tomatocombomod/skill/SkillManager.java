package com.xiaoshi2022.tomatocombomod.skill;

import com.xiaoshi2022.tomatocombomod.TomatoComboMod;
import com.xiaoshi2022.tomatocombomod.network.CooldownSyncPayload;
import com.xiaoshi2022.tomatocombomod.network.SkillSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class SkillManager {
    
    private static SkillManager instance;
    
    private final Map<String, ITomatoSkill> registeredSkills = new HashMap<>();
    // 服务端缓存 - 从PlayerDataStorage加载
    private final Map<String, Long> skillCooldowns = new HashMap<>();
    // 客户端缓存
    private final Map<String, Set<String>> clientPlayerSkills = new HashMap<>();
    private final Map<String, Long> clientCooldowns = new HashMap<>();
    
    private SkillManager() {
        registerSkill(new TomatoComboSkill());
        PlayerDataStorage.init();
    }
    
    public static SkillManager getInstance() {
        if (instance == null) {
            instance = new SkillManager();
        }
        return instance;
    }
    
    public void registerSkill(ITomatoSkill skill) {
        if (!registeredSkills.containsKey(skill.getId())) {
            registeredSkills.put(skill.getId(), skill);
            TomatoComboMod.LOGGER.info("Registered skill: {}", skill.getId());
        }
    }
    
    public ITomatoSkill getSkillById(String id) {
        return registeredSkills.get(id);
    }
    
    public Set<String> getPlayerSkillIds(Player player) {
        if (player.level().isClientSide()) {
            return clientPlayerSkills.getOrDefault(player.getUUID().toString(), Collections.emptySet());
        }
        if (player instanceof ServerPlayer serverPlayer) {
            return PlayerDataStorage.getPlayerSkills(serverPlayer);
        }
        return Collections.emptySet();
    }
    
    public List<ITomatoSkill> getPlayerSkills(Player player) {
        List<ITomatoSkill> skills = new ArrayList<>();
        Set<String> skillIds = getPlayerSkillIds(player);
        for (String id : skillIds) {
            ITomatoSkill skill = registeredSkills.get(id);
            if (skill != null) {
                skills.add(skill);
            }
        }
        return skills;
    }
    
    public boolean hasSkill(Player player, String skillId) {
        return getPlayerSkillIds(player).contains(skillId);
    }
    
    public void grantSkill(Player player, String skillId) {
        ITomatoSkill skill = registeredSkills.get(skillId);
        if (skill == null) {
            TomatoComboMod.LOGGER.warn("Skill not found: {}", skillId);
            return;
        }
        
        // 服务端保存到持久化存储
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerDataStorage.addSkill(serverPlayer, skillId);
            TomatoComboMod.LOGGER.info("Player {} granted skill: {}", player.getName().getString(), skillId);
            
            // 同步到客户端
            SkillSyncPayload syncPayload = new SkillSyncPayload(skillId, true);
            PacketDistributor.sendToPlayer(serverPlayer, syncPayload);
        }
    }
    
    public void revokeSkill(Player player, String skillId) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerDataStorage.removeSkill(serverPlayer, skillId);
            
            SkillSyncPayload syncPayload = new SkillSyncPayload(skillId, false);
            PacketDistributor.sendToPlayer(serverPlayer, syncPayload);
        }
    }
    
    public boolean isOnCooldown(Player player, ITomatoSkill skill) {
        String key = getCooldownKey(player, skill);
        
        if (player.level().isClientSide()) {
            return getClientCooldownRemaining(key) > 0;
        } else {
            Long endTime = skillCooldowns.get(key);
            return endTime != null && endTime > System.currentTimeMillis();
        }
    }
    
    public int getCooldownRemaining(Player player, ITomatoSkill skill) {
        String key = getCooldownKey(player, skill);
        
        if (player.level().isClientSide()) {
            return getClientCooldownRemaining(key);
        } else {
            Long endTime = skillCooldowns.get(key);
            if (endTime == null) return 0;
            long remaining = endTime - System.currentTimeMillis();
            return Math.max(0, (int) (remaining / 50));
        }
    }
    
    public void setCooldown(Player player, ITomatoSkill skill, int ticks) {
        String key = getCooldownKey(player, skill);
        long endTime = System.currentTimeMillis() + (long) ticks * 50;
        skillCooldowns.put(key, endTime);
        
        if (player instanceof ServerPlayer serverPlayer) {
            CooldownSyncPayload payload = new CooldownSyncPayload(skill.getId(), ticks);
            PacketDistributor.sendToPlayer(serverPlayer, payload);
        }
    }
    
    public boolean activateSkill(Player player, ITomatoSkill skill) {
        if (!hasSkill(player, skill.getId())) {
            return false;
        }
        
        if (isOnCooldown(player, skill)) {
            return false;
        }
        
        if (!skill.canActivate(player)) {
            return false;
        }
        
        boolean success = skill.activate(player);
        
        if (success && skill.getCooldown() > 0) {
            setCooldown(player, skill, skill.getCooldown());
        }
        
        return success;
    }
    
    public void tick(Player player) {
        // 只在服务端运行技能tick
        if (player.level().isClientSide()) {
            return;
        }
        
        for (ITomatoSkill skill : getPlayerSkills(player)) {
            skill.tick(player);
        }
        
        // 清理过期冷却
        String prefix = player.getUUID().toString();
        skillCooldowns.entrySet().removeIf(entry -> 
                entry.getKey().startsWith(prefix) && entry.getValue() <= System.currentTimeMillis());
    }
    
    public void cleanupPlayerState(Player player) {
        UUID uuid = player.getUUID();
        
        // 清理客户端缓存
        clientPlayerSkills.remove(uuid.toString());
        
        // 清理冷却
        String prefix = uuid.toString();
        skillCooldowns.keySet().removeIf(key -> key.startsWith(prefix));
        clientCooldowns.keySet().removeIf(key -> key.startsWith(prefix));
        
        // 清理技能状态
        for (ITomatoSkill skill : registeredSkills.values()) {
            skill.cleanupPlayerState(player);
        }
    }
    
    // 客户端冷却管理
    public void setClientCooldown(String skillId, long endTime) {
        clientCooldowns.put(skillId, endTime);
    }
    
    private int getClientCooldownRemaining(String key) {
        Long endTime = clientCooldowns.get(key);
        if (endTime == null) return 0;
        long remaining = endTime - System.currentTimeMillis();
        if (remaining <= 0) {
            clientCooldowns.remove(key);
            return 0;
        }
        return (int) (remaining / 50);
    }
    
    private String getCooldownKey(Player player, ITomatoSkill skill) {
        return player.getUUID().toString() + ":" + skill.getId();
    }
    
    // 处理客户端冷却同步
    public void handleCooldownSync(String skillId, int ticks) {
        String key = "CLIENT:" + skillId;
        long endTime = System.currentTimeMillis() + (long) ticks * 50;
        clientCooldowns.put(key, endTime);
    }
    
    // 处理技能同步（客户端）
    public void handleSkillSync(Player player, String skillId, boolean granted) {
        String uuidStr = player.getUUID().toString();
        if (granted) {
            clientPlayerSkills.computeIfAbsent(uuidStr, k -> new HashSet<>()).add(skillId);
        } else {
            Set<String> skills = clientPlayerSkills.get(uuidStr);
            if (skills != null) {
                skills.remove(skillId);
            }
        }
    }
}