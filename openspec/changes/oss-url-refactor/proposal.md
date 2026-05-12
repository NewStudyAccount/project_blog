## Why

当前 `sys_oss_file.file_url` 存储完整的访问 URL（如 `http://192.168.99.100:9000/my-bucket/uuid.jpg`），导致：

1. **迁移成本高** — 从 MinIO 迁移到阿里云 OSS 时，需要批量更新所有 `file_url` 记录
2. **硬编码配置名** — `OssFileServiceImpl` 第 55、97 行写死了 `"minio-local"`，切换存储后上传直接报错
3. **单配置限制** — `OssConfigServiceImpl.listActive()` 第 54 行抛异常阻止多配置共存
4. **URL 解析脆弱** — `downloadFileContent()` 需要从完整 URL 中反向解析 objectName，逻辑复杂且易出错

改为 `file_url` 仅存储文件名（objectName），查询时由后端拼接完整 URL，可实现存储迁移零数据变更。

## What Changes

- `sys_oss_file.file_url` 改为仅存储文件名（如 `uuid.jpg`），不再存储完整 URL
- `OssConfigService` 新增 `getActiveConfig()` 方法，带内存缓存
- `OssFileServiceImpl` 移除硬编码 `"minio-local"`，改用活跃配置
- `OssFileServiceImpl.downloadFileContent()` 简化为直接使用 objectName 下载，不再解析 URL
- `OssController.list()` 和 `getById()` 返回前将 `fileUrl` 拼接为完整 URL
- `OssFileServiceImpl.uploadFile()` 写入时仅存储文件名
- 移除 `listActive()` 的多配置异常限制
- 新增数据迁移 SQL，将现有完整 URL 提取为文件名
- 新增 `SysOssFile.fullUrl` 临时字段用于 API 响应

## Capabilities

### Modified Capabilities

- `oss-file-service`: file_url 存储语义变更为仅文件名，上传/下载/查询逻辑相应调整
- `oss-config-service`: 新增带缓存的 `getActiveConfig()` 方法，移除单配置限制

## Impact

- 影响模块：`myproject-oss`、`myproject-blog`（`SysArticleContentServiceImpl` 使用 `downloadFileContent`）
- 影响已有类：`OssFileServiceImpl`、`OssConfigServiceImpl`、`OssController`、`SysOssFile`
- 前端无改动 — 后端返回的 `fileUrl` 仍为完整 URL
- 数据库：`sys_oss_file` 需执行数据迁移 SQL
