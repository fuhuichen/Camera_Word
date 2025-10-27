# 快速開始指南

## 前提條件

- Java 17 或更高版本
- Maven 3.9+
- Docker 和 Docker Compose (推薦)

## 方式 1: 使用 Docker Compose (推薦)

### 1. 啟動所有服務
```bash
docker-compose up -d
```

這將啟動：
- PostgreSQL 16 數據庫 (端口 5432)
- Redis 7 (端口 6379)
- Camera Cloud 應用 (端口 8080)

### 2. 等待服務啟動
```bash
# 查看日誌
docker-compose logs -f app

# 等待應用啟動完成（看到 "Started CameraCloudApplication" 消息）
```

### 3. 訪問登入頁面 🎨

打開瀏覽器訪問：
```
http://localhost:8080/login
```

您將看到一個美觀的登入頁面，設計參考 [兌心科技 Insight Software](https://www.insight-software.com/)

**預設登入帳號**：
- 帳號：`admin`
- 密碼：`admin123`

登入後會自動跳轉到平台管理頁面。

### 4. 測試相機查看端點

```bash
# 第一次請求 - 應該成功 (200 OK)
curl "http://localhost:8080/view?camera_id=CAM_001"

# 在 60 秒內的第二次請求 - 應該被限制 (429 Too Many Requests)
curl "http://localhost:8080/view?camera_id=CAM_001"

# 等待 60 秒後再次請求 - 應該成功
sleep 60
curl "http://localhost:8080/view?camera_id=CAM_001"
```

### 5. 測試管理 API

```bash
# 使用網頁登入後訪問（推薦）
# 或使用 HTTP Basic Auth：

# 列出所有平台
curl -u admin:admin123 "http://localhost:8080/api/v1/admin/platforms" | jq .

# 列出所有相機
curl -u admin:admin123 "http://localhost:8080/api/v1/admin/cameras" | jq .

# 測試平台連通性
curl -X POST -u admin:admin123 "http://localhost:8080/api/v1/admin/platforms/youtube/test" | jq .
```

### 6. 查看健康狀態
```bash
curl "http://localhost:8080/actuator/health" | jq .
```

### 7. 登出系統
```bash
# 在瀏覽器中訪問：
http://localhost:8080/logout

# 或使用 curl：
curl -X POST "http://localhost:8080/logout" \
  -H "Cookie: JSESSIONID=your_session_id"
```

### 8. 停止服務
```bash
docker-compose down
```

## 登入頁面功能

### 視覺特色
- ✨ 現代漸層紫藍色背景
- 🎨 雙欄式佈局（桌面版）
- 📱 響應式設計（行動友善）
- 🔒 安全的表單驗證
- ⚡ 流暢的動畫效果

### 功能特色
- ✓ 記住我功能
- ✓ 忘記密碼連結
- ✓ 錯誤訊息自動隱藏
- ✓ 登入載入動畫
- ✓ 雙重認證支援（HTTP Basic + Form Login）

## 方式 2: 本地開發模式

### 1. 啟動依賴服務
```bash
# 只啟動 PostgreSQL 和 Redis
docker-compose up -d postgres redis
```

### 2. 運行應用
```bash
# 使用 Maven 運行
./mvnw spring-boot:run

# 或使用 Makefile
make run
```

### 3. 訪問登入頁面（同上）
```
http://localhost:8080/login
```

## 方式 3: 開發模式 (H2 內存數據庫)

### 1. 運行應用
```bash
# 使用 dev profile（使用 H2 和內存速率限制器）
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 或使用 Makefile
make run-dev
```

### 2. 訪問登入頁面（同上）

**注意**: 開發模式使用 H2 內存數據庫和內存速率限制器，僅適用於單實例開發環境。

## 詳細的登入流程

```
1. 打開瀏覽器
   ↓
2. 訪問 http://localhost:8080/login
   ↓
3. 輸入帳號：admin
   ↓
4. 輸入密碼：admin123
   ↓
5. （可選）勾選「記住我」
   ↓
6. 點擊「登入系統」
   ↓
7. 系統驗證帳號密碼
   ↓
8. 登入成功，自動跳轉到平台管理頁面
```

## 完整功能測試腳本

運行自動化測試：
```bash
./test_all.sh
```

這會測試：
- ✓ 健康檢查端點
- ✓ 相機查看功能
- ✓ 速率限制機制
- ✓ 無效 camera_id 驗證
- ✓ 管理 API 功能
- ✓ 相機重定向切換
- ✓ Prometheus 指標

## 常見問題

### 1. 無法訪問登入頁面
```bash
# 檢查應用是否運行
curl http://localhost:8080/actuator/health

# 查看應用日誌
docker-compose logs -f app
```

### 2. 登入後立即登出
- 檢查瀏覽器 Cookie 設定
- 確保允許第三方 Cookie
- 清除瀏覽器快取

### 3. 密碼正確但無法登入
- 檢查帳號是否正確（區分大小寫）
- 查看應用日誌確認錯誤訊息
- 嘗試使用 HTTP Basic Auth

### 4. 登入頁面樣式跑版
- 確保使用現代瀏覽器（Chrome 90+, Firefox 88+, Safari 14+）
- 清除瀏覽器快取
- 檢查 CSS 是否正確載入

## 更多資訊

- **登入頁面詳細說明**: [LOGIN_PAGE.md](LOGIN_PAGE.md)
- **完整文檔**: [README.md](README.md)
- **實施總結**: [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)
- **規格文檔**: [camera_view_page_spring_boot_spec_for_cursor.md](camera_view_page_spring_boot_spec_for_cursor.md)

## 相關連結

- [兌心科技官網](https://www.insight-software.com/)
- [專案 GitHub](https://github.com/your-repo)

---

© 2025 [兌心科技有限公司 Insight Software](https://www.insight-software.com/). All Rights Reserved.



