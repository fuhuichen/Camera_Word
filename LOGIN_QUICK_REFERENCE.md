# 登入頁面快速參考卡

## 🚀 快速開始

```bash
# 啟動系統
docker-compose up -d

# 訪問登入頁
http://localhost:8080/login
```

## 🔑 預設帳號

| 角色 | 帳號 | 密碼 | 權限 |
|------|------|------|------|
| 主要管理員 | `admin` | `admin123` | 完整權限 |
| 平台管理員 | `platform_admin` | `platform123` | 受限權限 |

## ⚠️ 錯誤訊息速查表

| 場景 | 訊息 | 解決方法 |
|------|------|---------|
| 帳密錯誤 | ⚠️ 登入失敗 | 檢查帳號和密碼（區分大小寫） |
| 空白欄位 | ⚠️ 輸入錯誤 | 填寫所有必填欄位 |
| 會話過期 | ⚠️ 會話已過期 | 重新登入 |
| 權限不足 | ⚠️ 存取被拒絕 | 使用有權限的帳號 |
| 帳號鎖定 | 🔒 帳號已鎖定 | 聯絡系統管理員 |
| 成功登出 | ✓ 登出成功 | - |

## 🎯 頁面流程

```
登入頁面 (/login)
    ↓
輸入帳號密碼
    ↓
點擊「登入系統」
    ↓
驗證成功？
    ↓ Yes
控制台 (/dashboard)
    ↓ No
顯示錯誤訊息
```

## ✨ 特色功能

- [x] 👁️ 顯示/隱藏密碼
- [x] ✅ 即時輸入驗證
- [x] 📱 響應式設計
- [x] ⏰ 錯誤訊息自動消失
- [x] 🎨 美觀的動畫效果
- [x] 🔒 安全的驗證機制

## 🧪 測試腳本

```bash
# 測試 1: 正確登入
curl -X POST http://localhost:8080/login \
  -d "username=admin&password=admin123"

# 測試 2: 錯誤密碼
curl -X POST http://localhost:8080/login \
  -d "username=admin&password=wrong"

# 測試 3: 檢查健康狀態
curl http://localhost:8080/actuator/health
```

## 📞 疑難排解

### 無法登入
1. 檢查帳號密碼是否正確
2. 確認大小寫是否正確
3. 清除瀏覽器 Cookie
4. 檢查應用是否正常運行

### 頁面顯示異常
1. 使用現代瀏覽器（Chrome 90+）
2. 清除瀏覽器快取
3. 強制重新載入（Ctrl+Shift+R）

### 錯誤訊息不顯示
1. 檢查瀏覽器 JavaScript 是否啟用
2. 開啟開發者工具查看錯誤
3. 確認網路連線正常

## 📚 詳細文檔

- 📖 [完整錯誤訊息說明](LOGIN_ERROR_MESSAGES.md)
- 📖 [登入頁面詳細說明](LOGIN_PAGE.md)
- 📖 [帳號管理系統](ACCOUNT_MANAGEMENT.md)

## 🎨 視覺示意

```
┌─────────────────────────────────────┐
│  [錯誤訊息區域]                      │
│  ⚠️ 登入失敗                        │
│  帳號或密碼錯誤，請檢查後重試         │
├─────────────────────────────────────┤
│  使用者帳號: [__________]            │
│  密碼:       [__________] 👁️        │
│  ☐ 記住我     忘記密碼？             │
│  [     登入系統     ]               │
├─────────────────────────────────────┤
│  預設測試帳號                        │
│  👤 admin / admin123                │
│  👤 platform_admin / platform123    │
└─────────────────────────────────────┘
```

## 🔗 快速連結

- 🌐 [登入頁面](http://localhost:8080/login)
- 📊 [控制台](http://localhost:8080/dashboard)
- ⚙️ [帳號設定](http://localhost:8080/account/settings)
- 🏥 [健康檢查](http://localhost:8080/actuator/health)

---

© 2025 [兌心科技有限公司 Insight Software](https://www.insight-software.com/)



