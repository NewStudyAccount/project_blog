# OSS 缓存失效机制 - 任务清单

## 任务列表

- [x] 1. OssClientFactory 接口新增 evictClient 方法
  - 文件: `myproject-oss/src/main/java/com/example/oss/factory/OssClientFactory.java`
  - 改动: 添加 `void evictClient(String configName)` 方法声明

- [x] 2. S3OssClientFactory 实现 evictClient 方法
  - 文件: `myproject-oss/src/main/java/com/example/oss/factory/S3OssClientFactory.java`
  - 改动: 确认已有 evictClient 实现，添加 @Override 注解

- [x] 3. OssConfigServiceImpl.update() 清除缓存
  - 文件: `myproject-oss/src/main/java/com/example/oss/service/impl/OssConfigServiceImpl.java`
  - 改动: 在 updateById 后调用 evictClient 清除旧配置缓存

- [x] 4. OssConfigServiceImpl.delete() 清除缓存
  - 文件: `myproject-oss/src/main/java/com/example/oss/service/impl/OssConfigServiceImpl.java`
  - 改动: 在 removeById 后调用 evictClient 清除缓存

- [ ] 5. 验证测试
  - 操作: 启动应用，更新 OSS 配置后上传文件
  - 预期: 日志显示 "S3客户端缓存已失效"，文件使用新配置上传
