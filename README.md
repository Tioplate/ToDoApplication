# ToDo Application

Vue 3 と Spring Boot で構築したフルスタックのタスク管理 Web アプリケーションです。
ユーザー認証、ToDo の CRUD、期限・優先度・完了状態の管理に加え、Docker Compose を使ってフロントエンド、バックエンド、MySQL、Redis をまとめて起動できる構成にしています。

## Features

- ユーザー登録 / ログイン / ログアウト
- JWT を利用した認証
- Redis を利用したログイン状態の管理
- ユーザー名・パスワードの更新
- ToDo の作成・編集・削除
- 期限の設定
- 優先度（高・中・低）の設定
- 完了 / 未完了の切り替え
- 作成日時・期限・優先度による並び替え

## Tech Stack

### Frontend
- Vue 3
- Vue Router
- Element Plus
- Vite

### Backend
- Java 21
- Spring Boot
- Spring Security
- JWT (JJWT)
- MyBatis

### Database / Infrastructure
- MySQL 8
- Redis 7
- Docker / Docker Compose
- Nginx

## Architecture

```text
Browser
   |
   v
Nginx
   |-- Vue 3 static files
   |
   `-- /api/*
         |
         v
    Spring Boot
      |      |
      v      v
    MySQL   Redis
```

Nginx が Vue の静的ファイルを配信し、`/api/` へのリクエストを Spring Boot にリバースプロキシします。バックエンドでは MySQL にユーザー・ToDo データを保存し、Redis を認証トークンの状態管理に利用しています。

## Implementation Highlights

- Spring Security と JWT を組み合わせた認証処理
- パスワードをハッシュ化してデータベースに保存
- Redis にログイン中のトークンを保持し、ログアウト時に無効化
- Controller / Service / Mapper に分けたバックエンド構成
- MyBatis XML Mapper を利用したデータアクセス
- Docker Compose による複数サービスの一括起動
- Nginx による SPA 配信と API リバースプロキシ
- 認証情報や JWT Secret を環境変数から注入

## Project Structure

```text
.
├── src/                       # Spring Boot backend
│   └── main/
│       ├── java/              # Controller / Service / Mapper / Security
│       └── resources/         # application.properties / MyBatis XML
├── frontend/                  # Vue 3 frontend
│   ├── src/
│   └── nginx.conf
├── createtable.sql            # MySQL initialization
├── Dockerfile                 # Backend image
├── docker-compose.yml
└── .env.example
```

## Run with Docker Compose

### 1. Prepare environment variables

macOS / Linux:

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

`.env` の `JWT_SECRET` は 32 文字以上のランダムな値に変更してください。必要に応じて MySQL のパスワードも変更してください。

### 2. Build and start

```bash
docker compose up -d --build
```

起動後、ブラウザから以下にアクセスできます。

```text
http://localhost
```

Docker Compose は以下のサービスを起動します。

```text
Frontend : Nginx + Vue
Backend  : Spring Boot
Database : MySQL
Cache    : Redis
```

### 3. Stop

```bash
docker compose down
```

データベースのボリュームも削除して初期化する場合：

```bash
docker compose down -v
```

## Security Notes

実際のデータベース認証情報や JWT Secret はリポジトリに含めず、環境変数から設定する構成にしています。`.env` は Git の追跡対象外です。

## Author

Yuren Chen
