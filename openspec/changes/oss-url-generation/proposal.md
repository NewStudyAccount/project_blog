# OSS URL 生成逻辑重构

## 问题

当前 `OssFileServiceImpl` 中 URL 生成逻辑硬编码为 Path Style 格式：

```java
url = endpoint + "/" + bucketName + "/" + fileName;
```

这对 MinIO 有效，但对阿里云 OSS 生成的 URL 是错误的：

| Provider | 期望 URL | 实际生成 |
|----------|----------|----------|
| MinIO | `http://localhost:9000/mybucket/file.png` | `http://localhost:9000/mybucket/file.png` ✅ |
| 阿里云 | `https://my-bucket.oss-cn-hangzhou.aliyuncs.com/file.png` | `https://oss-cn-hangzhou.aliyuncs.com/my-bucket/file.png` ❌ |

## 方案

在 `OssClientFactory` 接口新增 `generateUrl()` 方法，由实现类根据 `provider` 字段生成正确的 URL。

### 阿里云 OSS URL 格式 (Virtual Hosted Style)
```
https://{bucket}.{endpoint_host}/{objectName}
```

### MinIO / AWS URL 格式 (Path Style)
```
{endpoint}/{bucket}/{objectName}
```

## 影响范围

- `OssClientFactory` 接口
- `S3OssClientFactory` 实现类
- `OssFileServiceImpl` 服务类（3 处 URL 生成）
