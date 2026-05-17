# OSS Client 缓存失效机制

## 问题

当更新 `SysOssConfig` 配置（如 endpoint、accessKey、secretKey、bucketName 等）后，S3Client 缓存中的旧客户端仍然被使用，导致：
- 文件上传/下载使用旧的连接配置
- 可能连接到错误的 endpoint 或使用过期的密钥
- 需要重启应用才能使新配置生效

## 当前状态

### S3Client 缓存（已实现）
- 使用 Caffeine 缓存，key 为 `configName`
- 缓存策略：maxSize=50, expireAfterAccess=30min, expireAfterWrite=24h
- `S3OssClientFactory` 已有 `evictClient(String configName)` 方法

### 问题点
- `OssClientFactory` 接口未暴露 `evictClient` 方法
- `OssConfigServiceImpl.update()` 更新配置后未清除 S3Client 缓存
- `OssConfigServiceImpl.delete()` 删除配置后未清除 S3Client 缓存

## 方案

### 改动 1: OssClientFactory 接口新增缓存失效方法

```java
public interface OssClientFactory {
    // ... 现有方法 ...
    
    /**
     * 清除指定配置名称的客户端缓存
     */
    void evictClient(String configName);
}
```

### 改动 2: OssConfigServiceImpl.update() 清除缓存

```java
@Override
public SysOssConfig update(SysOssConfig sysOssConfig) {
    SysOssConfig existing = getById(sysOssConfig.getId());
    // ... 原有校验逻辑 ...
    
    baseMapper.updateById(sysOssConfig);
    
    // 清除旧的客户端缓存
    ossClientFactory.evictClient(existing.getConfigName());
    
    // 如果修改了 configName，也要清除新名称的缓存（防止残留）
    if (!existing.getConfigName().equals(sysOssConfig.getConfigName())) {
        ossClientFactory.evictClient(sysOssConfig.getConfigName());
    }
    
    log.info("更新OSS配置成功: id={}", sysOssConfig.getId());
    return getById(sysOssConfig.getId());
}
```

### 改动 3: OssConfigServiceImpl.delete() 清除缓存

```java
@Override
public boolean delete(Long id) {
    SysOssConfig existing = getById(id);
    // ... 原有校验逻辑 ...
    
    boolean result = removeById(id);
    if (result) {
        ossClientFactory.evictClient(existing.getConfigName());
        log.info("删除OSS配置成功: id={}", id);
    }
    return result;
}
```

## 影响范围

- `OssClientFactory` 接口
- `S3OssClientFactory` 实现类（已有 evictClient，无需改动）
- `OssConfigServiceImpl` 服务类

## 验证方式

1. 更新 OSS 配置后，观察日志是否打印 "S3客户端缓存已失效"
2. 使用新配置上传文件，验证连接到正确的 endpoint
3. 删除配置后，确认缓存被清除
