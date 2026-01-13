# Login Mod for Minecraft 1.20.1

一个用于 Minecraft 1.20.1 Forge 的登录模组。

## 功能

- 玩家加入服务器后，需要使用 `/login <用户名> <密码>` 命令登录
- 管理员可以使用 `/adduser <用户名> <密码>` 命令添加新用户
- 玩家离开服务器后，登录状态会被清除

## 用户数据配置

模组会从项目根目录的 `userdata.csv` 文件中读取用户账号信息。CSV 文件格式如下：

```csv
username,password
```

**配置提示：**
1. 第一行必须是标题行：`username,password`
2. 每行包含一个用户名和密码，用逗号分隔
3. 如果 `userdata.csv` 文件不存在或读取失败，模组会使用默认测试账号

## 预设测试账号

如果 `userdata.csv` 文件不存在，模组会使用以下默认测试账号：

- 用户名: `admin`, 密码: `admin123`
- 用户名: `player1`, 密码: `password1`
- 用户名: `player2`, 密码: `password2`

## 构建项目

```bash
./gradlew build
```

构建完成后，生成的 JAR 文件位于 `build/libs/` 目录。

## 安装

1. 确保你已安装 Minecraft 1.20.1 和 Forge 47.2.0 或更高版本
2. 将生成的 JAR 文件放入 `.minecraft/mods` 文件夹
3. 在 Minecraft 运行目录下创建或放置 `userdata.csv` 文件（包含用户账号数据）
4. 启动游戏

## 使用方法

### 普通玩家登录

1. 准备 `userdata.csv` 文件，确保格式正确
2. 进入服务器
3. 在聊天栏输入: `/login <用户名> <密码>`

### 管理员添加用户

服务器管理员可以使用以下命令添加新用户：

```
/adduser <用户名> <完整密码>
```

## 开发环境设置

1. 克隆项目
2. 运行 `./gradlew build` 来下载依赖
3. 使用 IntelliJ IDEA 或 Eclipse 导入项目

## 注意事项

- **重要**: 当前版本使用的是简单的内存存储和明文密码，仅供学习和测试使用
- 在生产环境中，应该使用数据库存储用户信息，并对密码进行加密（如 BCrypt）
- 建议添加注册功能和密码重置功能
- 可以考虑添加配置文件来管理用户账号

## 许可证

MIT License
