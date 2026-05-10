# MyBatis-Plus 自动填充审计字段

## 背景

项目中已有 `MyMetaObjectHandler` 实现了 MyBatis-Plus 的 `MetaObjectHandler`，但存在以下问题导致其完全未生效：

1. **无 `@TableField(fill=...)` 注解** — 所有实体类都没有声明哪些字段需要自动填充
2. **字段名不匹配** — Handler 填充 `createUserId`/`createTime`/`updateUserId`/`updateTime`，但实体中使用的是 `createId`/`createDate` 等不同命名
3. **Handler 存在 bug** — `updateFill` 中 `updateUserId` 误用了 `strictInsertFill` 而非 `strictUpdateFill`
4. **用户 ID 硬编码** — Handler 中写死了 `123456L`，未接入 `SecurityUtils` 获取当前登录用户
5. **无公共基类** — 各实体无共享的审计字段基类，字段定义分散且不一致

## 目标

为 OSS 模块和 Blog 模块的实体启用 MyBatis-Plus 自动填充，统一审计字段命名，建立可复用的 `BaseEntity` 基类。

## 范围

### 涉及模块

- `myproject-common` — 新建 `BaseEntity`，修改 `MyMetaObjectHandler`
- `myproject-oss` — `SysOssFile`、`SysOssConfig` 继承 BaseEntity
- `myproject-blog` — `SysArticle`、`SysArticleContent`、`SysArticleContentHistory`、`SysTag`、`SysCategory` 继承 BaseEntity

### 不涉及

- `myproject-system`（`SysUser` 等后续单独处理）
- 前端改动
- 数据库 DDL 变更（需手动执行 ALTER TABLE 添加列）

## 方案

### 1. 新建 BaseEntity

在 `myproject-common` 中创建 `com.example.domain.BaseEntity`：

```
BaseEntity (myproject-common)
├─ createBy    Long           @TableField(fill=INSERT)
├─ createTime  LocalDateTime  @TableField(fill=INSERT)
├─ updateBy    Long           @TableField(fill=INSERT_UPDATE)
└─ updateTime  LocalDateTime  @TableField(fill=INSERT_UPDATE)
```

使用 `@Data` + `@MappedSuperclass` 注解，所有审计实体继承此类。

### 2. 修复 MyMetaObjectHandler

- 字段名改为 `createBy`/`createTime`/`updateBy`/`updateTime`
- 修复 bug：`strictInsertFill` → `strictUpdateFill`（updateUserId 处）
- 用户 ID 改为 `SecurityUtils.getUserId()` 获取当前登录用户
- `createBy`/`updateBy` 类型改为 `Long`

### 3. 实体类改造

| 实体 | 改动 |
|------|------|
| `SysOssFile` | 继承 BaseEntity |
| `SysOssConfig` | 继承 BaseEntity，删除旧的 `createdAt`/`updatedAt` 字段 |
| `SysArticle` | 继承 BaseEntity |
| `SysArticleContent` | 继承 BaseEntity |
| `SysArticleContentHistory` | 继承 BaseEntity，保留 `replacedBy`/`replacedAt` 业务字段 |
| `SysTag` | 继承 BaseEntity，修复 `idDeleted` → `isDeleted` 拼写 |
| `SysCategory` | 继承 BaseEntity |

### 4. 数据库 DDL

需要手动为以下表添加审计列（`create_by`、`create_time`、`update_by`、`update_time`）：

- `sys_oss_file`
- `sys_oss_config`（已有 `created_at`/`updated_at`，需迁移或新增）
- `sys_article`
- `sys_article_content`
- `sys_article_content_history`
- `sys_tag`
- `sys_category`

## 风险

- `SysOssConfig` 已有 `createdAt`/`updatedAt` 字段依赖 MySQL DEFAULT，切换到应用层填充需要确认无副作用
- 数据库需要 ALTER TABLE，需在部署前执行 DDL
- `SecurityUtils.getUserId()` 在无登录上下文（如定时任务、系统内部调用）时可能返回 null，Handler 需做空值保护
