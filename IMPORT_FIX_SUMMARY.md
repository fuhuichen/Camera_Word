# 相机导入功能修复总结

## 问题描述
当执行相机导入时，没有新增相机到数据库中。

## 根本原因
在 `ImportService.java` 中发现了以下问题：

### 1. 计数器未更新
在 `processImportFileAsync` 方法中，`totalRows` 和 `successCount` 变量被初始化为 0，但在调用 `processExcelFile` 和 `processCsvFile` 后从未更新这些值。

```java
// 修复前
int successCount = 0;
int totalRows = 0;

if (file.getOriginalFilename().endsWith(".xlsx")) {
    processExcelFile(file, job, errors);  // 没有返回值
} else {
    processCsvFile(file, job, errors);  // 没有返回值
}

// 更新 job 时使用的是未更新的 0 值
job.setTotalRows(totalRows);  // 总是 0
job.setSuccessRows(successCount);  // 总是 0
```

### 2. Excel 列顺序不一致
Excel 文件解析时，列顺序与 CSV 文件不一致：
- **CSV 文件**：`camera_id, platform_code, model, status`
- **Excel 文件（修复前）**：`camera_id, model, platform_code, status`

## 修复方案

### 1. 返回处理结果
创建了 `ImportResult` 记录类来返回处理结果：

```java
private record ImportResult(int totalRows, int successCount) {}
```

### 2. 更新 Excel 和 CSV 处理方法
修改 `processExcelFile` 和 `processCsvFile` 方法返回 `ImportResult`，并在处理过程中正确追踪计数：

```java
private ImportResult processExcelFile(...) throws IOException {
    int totalRows = 0;
    int successCount = 0;
    
    while (rowIterator.hasNext()) {
        // ...
        totalRows++;
        try {
            processCameraRow(row, job, rowNum);
            successCount++;  // 成功时递增
        } catch (Exception e) {
            errors.add(...);  // 失败时记录错误
        }
    }
    
    return new ImportResult(totalRows, successCount);
}
```

### 3. 统一 Excel 列顺序
修改 Excel 行解析代码，使其与 CSV 文件保持一致：

```java
// 列顺序：camera_id, platform_code, model, status
private void processCameraRow(Row row, ImportJob job, int rowNum) {
    String cameraId = getCellValueAsString(row.getCell(0));
    String platformCode = getCellValueAsString(row.getCell(1));  // 修复：从第2列改为第1列
    String model = getCellValueAsString(row.getCell(2));          // 修复：从第1列改为第2列
    String status = getCellValueAsString(row.getCell(3));
    
    processCameraData(cameraId, model, platformCode, status, rowNum);
}
```

## 测试导入功能

### 1. 启动应用程序
```bash
# 使用开发模式（H2 内存数据库）
./mvnw spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=dev
```

### 2. 访问管理后台
1. 浏览器访问：`http://localhost:8080/login`
2. 登录凭据：
   - 用户名：`admin`
   - 密码：`admin123`

### 3. 导入相机数据

#### 使用 CSV 文件
示例文件路径：`sample_data/cameras_sample.csv`

CSV 格式：
```csv
camera_id,platform_code,model,status
CAM_001,DK,Camera Model A,ACTIVE
CAM_002,DK,Camera Model B,ACTIVE
CAM_003,DUIXIN,Camera Model C,DISABLED
```

#### 使用 Excel 文件
示例文件路径：`sample_data/cameras_sample.xlsx`

Excel 列顺序（与 CSV 一致）：
| camera_id | platform_code | model | status |
|-----------|---------------|-------|--------|
| CAM_001 | DK | Camera Model A | ACTIVE |
| CAM_002 | DK | Camera Model B | ACTIVE |

### 4. 验证导入结果
1. 导入完成后，可以在管理后台查看导入任务状态
2. 检查导入统计信息：
   - 总行数
   - 成功行数
   - 失败行数
3. 在平台详情页面查看新增的相机

## API 测试
可以使用 curl 命令测试导入 API：

```bash
# 导入 CSV 文件
curl -X POST \
  -u admin:admin123 \
  -F "file=@sample_data/cameras_sample.csv" \
  http://localhost:8080/api/v1/admin/cameras/import

# 导入 Excel 文件
curl -X POST \
  -u admin:admin123 \
  -F "file=@sample_data/cameras_sample.xlsx" \
  http://localhost:8080/api/v1/admin/cameras/import

# 检查导入任务状态
curl -u admin:admin123 \
  http://localhost:8080/api/v1/admin/cameras/import/{jobId}
```

## 数据验证要求

### 必填字段
- `camera_id`：相机的唯一标识符

### 字段格式要求
- `camera_id`：3-128个字符，只能包含字母、数字、下划线和连字符
- `platform_code`：必须是数据库中已存在的平台代码（如 DK, DUIXIN）
- `status`：可选值为 `ACTIVE` 或 `DISABLED`（不区分大小写）
- `model`：相机型号（可选）

### 导入行为
- 如果相机已存在（基于 camera_id），则更新现有相机的信息
- 如果相机不存在，则创建新相机
- 如果提供的 platform_code 不存在，该行导入将失败

## 相关文件
- `/src/main/java/com/example/cameracloud/service/ImportService.java` - 主要修复文件
- `/src/main/java/com/example/cameracloud/web/CameraImportController.java` - 导入控制器
- `/sample_data/cameras_sample.csv` - CSV 示例文件
- `/sample_data/cameras_sample.xlsx` - Excel 示例文件

## 注意事项
1. 文件大小限制：10MB
2. 支持的文件格式：`.xlsx` 和 `.csv`
3. 导入是异步处理的，可以通过 API 查询导入任务状态
4. 导入过程中的错误会被记录，可以通过 API 获取错误详情
5. 首行被视为表头，会被跳过

## 开发环境配置
如果需要使用 PostgreSQL 而不是 H2：

```bash
# 启动 PostgreSQL 和 Redis
docker-compose up -d postgres redis

# 运行应用程序（使用默认配置）
./mvnw spring-boot:run
```

默认数据库配置：
- URL: `jdbc:postgresql://localhost:5432/camera_cloud`
- Username: `camera_user`
- Password: `camera_pass`


