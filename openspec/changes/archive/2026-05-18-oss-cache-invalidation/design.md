# OSS 缓存失效机制 - 设计文档

## 架构概览

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              OSS 模块架构                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   OssConfigController                                                       │
│         │                                                                   │
│         ▼                                                                   │
│   OssConfigService (接口)                                                   │
│         │                                                                   │
│         ▼                                                                   │
│   OssConfigServiceImpl                                                      │
│         │                                                                   │
│         ├──► OssClientFactory (接口) ──► S3OssClientFactory (实现)          │
│         │         │                                                         │
│         │         └──► Cache<String, S3Client> (Caffeine)                   │
│         │                   │                                               │
│         │                   └── key: configName                            │
│         │                   └── value: S3Client                            │
│         │                                                                   │
│         └──► SysOssConfigMapper (数据库)                                    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 数据流

### 当前问题流程

```
update(sysOssConfig)
    │
    ├── 1. getById() ──────────────────────────────► 查询旧配置
    │
    ├── 2. updateById() ───────────────────────────► 更新数据库 ✅
    │
    └── 3. ??? ────────────────────────────────────► 缓存未清除 ❌

后续上传请求
    │
    └── clientCache.get("minio-local")
            │
            └── 命中旧 S3Client ──────────────────► 使用旧配置连接 ❌
```

### 修复后流程

```
update(sysOssConfig)
    │
    ├── 1. getById() ──────────────────────────────► 查询旧配置
    │
    ├── 2. updateById() ───────────────────────────► 更新数据库 ✅
    │
    ├── 3. evictClient(oldConfigName) ─────────────► 清除旧缓存 ✅
    │
    └── 4. evictClient(newConfigName) ─────────────► 清除新缓存(如改名) ✅

后续上传请求
    │
    └── clientCache.get("minio-local")
            │
            └── 未命中 ──► 创建新 S3Client ───────► 使用新配置连接 ✅
```

## 接口设计

### OssClientFactory 变更

```java
public interface OssClientFactory {
    
    Object createClient(SysOssConfig sysOssConfig);
    
    String getProvider();
    
    void uploadFile(SysOssConfig sysOssConfig, String objectName, String contentType, byte[] data);
    
    byte[] downloadFile(SysOssConfig sysOssConfig, String objectName);
    
    // 新增：清除指定配置的客户端缓存
    void evictClient(String configName);
}
```

## 时序图

### 配置更新流程

```
   Controller          ConfigService         ClientFactory           Cache
       │                     │                     │                   │
       │      update()       │                     │                   │
       │────────────────────►│                     │                   │
       │                     │                     │                   │
       │                     │   getById()         │                   │
       │                     │─────────────────────────────────────────►│
       │                     │◄────────────────────────────────────────│
       │                     │                     │                   │
       │                     │   updateById()      │                   │
       │                     │─────────────────────────────────────────►│
       │                     │                     │                   │
       │                     │  evictClient(old)   │                   │
       │                     │────────────────────►│                   │
       │                     │                     │   invalidate()    │
       │                     │                     │──────────────────►│
       │                     │                     │                   │
       │                     │  evictClient(new)   │                   │
       │                     │────────────────────►│                   │
       │                     │                     │   invalidate()    │
       │                     │                     │──────────────────►│
       │                     │                     │                   │
       │◄────────────────────│                     │                   │
       │      response       │                     │                   │
```

## 缓存键策略

使用 `configName` 作为缓存键的原因：
- 唯一性：数据库层面已保证 configName 唯一
- 稳定性：configName 不会频繁变更
- 可读性：便于日志追踪和调试

## 边界情况处理

| 场景 | 处理方式 |
|------|----------|
| 修改 configName | 清除旧名称和新名称的缓存 |
| 删除配置 | 清除对应 configName 的缓存 |
| 配置不存在 | 抛出异常，不清除缓存 |
| 缓存中无该 key | invalidate() 为空操作，无副作用 |
