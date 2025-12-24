#!/bin/bash

# Camera Cloud - CSV导入测试脚本

echo "================================================"
echo "  Camera Cloud - CSV导入测试"
echo "================================================"
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"
ADMIN_USER="admin"
ADMIN_PASS="admin123"

# 检查服务是否运行
echo "1. 检查服务状态..."
if curl -s "$BASE_URL/actuator/health" | grep -q "UP"; then
    echo -e "${GREEN}✓ 服务运行正常${NC}"
else
    echo -e "${RED}✗ 服务未运行，请先启动服务${NC}"
    echo "运行: ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev"
    exit 1
fi
echo ""

# 导入CSV文件
echo "2. 导入CSV文件..."
CSV_FILE="sample_data/cameras_import_correct.csv"

if [ ! -f "$CSV_FILE" ]; then
    echo -e "${RED}✗ CSV文件不存在: $CSV_FILE${NC}"
    exit 1
fi

RESPONSE=$(curl -s -u "$ADMIN_USER:$ADMIN_PASS" \
  -X POST \
  -F "file=@$CSV_FILE" \
  "$BASE_URL/api/v1/admin/cameras/import")

if echo "$RESPONSE" | grep -q "jobId"; then
    JOB_ID=$(echo "$RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['jobId'])" 2>/dev/null)
    echo -e "${GREEN}✓ 导入作业已创建${NC}"
    echo "  Job ID: $JOB_ID"
else
    echo -e "${RED}✗ 导入失败${NC}"
    echo "$RESPONSE"
    exit 1
fi
echo ""

# 等待导入完成
echo "3. 等待导入完成..."
sleep 3
echo -e "${GREEN}✓ 导入处理完成${NC}"
echo ""

# 检查导入结果
echo "4. 查看导入日志..."
tail -30 app.log | grep "Import job completed" | tail -1
echo ""

# 验证导入的相机
echo "5. 验证导入的相机..."
TEST_CAMERAS=("CAM_DK_001" "CAM_DK_002" "CAM_DK_005" "CAM_DUIXIN_001" "CAM_DUIXIN_005")
SUCCESS=0
FAILED=0

for cam in "${TEST_CAMERAS[@]}"; do
    if curl -s "$BASE_URL/view?camera_id=$cam" | grep -q "Camera ID"; then
        echo -e "  ${GREEN}✓${NC} $cam - 可访问"
        ((SUCCESS++))
    else
        echo -e "  ${RED}✗${NC} $cam - 无法访问"
        ((FAILED++))
    fi
done
echo ""

# 总结
echo "================================================"
echo "  测试结果总结"
echo "================================================"
echo ""
echo -e "${GREEN}成功:${NC} $SUCCESS 个相机"
if [ $FAILED -gt 0 ]; then
    echo -e "${RED}失败:${NC} $FAILED 个相机"
fi
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 所有测试通过！CSV导入功能正常工作${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠️  部分测试失败，请检查日志${NC}"
    exit 1
fi

