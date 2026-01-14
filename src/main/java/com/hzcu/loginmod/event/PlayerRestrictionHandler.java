package com.hzcu.loginmod.event;

import com.hzcu.loginmod.LoginMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

/**
 * 处理未登录玩家的行为限制
 */
public class PlayerRestrictionHandler {
    
    /**
     * 检查是否是假人玩家
     * 假人玩家用于自动化模组，应该被允许操作
     */
    private boolean isFakePlayer(ServerPlayer player) {
        return player instanceof FakePlayer;
    }

    /**
     * 限制玩家移动
     */
    @SubscribeEvent
    public void onPlayerMove(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 允许假人玩家
            if (isFakePlayer(player)) {
                return;
            }
            
            UUID playerUUID = player.getUUID();
            String playerName = player.getName().getString();
            
            // 如果玩家在白名单中，自动标记为已登录
            if (LoginMod.isPlayerWhitelisted(playerName) && !LoginMod.isPlayerLoggedIn(playerUUID)) {
                LoginMod.markPlayerLoggedIn(playerUUID);
                player.sendSystemMessage(Component.literal("§a您在白名单中，已自动登录！"));
                return;
            }
            
            // 如果玩家未登录，阻止移动
            if (!LoginMod.isPlayerLoggedIn(playerUUID)) {
                // 将玩家固定在当前位置
                player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
                
                // 每隔一段时间提醒玩家登录（避免刷屏）
                if (player.tickCount % 100 == 0) {
                    player.sendSystemMessage(Component.literal("§c请先使用 /login <用户名> <密码> 登录！"));
                }
            }
        }
    }

    /**
     * 限制玩家破坏方块
     */
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            // 允许假人玩家
            if (isFakePlayer(player)) {
                return;
            }
            
            UUID playerUUID = player.getUUID();
            
            // 如果玩家未登录，取消破坏方块
            if (!LoginMod.isPlayerLoggedIn(playerUUID)) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§c请先登录才能破坏方块！使用 /login <用户名> <密码>"));
            }
        }
    }

    /**
     * 限制玩家放置方块
     */
    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 允许假人玩家
            if (isFakePlayer(player)) {
                return;
            }
            
            UUID playerUUID = player.getUUID();
            
            // 如果玩家未登录，取消放置方块
            if (!LoginMod.isPlayerLoggedIn(playerUUID)) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§c请先登录才能放置方块！使用 /login <用户名> <密码>"));
            }
        }
    }

    /**
     * 限制玩家与方块交互（如开箱子、使用门等）
     */
    @SubscribeEvent
    public void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 允许假人玩家
            if (isFakePlayer(player)) {
                return;
            }
            
            UUID playerUUID = player.getUUID();
            
            // 如果玩家未登录，取消交互
            if (!LoginMod.isPlayerLoggedIn(playerUUID)) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§c请先登录才能与方块交互！使用 /login <用户名> <密码>"));
            }
        }
    }

    /**
     * 限制玩家使用物品
     */
    @SubscribeEvent
    public void onPlayerInteractItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 允许假人玩家
            if (isFakePlayer(player)) {
                return;
            }
            
            UUID playerUUID = player.getUUID();
            
            // 如果玩家未登录，取消使用物品
            if (!LoginMod.isPlayerLoggedIn(playerUUID)) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§c请先登录才能使用物品！使用 /login <用户名> <密码>"));
            }
        }
    }

    /**
     * 限制玩家攻击实体
     */
    @SubscribeEvent
    public void onPlayerAttack(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 允许假人玩家
            if (isFakePlayer(player)) {
                return;
            }
            
            UUID playerUUID = player.getUUID();
            
            // 如果玩家未登录，提醒玩家
            if (!LoginMod.isPlayerLoggedIn(playerUUID)) {
                player.sendSystemMessage(Component.literal("§c请先登录才能进行操作！使用 /login <用户名> <密码>"));
            }
        }
    }

    /**
     * 玩家离开服务器时清除登录状态
     */
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UUID playerUUID = player.getUUID();
            LoginMod.removePlayerLoginStatus(playerUUID);
        }
    }

    /**
     * 玩家加入服务器时提醒登录
     */
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            String playerName = player.getName().getString();
            
            // 检查是否在白名单中
            if (LoginMod.isPlayerWhitelisted(playerName)) {
                LoginMod.markPlayerLoggedIn(player.getUUID());
                player.sendSystemMessage(Component.literal("§e============HZCU-CA—Minecraft==========="));
                player.sendSystemMessage(Component.literal("§a欢迎来到服务器！"));
                player.sendSystemMessage(Component.literal("§a您在白名单中，已自动登录！"));
                player.sendSystemMessage(Component.literal("§e========================================"));
                LoginMod.LOGGER.info("Player {} (whitelisted) auto-logged in", playerName);
            } else {
                player.sendSystemMessage(Component.literal("§e============HZCU-CA—Minecraft==========="));
                player.sendSystemMessage(Component.literal("§e欢迎来到服务器！"));
                player.sendSystemMessage(Component.literal("§e请使用 §6/login <用户名> <密码> §e进行登录"));
                player.sendSystemMessage(Component.literal("§e用户名为学号或工号，密码为证件号码的后六位（X大写）"));
                player.sendSystemMessage(Component.literal("§e若有疑问或新增非校内用户，请联系管理员"));
                player.sendSystemMessage(Component.literal("§e========================================"));
                LoginMod.LOGGER.info("Player {} login the server", playerName);
            }
        }
    }
}
