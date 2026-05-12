## 1. 活跃配置缓存

- [x] 1.1 `OssConfigService` 接口新增 `SysOssConfig getActiveConfig()` 方法
- [x] 1.2 `OssConfigServiceImpl` 实现 `getActiveConfig()`，使用 `ConcurrentHashMap` 缓存活跃配置，缓存 key 固定为 `"active"`，config 变更时（`create`/`update`/`delete`）清除缓存
- [x] 1.3 `OssConfigServiceImpl.listActive()` 移除第 54 行 `if (configs.size()>1) throw` 限制，改为返回第一个活跃配置（或全部返回由调用方决定）

## 2. 移除硬编码配置名

- [x] 2.1 `OssFileServiceImpl.uploadFile(MultipartFile)` 第 55 行：`getByConfigName("minio-local")` 改为 `getActiveConfig()`
- [x] 2.2 `OssFileServiceImpl.uploadFile(Long ossId, ...)` 第 97 行：`getByConfigName("minio-local")` 改为 `getActiveConfig()`
- [x] 2.3 `OssFileServiceImpl.downloadFileContent()` 第 130 行：`getByConfigName("minio-local")` 改为 `getActiveConfig()`

## 3. file_url 存储语义变更为 objectName

- [x] 3.1 `OssFileServiceImpl.uploadFile(MultipartFile)` 第 62-63 行：`url = newFileName`（仅存文件名），不再拼接 endpoint/bucket
- [x] 3.2 `OssFileServiceImpl.uploadFile(Long ossId, ...)` 第 100 行：`url = newFileName`（仅存文件名）
- [x] 3.3 `SysOssFile` 新增 `@TableField(exist = false) private String fullUrl` 字段（含 getter/setter，Lombok @Data 自动生成）

## 4. 查询时拼接完整 URL

- [x] 4.1 新增私有工具方法 `OssFileServiceImpl.buildFullUrl(String fileName)` → 从活跃配置获取 endpoint/bucket，拼接为完整 URL
- [x] 4.2 `OssFileServiceImpl.querySysOssFileListPage()`：查询结果遍历，为每条记录设置 `fullUrl = buildFullUrl(fileUrl)`
- [x] 4.3 `OssFileServiceImpl.queryById()`：查询后设置 `fullUrl = buildFullUrl(entity.getFileUrl())`
- [x] 4.4 `OssController.upload()` 第 49 行：返回值中 `url` 改为使用 `buildFullUrl` 拼接后的完整 URL

## 5. 简化 downloadFileContent

- [x] 5.1 `OssFileServiceImpl.downloadFileContent()`：移除 URL 解析逻辑（第 135-143 行），`fileUrl` 参数直接作为 objectName 传入 `ossClientFactory.downloadFile()`

## 6. 数据库迁移

- [x] 6.1 编写 SQL 迁移脚本：`UPDATE sys_oss_file SET file_url = SUBSTRING_INDEX(file_url, '/', -1)` 将现有完整 URL 提取为文件名
- [x] 6.2 更新 `sys_oss_config.sql` seed data 中的示例数据（如有）

## 7. 验证

- [ ] 7.1 启动后端，验证文件上传功能（新上传的 file_url 仅为文件名）
- [ ] 7.2 验证文件列表接口（返回的 fullUrl 为完整 URL）
- [ ] 7.3 验证博客文章内容读取（`SysArticleContentServiceImpl` 正常下载 .md 文件）
- [ ] 7.4 验证活跃配置切换（禁用 minio，启用新配置后上传/下载正常）
