# Tasks: 增强博客展示功能

## 任务概览

- 总任务数: 8
- 预计总时长: ~2 小时

---

## Phase 1: 后端改动 (45min)

### Task 1.1: 更新 PublicArticleVo
- [x] 添加 `categories` 字段（List<CategoryItem>）
- [x] 添加 `tags` 字段（List<TagItem>）
- [x] 添加 CategoryItem 和 TagItem 内部类
- **预计时长**: 10min
- **文件**: `myproject-blog/.../domain/vo/PublicArticleVo.java`

### Task 1.2: 更新 SysArticleService 接口
- [x] 添加 `buildPublicArticleVoList` 方法声明
- **预计时长**: 5min
- **文件**: `myproject-blog/.../service/SysArticleService.java`

### Task 1.3: 更新 SysArticleServiceImpl 实现
- [x] 实现 `buildPublicArticleVoList` 方法
- [x] 批量查询分类关联（sys_article_category_rel）
- [x] 批量查询标签关联（sys_article_tag_rel）
- [x] 批量查询分类名称（sys_category）
- [x] 批量查询标签名称（sys_tag）
- [x] 修改 `queryPublicArticleList` 调用新方法
- **预计时长**: 30min
- **文件**: `myproject-blog/.../service/impl/SysArticleServiceImpl.java`

---

## Phase 2: 前端改动 (1h 15min)

### Task 2.1: 更新 publicApi.ts 类型定义
- [x] 更新 `PublicArticleVo` 接口，添加 categories 和 tags 字段
- **预计时长**: 5min
- **文件**: `src/api/blog/publicApi.ts`

### Task 2.2: 更新首页卡片展示
- [x] 修改卡片模板，添加分类和标签展示区域
- [x] 添加 card-tags 样式
- [x] 调整卡片布局（card-body 包裹 title 和 tags）
- **预计时长**: 20min
- **文件**: `src/views/blog/home/index.vue`

### Task 2.3: 更新详情页布局
- [x] 将单列布局改为双列布局（content + sidebar）
- [x] 添加 detail-body、detail-content、detail-sidebar 结构
- **预计时长**: 15min
- **文件**: `src/views/blog/detail/index.vue`

### Task 2.4: 添加侧边栏分类和标签展示
- [x] 添加分类展示区域
- [x] 添加标签展示区域
- [x] 添加 sidebar-section 样式
- **预计时长**: 10min
- **文件**: `src/views/blog/detail/index.vue`

### Task 2.5: 实现目录功能
- [x] 添加 TocItem 接口定义
- [x] 实现 generateToc 方法（解析 h1-h6 标签）
- [x] 实现 setupObserver 方法（IntersectionObserver 监听滚动）
- [x] 实现 scrollToHeading 方法（点击滚动到对应位置）
- [x] 添加目录列表模板
- [x] 添加 toc-item 样式（包含 active 状态）
- [x] 在 fetchContent 成功后调用 generateToc 和 setupObserver
- **预计时长**: 25min
- **文件**: `src/views/blog/detail/index.vue`

---

## 依赖关系

```
Task 1.1 → 1.2 → 1.3
                    ↓
Task 2.1 → 2.2（依赖后端返回分类标签数据）

Task 2.3 → 2.4 → 2.5（独立链路，可与 2.1/2.2 并行）
```

---

## 验证清单

- [ ] 首页卡片正确显示分类和标签
- [ ] 分类和标签以小标签形式展示
- [ ] 详情页右侧显示分类、标签和目录
- [ ] 目录正确解析 h1-h6 标题
- [ ] 点击目录项滚动到对应位置
- [ ] 滚动时目录高亮当前章节
- [ ] 移动端侧边栏正确换行显示
