# 文章分类与标签关联

## 背景

数据库已建好 `sys_article_tag_rel` 和 `sys_article_category_rel` 关系表，`SysArticleReq` DTO 已定义 `tagIds` 和 `categoryId` 字段，但全链路未接通：Controller 用的是 `SysArticle` 而非 `SysArticleReq`，Service 不写关系表，前端无选择器。

## 目标

- 文章支持多分类和多标签（many-to-many）
- 新增/编辑文章时可通过下拉框选择分类和标签
- 查看文档页侧边栏展示文章的分类和标签

## 范围

- `myproject-blog` — 后端 Entity/Mapper/Service/Controller 改造
- `myproject-frontend` — 文章管理页 + 查看文档页

## 方案

### 后端

1. 新建 `SysArticleTagRel` Entity + Mapper
2. 新建 `SysArticleCategoryRel` Entity + Mapper
3. `SysArticleReq.categoryId` → `categoryIds: List<Long>`
4. Controller add/update 改用 `SysArticleReq`
5. Service 新增/更新时写关系表，查询时带出分类和标签
6. 列表和详情接口返回关联的 category 和 tag 信息

### 前端

1. 文章管理页新增分类/标签多选下拉框
2. `sysArticleApi.ts` 接口调整
3. 查看文档页侧边栏上方增加分类和标签展示区
