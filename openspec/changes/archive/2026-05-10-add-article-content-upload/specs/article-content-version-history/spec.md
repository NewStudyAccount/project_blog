## ADDED Requirements

### Requirement: 文章内容版本历史表
系统 SHALL 维护 `sys_article_content_history` 表，记录文章内容每次被替换的旧版本。每篇文章的版本号 SHALL 从 1 开始独立自增。

#### Scenario: 覆盖时自动写入历史记录
- **WHEN** 文章内容被覆盖上传
- **THEN** 系统在 `sys_article_content_history` 中插入一条记录，包含 article_id、oss_id（旧版本）、version（当前最大版本号+1）、replaced_at（当前时间）

#### Scenario: 首次上传不产生历史记录
- **WHEN** 文章首次上传内容（无已有内容）
- **THEN** 系统不向 `sys_article_content_history` 写入记录

### Requirement: 版本历史查询接口
系统 SHALL 提供接口查询某篇文章的所有内容版本历史，按版本号降序排列。

#### Scenario: 查询版本历史
- **WHEN** 用户调用 `GET /project/sysArticleContent/history/{articleId}`
- **THEN** 系统返回该文章的所有历史版本记录，包含版本号、替换时间、文件信息，按版本号降序排列

#### Scenario: 文章无历史版本
- **WHEN** 用户查询一篇从未覆盖过的文章的版本历史
- **THEN** 系统返回空列表
