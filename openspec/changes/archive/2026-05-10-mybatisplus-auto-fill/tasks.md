# 任务清单

## 1. 创建 BaseEntity 基类
- [x] 在 `myproject-common` 中新建 `com.example.domain.BaseEntity`
- [x] 添加 `createBy`(Long)、`createTime`(LocalDateTime)、`updateBy`(Long)、`updateTime`(LocalDateTime)
- [x] 添加 `@TableField(fill=...)` 注解
- [x] 使用 `@Data` + `abstract` 基类

## 2. 修复 MyMetaObjectHandler
- [x] 字段名改为 `createBy`/`createTime`/`updateBy`/`updateTime`
- [x] 修复 `updateFill` 中的 bug（`strictInsertFill` → `strictUpdateFill`）
- [x] 用户 ID 改为 `SecurityUtils.getLoginUserId()`
- [x] 添加空值保护（无登录用户时不报错）

## 3. 改造 OSS 模块实体
- [x] `SysOssFile` 继承 BaseEntity
- [x] `SysOssConfig` 继承 BaseEntity，删除旧 `createdAt`/`updatedAt` 字段

## 4. 改造 Blog 模块实体
- [x] `SysArticle` 继承 BaseEntity
- [x] `SysArticleContent` 继承 BaseEntity
- [x] `SysArticleContentHistory` 继承 BaseEntity
- [x] `SysTag` 继承 BaseEntity，修复 `idDeleted` → `isDeleted`
- [x] `SysCategory` 继承 BaseEntity

## 5. 数据库 DDL
- [x] 编写 ALTER TABLE 语句为各表添加审计列
