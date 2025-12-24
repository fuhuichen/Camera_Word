# Camera CSV 导入指南

## 导入成功！

✅ 服务已启动并成功导入了 `cameras_import_correct.csv` 文件
✅ 10条相机记录成功导入
✅ 所有相机都可以正常访问

## CSV 文件格式要求

### 必需的列名（英文，小写）

```csv
camera_id,model,platform_code,status
```

### 列说明

1. **camera_id** (必需)
   - 相机的唯一标识符
   - 只能包含字母、数字、下划线和连字符
   - 长度：3-128 个字符
   - 示例：`CAM_DK_001`, `CAM_DUIXIN_001`

2. **model** (可选)
   - 相机型号
   - 最大长度：100 个字符
   - 示例：`Model-A1`, `Model-X2`

3. **platform_code** (可选)
   - 平台代码（小写）
   - 可用的平台：
     - `dk` - DK 平台
     - `duixin` - 兑心平台
   - 注意：必须使用小写！

4. **status** (可选，默认：active)
   - 相机状态
   - 可选值：
     - `active` - 激活状态
     - `disabled` - 禁用状态

### 正确的CSV示例

```csv
camera_id,model,platform_code,status
CAM_DK_001,Model-A1,dk,active
CAM_DK_002,Model-A2,dk,active
CAM_DUIXIN_001,Model-X1,duixin,active
```

### 常见错误

❌ **错误 1：平台代码使用大写**
```csv
CAM_DK_001,Model-A1,DK,active    # 错误！应该是 dk
```

✅ **正确**
```csv
CAM_DK_001,Model-A1,dk,active
```

❌ **错误 2：使用中文列名**
```csv
相機序號,出廠時間,到期時間,相機型號,出貨批次
```

✅ **正确**
```csv
camera_id,model,platform_code,status
```

❌ **错误 3：CSV文件末尾有空行**
- 会导致1条失败记录
- 建议：删除CSV文件末尾的空行

### 导入方法

#### 使用 curl 命令行

```bash
curl -u admin:admin123 \
  -X POST \
  -F "file=@sample_data/cameras_import_correct.csv" \
  http://localhost:8080/api/v1/admin/cameras/import
```

#### 使用 Web UI

1. 访问：http://localhost:8080/dashboard
2. 登录（admin / admin123）
3. 找到导入功能
4. 上传CSV文件

### 导入结果示例

成功导入后，您将收到一个 jobId：

```json
{
    "jobId": "c961f8f8-b0ff-439a-8e7e-c8c4cf2df6f9"
}
```

查看日志确认导入状态：

```
Import job completed: c961f8f8-b0ff-439a-8e7e-c8c4cf2df6f9 - Success: 10, Failed: 1
```

### 验证导入

访问导入的相机：

```bash
curl http://localhost:8080/view?camera_id=CAM_DK_001
curl http://localhost:8080/view?camera_id=CAM_DUIXIN_001
```

### 当前可用的平台

- **dk** - DK 平台（专业直播平台）
- **duixin** - 兑心平台（智能监控平台）

## 文件位置

- ✅ 正确格式的CSV：`sample_data/cameras_import_correct.csv`
- ❌ 旧格式的CSV：`sample_data/cameras_import_sample.csv`（需要更新）

## 服务信息

- 服务地址：http://localhost:8080
- 健康检查：http://localhost:8080/actuator/health
- 登录页面：http://localhost:8080/login
- 默认账号：admin / admin123

## 技术细节

### 已修复的问题

1. ✅ 修复了 ImportService 中的计数bug（successCount和totalRows未更新）
2. ✅ 添加了详细的错误日志记录
3. ✅ 修复了 uploaderUser 必需字段的问题
4. ✅ 改进了错误消息的输出

### 代码更改

- `ImportService.java` - 修复导入统计和错误日志
- `CameraImportController.java` - 添加用户名传递
- 创建了 `cameras_import_correct.csv` - 正确格式的示例文件

---

📝 **注意**：如果需要导入原始的 `cameras_import_sample.csv`，需要先将其转换为正确的格式（更改列名和平台代码）。

