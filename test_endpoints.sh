#!/bin/bash

# Camera Cloud - Endpoint Testing Script
# This script tests all the main endpoints of the camera cloud application

BASE_URL="http://localhost:8080"
ADMIN_USER="admin"
ADMIN_PASS="admin123"

echo "🎥 Camera Cloud - Endpoint Testing"
echo "=================================="

# Test 1: Health Check
echo "1. Testing health check..."
curl -s "$BASE_URL/actuator/health" | jq . || echo "Health check failed"
echo ""

# Test 2: Camera View - First request (should succeed)
echo "2. Testing camera view - first request (should succeed)..."
curl -s -w "\nHTTP Status: %{http_code}\n" "$BASE_URL/view?camera_id=CAM_001"
echo ""

# Test 3: Camera View - Second request within 60s (should be rate limited)
echo "3. Testing camera view - second request (should be rate limited)..."
curl -s -w "\nHTTP Status: %{http_code}\n" "$BASE_URL/view?camera_id=CAM_001"
echo ""

# Test 4: Camera View - Invalid camera ID (should return 400)
echo "4. Testing camera view - invalid camera ID..."
curl -s -w "\nHTTP Status: %{http_code}\n" "$BASE_URL/view?camera_id=invalid..id"
echo ""

# Test 5: Camera View - Non-existent camera (should return 404)
echo "5. Testing camera view - non-existent camera..."
curl -s -w "\nHTTP Status: %{http_code}\n" "$BASE_URL/view?camera_id=NONEXISTENT"
echo ""

# Test 6: Admin API - List platforms (requires authentication)
echo "6. Testing admin API - list platforms..."
curl -s -u "$ADMIN_USER:$ADMIN_PASS" -w "\nHTTP Status: %{http_code}\n" "$BASE_URL/api/v1/admin/platforms" | jq . || echo "Failed to parse JSON"
echo ""

# Test 7: Admin API - List cameras (requires authentication)
echo "7. Testing admin API - list cameras..."
curl -s -u "$ADMIN_USER:$ADMIN_PASS" -w "\nHTTP Status: %{http_code}\n" "$BASE_URL/api/v1/admin/cameras" | jq . || echo "Failed to parse JSON"
echo ""

# Test 8: Admin API - Create platform (requires authentication)
echo "8. Testing admin API - create platform..."
curl -s -u "$ADMIN_USER:$ADMIN_PASS" \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"code":"test_platform","name":"Test Platform"}' \
  -w "\nHTTP Status: %{http_code}\n" \
  "$BASE_URL/api/v1/admin/platforms" | jq . || echo "Failed to parse JSON"
echo ""

# Test 9: Admin API - Test platform (requires authentication)
echo "9. Testing admin API - test platform..."
curl -s -u "$ADMIN_USER:$ADMIN_PASS" \
  -X POST \
  -w "\nHTTP Status: %{http_code}\n" \
  "$BASE_URL/api/v1/admin/platforms/youtube/test" | jq . || echo "Failed to parse JSON"
echo ""

# Test 10: Metrics endpoint
echo "10. Testing metrics endpoint..."
curl -s "$BASE_URL/actuator/prometheus" | head -20
echo ""

echo "✅ Endpoint testing completed!"
echo ""
echo "📋 Summary:"
echo "- Camera view endpoint with rate limiting"
echo "- Admin API with authentication"
echo "- Health checks and metrics"
echo "- Error handling for invalid inputs"
echo ""
echo "🔧 To test rate limiting, wait 60 seconds and run the camera view test again"
echo "📊 Check the logs for audit trail and rate limiting events"
