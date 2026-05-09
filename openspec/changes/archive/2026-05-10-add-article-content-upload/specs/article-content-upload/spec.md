## ADDED Requirements

### Requirement: 文章内容首次文件上传
系统 SHALL 提供文件上传接口，接受 `MultipartFile` 和 `articleId` 参数，将 Markdown 文件上传至 OSS 并创建 `sys_article_content` 记录绑定到指定文章。若该文章已存在内容记录，接口 SHALL 拒绝上传并返回特定错误码。

#### Scenario: 首次上传成功
- **WHEN** 用户调用 `POST /project/sysArticleContent/upload` 上传 .md 文件，且该文章尚无内容记录
- **THEN** 系统将文件上传至 OSS，创建 `sys_article_content` 记录（articleId + 新 ossId），返回成功

#### Scenario: 文章已有内容时拒绝首次上传
- **WHEN** 用户调用 `POST /project/sysArticleContent/upload`，但该文章已有 `sys_article_content` 记录
- **THEN** 系统拒绝上传，返回错误码 `4090`（需确认覆盖），并返回当前版本信息

#### Scenario: 上传文件类型校验
- **WHEN** 用户上传非 .md 文件
- **THEN** 系统拒绝上传，返回文件类型不支持的错误提示

### Requirement: 文章内容覆盖上传
系统 SHALL 提供覆盖上传接口 `POST /project/sysArticleContent/uploadOverride`，接受 `MultipartFile`、`articleId` 和 `confirmOverride` 参数。覆盖上传时 SHALL 将旧版本写入历史记录表，新文件上传至 OSS 并更新 `sys_article_content` 指向新文件。未携带 `confirmOverride=true` 时 SHALL 拒绝请求。

#### Scenario: 覆盖上传成功
- **WHEN** 用户调用 `POST /project/sysArticleContent/uploadOverride`，携带 `confirmOverride=true`，且文章已有内容记录
- **THEN** 系统将旧内容的 ossId 写入 `sys_article_content_history`（版本号自增），上传新文件至 OSS，更新 `sys_article_content` 的 ossId 指向新文件，返回成功

#### Scenario: 未确认覆盖时拒绝
- **WHEN** 用户调用 `POST /project/sysArticleContent/uploadOverride`，未携带 `confirmOverride` 或值不为 `true`
- **THEN** 系统拒绝上传，返回错误码 `4090`

#### Scenario: 文章无内容时覆盖上传
- **WHEN** 用户调用 `POST /project/sysArticleContent/uploadOverride`，但该文章尚无内容记录
- **THEN** 系统拒绝上传，提示应使用首次上传接口

### Requirement: 旧版本文件保留
覆盖上传时，系统 SHALL 不删除旧的 `SysOssFile` 记录和 S3 文件。旧文件通过 `sys_article_content_history` 的 `oss_id` 关联，保持可追溯。

#### Scenario: 覆盖后旧文件仍可访问
- **WHEN** 文章内容被覆盖上传
- **THEN** 旧的 `SysOssFile` 记录在数据库中仍存在，S3 文件仍可下载
