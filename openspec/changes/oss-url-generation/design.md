# OSS URL 生成逻辑重构 - 设计文档

## 架构概览

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              调用流程                                            │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│   OssFileServiceImpl                                                            │
│         │                                                                       │
│         ├── uploadFile()                                                        │
│         │     │                                                                 │
│         │     ├── ossClientFactory.uploadFile(...)   ← 上传文件                │
│         │     └── ossClientFactory.generateUrl(...)   ← 生成 URL (新增)        │
│         │                                                                       │
│         └── downloadFileContent()                                              │
│               │                                                                 │
│               └── 从 URL 解析 objectName                                       │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## 接口设计

### OssClientFactory 新增方法

```java
/**
 * 生成文件访问 URL
 *
 * @param sysOssConfig OSS配置
 * @param objectName   对象名称
 * @return 文件访问 URL
 */
String generateUrl(SysOssConfig sysOssConfig, String objectName);
```

### S3OssClientFactory 实现

```java
@Override
public String generateUrl(SysOssConfig sysOssConfig, String objectName) {
    String provider = sysOssConfig.getProvider();
    String endpoint = sysOssConfig.getEndpoint();
    String bucketName = sysOssConfig.getBucketName();

    if ("aliyun".equals(provider)) {
        // Virtual Hosted Style: https://bucket.endpoint/objectName
        URI uri = URI.create(endpoint);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        return scheme + "://" + bucketName + "." + host + "/" + objectName;
    } else {
        // Path Style: endpoint/bucket/objectName
        return endpoint + "/" + bucketName + "/" + objectName;
    }
}
```

## URL 格式对比

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              URL 格式对比                                        │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│   阿里云 (Virtual Hosted Style):                                                │
│   ┌─────────────────────────────────────────────────────────────────────┐       │
│   │ https://my-bucket.oss-cn-hangzhou.aliyuncs.com/file.png             │       │
│   │ └──────────── host ─────────────────────────────┘└── path ──┘      │       │
│   └─────────────────────────────────────────────────────────────────────┘       │
│                                                                                 │
│   MinIO (Path Style):                                                           │
│   ┌─────────────────────────────────────────────────────────────────────┐       │
│   │ http://localhost:9000/mybucket/file.png                             │       │
│   │ └── endpoint ──┘└────── path ──────────────────────┘               │       │
│   └─────────────────────────────────────────────────────────────────────┘       │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## 数据流

```
上传文件流程 (修改后):

1. ossConfigService.getByConfigName("aliyun-default")
   └── SysOssConfig { provider: "aliyun", endpoint: "https://oss-cn-hangzhou.aliyuncs.com", bucket: "my-bucket" }

2. ossClientFactory.uploadFile(sysOssConfig, "uuid.png", contentType, data)
   └── S3Client 使用 Virtual Hosted Style 上传 ✅

3. ossClientFactory.generateUrl(sysOssConfig, "uuid.png")
   └── 根据 provider = "aliyun" 生成:
       "https://my-bucket.oss-cn-hangzhou.aliyuncs.com/uuid.png" ✅
```

## 修改点清单

| 文件 | 方法 | 改动 |
|------|------|------|
| OssClientFactory | - | 新增 `generateUrl()` 接口方法 |
| S3OssClientFactory | generateUrl() | 实现 URL 生成逻辑 |
| OssFileServiceImpl | uploadFile(MultipartFile) | 替换 URL 生成 |
| OssFileServiceImpl | uploadFile(Long, String, String, byte[]) | 替换 URL 生成 |
| OssFileServiceImpl | downloadFileContent() | 使用 generateUrl 的逆向解析 |
