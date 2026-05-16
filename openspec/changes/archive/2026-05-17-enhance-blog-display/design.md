# Design: 增强博客展示功能

## 架构概览

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              数据流                                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   后端                                                                    │
│   ┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐ │
│   │ sys_article     │      │ sys_article_    │      │ sys_category    │ │
│   │                 │ ───▶ │ category_rel    │ ───▶ │                 │ │
│   │ id, title, cover│      │ article_id,     │      │ id, name        │ │
│   └─────────────────┘      │ category_id     │      └─────────────────┘ │
│                            └─────────────────┘                          │
│                            ┌─────────────────┐      ┌─────────────────┐ │
│                            │ sys_article_    │      │ sys_tag         │ │
│                            │ tag_rel         │ ───▶ │                 │ │
│                            │ article_id,     │      │ id, name        │ │
│                            │ tag_id          │      └─────────────────┘ │
│                            └─────────────────┘                          │
│                                                                         │
│   前端                                                                    │
│   ┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐ │
│   │ PublicArticleVo │ ───▶ │ home/index.vue  │ ───▶ │ 卡片展示        │ │
│   │ + categories    │      │                 │      │ [分类] [标签]    │ │
│   │ + tags          │      └─────────────────┘      └─────────────────┘ │
│   └─────────────────┘                                                   │
│   ┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐ │
│   │ HTML Content    │ ───▶ │ detail/index.vue│ ───▶ │ 内容 + 目录     │ │
│   │                 │      │                 │      │ ┌─────┬───────┐ │ │
│   └─────────────────┘      │ 解析 h1-h6      │      │ │内容 │ 目录  │ │ │
│                            │ 生成目录树       │      │ │     │       │ │ │
│                            └─────────────────┘      │ └─────┴───────┘ │ │
│                                                     └─────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
```

## 数据模型

### PublicArticleVo（更新）

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

### 前端类型定义（更新）

```typescript
// publicApi.ts
export interface PublicArticleVo {
  id: string
  title: string
  cover: string
  categories: { id: number; name: string }[]  // 新增
  tags: { id: number; name: string }[]         // 新增
}
```

## 接口设计

### 公开文章列表（更新响应）

```
POST /project/sysArticle/public/list

Response:
{
  "code": "200",
  "data": {
    "rows": [
      {
        "id": "1234567890",
        "title": "文章标题",
        "cover": "https://xxx/cover.jpg",
        "categories": [
          { "id": 1, "name": "技术" }
        ],
        "tags": [
          { "id": 1, "name": "Java" },
          { "id": 2, "name": "Spring" }
        ]
      }
    ],
    "total": 50
  }
}
```

## 前端组件设计

### home/index.vue 卡片更新

```vue
<template>
  <div class="article-card" @click="goDetail(article.id)">
    <div class="card-cover">
      <img :src="article.cover" :alt="article.title" />
    </div>
    <div class="card-body">
      <div class="card-title">{{ article.title }}</div>
      <div class="card-tags">
        <el-tag v-for="cat in article.categories" :key="cat.id" size="small">
          {{ cat.name }}
        </el-tag>
        <el-tag v-for="tag in article.tags" :key="tag.id" size="small" type="info">
          {{ tag.name }}
        </el-tag>
      </div>
    </div>
  </div>
</template>
```

**样式：**
```scss
.card-body {
  padding: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;

  .el-tag {
    --el-tag-font-size: 11px;
  }
}
```

### detail/index.vue 目录功能

**布局结构：**
```vue
<template>
  <div class="blog-detail">
    <div class="detail-header">
      <!-- 标题、分类、标签、阅读次数 -->
    </div>

    <div class="detail-body">
      <div class="detail-content">
        <div class="markdown-body" v-html="htmlContent"></div>
      </div>
      <div class="detail-sidebar">
        <!-- 分类 -->
        <aside v-if="article.categories?.length" class="sidebar-section">
          <div class="section-title">分类</div>
          <div class="tag-list">
            <el-tag v-for="cat in article.categories" :key="cat.id" size="small">
              {{ cat.name }}
            </el-tag>
          </div>
        </aside>

        <!-- 标签 -->
        <aside v-if="article.tags?.length" class="sidebar-section">
          <div class="section-title">标签</div>
          <div class="tag-list">
            <el-tag v-for="tag in article.tags" :key="tag.id" size="small" type="info">
              {{ tag.name }}
            </el-tag>
          </div>
        </aside>

        <!-- 目录 -->
        <aside v-if="tocItems.length" class="sidebar-section">
          <div class="section-title">目录</div>
          <nav class="toc-list">
            <a
              v-for="item in tocItems"
              :key="item.id"
              :class="['toc-item', { active: activeTocId === item.id }]"
              :style="{ paddingLeft: 12 + (item.level - 1) * 16 + 'px' }"
              @click.prevent="scrollToHeading(item.id)"
            >
              {{ item.text }}
            </a>
          </nav>
        </aside>
      </div>
    </div>
  </div>
</template>
```

**目录生成逻辑：**
```typescript
interface TocItem {
  id: string
  text: string
  level: number
}

const tocItems = ref<TocItem[]>([])
const activeTocId = ref('')

const generateToc = () => {
  const container = document.querySelector('.markdown-body')
  if (!container) return

  const headings = container.querySelectorAll('h1, h2, h3, h4, h5, h6')
  const items: TocItem[] = []

  headings.forEach((heading, index) => {
    const id = `heading-${index}`
    heading.id = id
    items.push({
      id,
      text: heading.textContent || '',
      level: parseInt(heading.tagName[1], 10),
    })
  })

  tocItems.value = items
  if (items.length > 0) {
    activeTocId.value = items[0].id
  }
}
```

**滚动监听逻辑：**
```typescript
let observer: IntersectionObserver | null = null

const setupObserver = () => {
  if (observer) observer.disconnect()

  observer = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          activeTocId.value = entry.target.id
        }
      }
    },
    {
      rootMargin: '-80px 0px -60% 0px',
      threshold: 0,
    }
  )

  const container = document.querySelector('.markdown-body')
  if (!container) return

  const headings = container.querySelectorAll('h1, h2, h3, h4, h5, h6')
  headings.forEach((heading) => {
    if (heading.id) observer!.observe(heading)
  })
}

const scrollToHeading = (id: string) => {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}
```

**侧边栏样式：**
```scss
.detail-body {
  display: flex;
  gap: 24px;
}

.detail-content {
  flex: 1;
  min-width: 0;
}

.detail-sidebar {
  width: 240px;
  flex-shrink: 0;
  position: sticky;
  top: 84px;
  align-self: flex-start;
  max-height: calc(100vh - 108px);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sidebar-section {
  background: #fff;
  border-radius: 8px;
  padding: 16px 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  .section-title {
    padding: 0 16px 12px;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 8px;
  }

  .tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    padding: 0 16px;
  }
}

.toc-list {
  display: flex;
  flex-direction: column;
}

.toc-item {
  display: block;
  padding: 6px 16px;
  font-size: 13px;
  line-height: 1.5;
  color: #666;
  text-decoration: none;
  cursor: pointer;
  border-left: 2px solid transparent;
  transition: color 0.2s, border-color 0.2s, background 0.2s;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  &:hover {
    color: #409eff;
    background: #f5f7fa;
  }

  &.active {
    color: #409eff;
    border-left-color: #409eff;
    background: #ecf5ff;
    font-weight: 500;
  }
}
```

## 后端实现

### Service 层改动

**复用现有逻辑：**
- `SysArticleServiceImpl.buildArticleVoList()` 已有完整的关联查询逻辑
- 新增 `buildPublicArticleVoList()` 方法，复用关联查询但只返回必要字段

```java
private List<PublicArticleVo> buildPublicArticleVoList(List<SysArticle> articles) {
    if (CollectionUtils.isEmpty(articles)) {
        return Collections.emptyList();
    }

    List<Long> articleIds = articles.stream()
            .map(SysArticle::getId)
            .collect(Collectors.toList());

    // 批量查询关系（复用现有逻辑）
    List<SysArticleTagRel> tagRels = articleTagRelMapper.selectList(
            new LambdaQueryWrapper<SysArticleTagRel>()
                    .in(SysArticleTagRel::getArticleId, articleIds)
    );
    List<SysArticleCategoryRel> catRels = articleCategoryRelMapper.selectList(
            new LambdaQueryWrapper<SysArticleCategoryRel>()
                    .in(SysArticleCategoryRel::getArticleId, articleIds)
    );

    // 批量查询名称
    Set<Long> tagIds = tagRels.stream()
            .map(SysArticleTagRel::getTagId)
            .collect(Collectors.toSet());
    Set<Long> catIds = catRels.stream()
            .map(SysArticleCategoryRel::getCategoryId)
            .collect(Collectors.toSet());

    Map<Long, String> tagNameMap = CollectionUtils.isEmpty(tagIds) ? Collections.emptyMap() :
            tagMapper.selectBatchIds(tagIds).stream()
                    .collect(Collectors.toMap(SysTag::getId, SysTag::getName));
    Map<Long, String> catNameMap = CollectionUtils.isEmpty(catIds) ? Collections.emptyMap() :
            categoryMapper.selectBatchIds(catIds).stream()
                    .collect(Collectors.toMap(SysCategory::getId, SysCategory::getName));

    // 按 articleId 分组
    Map<Long, List<SysArticleTagRel>> tagRelMap = tagRels.stream()
            .collect(Collectors.groupingBy(SysArticleTagRel::getArticleId));
    Map<Long, List<SysArticleCategoryRel>> catRelMap = catRels.stream()
            .collect(Collectors.groupingBy(SysArticleCategoryRel::getArticleId));

    // 构建 VO
    return articles.stream().map(article -> {
        PublicArticleVo vo = new PublicArticleVo();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setCover(article.getCover());

        // 设置标签
        List<PublicArticleVo.TagItem> tags = tagRelMap
                .getOrDefault(article.getId(), Collections.emptyList())
                .stream().map(rel -> {
                    PublicArticleVo.TagItem item = new PublicArticleVo.TagItem();
                    item.setId(rel.getTagId());
                    item.setName(tagNameMap.get(rel.getTagId()));
                    return item;
                }).collect(Collectors.toList());
        vo.setTags(tags);

        // 设置分类
        List<PublicArticleVo.CategoryItem> categories = catRelMap
                .getOrDefault(article.getId(), Collections.emptyList())
                .stream().map(rel -> {
                    PublicArticleVo.CategoryItem item = new PublicArticleVo.CategoryItem();
                    item.setId(rel.getCategoryId());
                    item.setName(catNameMap.get(rel.getCategoryId()));
                    return item;
                }).collect(Collectors.toList());
        vo.setCategories(categories);

        return vo;
    }).collect(Collectors.toList());
}
```

## 响应式设计

### 移动端适配

```scss
@media (max-width: 768px) {
  .detail-body {
    flex-direction: column;
  }

  .detail-sidebar {
    width: 100%;
    position: static;
    max-height: none;
    order: -1; // 侧边栏移到内容上方
  }
}
```

## 依赖关系

```
后端 PublicArticleVo 改动
    ↓
前端 publicApi.ts 类型更新
    ↓
前端 home/index.vue 卡片展示（依赖类型更新）

前端 detail/index.vue 目录功能（独立，可并行开发）
```
