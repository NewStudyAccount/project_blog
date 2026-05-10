# 设计文档

## BaseEntity 设计

```java
package com.example.domain;

@Data
@MappedSuperclass
public class BaseEntity {

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

放在 `myproject-common` 模块的 `com.example.domain` 包下，因为 common 模块已被所有其他模块依赖。

## MyMetaObjectHandler 设计

```java
@Override
public void insertFill(MetaObject metaObject) {
    Long userId = SecurityUtils.getUserId();  // 可能为 null
    this.strictInsertFill(metaObject, "createBy", Long.class, userId);
    this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
}

@Override
public void updateFill(MetaObject metaObject) {
    Long userId = SecurityUtils.getUserId();
    this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
    this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
}
```

注意：`strictInsertFill`/`strictUpdateFill` 只在字段值为 null 时才填充，不会覆盖业务层手动设置的值。

## SysOssConfig 特殊处理

`SysOssConfig` 已有 `createdAt`/`updatedAt` 字段，对应数据库列 `created_at`/`updated_at`。两种处理方式：

- **方案 A**：删除旧字段，使用 BaseEntity 的统一字段（需改数据库列名）
- **方案 B**：保留旧字段，不继承 BaseEntity（不统一）

采用方案 A，保持一致性。需要 DDL 中做列重命名或新增/删除。

## SysTag 拼写修复

`SysTag` 中 `idDeleted` 字段名拼写错误，应为 `isDeleted`。同时加上 `@TableLogic` 注解启用逻辑删除。
