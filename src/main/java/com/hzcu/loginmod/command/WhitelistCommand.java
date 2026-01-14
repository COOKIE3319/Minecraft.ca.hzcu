package com.hzcu.loginmod.command;

import com.hzcu.loginmod.config.WhitelistConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * 白名单管理命令
 * 允许管理员添加/移除不需要登录的用户
 */
public class WhitelistCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("loginwhitelist")
            .requires(source -> {
                // 只有管理员才能使用此命令
                if (source.getEntity() instanceof ServerPlayer player) {
                    return WhitelistConfig.isAdministrator(player.getName().getString());
                }
                return source.hasPermission(2); // 服务器控制台也可以使用
            })
            .then(Commands.literal("add")
                .then(Commands.argument("player", StringArgumentType.string())
                    .executes(WhitelistCommand::addPlayer)))
            .then(Commands.literal("remove")
                .then(Commands.argument("player", StringArgumentType.string())
                    .executes(WhitelistCommand::removePlayer)))
            .then(Commands.literal("list")
                .executes(WhitelistCommand::listPlayers))
            .then(Commands.literal("reload")
                .executes(WhitelistCommand::reloadConfig))
            .executes(WhitelistCommand::showHelp));
    }
    
    /**
     * 添加玩家到白名单
     */
    private static int addPlayer(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "player");
        
        if (WhitelistConfig.addToWhitelist(playerName)) {
            context.getSource().sendSuccess(
                () -> Component.literal("§a成功将玩家 §e" + playerName + " §a添加到白名单！"),
                true
            );
            return 1;
        } else {
            context.getSource().sendFailure(
                Component.literal("§c玩家 §e" + playerName + " §c已在白名单中！")
            );
            return 0;
        }
    }
    
    /**
     * 从白名单移除玩家
     */
    private static int removePlayer(CommandContext<CommandSourceStack> context) {
        String playerName = StringArgumentType.getString(context, "player");
        
        if (WhitelistConfig.removeFromWhitelist(playerName)) {
            context.getSource().sendSuccess(
                () -> Component.literal("§a成功将玩家 §e" + playerName + " §a从白名单移除！"),
                true
            );
            return 1;
        } else {
            context.getSource().sendFailure(
                Component.literal("§c玩家 §e" + playerName + " §c不在白名单中！")
            );
            return 0;
        }
    }
    
    /**
     * 列出所有白名单玩家
     */
    private static int listPlayers(CommandContext<CommandSourceStack> context) {
        List<String> whitelistedPlayers = WhitelistConfig.getWhitelistedPlayers();
        
        context.getSource().sendSuccess(
            () -> Component.literal("§e========== 白名单玩家列表 =========="),
            false
        );
        
        if (whitelistedPlayers.isEmpty()) {
            context.getSource().sendSuccess(
                () -> Component.literal("§7当前白名单为空"),
                false
            );
        } else {
            context.getSource().sendSuccess(
                () -> Component.literal("§a共有 §e" + whitelistedPlayers.size() + " §a个玩家在白名单中："),
                false
            );
            for (String player : whitelistedPlayers) {
                context.getSource().sendSuccess(
                    () -> Component.literal("  §7- §f" + player),
                    false
                );
            }
        }
        
        context.getSource().sendSuccess(
            () -> Component.literal("§e===================================="),
            false
        );
        
        return 1;
    }
    
    /**
     * 重新加载配置
     */
    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        WhitelistConfig.reload();
        context.getSource().sendSuccess(
            () -> Component.literal("§a白名单配置已重新加载！"),
            true
        );
        return 1;
    }
    
    /**
     * 显示帮助信息
     */
    private static int showHelp(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(
            () -> Component.literal("§e========== 白名单管理命令帮助 =========="),
            false
        );
        context.getSource().sendSuccess(
            () -> Component.literal("§6/loginwhitelist add <玩家名> §7- 添加玩家到白名单"),
            false
        );
        context.getSource().sendSuccess(
            () -> Component.literal("§6/loginwhitelist remove <玩家名> §7- 从白名单移除玩家"),
            false
        );
        context.getSource().sendSuccess(
            () -> Component.literal("§6/loginwhitelist list §7- 查看所有白名单玩家"),
            false
        );
        context.getSource().sendSuccess(
            () -> Component.literal("§6/loginwhitelist reload §7- 重新加载配置文件"),
            false
        );
        context.getSource().sendSuccess(
            () -> Component.literal("§e========================================"),
            false
        );
        return 1;
    }
}
