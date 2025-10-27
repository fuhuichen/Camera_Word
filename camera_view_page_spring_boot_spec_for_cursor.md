# Camera View Page — Minimal Spec (Java + Spring Boot)

> **Purpose:** Build a single web page endpoint `GET /view?camera_id=...` that renders a simple HTML page. **Constraint:** For the same `camera_id`, only the **first successful request per rolling 60 seconds** is allowed; subsequent requests within 60 seconds must receive **HTTP 429**. **No tokens, no user binding** — the only parameter is `camera_id`.

---

## 1. Scope & Goals
- **In-scope**
  - Public endpoint: `GET /view?camera_id=<string>` that returns an HTML page.
  - **Per-camera_id rate limit**: allow once per 60 seconds; else 429.
  - Input validation for `camera_id` format.
  - Minimal anti-embedding / anti-caching headers.
  - Structured logging for observability.
- **Out-of-scope**
  - Authentication, authorization, tokens, IP/UA binding.
  - Any media streaming logic beyond rendering a placeholder.

## 2. Tech Stack
- **Runtime:** Java 21, Spring Boot 3.x
- **Cache/Rate limit store (primary):** Redis 6+
- **Alternative (dev-only):** In-memory limiter (single-instance only)
- **Build:** Maven
- **Container (optional):** Docker, Docker Compose (for Redis)

## 3. Endpoint Contract
### 3.1 Request
- **Method/Path**: `GET /view`
- **Query Params**:
  - `camera_id` *(required, string)*: 3–128 chars, regex: `^[A-Za-z0-9_-]{3,128}$`

### 3.2 Responses
- **200 OK (text/html; charset=utf-8)**
  - Renders a basic HTML page that shows the camera id and current time.
  - **Headers:**
    - `Cache-Control: no-store, no-cache, must-revalidate`
    - `Pragma: no-cache`
    - `X-Frame-Options: DENY`
    - `Referrer-Policy: no-referrer`
- **400 Bad Request (text/html)** — invalid or missing `camera_id`.
- **429 Too Many Requests (text/html)** — rate limit triggered for the given `camera_id` within the last 60 seconds.
- **5xx** — unexpected errors.

### 3.3 Examples
**Success**
```
GET /view?camera_id=CAMERA_001
200 OK
Content-Type: text/html; charset=utf-8
Cache-Control: no-store, no-cache, must-revalidate
X-Frame-Options: DENY
Referrer-Policy: no-referrer
```
(HTML body displays the camera id and server time.)

**Invalid camera_id**
```
GET /view?camera_id=..
400 Bad Request
```

**Rate limited**
```
GET /view?camera_id=CAMERA_001  (second call within 60s)
429 Too Many Requests
```

## 4. Functional Details
### 4.1 Rate Limiting Rule
- Keyed strictly by `camera_id`.
- **Rule:** First request that passes validation should **set a Redis key** and succeed; repeated requests for the same `camera_id` within **60 seconds** receive `429`.
- **Recommended Redis operation:** `SET key value NX EX 60` (atomic).
  - Key pattern: `rate:view:{camera_id}`
  - Value: any constant (e.g., `1`)
  - TTL: 60 seconds
- **Concurrency requirement:** race-safe — multiple near-simultaneous requests for the same `camera_id` must result in only one `200`, others `429`.

### 4.2 Input Validation
- Accept only `^[A-Za-z0-9_-]{3,128}$`.
- On violation: respond `400` with a short HTML message.

### 4.3 Headers & Robots
- Add headers listed in **3.2**.
- Include `<meta name="robots" content="noindex,nofollow">` in the HTML.

### 4.4 Logging
- Log at `INFO`: `{ts, remote_ip, camera_id, outcome: ALLOW|RATE_LIMIT|BAD_REQUEST}`
- Log at `ERROR`: unexpected exceptions with stacktrace (sanitized).

## 5. Non-Functional Requirements
- **Performance:** Endpoint should respond in < 50ms p50 (excluding network) with Redis warm.
- **Scalability:** Multiple app instances supported (Redis centralizes rate state).
- **Security:** No tokens. Basic header hardening only. Input validation mandatory.
- **Observability:** Metrics (optional): total requests, allowed, rate-limited, bad-request; export via Micrometer/Prometheus if convenient.

## 6. Configuration
- **Environment variables / application.yaml**
  - `server.port` (default 8080)
  - Redis:
    - `spring.redis.host`
    - `spring.redis.port`
    - `spring.redis.password` (optional)
- **Config defaults**
  - Rate window seconds: `60` (property `app.rate.window-seconds`, default 60)

## 7. Project Layout (suggested)
```
src/main/java/com/example/
  Application.java
  web/ViewController.java
  rl/CameraRateLimiter.java        # Redis-based
aop/GlobalExceptionHandler.java    # Optional
resources/
  application.yaml
```

## 8. Implementation Notes
### 8.1 Redis-based limiter
Pseudo:
```java
boolean tryAcquire(String cameraId) {
  String key = "rate:view:" + cameraId;
  return redis.setIfAbsent(key, "1", Duration.ofSeconds(windowSeconds)); // SET NX EX
}
```

### 8.2 Controller behavior
- Validate `camera_id`.
- Call `tryAcquire(camera_id)`.
  - If `true`: return `200` with HTML body and headers.
  - If `false`: return `429` with HTML error page.

### 8.3 In-memory fallback (dev-only)
- Provide `@Primary` in-memory implementation guarded by profile `dev`.
- **Warning:** Not for multi-instance deployments.

## 9. Error Pages (HTML minimal)
- 400 page: title `Bad Request` + reason.
- 429 page: title `Too Many Requests` + message: "此 camera_id 在 60 秒內已被使用，請稍後再試。"

## 10. Nginx (Optional Edge Limit)
If you need an edge-only throttle (not exact first-win semantics):
```nginx
limit_req_zone $arg_camera_id zone=per_camera:10m rate=1r/m;
location /view {
  limit_req zone=per_camera burst=0 nodelay;
  proxy_pass http://spring_backend;
}
```
> This is best-effort averaging, **not** strict first-success semantics. Prefer Redis approach for correctness.

## 11. Testing Plan
### 11.1 Unit
- `CameraRateLimiter` returns `true` for first call, `false` thereafter within window.
- Regex validation.

### 11.2 Integration
- With a running Redis, concurrent requests (>= 10) for the same `camera_id` within a few ms must yield exactly 1×`200`, others `429`.
- Different `camera_id` values are isolated.

### 11.3 E2E (manual)
- `curl \
  -H 'Accept:text/html' \
  "http://localhost:8080/view?camera_id=ABC_123"` → `200`
- Repeat within 60s → `429`
- Wait 60s → `200` again

## 12. Acceptance Criteria
- [ ] `GET /view?camera_id=<valid>` returns HTML `200` once per 60s window per camera_id.
- [ ] Subsequent calls within window return `429` with HTML body.
- [ ] Invalid `camera_id` returns `400` with HTML body.
- [ ] Required headers present on `200` responses.
- [ ] Works correctly across multiple app instances (with Redis).
- [ ] Structured logs emitted as specified.

## 13. Nice-to-have (Optional)
- Prometheus/Micrometer metrics for allow/limit counts.
- Docker Compose for Redis (`redis:7-alpine`).
- GitHub Actions workflow (build, unit tests).

---

## 14. Task Breakdown for Cursor
1. Scaffold Spring Boot 3 project (Java 21, Maven, Web, Data Redis).
2. Implement `CameraRateLimiter` (Redis) and `InMemoryCameraRateLimiter` (dev profile).
3. Implement `ViewController` with validation, limiter call, and HTML rendering.
4. Add response headers and HTML templates (or inline strings) for 200/400/429.
5. Add structured logging (SLF4J) and basic exception handler.
6. Provide `application.yaml` and README with run instructions.
7. (Optional) Add Docker Compose for Redis and a tiny Makefile.

## 15. Run Instructions (example)
- Local with Redis:
  - `docker run -p 6379:6379 redis:7-alpine`
  - `./mvnw spring-boot:run`
  - Open: `http://localhost:8080/view?camera_id=CAM_001`
- Dev-only (no Redis): run with profile `dev` to activate in-memory limiter.



---

# 16. Admin Portal Spec（管理網站）
> **Roles**：
> - **MAIN_ADMIN（主要管理者）**：全域最高權限；維護平台清單、測試平台、批次匯入相機與指派平台、停用相機串流 redirect。
> - **PLATFORM_ADMIN（串流平台管理員）**：僅能管理自己負責的平台（或多個被授權的平台）之相機；不可新增/移除平台；可檢視並操作「相機指派」、「停用/啟用 redirect」。

## 16.1 權限矩陣
| 功能 | MAIN_ADMIN | PLATFORM_ADMIN |
|---|---|---|
| 平台列表（檢視） | ✅ | ✅（僅被授權的平台） |
| 平台建立/修改/停用 | ✅ | ❌ |
| 平台「測試連通」 | ✅ | ✅（僅被授權的平台） |
| 匯入相機（Excel） | ✅ | ❌ |
| 相機列表（檢視） | ✅ | ✅（僅所屬/被指派平台） |
| 指派/變更相機的平台 | ✅ | ✅（僅被授權的平台） |
| 停用/啟用相機 redirect | ✅ | ✅（僅被授權的平台） |

> **被授權的平台**：對於 PLATFORM_ADMIN，需在後台為使用者綁定一個或多個 `platform_code`。

## 16.2 資料模型（DB 變更）
```sql
-- 平台登錄（支援平台清單）
CREATE TABLE platforms (
  code TEXT PRIMARY KEY,               -- 例："youtube"、"twitch"、"facebook"
  name TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'active',  -- active|disabled
  test_endpoint TEXT,                  -- 用於「測試」的健康檢查 URL 或代碼標記
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now()
);

-- 使用者（若已有帳號系統，此表可省略僅補欄位）
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT UNIQUE NOT NULL,
  display_name TEXT,
  role TEXT NOT NULL,                 -- MAIN_ADMIN | PLATFORM_ADMIN
  created_at TIMESTAMPTZ DEFAULT now()
);

-- 平台管理員對應授權的平台（僅 PLATFORM_ADMIN 需要）
CREATE TABLE user_platforms (
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  platform_code TEXT REFERENCES platforms(code) ON DELETE CASCADE,
  PRIMARY KEY (user_id, platform_code)
);

-- cameras 追加欄位：目標平台與 redirect 開關
ALTER TABLE cameras
  ADD COLUMN target_platform_code TEXT REFERENCES platforms(code),
  ADD COLUMN redirect_enabled BOOLEAN NOT NULL DEFAULT TRUE; -- 停用後，/view 應直接拒絕

-- 匯入作業追蹤
CREATE TABLE import_jobs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  uploader_user_id UUID REFERENCES users(id),
  file_name TEXT,
  total_rows INT,
  success_rows INT,
  failed_rows INT,
  created_at TIMESTAMPTZ DEFAULT now(),
  status TEXT NOT NULL DEFAULT 'done' -- queued|processing|done|failed
);

CREATE TABLE import_job_errors (
  id BIGSERIAL PRIMARY KEY,
  job_id UUID REFERENCES import_jobs(id) ON DELETE CASCADE,
  row_no INT,
  camera_id_in_file TEXT,
  error_message TEXT
);
```

## 16.3 API 介面（Admin 專用）
> Base path: `/api/v1/admin/*`（僅登入後且具相應角色可用）

### 16.3.1 平台管理
- `GET /platforms` → 列出平台（PLATFORM_ADMIN 僅看被授權的平台）
- `POST /platforms` (MAIN_ADMIN) → 新增平台 `{code,name}`
- `PATCH /platforms/{code}` (MAIN_ADMIN) → 更新 `{name?, status?, test_endpoint?}`
- `POST /platforms/{code}/test` → 連通測試；回 `{status: "ok"|"fail", details}`

### 16.3.2 相機管理
- `GET /cameras?platform=code&status=active&redirect=on/off&search=...` → 分頁列表
- `POST /cameras/{publicId}/assign-platform` → 指派/變更 `{platform_code}`
- `PATCH /cameras/{publicId}/redirect` → `{enabled: true|false}`  // 停用/啟用 redirect

### 16.3.3 匯入相機（Excel）
- `POST /cameras/import` (multipart/form-data)
  - 表單欄位：`file`（.xlsx 或 .csv）
  - 伺服器以背景 job 解析，立即回 `{job_id}`
- `GET /cameras/import/{job_id}` → 回傳 job 狀態與錯誤明細（前端可做進度輪詢）

> **錯誤碼範例**：`40001 invalid_platform_code`、`40002 invalid_camera_id`、`40901 camera_exists`、`40301 forbidden`。

## 16.4 Excel/CSV 匯入規格
- 支援副檔名：`.xlsx` / `.csv`
- **必要欄位**（首列標題）：
  - `camera_id`（3–128, `^[A-Za-z0-9_-]{3,128}$`）
  - `platform_code`（需存在於 `platforms`）
- **可選欄位**：`model`, `status`（active/disabled）
- **處理邏輯**：
  1. 驗證欄位 → 不通過者記錄至 `import_job_errors`
  2. `camera_id` 不存在：建立 `cameras(public_id=camera_id, target_platform_code, status)`
  3. 已存在：更新 `target_platform_code`（若允許覆寫）與 `status`
  4. 統計成功/失敗，更新 `import_jobs`

## 16.5 「停用相機的串流 redirect」行為
- 當 `cameras.redirect_enabled = FALSE` 時：
  - `GET /view?camera_id=...` 應回 `403 Forbidden`（或顯示停用提示頁）
  - 同時寫入 `audit_logs(action='redirect_disabled_block', resource_type='camera', resource_id=camera_id)`
- 後台切換：`PATCH /api/v1/admin/cameras/{publicId}/redirect` with `{enabled:false}`

## 16.6 前端（Next.js）頁面與流程
```
/app/admin/platforms/page.tsx           # 平台清單（搜尋/篩選/狀態顯示）
/app/admin/platforms/[code]/test/page.tsx # 平台測試頁（顯示測試結果/延遲/錯誤）
/app/admin/cameras/page.tsx             # 相機列表（依平台過濾、搜尋 camera_id、redirect toggle）
/app/admin/cameras/import/page.tsx      # 匯入精靈（上傳→背景處理→結果報告）
```
**UI 要點**：
- 平台清單：顯示 `code/name/status`、「測試」按鈕（呼叫 `/platforms/{code}/test`）
- 相機頁：表格欄位 `camera_id / platform / status / redirect_enabled / lastUpdated`；
  - 行內操作：`指派平台`（下拉）、「停用/啟用 redirect」切換；
- 匯入精靈：上傳檔案 → 顯示 `job_id` → 自動輪詢狀態與錯誤清單下載。

## 16.7 後端（Spring Boot）實作重點
- **Security**：Spring Security 加角色控管；PLATFORM_ADMIN 的查詢與操作必須以 `WHERE platform_code IN (:userPlatforms)` 受限。
- **Validation**：`platform_code` 存在性、`camera_id` 正規表達式、`status` 合法值。
- **Rate Limit**：Admin API 也應有基本節流（防止誤操作造成壓力）。
- **Audit**：所有 Admin 操作寫入 `audit_logs`（userId、action、resource、before/after）。
- **Import**：使用 `@Async` 或排程工作佇列處理；大檔案以串流解析（Apache POI / Jackson CSV）。

## 16.8 驗收標準（Admin）
- [ ] MAIN_ADMIN 能新增/修改/停用平台，並執行「測試」取得成功/失敗回饋。
- [ ] MAIN_ADMIN 能上傳 Excel/CSV 匯入；成功與錯誤數據與詳細錯誤可見。
- [ ] PLATFORM_ADMIN 僅能看與操作被授權平台下的相機。
- [ ] 任一相機切換 `redirect_enabled=false` 後，`/view` 立即拒絕（403），且有審計記錄。
- [ ] 大量匯入（1–5 萬列）能在可接受時間內完成（依環境定基準），並且 UI 有進度可見。

## 16.9 開發任務（增補 Cursor 任務）
1. DB migration：`platforms`、`users`（若無）、`user_platforms`、`cameras` 欄位擴充、`import_jobs*`。
2. Spring Security 角色與方法/查詢層級的授權過濾（`@PreAuthorize` + Repository 條件）。
3. Admin API：平台 CRUD + 測試、相機列表/指派/redirect 切換、匯入 job + 狀態查詢。
4. Excel/CSV 解析服務與錯誤收集；大批量最佳化（batch insert/update）。
5. Next.js Admin UI：平台列表/測試、相機列表（過濾/切換）、匯入精靈（上傳→輪詢→報告）。
6. 審計/日誌/指標（可用 Micrometer + Prometheus）。

