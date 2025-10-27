#!/bin/bash

echo "================================================"
echo "  Insight Software Portals - 啟動腳本"
echo "================================================"
echo ""

# 顏色定義
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 檢查 Java 版本
echo "檢查 Java 版本..."
if ! command -v java &> /dev/null; then
    echo -e "${RED}錯誤：未找到 Java，請先安裝 Java 17 或更高版本${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}錯誤：需要 Java 17 或更高版本，當前版本：$JAVA_VERSION${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Java 版本正常${NC}"
echo ""

# 選擇運行模式
echo "請選擇運行模式："
echo "1) 開發模式（使用 H2 內存數據庫）"
echo "2) 生產模式（需要 PostgreSQL 和 Redis）"
echo ""
read -p "請輸入選項 (1 或 2): " MODE

if [ "$MODE" = "1" ]; then
    echo ""
    echo -e "${YELLOW}正在啟動開發模式...${NC}"
    echo ""
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev &
    
    echo "等待應用啟動（約 30 秒）..."
    sleep 30
    
elif [ "$MODE" = "2" ]; then
    echo ""
    echo -e "${YELLOW}正在啟動生產模式...${NC}"
    echo ""
    echo "請確保 PostgreSQL 和 Redis 已經運行"
    echo "PostgreSQL: localhost:5432"
    echo "Redis: localhost:6379"
    echo ""
    read -p "按 Enter 繼續..."
    
    ./mvnw spring-boot:run &
    
    echo "等待應用啟動（約 30 秒）..."
    sleep 30
else
    echo -e "${RED}無效的選項${NC}"
    exit 1
fi

# 檢查應用是否啟動成功
echo ""
echo "檢查應用狀態..."
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 應用啟動成功！${NC}"
    echo ""
    echo "================================================"
    echo "  訪問以下 URL 開始使用："
    echo "================================================"
    echo ""
    echo -e "${GREEN}登入頁面：${NC}"
    echo "  http://localhost:8080/login"
    echo ""
    echo -e "${GREEN}預設帳號：${NC}"
    echo "  帳號：admin"
    echo "  密碼：admin123"
    echo ""
    echo -e "${GREEN}控制台：${NC}"
    echo "  http://localhost:8080/dashboard"
    echo ""
    echo -e "${GREEN}帳號設定：${NC}"
    echo "  http://localhost:8080/account/settings"
    echo ""
    echo -e "${GREEN}相機查看（公開）：${NC}"
    echo "  http://localhost:8080/view?camera_id=CAM_001"
    echo ""
    echo -e "${GREEN}健康檢查：${NC}"
    echo "  http://localhost:8080/actuator/health"
    echo ""
    echo "================================================"
    echo ""
    echo "按 Ctrl+C 停止應用"
    
    # 在 macOS 上自動打開瀏覽器
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sleep 2
        open http://localhost:8080/login
    fi
    
    # 等待用戶中斷
    wait
else
    echo -e "${RED}✗ 應用啟動失敗${NC}"
    echo ""
    echo "請檢查日誌以獲取更多信息："
    echo "  ./mvnw spring-boot:run"
    exit 1
fi



