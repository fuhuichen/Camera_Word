# 🚀 應用程式啟動指南

## ✅ 應用已成功啟動！

### 📍 訪問資訊

#### 登入頁面（主要入口）
```
http://localhost:8080/login
```

#### 控制台（登入後）
```
http://localhost:8080/dashboard
```

#### 帳號設定
```
http://localhost:8080/account/settings
```

#### 健康檢查
```
http://localhost:8080/actuator/health
```

## 🔑 登入資訊

### 預設帳號

**主要管理員**：
- 帳號：`admin`
- 密碼：`admin123`
- 權限：完整管理權限

**平台管理員**：
- 帳號：`platform_admin`
- 密碼：`platform123`
- 權限：受限管理權限

## 🎯 使用步驟

### 步驟 1: 訪問登入頁面
打開瀏覽器訪問：
```
http://localhost:8080/login
```

### 步驟 2: 輸入帳號密碼
```
帳號：admin
密碼：admin123
```

### 步驟 3: 點擊登入
- 點擊「登入系統」按鈕
- 按鈕會顯示「驗證中...」
- 驗證成功後自動跳轉到控制台

### 步驟 4: 開始使用
在控制台可以：
- 查看系統統計
- 管理平台
- 管理相機
- 批量匯入
- 修改帳號設定

## 🧪 測試錯誤訊息

### 測試 1: 錯誤密碼
```
1. 訪問 http://localhost:8080/login
2. 輸入帳號：admin
3. 輸入密碼：wrong_password
4. 點擊登入

結果：
✓ 顯示紅色錯誤訊息
✓ 訊息內容：「帳號或密碼錯誤，請檢查後重試」
✓ 提示：「請確認：帳號和密碼區分大小寫」
```

### 測試 2: 空白欄位
```
1. 不輸入任何內容
2. 直接點擊「登入系統」

結果：
✓ 立即顯示錯誤訊息（不發送請求）
✓ 訊息內容：「請填寫帳號和密碼」
✓ 輸入框邊框變紅色
```

### 測試 3: 正確登入
```
1. 輸入帳號：admin
2. 輸入密碼：admin123
3. 點擊登入

結果：
✓ 按鈕顯示「驗證中...」
✓ 自動跳轉到 /dashboard
✓ 顯示控制台頁面
```

### 測試 4: 顯示密碼功能
```
1. 在密碼框輸入：admin123
2. 點擊右側的 👁️ 圖示

結果：
✓ 密碼變為明文顯示
✓ 圖示變為 🙈
✓ 再次點擊恢復隱藏
```

## 🔄 重新啟動應用

如果需要重新啟動：

### 方法 1: 停止並重新啟動
```bash
# 在專案目錄執行
cd /Users/fuhuichen/Work/insight/camera_cloud

# 停止應用（如果正在運行）
pkill -f camera-cloud || true

# 重新編譯
./mvnw clean package -DskipTests

# 啟動應用
./mvnw spring-boot:run
```

### 方法 2: 使用 Makefile
```bash
# 停止現有應用
make clean

# 重新構建
make build

# 啟動應用
make run
```

### 方法 3: 使用 Docker
```bash
# 停止所有服務
docker compose down

# 重新構建並啟動
docker compose up -d --build

# 查看日誌
docker compose logs -f app
```

## 📊 應用狀態檢查

### 檢查應用是否運行
```bash
curl http://localhost:8080/actuator/health
```

預期輸出：
```json
{"status":"UP"}
```

### 檢查登入頁面
```bash
curl -I http://localhost:8080/login
```

預期輸出：
```
HTTP/1.1 200 OK
Content-Type: text/html;charset=UTF-8
```

### 查看應用日誌
```bash
# 如果使用 Maven 運行
tail -f logs/spring.log

# 如果使用 Docker
docker compose logs -f app
```

## 🐛 疑難排解

### 問題 1: 登入後沒有跳轉
**可能原因**:
- CSRF Token 問題
- Session 設定問題
- 瀏覽器 Cookie 被禁用

**解決方案**:
```bash
# 1. 清除瀏覽器 Cookie 和快取
# 2. 檢查瀏覽器控制台錯誤
# 3. 確認 CSRF 設定正確
# 4. 重新啟動應用
```

### 問題 2: 404 錯誤
**可能原因**: Thymeleaf 模板未正確載入

**解決方案**:
```bash
# 檢查模板檔案是否存在
ls -la src/main/resources/templates/

# 應該看到：
# - login.html
# - dashboard.html
# - account-settings.html

# 重新編譯
./mvnw clean package -DskipTests
```

### 問題 3: 錯誤訊息不顯示
**可能原因**: JavaScript 未執行

**解決方案**:
- 打開瀏覽器開發者工具（F12）
- 查看 Console 是否有 JavaScript 錯誤
- 確認瀏覽器允許 JavaScript 執行

### 問題 4: 無法連接資料庫
**解決方案**:
```bash
# 使用開發模式（H2 內存資料庫）
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 或確保 PostgreSQL 正在運行
docker compose up -d postgres
```

## 📞 即時支援

應用現在正在運行於：
- **埠號**: 8080
- **登入頁**: http://localhost:8080/login
- **健康檢查**: http://localhost:8080/actuator/health

## 🎯 下一步

1. ✅ **訪問登入頁面** - http://localhost:8080/login
2. ✅ **測試錯誤訊息** - 嘗試輸入錯誤密碼
3. ✅ **正確登入** - 使用 admin/admin123
4. ✅ **瀏覽控制台** - 查看系統統計
5. ✅ **修改密碼** - 訪問帳號設定頁面

## 💡 提示

- 登入頁面會在 **8秒後** 自動隱藏錯誤訊息
- 輸入框離開時會自動驗證（紅色=未填，綠色=已填）
- 可以使用 👁️ 圖示顯示/隱藏密碼
- 記住我功能可以保持登入狀態

---

© 2025 [兌心科技有限公司 Insight Software](https://www.insight-software.com/)



