package com.hzcu.loginmod;

import com.hzcu.loginmod.command.LoginCommand;
import com.hzcu.loginmod.command.WhitelistCommand;
import com.hzcu.loginmod.config.WhitelistConfig;
import com.hzcu.loginmod.event.PlayerRestrictionHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod(LoginMod.MODID)
public class LoginMod {
    public static final String MODID = "loginmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(LoginMod.class);
    
    // 存储已登录玩家的UUID
    private static final Set<UUID> loggedInPlayers = new HashSet<>();

    public LoginMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        
        // 初始化白名单配置
        WhitelistConfig.init();
        
        // 注册事件处理器
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new PlayerRestrictionHandler());
        
        LOGGER.info("LoginMod initialized");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("LoginMod common setup");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LoginCommand.register(event.getDispatcher());
        WhitelistCommand.register(event.getDispatcher());
    }
    
    /**
     * 标记玩家已登录
     */
    public static void markPlayerLoggedIn(UUID playerUUID) {
        loggedInPlayers.add(playerUUID);
        LOGGER.info("Player {} logged in", playerUUID);
    }
    
    /**
     * 检查玩家是否已登录
     */
    public static boolean isPlayerLoggedIn(UUID playerUUID) {
        return loggedInPlayers.contains(playerUUID);
    }
    
    /**
     * 移除玩家登录状态（玩家离开服务器时调用）
     */
    public static void removePlayerLoginStatus(UUID playerUUID) {
        loggedInPlayers.remove(playerUUID);
        LOGGER.info("Player {} login status removed", playerUUID);
    }
    
    /**
     * 检查玩家是否在白名单中（不需要登录）
     */
    public static boolean isPlayerWhitelisted(String playerName) {
        return WhitelistConfig.isWhitelisted(playerName);
    }
}
