package com.hzcu.loginmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.hzcu.loginmod.LoginMod;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 白名单配置管理类
 * 管理不需要登录的用户列表
 */
public class WhitelistConfig {
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = "hzculogin.json";
    private static final Path CONFIG_PATH = Paths.get(CONFIG_DIR, CONFIG_FILE);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private static WhitelistData whitelistData = new WhitelistData();
    
    /**
     * 白名单数据结构
     */
    private static class WhitelistData {
        private List<String> whitelistedPlayers = new ArrayList<>();
        private List<String> administrators = new ArrayList<>();
        
        public WhitelistData() {
            // 默认管理员列表（可以根据需要修改）
            administrators.add("admin");
        }
    }
    
    /**
     * 初始化配置
     */
    public static void init() {
        try {
            // 确保 config 目录存在
            Path configDir = Paths.get(CONFIG_DIR);
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                LoginMod.LOGGER.info("创建配置目录: {}", CONFIG_DIR);
            }
            
            // 加载或创建配置文件
            if (Files.exists(CONFIG_PATH)) {
                loadConfig();
            } else {
                saveConfig();
                LoginMod.LOGGER.info("创建默认白名单配置文件: {}", CONFIG_PATH);
            }
        } catch (IOException e) {
            LoginMod.LOGGER.error("初始化白名单配置时出错", e);
        }
    }
    
    /**
     * 从文件加载配置
     */
    private static void loadConfig() {
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            WhitelistData loaded = GSON.fromJson(reader, WhitelistData.class);
            if (loaded != null) {
                whitelistData = loaded;
                LoginMod.LOGGER.info("成功加载白名单配置，包含 {} 个白名单用户和 {} 个管理员", 
                    whitelistData.whitelistedPlayers.size(), 
                    whitelistData.administrators.size());
            }
        } catch (IOException | JsonSyntaxException e) {
            LoginMod.LOGGER.error("加载白名单配置时出错，使用默认配置", e);
            whitelistData = new WhitelistData();
        }
    }
    
    /**
     * 保存配置到文件
     */
    private static void saveConfig() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(whitelistData, writer);
            LoginMod.LOGGER.info("白名单配置已保存到: {}", CONFIG_PATH);
        } catch (IOException e) {
            LoginMod.LOGGER.error("保存白名单配置时出错", e);
        }
    }
    
    /**
     * 重新加载配置
     */
    public static void reload() {
        loadConfig();
    }
    
    /**
     * 检查玩家是否在白名单中
     */
    public static boolean isWhitelisted(String playerName) {
        return whitelistData.whitelistedPlayers.contains(playerName);
    }
    
    /**
     * 检查玩家是否是管理员
     */
    public static boolean isAdministrator(String playerName) {
        return whitelistData.administrators.contains(playerName);
    }
    
    /**
     * 添加玩家到白名单
     */
    public static boolean addToWhitelist(String playerName) {
        if (whitelistData.whitelistedPlayers.contains(playerName)) {
            return false; // 已存在
        }
        whitelistData.whitelistedPlayers.add(playerName);
        saveConfig();
        return true;
    }
    
    /**
     * 从白名单移除玩家
     */
    public static boolean removeFromWhitelist(String playerName) {
        boolean removed = whitelistData.whitelistedPlayers.remove(playerName);
        if (removed) {
            saveConfig();
        }
        return removed;
    }
    
    /**
     * 添加管理员
     */
    public static boolean addAdministrator(String playerName) {
        if (whitelistData.administrators.contains(playerName)) {
            return false; // 已存在
        }
        whitelistData.administrators.add(playerName);
        saveConfig();
        return true;
    }
    
    /**
     * 移除管理员
     */
    public static boolean removeAdministrator(String playerName) {
        boolean removed = whitelistData.administrators.remove(playerName);
        if (removed) {
            saveConfig();
        }
        return removed;
    }
    
    /**
     * 获取所有白名单玩家
     */
    public static List<String> getWhitelistedPlayers() {
        return new ArrayList<>(whitelistData.whitelistedPlayers);
    }
    
    /**
     * 获取所有管理员
     */
    public static List<String> getAdministrators() {
        return new ArrayList<>(whitelistData.administrators);
    }
    
    /**
     * 获取白名单玩家数量
     */
    public static int getWhitelistCount() {
        return whitelistData.whitelistedPlayers.size();
    }
    
    /**
     * 获取管理员数量
     */
    public static int getAdministratorCount() {
        return whitelistData.administrators.size();
    }
}
