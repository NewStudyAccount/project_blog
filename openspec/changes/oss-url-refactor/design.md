## Context

`sys_oss_file.file_url` 当前存储完整 URL（`endpoint/bucket/fileName`）。`OssFileServiceImpl` 在上传时拼接完整 URL 存入 DB，下载时再从 URL 中解析出 objectName。这种设计导致存储迁移时必须批量更新数据库。

此外，`OssFileServiceImpl` 硬编码了 `"minio-local"` 配置名（第 55、97 行），`OssConfigServiceImpl.listActive()` 限制只能有一个活跃配置（第 54 行），进一步增加了切换存储的难度。

本次改造将 `file_url` 的存储语义从"完整 URL"变更为"仅 objectName"，URL 拼接移到查询时由后端完成。

## Goals / Non-Goals

**Goals:**
- `file_url` 仅存储 objectName（文件名），如 `uuid.jpg`
- 后端查询时拼接完整 URL 返回前端，前端零改动
- 活跃配置带内存缓存，避免每次请求查 DB
- 移除硬编码 `"minio-local"`，改用活跃配置
- 移除 `listActive()` 的多配置异常限制
- `downloadFileContent()` 直接接收 objectName，不再解析 URL
- 提供数据迁移 SQL 将现有完整 URL 提取为文件名

**Non-Goals:**
- 不实现多存储并行写入（本次只切换单一活跃配置）
- 不实现文件跨存储自动迁移工具
- 不改变前端 API 调用方式
- 不改变 `OssClientFactory` 接口

## Decisions

### 1. file_url 仅存 objectName

`file_url` 字段存储语义变更为 objectName（UUID 文件名），如 `0b52a287-e29b-41d4-a716-446655440000.jpg`。

**理由：** objectName 是存储系统无关的标识符，切换存储时无需变更。完整 URL 在运行时按活跃配置拼接。

**替代方案考虑：**
- 新增 `object_name` 列，`file_url` 继续存完整 URL → 需要维护两个字段的一致性，且迁移时仍需更新 `file_url`
- `file_url` 改为相对路径 `bucket/fileName` → 仍然绑定了 bucket 名称

### 2. 活跃配置使用 ConcurrentHashMap 缓存

`OssConfigService` 新增 `getActiveConfig()` 方法，返回唯一的活跃配置。内部使用 `ConcurrentHashMap` 缓存，config 变更时清除缓存。

**理由：** 避免每次文件操作都查 DB。使用 `ConcurrentHashMap` 而非 Caffeine，因为这里只需要简单的缓存失效，不需要过期策略。

**替代方案考虑：**
- Caffeine 缓存 → 多了一个依赖，对于单配置场景过于复杂
- 每次查 DB → 每次文件操作多一次 DB 查询，浪费资源

### 3. URL 拼接在 Controller 层完成

`OssController.list()` 和 `getById()` 返回 `SysOssFile` 前，将 `fileUrl`（objectName）拼接为完整 URL。

**理由：** Controller 是 API 响应的出口，在此处拼接可保证前端收到的 `fileUrl` 始终是完整 URL。Service 层保持存储语义，不混入展示逻辑。

**替代方案考虑：**
- Service 层拼接 → 混淆了存储和展示职责
- 前端拼接 → 前端需要知道 endpoint/bucket 配置，增加前端复杂度
- `@JsonSerializer` 自动转换 → 依赖注入不便，且 SysOssFile 是纯 POJO

### 4. downloadFileContent 直接使用 objectName

方法签名不变 `downloadFileContent(String fileUrl)`，但参数语义从"完整 URL"变为"objectName"。内部直接用 objectName 构建下载请求，不再解析 URL。

**理由：** 简化逻辑，消除 URL 解析的脆弱性。`SysArticleContentServiceImpl` 调用方式 `sysOssFile.getFileUrl()` 无需变更，因为 `getFileUrl()` 现在返回的就是 objectName。

### 5. SysOssFile 新增 fullUrl 临时字段

新增 `@TableField(exist = false) private String fullUrl` 字段，Controller 拼接后设置此字段返回。`fileUrl` 保持 objectName 不变，`fullUrl` 为完整 URL。

**理由：** 避免修改 `fileUrl` 的值（保持 objectName 语义），同时给前端提供完整 URL。前端可使用 `fullUrl`（优先）或 `fileUrl`。

**替代方案考虑：**
- 直接覆盖 `fileUrl` 为完整 URL → API 响应中 `fileUrl` 语义不一致，调试时容易混淆

## Risks / Trade-offs

- **[Risk]** 数据迁移 SQL 执行后，旧版本代码无法正常工作（`downloadFileContent` 期望完整 URL）→ **Mitigation：** 先部署新代码，再执行迁移 SQL，确保原子切换
- **[Risk]** 前端依赖 `fileUrl` 字段 → **Mitigation：** 新增 `fullUrl` 字段，前端可逐步迁移；Controller 同时填充两个字段
- **[Risk]** 活跃配置缓存与数据库不一致 → **Mitigation：** OssConfig 的增删改操作中清除缓存
- **[Trade-off]** `fullUrl` 是冗余字段，增加了序列化体积 → 可接受，单条记录仅多几十字节
