# 測試登入跳轉功能

## 🔧 問題修復

已修復登入無法跳轉的問題：
- ✅ 添加 Thymeleaf action: `th:action="@{/login}"`
- ✅ 添加 CSRF Token: `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />`
- ✅ 配置正確的成功跳轉路徑: `/dashboard`

## 🚀 啟動應用

### 方法 1: 使用 Docker Compose
```bash
# 停止現有服務
docker compose down

# 重新構建並啟動
docker compose up -d --build

# 等待服務啟動（約10-15秒）
sleep 15

# 檢查服務狀態
docker compose ps
docker compose logs app
```

### 方法 2: 本地運行
```bash
# 啟動依賴服務
docker compose up -d postgres redis

# 等待數據庫就緒
sleep 10

# 運行應用
./mvnw spring-boot:run
```

## ✅ 測試登入跳轉

### 測試步驟

#### 1. 打開登入頁面
```
http://localhost:8080/login
```

#### 2. 測試正確登入（應該跳轉）
**輸入**：
- 帳號：`admin`
- 密碼：`admin123`

**預期結果**：
- ✅ 點擊「登入系統」後
- ✅ 按鈕文字變為「驗證中...」
- ✅ 自動跳轉到控制台頁面：`http://localhost:8080/dashboard`
- ✅ 顯示歡迎訊息和統計資料

#### 3. 測試錯誤登入（應該顯示錯誤）
**輸入**：
- 帳號：`admin`
- 密碼：`wrongpassword`

**預期結果**：
- ✅ 停留在登入頁面：`http://localhost:8080/login?error=true`
- ✅ 顯示紅色錯誤訊息：「⚠️ 登入失敗」
- ✅ 錯誤訊息8秒後自動消失

#### 4. 測試空白欄位（客戶端驗證）
**輸入**：
- 不輸入任何內容
- 直接點擊「登入系統」

**預期結果**：
- ✅ 不發送請求到伺服器
- ✅ 立即顯示錯誤：「請填寫帳號和密碼」
- ✅ 輸入框邊框變紅色

#### 5. 測試登出功能
**步驟**：
- 在控制台頁面點擊右上角「登出」按鈕

**預期結果**：
- ✅ 自動跳轉到登入頁面：`http://localhost:8080/login?logout=true`
- ✅ 顯示綠色成功訊息：「✓ 登出成功」
- ✅ 訊息8秒後自動消失

## 🐛 疑難排解

### 問題 1: 登入後沒有跳轉

**可能原因**：
- CSRF Token 缺失
- 表單 action 不正確
- Spring Security 配置問題

**解決方案**：
```bash
# 1. 檢查應用日誌
./mvnw spring-boot:run

# 或使用 Docker
docker compose logs app

# 2. 檢查登入頁面的 HTML 源碼
# 應該包含：
# <form th:action="@{/login}" method="post">
# <input type="hidden" name="_csrf" value="..."/>

# 3. 清除瀏覽器快取
Ctrl+Shift+Delete（Chrome）
```

### 問題 2: 顯示 403 Forbidden

**原因**: CSRF Token 驗證失敗

**解決方案**：
```bash
# 確認 login.html 包含 CSRF token：
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />

# 重新構建應用
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

### 問題 3: 跳轉到錯誤的頁面

**原因**: defaultSuccessUrl 配置不正確

**解決方案**：
檢查 `SecurityConfig.java`:
```java
.formLogin(form -> form
    .loginPage("/login")
    .loginProcessingUrl("/login")
    .defaultSuccessUrl("/dashboard", true)  // 確認這行
    .failureUrl("/login?error=true")
    .permitAll()
)
```

## 🧪 驗證清單

運行以下檢查確保一切正常：

- [ ] 應用成功啟動（無錯誤日誌）
- [ ] 訪問 `/login` 可以看到登入頁面
- [ ] 頁面 HTML 源碼包含 CSRF token
- [ ] 輸入正確帳密可以登入
- [ ] 登入後跳轉到 `/dashboard`
- [ ] 控制台顯示統計資料
- [ ] 輸入錯誤帳密顯示錯誤訊息
- [ ] 空白提交顯示客戶端驗證訊息
- [ ] 點擊登出跳轉回 `/login?logout=true`
- [ ] 顯示登出成功訊息

## 📝 測試報告範本

```
測試日期: 2025-10-22
測試人員: [您的名字]
應用版本: 1.0.0-SNAPSHOT

測試結果：
✅ 登入頁面顯示正常
✅ 正確帳密可以登入
✅ 登入後跳轉到控制台
✅ 錯誤帳密顯示錯誤訊息
✅ 空白提交顯示驗證訊息
✅ 登出功能正常
✅ 錯誤訊息自動消失

備註：
- 所有功能運作正常
- 使用者體驗良好
- 錯誤訊息清楚明確
```

## 🎯 核心修復內容

### 修復前
```html
<!-- 錯誤：缺少 Thymeleaf 和 CSRF -->
<form action="/login" method="post">
    <input type="text" name="username" />
    <input type="password" name="password" />
    <button type="submit">登入</button>
</form>
```

### 修復後
```html
<!-- 正確：使用 Thymeleaf 和 CSRF -->
<form th:action="@{/login}" method="post">
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
    <input type="text" name="username" />
    <input type="password" name="password" />
    <button type="submit">登入</button>
</form>
```

## 📊 登入流程圖

```
開始
  ↓
訪問 /login
  ↓
輸入帳號密碼
  ↓
點擊登入按鈕
  ↓
客戶端驗證 ─→ 失敗 ─→ 顯示錯誤（不發送請求）
  ↓ 通過
發送 POST /login（包含 CSRF token）
  ↓
Spring Security 驗證
  ↓
驗證成功？
  ├─ Yes ─→ 跳轉到 /dashboard ─→ 顯示控制台
  └─ No  ─→ 跳轉到 /login?error=true ─→ 顯示錯誤訊息
```

## 🔍 檢查應用狀態

```bash
# 檢查應用是否運行
curl http://localhost:8080/actuator/health

# 應該返回：
# {"status":"UP"}

# 檢查登入頁面
curl -I http://localhost:8080/login

# 應該返回：
# HTTP/1.1 200 OK

# 檢查控制台（未登入應重定向）
curl -I http://localhost:8080/dashboard

# 應該返回：
# HTTP/1.1 302 Found
# Location: http://localhost:8080/login
```

## 📚 相關文檔

- [登入錯誤訊息說明](LOGIN_ERROR_MESSAGES.md)
- [登入頁面完整說明](LOGIN_PAGE.md)
- [快速參考卡](LOGIN_QUICK_REFERENCE.md)
- [帳號管理系統](ACCOUNT_MANAGEMENT.md)

---

## ✨ 修復摘要

**問題**: 登入沒有跳轉

**原因**: 
1. 表單使用靜態 `action="/login"` 而非 Thymeleaf `th:action`
2. 缺少 CSRF Token

**修復**:
1. ✅ 改用 `th:action="@{/login}"`
2. ✅ 添加 CSRF hidden input
3. ✅ 確認 SecurityConfig 設定正確

**結果**: 
- ✅ 登入成功後正確跳轉到 `/dashboard`
- ✅ 登入失敗顯示錯誤訊息
- ✅ 所有功能正常運作

---

© 2025 [兌心科技有限公司 Insight Software](https://www.insight-software.com/)
