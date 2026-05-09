## 1. 数据库

- [x] 1.1 创建 `sys_article_content_history` 表 DDL（id, article_id, oss_id, version, replaced_by, replaced_at, remark）

## 2. 后端 - 历史记录实体与数据层

- [x] 2.1 在 myproject-blog 模块创建 `SysArticleContentHistory` 实体类（domain/pojo）
- [x] 2.2 创建 `SysArticleContentHistoryMapper` 接口
- [x] 2.3 创建 `SysArticleContentHistoryService` 接口和 `SysArticleContentHistoryServiceImpl` 实现

## 3. 后端 - 上传接口

- [x] 3.1 在 `SysArticleContentService` 新增 `uploadContent(MultipartFile file, Long articleId)` 方法（首次上传，已存在则拒绝返回 4090）
- [x] 3.2 在 `SysArticleContentService` 新增 `overrideUploadContent(MultipartFile file, Long articleId, boolean confirmOverride)` 方法（覆盖上传，写入历史、上传新文件、更新关联）
- [x] 3.3 在 `SysArticleContentController` 新增 `POST /upload` 接口（首次上传）
- [x] 3.4 在 `SysArticleContentController` 新增 `POST /uploadOverride` 接口（覆盖上传，校验 confirmOverride）

## 4. 后端 - 历史查询接口

- [x] 4.1 在 `SysArticleContentController` 新增 `GET /history/{articleId}` 接口
- [x] 4.2 实现版本历史查询逻辑（按 version 降序，关联 OSS 文件信息）

## 5. 前端 - API 层

- [x] 5.1 在 `src/api/blog/sysArticleContentApi.ts` 新增上传接口（uploadContent → POST /upload，uploadOverride → POST /uploadOverride）
- [x] 5.2 新增版本历史查询接口（getContentHistory）

## 6. 前端 - 上传组件与确认弹窗

- [x] 6.1 创建文章内容上传组件（文件选择、类型校验、上传进度）
- [x] 6.2 实现覆盖确认弹窗逻辑（捕获 4090 错误码，弹窗确认后携带 confirmOverride 重试）
- [x] 6.3 在文章管理页面集成"上传内容"按钮

## 7. 前端 - 版本历史展示

- [x] 7.1 创建版本历史列表组件（版本号、替换时间、文件名）
- [x] 7.2 在文章管理页面集成"版本历史"按钮和弹窗

## 8. 集成测试

- [ ] 8.1 测试首次上传流程（无内容 → 上传成功）
- [ ] 8.2 测试覆盖上传流程（有内容 → 4090 → 确认 → 覆盖成功，历史记录正确）
- [ ] 8.3 测试版本历史查询（多版本数据验证）
