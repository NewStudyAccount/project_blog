# 任务清单

## 1. 后端关系表 Entity + Mapper
- [x] 新建 `SysArticleTagRel` Entity
- [x] 新建 `SysArticleTagRelMapper`
- [x] 新建 `SysArticleCategoryRel` Entity
- [x] 新建 `SysArticleCategoryRelMapper`

## 2. 后端 DTO + Controller 改造
- [x] `SysArticleReq.categoryId` → `categoryIds: List<Long>`
- [x] Controller add/update 改用 `SysArticleReq`
- [x] 新建 `SysArticleVo`（包含分类和标签信息）
- [x] Controller list/detail 返回 `SysArticleVo`

## 3. 后端 Service 改造
- [x] `addSysArticle` 写入分类和标签关系表
- [x] `updateSysArticleById` 先删后插关系表
- [x] 查询接口返回分类和标签信息（列表+详情）

## 4. 前端 API + 文章管理页
- [x] `sysArticleApi.ts` 接口增加 tagIds/categoryIds
- [x] 文章管理页新增分类多选下拉框
- [x] 文章管理页新增标签多选下拉框

## 5. 前端查看文档页
- [x] 侧边栏上方增加分类展示区
- [x] 侧边栏上方增加标签展示区
