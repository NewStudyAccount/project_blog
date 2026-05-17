# OSS URL 生成逻辑重构 - 任务清单

## 任务列表

- [x] 1. OssClientFactory 接口新增 generateUrl 方法
  - 文件: `myproject-oss/src/main/java/com/example/oss/factory/OssClientFactory.java`
  - 改动: 添加 `String generateUrl(SysOssConfig sysOssConfig, String objectName)` 方法声明

- [x] 2. S3OssClientFactory 实现 generateUrl 方法
  - 文件: `myproject-oss/src/main/java/com/example/oss/factory/S3OssClientFactory.java`
  - 改动: 实现 URL 生成逻辑，根据 provider 区分阿里云 (Virtual Hosted) 和其他 (Path Style)

- [x] 3. OssFileServiceImpl.uploadFile(MultipartFile) 使用 generateUrl
  - 文件: `myproject-oss/src/main/java/com/example/oss/service/impl/OssFileServiceImpl.java`
  - 改动: 将 `url = endpoint+"/"+bucketName+"/"+newFileName` 替换为 `ossClientFactory.generateUrl(sysOssConfig, newFileName)`

- [x] 4. OssFileServiceImpl.uploadFile(Long, String, String, byte[]) 使用 generateUrl
  - 文件: `myproject-oss/src/main/java/com/example/oss/service/impl/OssFileServiceImpl.java`
  - 改动: 同样替换 URL 生成逻辑

- [ ] 5. 验证测试
  - 操作: 分别使用阿里云和 MinIO 配置上传文件，检查生成的 URL 是否正确
  - 预期: 阿里云生成 Virtual Hosted Style URL，MinIO 生成 Path Style URL
