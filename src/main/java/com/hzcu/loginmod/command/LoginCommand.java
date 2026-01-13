package com.hzcu.loginmod.command;

import com.hzcu.loginmod.LoginMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginCommand {
    // 用户名密码存储
    private static final Map<String, String> userCredentials = new HashMap<>();
    private static final String CSV_FILE_PATH = "userdata.csv";
    
    static {
        // 从 CSV 文件加载用户数据
        loadUsersFromCSV();
    }
    
    /**
     * 从 CSV 文件加载用户数据
     */
    private static void loadUsersFromCSV() {
        Path csvPath = Paths.get(CSV_FILE_PATH);
        
        if (!Files.exists(csvPath)) {
            LoginMod.LOGGER.error("用户数据文件 {} 不存在！", CSV_FILE_PATH);
            LoginMod.LOGGER.warn("使用默认测试账号");
            // 如果文件不存在，使用默认测试账号
            userCredentials.put("admin", "admin123");
            userCredentials.put("player1", "password1");
            userCredentials.put("player2", "password2");
            return;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line;
            boolean isFirstLine = true;
            int lineCount = 0;
            
            while ((line = reader.readLine()) != null) {
                // 跳过标题行
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // 解析 CSV 行
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    
                    if (!username.isEmpty() && !password.isEmpty()) {
                        userCredentials.put(username, password);
                        lineCount++;
                    }
                }
            }
            
            LoginMod.LOGGER.info("成功从 {} 加载了 {} 个用户账号", CSV_FILE_PATH, lineCount);
            
        } catch (IOException e) {
            LoginMod.LOGGER.error("读取用户数据文件 {} 时出错: {}", CSV_FILE_PATH, e.getMessage());
            LoginMod.LOGGER.warn("使用默认测试账号");
            // 如果读取失败，使用默认测试账号
            userCredentials.put("admin", "admin123");
            userCredentials.put("player1", "password1");
            userCredentials.put("player2", "password2");
        }
    }
    
    /**
     * 重新加载用户数据（用于动态更新用户列表）
     */
    public static void reloadUsers() {
        userCredentials.clear();
        loadUsersFromCSV();
        LoginMod.LOGGER.info("用户数据已重新加载");
    }
    
    /**
     * 获取当前加载的用户数量
     */
    public static int getUserCount() {
        return userCredentials.size();
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 登录命令
        dispatcher.register(Commands.literal("login")
                .then(Commands.argument("username", StringArgumentType.word())
                        .then(Commands.argument("password", StringArgumentType.word())
                                .executes(LoginCommand::executeLogin))));
        
        // 添加用户命令（管理员专用）
        dispatcher.register(Commands.literal("adduser")
                .requires(source -> source.hasPermission(2)) // 需要管理员权限
                .then(Commands.argument("username", StringArgumentType.word())
                        .then(Commands.argument("password", StringArgumentType.greedyString())
                                .executes(LoginCommand::executeAddUser))));
    }

    private static int executeLogin(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        // 确保是玩家执行的命令
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("此命令只能由玩家执行！"));
            return 0;
        }

        String username = StringArgumentType.getString(context, "username");
        String password = StringArgumentType.getString(context, "password");
        
        // 检查玩家是否已经登录
        UUID playerUUID = player.getUUID();
        if (LoginMod.isPlayerLoggedIn(playerUUID)) {
            player.sendSystemMessage(Component.literal("§e您已经登录过了！"));
            return 1;
        }

        // 验证用户名和密码后六位
        if (userCredentials.containsKey(username)) {
            String fullPassword = userCredentials.get(username);
            
            // 获取完整密码的后六位
            String lastSixDigits = fullPassword.length() >= 6 
                ? fullPassword.substring(fullPassword.length() - 6) 
                : fullPassword;
            
            // 比较用户输入的密码和完整密码的后六位
            if (lastSixDigits.equals(password)) {
                // 登录成功
                LoginMod.markPlayerLoggedIn(playerUUID);
                player.sendSystemMessage(Component.literal("§a登录成功！欢迎回来，" + username + "！"));
                player.sendSystemMessage(Component.literal("§a您现在可以自由移动和破坏方块了。"));
                LoginMod.LOGGER.info("Player {} ({}) logged in successfully", player.getName().getString(), username);
                return 1;
            } else {
                // 密码错误
                player.sendSystemMessage(Component.literal("§c密码错误！请重试。"));
                LoginMod.LOGGER.warn("Player {} failed login attempt for username {}", player.getName().getString(), username);
                return 0;
            }
        } else {
            // 用户名不存在
            player.sendSystemMessage(Component.literal("§c用户名不存在！请检查您的用户名。"));
            LoginMod.LOGGER.warn("Player {} attempted login with non-existent username {}", player.getName().getString(), username);
            return 0;
        }
    }
    
    /**
     * 执行添加用户命令
     */
    private static int executeAddUser(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        String newUsername = StringArgumentType.getString(context, "username");
        String newPassword = StringArgumentType.getString(context, "password");
        
        // 检查用户名是否已存在
        if (userCredentials.containsKey(newUsername)) {
            source.sendFailure(Component.literal("§c用户名 " + newUsername + " 已存在！"));
            return 0;
        }
        
        // 验证用户名和密码格式
        if (newUsername.isEmpty() || newPassword.isEmpty()) {
            source.sendFailure(Component.literal("§c用户名和密码不能为空！"));
            return 0;
        }
        
        if (newUsername.contains(",") || newPassword.contains(",")) {
            source.sendFailure(Component.literal("§c用户名和密码不能包含逗号！"));
            return 0;
        }
        
        // 添加到内存
        userCredentials.put(newUsername, newPassword);
        
        // 写入 CSV 文件
        if (saveUserToCSV(newUsername, newPassword)) {
            source.sendSuccess(() -> Component.literal("§a成功添加用户: " + newUsername), true);
            LoginMod.LOGGER.info("Admin {} added new user: {}", 
                source.getEntity() != null ? source.getEntity().getName().getString() : "Console", 
                newUsername);
            return 1;
        } else {
            source.sendFailure(Component.literal("§c添加用户到文件失败！请检查服务器日志。"));
            // 如果写入文件失败，从内存中移除
            userCredentials.remove(newUsername);
            return 0;
        }
    }
    
    /**
     * 将新用户保存到 CSV 文件
     */
    private static boolean saveUserToCSV(String username, String password) {
        Path csvPath = Paths.get(CSV_FILE_PATH);
        
        try {
            // 如果文件不存在，创建并写入标题行
            if (!Files.exists(csvPath)) {
                try (BufferedWriter writer = Files.newBufferedWriter(csvPath)) {
                    writer.write("username,password");
                    writer.newLine();
                }
            }
            
            // 追加新用户
            try (BufferedWriter writer = Files.newBufferedWriter(csvPath, 
                    StandardOpenOption.APPEND)) {
                writer.write(username + "," + password);
                writer.newLine();
            }
            
            LoginMod.LOGGER.info("Successfully saved new user {} to CSV file", username);
            return true;
            
        } catch (IOException e) {
            LoginMod.LOGGER.error("Failed to save user {} to CSV file: {}", username, e.getMessage());
            return false;
        }
    }
    
    /**
     * 添加新用户（可用于注册功能）
     */
    public static void addUser(String username, String password) {
        userCredentials.put(username, password);
        LoginMod.LOGGER.info("Added new user: {}", username);
    }
}
