## Context

当前 `SysArticleContent` 通过 `ossId` 一对一关联 `SysOssFile`，文章内容以 Markdown 文件形式存储在 S3。更新内容时直接覆盖 OSS 文件，无版本管理。需要在不破坏现有流程的前提下增加文件上传和版本历史能力。

## Goals / Non-Goals

**Goals:**
- 支持文章内容通过 `MultipartFile` 文件上传（.md 文件）
- 覆盖上传时保留旧文件，写入历史记录表
- 前后端双重校验防止误覆盖
- 支持查看文章内容的版本历史

**Non-Goals:**
- 不做文件内容 diff（版本间对比）
- 不做版本回滚功能（仅查看历史）
- 不修改现有 OSS 模块的文件存储逻辑
- 不修改 `SysOssFile` 表结构（复用 `is_deleted` 字段标记归档）

## Decisions

### 1. 历史表设计：方案 B（当前版本 + 历史分离）

**选择**: `sys_article_content` 保持当前生效版本，新增 `sys_article_content_history` 保存被替换的旧版本。

**替代方案**: 方案 A（统一历史表，当前版本也是历史记录之一）——查询当前版本需要额外过滤，不如方案 B 直观。

**理由**: 方案 B 查询当前版本只需读 `sys_article_content`，无需额外过滤；历史表结构更简洁，每条记录都是"被替换的旧版本"。

### 2. 旧 OSS 文件处理：原封不动保留

**选择**: 覆盖上传时，旧 `SysOssFile` 记录和 S3 文件均不删除、不修改。`sys_article_content_history` 通过 `oss_id` 指向旧记录。

**替代方案**: 给 `sys_oss_file` 加 `status` 字段（active/archived）——需要改 OSS 模块，侵入性大。

**理由**: 旧文件保持原状，通过历史表关联即可追溯。如需清理可后续加定时任务。

### 3. 版本号策略：每篇文章独立自增

**选择**: `sys_article_content_history` 中 `version` 字段由 1 开始，每篇文章独立计数。`sys_article_content` 不存版本号。

**理由**: 当前版本永远是"最新"，版本号是历史维度的概念，放在历史表更合理。

### 4. 上传接口：统一 POST，路径区分

**选择**:
- `POST /project/sysArticleContent/upload` — 首次上传，若已存在内容则拒绝
- `POST /project/sysArticleContent/uploadOverride` — 覆盖上传，必须携带 `confirmOverride=true`

**替代方案**: POST/PUT 区分创建和更新——项目现有接口风格统一使用 POST，保持一致。

**理由**: 与项目现有接口风格一致（现有 add/update 均为 POST），路径名明确表达意图。

### 5. 覆盖确认的后端校验

**选择**: `uploadOverride` 接口校验 `confirmOverride` 参数，未携带或为 false 时返回特定错误码（如 `code: "4090"` 表示需要确认）。前端根据错误码弹窗。

**理由**: 后端始终保留历史（不依赖前端确认），但通过参数校验确保前端已展示确认流程。

## Risks / Trade-offs

- **S3 存储成本增长** — 历史文件永久保留，长期会增加存储开销 → 后续可加定时清理策略
- **大文件上传超时** — MultipartFile 直接上传到后端再转存 S3，大文件可能超时 → 当前场景 .md 文件体积小，暂不处理；后续可考虑前端直传 S3
- **并发覆盖** — 两人同时编辑同一篇文章可能产生冲突 → 当前不做乐观锁，后续可加版本号校验

## Migration Plan

1. 执行 DDL 创建 `sys_article_content_history` 表
2. 部署后端新接口，不影响现有功能
3. 部署前端新组件
4. 现有数据无需迁移（历史表从新上传开始记录）
