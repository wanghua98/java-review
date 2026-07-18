# weektwo 一键部署

## 最快启动方式

启动 Docker Desktop 后执行：

```bash
./deploy.sh
```

脚本会在首次运行时生成随机数据库、Redis、管理员和预览签名密钥，然后构建并启动全部服务。首次管理员账号为 `admin`，随机密码只会在首次生成时输出一次。

## 手动配置方式

### 1. 准备配置

```bash
cp .env.example .env
```

编辑 `.env`，替换所有 `CHANGE_ME`。`PREVIEW_SIGNING_SECRET` 至少32个字符，可使用：

```bash
openssl rand -base64 48
```

### 2. 启动

```bash
docker compose up -d --build
```

首次启动会拉取带 LibreOffice 的 kkFileView 官方预构建镜像，下载量较大；后续会直接复用本地镜像。

默认入口为 <http://localhost:8080>。Nginx 在同一域名下提供：

- `/`：Vue前端
- `/api/`：附件服务
- `/preview/`：kkFileView

查看状态和日志：

```bash
docker compose ps
docker compose logs -f attachment-service kkfileview web
```

## 已有数据库升级

`docs/init.sql` 只会在全新 MySQL 数据卷初始化时运行。已有环境请先备份数据库，然后执行：

```bash
mysql -u root -p myapp < attachment-service/docs/migration-v2-file-share.sql
```

## 生产部署注意事项

- 将 `APP_PUBLIC_BASE_URL` 改为实际 HTTPS 域名。
- HTTPS 部署时将 `SA_TOKEN_COOKIE_SECURE` 设置为 `true`。
- 只暴露 `web` 服务；不要公开 MySQL、Redis、后端8001或kkFileView 8012端口。
- kkFileView 默认通过公开 HTTPS 域名 `https://a.zipq.qzz.io` 获取文件源（可用 `PREVIEW_SOURCE_BASE_URL` 覆盖），文件源 URL 使用两分钟 HMAC 票据；如更换域名，同步设置 `KK_TRUST_HOST`。
- kkFileView固定使用官方 `keking/kkfileview:4.1.0` 预构建镜像。官方当前仅提供 amd64，Apple Silicon 由 Docker Desktop 转译；升级前请完成预览格式与安全回归测试。
- 分享链接最长有效30天，撤销后立即失效；已签发的预览票据最多继续有效两分钟。
- 定期备份 `mysql-data` 与 `uploaded-files` 数据卷。
