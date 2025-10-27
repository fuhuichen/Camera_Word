#!/bin/bash

# 快速功能测试
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "=========================================="
echo "Camera Cloud - 功能验证测试"
echo "=========================================="
echo ""

# 1. 健康检查
echo "【测试 1】健康检查"
if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
    echo -e "${GREEN}✓ 健康检查通过${NC}"
else
    echo -e "${RED}✗ 健康检查失败${NC}"
fi
echo ""

# 2. 相机查看 - 首次访问
echo "【测试 2】相机查看 - 首次访问 CAM_001"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/view?camera_id=CAM_001")
if [ "$STATUS" -eq "200" ]; then
    echo -e "${GREEN}✓ 首次访问成功 (200)${NC}"
else
    echo -e "${RED}✗ 首次访问失败 (got $STATUS)${NC}"
fi
echo ""

# 3. 速率限制
echo "【测试 3】速率限制 - 60秒内第二次访问"
sleep 2
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/view?camera_id=CAM_001")
if [ "$STATUS" -eq "429" ]; then
    echo -e "${GREEN}✓ 速率限制生效 (429)${NC}"
else
    echo -e "${RED}✗ 速率限制未生效 (got $STATUS)${NC}"
fi
echo ""

# 4. 不同相机ID不受影响
echo "【测试 4】不同相机ID - 访问 CAM_002"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/view?camera_id=CAM_002")
if [ "$STATUS" -eq "200" ]; then
    echo -e "${GREEN}✓ 不同相机ID正常访问 (200)${NC}"
else
    echo -e "${RED}✗ 访问失败 (got $STATUS)${NC}"
fi
echo ""

# 5. 无效相机ID
echo "【测试 5】输入验证 - 太短的ID"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/view?camera_id=AB")
if [ "$STATUS" -eq "400" ]; then
    echo -e "${GREEN}✓ 输入验证通过 (400)${NC}"
else
    echo -e "${RED}✗ 输入验证失败 (got $STATUS)${NC}"
fi
echo ""

# 6. 管理API - 平台列表
echo "【测试 6】管理API - 获取平台列表"
RESPONSE=$(curl -s -u admin:admin123 "http://localhost:8080/api/v1/admin/platforms")
if echo "$RESPONSE" | grep -q "youtube"; then
    echo -e "${GREEN}✓ 平台列表获取成功${NC}"
    echo "   平台数量: $(echo "$RESPONSE" | grep -o '"code"' | wc -l)"
else
    echo -e "${RED}✗ 平台列表获取失败${NC}"
fi
echo ""

# 7. 管理API - 相机列表
echo "【测试 7】管理API - 获取相机列表"
RESPONSE=$(curl -s -u admin:admin123 "http://localhost:8080/api/v1/admin/cameras")
if echo "$RESPONSE" | grep -q "CAM"; then
    echo -e "${GREEN}✓ 相机列表获取成功${NC}"
    echo "   相机数量: $(echo "$RESPONSE" | grep -o '"publicId"' | wc -l)"
else
    echo -e "${RED}✗ 相机列表获取失败${NC}"
fi
echo ""

# 8. 管理API - 平台测试
echo "【测试 8】管理API - 测试平台连通性"
RESPONSE=$(curl -s -X POST -u admin:admin123 "http://localhost:8080/api/v1/admin/platforms/youtube/test")
if echo "$RESPONSE" | grep -q '"status"'; then
    echo -e "${GREEN}✓ 平台测试功能正常${NC}"
    echo "   响应: $(echo "$RESPONSE" | head -c 100)..."
else
    echo -e "${RED}✗ 平台测试失败${NC}"
fi
echo ""

# 9. Prometheus指标
echo "【测试 9】Prometheus 指标"
if curl -s "http://localhost:8080/actuator/prometheus" | grep -q "jvm_memory"; then
    echo -e "${GREEN}✓ Prometheus 指标可用${NC}"
else
    echo -e "${RED}✗ Prometheus 指标不可用${NC}"
fi
echo ""

echo "=========================================="
echo -e "${GREEN}测试完成！${NC}"
echo "=========================================="



