# Login Mod for Minecraft 1.20.1 Use CA.HZCU

一个调用HZCU统一身份认证登录的，用于 Minecraft 1.20.1 Forge 的登录模组。

## 功能

- 使用 `/login <用户名> <密码>` 登录
- 管理员使用 `/adduser <用户名> <密码>` 添加新用户

## 用户数据配置

模组会从根目录的 `userdata.csv` 文件中读取用户账号信息。

```csv
username,password
```

若 `userdata.csv` 不存在则会使用默认测试账号

- 用户名: `admin`, 密码: `admin123`
- 用户名: `player1`, 密码: `password1`
- 用户名: `player2`, 密码: `password2`

## 构建项目

```bash
./gradlew build
```
