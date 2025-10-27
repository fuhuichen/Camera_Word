# 登入錯誤訊息完整說明

## 📋 概述

Insight Software Portals 的登入頁面現在擁有完整且友善的錯誤訊息系統，能夠清楚告知使用者登入失敗的原因。

## ⚠️ 錯誤訊息類型

### 1. 帳號或密碼錯誤
**觸發條件**: 輸入的帳號或密碼不正確

**顯示訊息**:
```
⚠️ 登入失敗
帳號或密碼錯誤，請檢查後重試
請確認：帳號和密碼區分大小寫
```

**URL 參數**: `?error=true`

**測試方式**:
```bash
# 1. 訪問登入頁面
http://localhost:8080/login

# 2. 輸入錯誤的帳號或密碼
帳號: admin
密碼: wrong_password

# 3. 點擊登入，會看到錯誤訊息
```

### 2. 會話已過期
**觸發條件**: 登入會話已過期，需要重新登入

**顯示訊息**:
```
⚠️ 會話已過期
您的登入會話已過期，請重新登入
```

**URL 參數**: `?invalid=true`

### 3. 存取被拒絕
**觸發條件**: 使用者沒有權限訪問特定頁面

**顯示訊息**:
```
⚠️ 存取被拒絕
您沒有權限訪問該頁面，請使用正確的帳號登入
```

**URL 參數**: `?denied=true`

### 4. 帳號已鎖定
**觸發條件**: 使用者帳號已被系統管理員鎖定

**顯示訊息**:
```
🔒 帳號已鎖定
您的帳號已被鎖定，請聯絡系統管理員
```

**URL 參數**: `?locked=true`

### 5. 登出成功（成功訊息）
**觸發條件**: 使用者成功登出系統

**顯示訊息**:
```
✓ 登出成功
您已安全登出系統，感謝使用
```

**URL 參數**: `?logout=true`

### 6. 空白欄位錯誤（客戶端驗證）
**觸發條件**: 未填寫帳號或密碼就點擊登入

**顯示訊息**:
```
⚠️ 輸入錯誤
請填寫帳號和密碼
```

**特點**: 在客戶端即時驗證，不發送請求到伺服器

## 🎨 視覺設計

### 錯誤訊息樣式
- 📍 **位置**: 顯示在登入表單上方
- 🎨 **顏色**: 紅色背景（#fee2e2）+ 紅色文字（#991b1b）
- 🔲 **邊框**: 左側4px紅色實線強調
- ⏱️ **動畫**: 從上方滑入（0.3秒）
- ⏰ **持續時間**: 顯示8秒後自動淡出

### 成功訊息樣式
- 🎨 **顏色**: 綠色背景（#d1fae5）+ 綠色文字（#065f46）
- 🔲 **邊框**: 左側4px綠色實線
- ⏱️ **動畫**: 同錯誤訊息
- ⏰ **持續時間**: 8秒後自動淡出

## ✨ 增強功能

### 1. 即時輸入驗證
當使用者離開輸入框（blur）時：
- ✅ **已填寫**: 輸入框邊框變綠色
- ❌ **未填寫**: 輸入框邊框變紅色
- 🔵 **聚焦時**: 輸入框邊框變藍色

### 2. 顯示密碼功能
- 👁️ 點擊密碼框旁的眼睛圖示
- 🙈 切換顯示/隱藏密碼
- 💡 方便使用者確認輸入內容

### 3. 登入狀態提示
點擊登入按鈕後：
- 按鈕文字變更為「驗證中...」
- 按鈕被禁用防止重複提交
- 顯示載入動畫

### 4. 預設帳號顯示
在登入表單下方顯示：
```
預設測試帳號
👤 主要管理員：admin / admin123
👤 平台管理員：platform_admin / platform123
```

## 🧪 測試場景

### 場景 1: 錯誤的密碼
```
步驟：
1. 訪問 http://localhost:8080/login
2. 輸入帳號：admin
3. 輸入密碼：wrongpassword
4. 點擊「登入系統」

預期結果：
- 頁面重新載入並顯示錯誤訊息
- URL 變為：/login?error=true
- 顯示紅色錯誤框：「帳號或密碼錯誤」
```

### 場景 2: 空白帳號
```
步驟：
1. 訪問登入頁面
2. 不輸入任何內容
3. 直接點擊「登入系統」

預期結果：
- 不發送請求
- 立即顯示錯誤訊息：「請填寫帳號和密碼」
- 輸入框邊框變紅
```

### 場景 3: 正確登入
```
步驟：
1. 訪問登入頁面
2. 輸入帳號：admin
3. 輸入密碼：admin123
4. 點擊「登入系統」

預期結果：
- 按鈕文字變為「驗證中...」
- 自動跳轉到 /dashboard
- 不顯示任何錯誤訊息
```

### 場景 4: 登出後回到登入頁
```
步驟：
1. 已登入狀態
2. 點擊右上角「登出」按鈕
3. 自動跳轉到登入頁

預期結果：
- URL 顯示：/login?logout=true
- 顯示綠色成功訊息：「登出成功」
- 8秒後訊息自動消失
```

### 場景 5: 使用顯示密碼功能
```
步驟：
1. 在密碼框輸入密碼
2. 點擊右側的 👁️ 圖示

預期結果：
- 密碼變為明文顯示
- 圖示變為 🙈
- 再次點擊恢復為隱藏
```

## 📊 錯誤訊息狀態表

| 情況 | URL 參數 | 訊息標題 | 訊息內容 | 顏色 |
|------|---------|---------|---------|------|
| 登入失敗 | `?error` | ⚠️ 登入失敗 | 帳號或密碼錯誤... | 紅色 |
| 會話過期 | `?invalid` | ⚠️ 會話已過期 | 您的登入會話已過期... | 紅色 |
| 權限不足 | `?denied` | ⚠️ 存取被拒絕 | 您沒有權限訪問... | 紅色 |
| 帳號鎖定 | `?locked` | 🔒 帳號已鎖定 | 您的帳號已被鎖定... | 紅色 |
| 成功登出 | `?logout` | ✓ 登出成功 | 您已安全登出系統... | 綠色 |
| 空白欄位 | (客戶端) | ⚠️ 輸入錯誤 | 請填寫帳號和密碼 | 紅色 |

## 🔧 技術實現

### 伺服器端錯誤處理
```html
<!-- Thymeleaf 條件渲染 -->
<div th:if="${param.error}" class="alert alert-danger">
    <strong>⚠️ 登入失敗</strong><br>
    <span th:if="${session.SPRING_SECURITY_LAST_EXCEPTION}">
        <span th:text="${session.SPRING_SECURITY_LAST_EXCEPTION.message}"></span>
    </span>
    <span th:unless="${session.SPRING_SECURITY_LAST_EXCEPTION}">
        帳號或密碼錯誤，請檢查後重試
    </span>
</div>
```

### 客戶端驗證
```javascript
// 提交前驗證
document.querySelector('form').addEventListener('submit', function(e) {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    if (!username || !password) {
        e.preventDefault();
        showError('請填寫帳號和密碼');
        return false;
    }
});
```

### 動態錯誤顯示
```javascript
function showError(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'alert alert-danger custom-error';
    errorDiv.innerHTML = '<strong>⚠️ 輸入錯誤</strong><br>' + message;
    
    // 插入表單前
    form.parentNode.insertBefore(errorDiv, form);
    
    // 5秒後自動移除
    setTimeout(() => errorDiv.remove(), 5000);
}
```

## 🎯 使用者體驗改進

### 改進前
- ❌ 錯誤訊息不明確
- ❌ 沒有即時驗證
- ❌ 無法確認輸入內容
- ❌ 不知道預設帳號

### 改進後
- ✅ 清楚的錯誤訊息和原因
- ✅ 即時的輸入欄位驗證
- ✅ 可以顯示密碼確認輸入
- ✅ 明確顯示預設測試帳號
- ✅ 美觀的動畫效果
- ✅ 自動消失不干擾操作

## 📱 響應式設計

錯誤訊息在不同裝置上都能正確顯示：

- **桌面版**: 完整訊息顯示，左側強調線
- **平板版**: 自適應寬度，保持可讀性
- **手機版**: 自動調整字體大小和內距

## 🔐 安全性考量

### 訊息設計原則
1. **不洩露具體資訊**: 不明確指出是帳號還是密碼錯誤
2. **統一錯誤訊息**: 避免暴力破解帳號
3. **友善提示**: 提供足夠資訊協助使用者修正
4. **防止列舉**: 不透露帳號是否存在

### 範例對比
❌ **不安全的訊息**: 
- "帳號不存在"（洩露帳號資訊）
- "密碼錯誤"（確認帳號存在）

✅ **安全的訊息**:
- "帳號或密碼錯誤"（模糊但有幫助）

## 🚀 快速測試

### 使用瀏覽器測試
```bash
# 1. 啟動應用
docker-compose up -d

# 2. 打開瀏覽器
open http://localhost:8080/login

# 3. 測試各種錯誤場景
- 空白提交
- 錯誤密碼
- 正確登入
- 登出後重新登入
```

### 使用 curl 測試
```bash
# 測試錯誤登入（會重定向到 /login?error=true）
curl -i -X POST http://localhost:8080/login \
  -d "username=admin&password=wrong" \
  -H "Content-Type: application/x-www-form-urlencoded"

# 測試正確登入
curl -i -X POST http://localhost:8080/login \
  -d "username=admin&password=admin123" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -c cookies.txt

# 測試登出
curl -i -X POST http://localhost:8080/logout \
  -b cookies.txt
```

## 📚 相關文檔

- [登入頁面說明](LOGIN_PAGE.md)
- [帳號管理系統](ACCOUNT_MANAGEMENT.md)
- [快速開始指南](QUICKSTART_CN.md)
- [完整 README](README.md)

## 🔗 相關連結

- [Spring Security 文檔](https://spring.io/projects/spring-security)
- [兌心科技官網](https://www.insight-software.com/)

---

## 📞 常見問題

### Q: 錯誤訊息為什麼會自動消失？
A: 為了不干擾使用者操作，錯誤訊息會在8秒後自動淡出。使用者可以在消失前閱讀訊息。

### Q: 可以修改錯誤訊息的顯示時間嗎？
A: 可以，在 `login.html` 的 JavaScript 中修改 `setTimeout` 的時間參數（目前是8000毫秒）。

### Q: 如何自訂錯誤訊息內容？
A: 編輯 `login.html` 中對應的 Thymeleaf 條件塊，修改訊息文字即可。

### Q: 密碼顯示功能安全嗎？
A: 這是常見的 UX 功能，方便使用者確認輸入。建議在公共場所不要使用此功能。

---

© 2025 [兌心科技有限公司 Insight Software](https://www.insight-software.com/). All Rights Reserved.



