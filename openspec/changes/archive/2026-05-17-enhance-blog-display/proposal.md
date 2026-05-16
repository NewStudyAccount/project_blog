# Proposal: 增强博客展示功能

## 概述

增强博客首页卡片和文章详情页的展示功能：
1. 首页卡片显示分类和标签信息
2. 详情页添加右侧目录导航

## 动机

- 当前首页卡片只显示封面和标题，信息不够丰富
- 详情页缺少目录导航，长文章阅读体验不佳

## 范围

### 改动一：首页卡片增加分类和标签

**当前状态：**
```
┌─────────────────┐
│    封面图片     │
├─────────────────┤
│   文章标题      │
└─────────────────┘
```

**目标状态：**
```
┌─────────────────┐
│    封面图片     │
├─────────────────┤
│   文章标题      │
│ [分类] [标签1] [标签2]  ← 小标签形式
└─────────────────┘
```

**后端改动：**
- `PublicArticleVo.java` — 添加 `categories` 和 `tags` 字段
- `SysArticleServiceImpl.java` — `queryPublicArticleList` 方法增加批量查询分类/标签关联

**前端改动：**
- `publicApi.ts` — 更新 `PublicArticleVo` 类型定义
- `home/index.vue` — 卡片底部展示分类和标签

---

### 改动二：详情页增加目录导航

**当前状态：**
- 已有标题、分类、标签、阅读次数显示
- 无目录导航

**目标状态：**
```
┌──────────────────────┬──────────────┐
│                      │  分类        │
│                      │  [cat1]      │
│     文章内容         │              │
│                      │  标签        │
│                      │  [tag1]      │
│                      │              │
│                      │  目录        │
│                      │  ├─ 第一章   │
│                      │  │  ├─ 1.1   │
│                      │  │  └─ 1.2   │
│                      │  └─ 第二章   │
└──────────────────────┴──────────────┘
```

**前端改动：**
- `detail/index.vue` — 添加右侧目录侧边栏（参考 doc/index.vue）

**复用逻辑：**
- 解析 h1-h6 标签生成目录树
- 点击目录项滚动到对应位置
- IntersectionObserver 监听滚动高亮当前章节

---

## 技术方案

### 后端

**PublicArticleVo 新增字段：**
```java
@Data
public class PublicArticleVo {
    private Long id;
    private String title;
    private String cover;
    private List<CategoryItem> categories;  // 新增
    private List<TagItem> tags;              // 新增

    @Data
    public static class CategoryItem {
        private Long id;
        private String name;
    }

    @Data
    public static class TagItem {
        private Long id;
        private String name;
    }
}
```

**Service 改动：**
- 复用现有的 `buildArticleVoList` 逻辑中的关联查询部分
- 批量查询 `sys_article_category_rel` 和 `sys_article_tag_rel`
- 批量查询 `sys_category` 和 `sys_tag` 获取名称

### 前端

**目录功能实现：**
```typescript
interface TocItem {
  id: string
  text: string
  level: number
}

// 1. 渲染完成后解析 h1-h6 标签
const generateToc = () => {
  const container = document.querySelector('.markdown-body')
  const headings = container.querySelectorAll('h1, h2, h3, h4, h5, h6')
  // 生成目录树...
}

// 2. IntersectionObserver 监听滚动
const setupObserver = () => {
  observer = new IntersectionObserver(callback, options)
  // 监听所有标题元素...
}

// 3. 点击目录项滚动到对应位置
const scrollToHeading = (id: string) => {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' })
}
```

---

## 依赖关系

```
后端 PublicArticleVo 改动
    ↓
前端 publicApi.ts 类型更新
    ↓
前端 home/index.vue 卡片展示

前端 detail/index.vue 目录功能（独立，可并行）
```

---

## 风险

- **低风险**：改动范围明确，复用现有逻辑
- **性能考虑**：列表接口增加关联查询，但使用批量查询优化

---

## 时间估算

| 任务 | 预计时长 |
|------|----------|
| 后端 PublicArticleVo 改动 | 15min |
| 后端 Service 关联查询 | 30min |
| 前端 publicApi 类型更新 | 5min |
| 前端 home 卡片展示 | 20min |
| 前端 detail 目录功能 | 45min |
| **总计** | **~2h** |
