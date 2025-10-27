# 🚀 Insight Software Portals - 啟動指南

## 快速啟動

### 方法 1: 使用啟動腳本（推薦）⭐

```bash
cd /Users/fuhuichen/Work/insight/camera_cloud
./start.sh
```

腳本會自動：
1. ✅ 檢查 Java 版本
2. ✅ 選擇運行模式
3. ✅ 啟動應用
4. ✅ 等待應用就緒
5. ✅ 顯示訪問 URL
6. ✅ 自動打開瀏覽器（macOS）

### 方法 2: 手動啟動（開發模式）

```bash
cd /Users/fuhuichen/Work/insight/camera_cloud

# 啟動應用（使用 H2 內存數據庫）
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 等待約 30 秒後訪問
open http://localhost:8080/login
```

### 方法 3: 手動啟動（生產模式）

```bash
# 1. 先啟動依賴服務
docker compose up -d postgres redis
# 或
docker-compose up -d postgres redis

# 2. 啟動應用
./mvnw spring-boot:run

# 3. 等待約 30 秒後訪問
open http://localhost:8080/login
```

### 方法 4: 使用 Docker（完整環境）

```bash
# 構建並啟動所有服務
docker compose up -d --build
# 或
docker-compose up -d --build

# 查看日誌
docker compose logs -f app
# 或
docker-compose logs -f app
```

## 🌐 訪問應用

### 主要頁面

| 頁面 | URL | 說明 |
|------|-----|------|
| 🔐 **登入頁面** | http://localhost:8080/login | 系統入口 |
| 📊 **控制台** | http://localhost:8080/dashboard | 登入後主頁 |
| ⚙️ **帳號設定** | http://localhost:8080/account/settings | 密碼管理 |
| 📹 **相機查看** | http://localhost:8080/view?camera_id=CAM_001 | 公開端點 |
| 🏥 **健康檢查** | http://localhost:8080/actuator/health | 系統狀態 |

### 管理 API

| API | URL | 說明 |
|-----|-----|------|
| 平台列表 | http://localhost:8080/api/v1/admin/platforms | 管理平台 |
| 相機列表 | http://localhost:8080/api/v1/admin/cameras | 管理相機 |

## 👤 預設帳號

### 主要管理員
```
帳號：admin
密碼：admin123
權限：MAIN_ADMIN（完整權限）
```

### 平台管理員
```
帳號：platform_admin
密碼：platform123
權限：PLATFORM_ADMIN（受限權限）
```

## 📋 啟動檢查清單

### 啟動前檢查
- [ ] Java 17 或更高版本已安裝
- [ ] Maven 已安裝（或使用 ./mvnw）
- [ ] 端口 8080 未被占用
- [ ] （生產模式）PostgreSQL 運行於 5432
- [ ] （生產模式）Redis 運行於 6379

### 驗證應用是否成功啟動

```bash
# 1. 檢查健康狀態
curl http://localhost:8080/actuator/health

# 應該返回：
# {"status":"UP"}

# 2. 檢查登入頁面
curl -I http://localhost:8080/login

# 應該返回：
# HTTP/1.1 200

# 3. 測試相機查看
curl http://localhost:8080/view?camera_id=CAM_001

# 應該返回 HTML 頁面
```

## 🎯 完整使用流程

### 1. 啟動應用
```bash
./start.sh
```

### 2. 訪問登入頁面
打開瀏覽器：
```
http://localhost:8080/login
```

### 3. 登入系統
- 輸入帳號：`admin`
- 輸入密碼：`admin123`
- 點擊「登入系統」

### 4. 查看控制台
登入成功後自動跳轉到：
```
http://localhost:8080/dashboard
```

您將看到：
- 📊 系統統計卡片
- 🚀 快速操作按鈕
- 👤 用戶信息
- 🔴 登出按鈕

### 5. 管理帳號
點擊導航列的「帳號設定」或訪問：
```
http://localhost:8080/account/settings
```

可以：
- 查看帳號信息
- 修改密碼
- 查看安全建議

### 6. 使用管理功能

#### 平台管理
```
http://localhost:8080/api/v1/admin/platforms
```
- 查看所有串流平台
- 新增平台（MAIN_ADMIN）
- 測試平台連通性

#### 相機管理
```
http://localhost:8080/api/v1/admin/cameras
```
- 查看所有相機
- 指派相機到平台
- 啟用/禁用相機
- 批量匯入相機

### 7. 測試相機查看
```
http://localhost:8080/view?camera_id=CAM_001
```
- 第一次訪問應該成功
- 60 秒內再次訪問會被限制（429）

## 🔧 開發模式 vs 生產模式

### 開發模式（dev）
- ✅ 無需外部數據庫
- ✅ 使用 H2 內存數據庫
- ✅ 使用內存速率限制器
- ✅ 快速啟動
- ❌ 數據不持久化
- ❌ 僅適合單機開發

### 生產模式（default）
- ✅ 數據持久化
- ✅ 支持多實例部署
- ✅ Redis 集中式速率限制
- ✅ PostgreSQL 關係數據庫
- ⚠️ 需要外部服務
- ⚠️ 需要配置環境變數

## 🐛 常見問題排查

### 問題 1: 端口被占用
**錯誤信息**：
```
Port 8080 is already in use
```

**解決方案**：
```bash
# 查找占用端口的進程
lsof -i :8080

# 終止該進程
kill -9 <PID>

# 或使用其他端口
./mvnw spring-boot:run -Dserver.port=8081
```

### 問題 2: 應用啟動失敗
**症狀**：應用無法啟動或立即退出

**解決方案**：
```bash
# 查看詳細日誌
./mvnw spring-boot:run

# 檢查 Java 版本
java -version

# 清理並重新構建
./mvnw clean package -DskipTests
```

### 問題 3: 無法連接數據庫
**錯誤信息**：
```
Connection refused: connect
```

**解決方案**：
```bash
# 開發模式：使用 H2 內存數據庫
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 生產模式：確保 PostgreSQL 運行
docker compose ps postgres
docker compose up -d postgres
```

### 問題 4: 頁面無法訪問
**症狀**：瀏覽器顯示無法連接

**檢查步驟**：
```bash
# 1. 確認應用已啟動
curl http://localhost:8080/actuator/health

# 2. 檢查防火牆設置

# 3. 嘗試使用 127.0.0.1 而非 localhost
open http://127.0.0.1:8080/login
```

### 問題 5: 登入失敗
**症狀**：密碼正確但無法登入

**解決方案**：
- 確認帳號正確（區分大小寫）
- 清除瀏覽器 Cookie
- 檢查瀏覽器 Console 錯誤
- 使用無痕模式嘗試

## 📊 查看日誌

### 應用日誌
```bash
# 直接運行時查看
./mvnw spring-boot:run

# Docker 運行時查看
docker compose logs -f app
docker-compose logs -f app
```

### 數據庫日誌
```bash
docker compose logs postgres
docker-compose logs postgres
```

### Redis 日誌
```bash
docker compose logs redis
docker-compose logs redis
```

## 🛑 停止應用

### 停止 Spring Boot 應用
```bash
# 按 Ctrl+C 停止前台進程

# 或查找並終止進程
ps aux | grep spring-boot
kill <PID>
```

### 停止 Docker 服務
```bash
docker compose down
# 或
docker-compose down

# 同時刪除數據卷
docker compose down -v
```

## 📦 重新構建

### 清理並重新構建
```bash
# 清理 Maven 構建
./mvnw clean

# 重新編譯
./mvnw clean package -DskipTests

# 重新構建 Docker 鏡像
docker compose build --no-cache
```

## 🎓 更多資源

- **帳號管理指南**: [ACCOUNT_MANAGEMENT.md](ACCOUNT_MANAGEMENT.md)
- **登入頁面說明**: [LOGIN_PAGE.md](LOGIN_PAGE.md)
- **快速開始**: [QUICKSTART_CN.md](QUICKSTART_CN.md)
- **完整文檔**: [README.md](README.md)
- **實施總結**: [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)

## 🔗 相關連結

- [兌心科技官網](https://www.insight-software.com/)
- [Spring Boot 文檔](https://spring.io/projects/spring-boot)
- [Thymeleaf 文檔](https://www.thymeleaf.org/)

## 💡 提示

1. **首次啟動建議使用開發模式**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. **生產環境務必修改預設密碼**
   ```bash
   export ADMIN_PASSWORD=your_secure_password
   ```

3. **啟動時間約需 20-30 秒**
   - 請耐心等待
   - 觀察日誌確認啟動成功

4. **使用 start.sh 腳本最方便**
   ```bash
   ./start.sh
   ```

---

© 2025 [兌心科技有限公司 Insight Software](https://www.insight-software.com/). All Rights Reserved.



