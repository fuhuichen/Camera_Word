#!/bin/bash

# Camera Cloud - 啟動和測試腳本

set -e

echo "========================================"
echo "Camera Cloud - 啟動和測試"
echo "========================================"
echo ""

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 步驟 1: 停止現有服務
echo -e "${YELLOW}步驟 1: 停止現有服務...${NC}"
docker-compose down
echo -e "${GREEN}✓ 服務已停止${NC}"
echo ""

# 步驟 2: 清理舊的構建
echo -e "${YELLOW}步驟 2: 清理舊的構建...${NC}"
./mvnw clean
echo -e "${GREEN}✓ 清理完成${NC}"
echo ""

# 步驟 3: 重新編譯
echo -e "${YELLOW}步驟 3: 重新編譯應用...${NC}"
./mvnw package -DskipTests
echo -e "${GREEN}✓ 編譯成功${NC}"
echo ""

# 步驟 4: 驗證模板文件
echo -e "${YELLOW}步驟 4: 驗證模板文件...${NC}"
if unzip -l target/camera-cloud-1.0.0-SNAPSHOT.jar | grep -q "templates/login.html"; then
    echo -e "${GREEN}✓ login.html 已打包${NC}"
else
    echo -e "${RED}✗ login.html 未找到${NC}"
    exit 1
fi

if unzip -l target/camera-cloud-1.0.0-SNAPSHOT.jar | grep -q "templates/dashboard.html"; then
    echo -e "${GREEN}✓ dashboard.html 已打包${NC}"
else
    echo -e "${RED}✗ dashboard.html 未找到${NC}"
    exit 1
fi

if unzip -l target/camera-cloud-1.0.0-SNAPSHOT.jar | grep -q "templates/account-settings.html"; then
    echo -e "${GREEN}✓ account-settings.html 已打包${NC}"
else
    echo -e "${RED}✗ account-settings.html 未找到${NC}"
    exit 1
fi
echo ""

# 步驟 5: 啟動服務
echo -e "${YELLOW}步驟 5: 啟動 Docker 服務...${NC}"
docker-compose up -d --build
echo -e "${GREEN}✓ 服務已啟動${NC}"
echo ""

# 步驟 6: 等待應用啟動
echo -e "${YELLOW}步驟 6: 等待應用啟動（最多60秒）...${NC}"
COUNTER=0
MAX_WAIT=60

while [ $COUNTER -lt $MAX_WAIT ]; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}✓ 應用已成功啟動${NC}"
        break
    fi
    echo -n "."
    sleep 2
    COUNTER=$((COUNTER + 2))
done

if [ $COUNTER -ge $MAX_WAIT ]; then
    echo -e "${RED}✗ 應用啟動超時${NC}"
    echo "請查看日誌: docker-compose logs app"
    exit 1
fi
echo ""

# 步驟 7: 測試登入頁面
echo -e "${YELLOW}步驟 7: 測試登入頁面...${NC}"
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/login)
if [ "$RESPONSE" = "200" ]; then
    echo -e "${GREEN}✓ 登入頁面回應正常 (HTTP 200)${NC}"
else
    echo -e "${RED}✗ 登入頁面回應異常 (HTTP $RESPONSE)${NC}"
fi
echo ""

# 步驟 8: 檢查頁面內容
echo -e "${YELLOW}步驟 8: 檢查登入頁面內容...${NC}"
PAGE_CONTENT=$(curl -s http://localhost:8080/login)

if echo "$PAGE_CONTENT" | grep -q "Insight Software Portals"; then
    echo -e "${GREEN}✓ 找到頁面標題${NC}"
else
    echo -e "${RED}✗ 未找到頁面標題${NC}"
fi

if echo "$PAGE_CONTENT" | grep -q "使用者帳號"; then
    echo -e "${GREEN}✓ 找到帳號輸入框${NC}"
else
    echo -e "${RED}✗ 未找到帳號輸入框${NC}"
fi

if echo "$PAGE_CONTENT" | grep -q "密碼"; then
    echo -e "${GREEN}✓ 找到密碼輸入框${NC}"
else
    echo -e "${RED}✗ 未找到密碼輸入框${NC}"
fi

if echo "$PAGE_CONTENT" | grep -q "登入系統"; then
    echo -e "${GREEN}✓ 找到登入按鈕${NC}"
else
    echo -e "${RED}✗ 未找到登入按鈕${NC}"
fi
echo ""

# 完成
echo "========================================"
echo -e "${GREEN}✓ 所有檢查完成！${NC}"
echo "========================================"
echo ""
echo -e "${BLUE}請在瀏覽器中訪問：${NC}"
echo -e "${BLUE}http://localhost:8080/login${NC}"
echo ""
echo -e "${BLUE}預設帳號：${NC}"
echo "  帳號: admin"
echo "  密碼: admin123"
echo ""
echo -e "${YELLOW}提示：${NC}"
echo "1. 如果頁面顯示不正確，請清除瀏覽器快取"
echo "2. 或使用無痕模式開啟（Ctrl+Shift+N / Cmd+Shift+N）"
echo "3. 或強制重新載入（Ctrl+Shift+R / Cmd+Shift+R）"
echo ""
echo -e "${BLUE}查看日誌：${NC}"
echo "  docker-compose logs -f app"
echo ""



