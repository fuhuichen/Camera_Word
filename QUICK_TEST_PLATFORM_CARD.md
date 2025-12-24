# 平台卡片功能快速测试指南

## 修复内容
✅ 平台卡片现在可以点击并跳转到详情页面
✅ 修复了相机列表显示问题
✅ Dashboard 平台管理功能已增强

## 快速测试步骤

### 1. 启动应用程序
```bash
cd /Users/fuhuichen/Work/insight/camera_cloud
./start.sh
# 或者
mvn spring-boot:run
```

### 2. 测试平台卡片点击
1. 打开浏览器访问: http://localhost:8080/login
2. 使用以下凭据登录:
   - 用户名: `admin`
   - 密码: `admin123`
3. 登录后，点击导航栏的"平台管理"或访问: http://localhost:8080/platforms
4. **测试点击**: 点击任意平台卡片
5. **预期结果**: 
   - ✅ 页面跳转到平台详情页 (URL: `/platforms/{平台代码}`)
   - ✅ 显示平台详细信息（名称、代码、描述等）
   - ✅ 显示该平台的相机列表（如果有相机）

### 3. 测试 Dashboard 平台选择
1. 访问: http://localhost:8080/dashboard
2. 点击导航栏的"平台管理"标签
3. 在"选择平台"下拉选择器中选择一个平台
4. **预期结果**:
   - ✅ 页面刷新并显示选中平台的信息
   - ✅ 显示平台详细信息区块
   - ✅ 显示相机列表

### 4. 测试视觉效果
- 鼠标悬停在平台卡片上时，卡片应该有轻微上移效果
- 卡片底部显示"查看详情"文字和箭头图标
- 点击卡片的任何位置都应该触发跳转

## 数据库配置检查

如果应用启动失败，请检查数据库配置:

```yaml
# src/main/resources/application.yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/camera_cloud
    username: postgres
    password: <your-password>
```

确保 PostgreSQL 数据库正在运行:
```bash
# macOS (使用 Homebrew)
brew services start postgresql

# 或者使用 Docker
docker-compose up -d
```

## 验证已完成的修改

### 文件修改清单
- ✅ `src/main/resources/templates/platforms.html` - 添加卡片链接
- ✅ `src/main/resources/templates/platform-detail.html` - 修复属性名称
- ✅ `src/main/java/com/example/cameracloud/web/DashboardController.java` - 增强功能

### 编译状态
```bash
mvn clean compile -DskipTests
```
✅ 编译成功，无错误

## 常见问题排查

### 问题 1: 点击卡片没有反应
**可能原因**: JavaScript 阻止了默认行为
**解决方案**: 检查浏览器控制台是否有错误，清除浏览器缓存

### 问题 2: 相机列表不显示
**可能原因**: 数据库中该平台没有相机数据
**解决方案**: 
1. 确认数据库中有相机数据
2. 检查相机的 `target_platform_code` 是否与平台代码匹配

### 问题 3: 平台详情页面显示 404
**可能原因**: 平台代码不存在或路由配置错误
**解决方案**: 
1. 确认 URL 中的平台代码在数据库中存在
2. 检查应用日志查看详细错误信息

## 功能路由表

| 路由 | 功能 | 页面 |
|------|------|------|
| `/platforms` | 平台列表 | platforms.html |
| `/platforms/{code}` | 平台详情 | platform-detail.html |
| `/dashboard` | 控制台首页 | dashboard.html |
| `/dashboard/platform` | 控制台-平台管理 | dashboard.html (带平台选择) |
| `/dashboard/platform?platformCode={code}` | 控制台-特定平台 | dashboard.html (显示平台信息) |

## 测试建议

### 边界情况测试
1. 测试没有相机的平台
2. 测试有大量相机的平台（性能）
3. 测试已停用的平台
4. 测试不存在的平台代码（应该重定向到列表页）

### 浏览器兼容性
- ✅ Chrome/Edge (推荐)
- ✅ Firefox
- ✅ Safari
- ✅ 移动设备响应式设计

---
**最后更新**: 2025-11-05
**状态**: ✅ 准备测试


