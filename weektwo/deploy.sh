#!/bin/sh
set -eu

cd "$(dirname "$0")"

if ! command -v docker >/dev/null 2>&1; then
  echo "未找到 Docker，请先安装并启动 Docker Desktop。" >&2
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "Docker 守护进程未运行，请先启动 Docker Desktop。" >&2
  exit 1
fi

if [ ! -f .env ]; then
  if ! command -v openssl >/dev/null 2>&1; then
    echo "首次部署需要 openssl 来生成随机密钥。" >&2
    exit 1
  fi
  umask 077
  mysql_root_password="$(openssl rand -hex 24)"
  database_password="$(openssl rand -hex 24)"
  redis_password="$(openssl rand -hex 24)"
  admin_password="$(openssl rand -base64 24 | tr '/+' '_-' | tr -d '=\n')"
  preview_secret="$(openssl rand -base64 48 | tr '/+' '_-' | tr -d '=\n')"
  {
    echo "MYSQL_ROOT_PASSWORD=${mysql_root_password}"
    echo "DB_PASSWORD=${database_password}"
    echo "REDIS_PASSWORD=${redis_password}"
    echo "ADMIN_INITIAL_PASSWORD=${admin_password}"
    echo "PREVIEW_SIGNING_SECRET=${preview_secret}"
    echo "APP_PUBLIC_BASE_URL=http://localhost:8080"
    echo "HTTP_PORT=8080"
    echo "KKFILEVIEW_IMAGE=keking/kkfileview:4.1.0"
    echo "KKFILEVIEW_PLATFORM=linux/amd64"
  } > .env
  echo "已生成仅本机可读的 .env。首次管理员账号：admin"
  echo "首次管理员密码：${admin_password}"
  echo "请立即安全保存该密码。"
fi

docker compose up -d --build
docker compose ps
