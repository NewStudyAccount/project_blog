# Design: 新增博客首页

## 架构概览

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              前端架构                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  /blog                          BlogLayout.vue                          │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │  ┌─────────────────────────────────────────────────────────────┐  │  │
│  │  │                         Header                              │  │  │
│  │  │  [博客名称]              [首页] [分类▾] [标签▾]              │  │  │
│  │  └─────────────────────────────────────────────────────────────┘  │  │
│  │                                                                   │  │
│  │  ┌─────────────────────────────────────────────────────────────┐  │  │
│  │  │                      <router-view />                        │  │  │
│  │  │                                                             │  │  │
│  │  │   /blog/home              /blog/detail/:id                  │  │  │
│  │  │   ┌─────┐ ┌─────┐        ┌─────────────────────────┐       │  │  │
│  │  │   │card │ │card │        │      文章详情            │       │  │  │
│  │  │   └─────┘ └─────┘        │                         │       │  │  │
│  │  │   ┌─────┐ ┌─────┐        └─────────────────────────┘       │  │  │
│  │  │   │card │ │card │                                           │  │  │
│  │  │   └─────┘ └─────┘                                           │  │  │
│  │  └─────────────────────────────────────────────────────────────┘  │  │
│  │                                                                   │  │
│  │  ┌─────────────────────────────────────────────────────────────┐  │  │
│  │  │                         Footer                              │  │  │
│  │  │        [© 版权]  [GitHub]  [备案号]                         │  │  │
│  │  └─────────────────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## 数据模型

### sys_config 表

```sql
CREATE TABLE sys_config (
  id            BIGINT PRIMARY KEY COMMENT '主键ID',
  config_key    VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
  config_value  VARCHAR(500) DEFAULT '' COMMENT '配置值',
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted    TINYINT DEFAULT 0 COMMENT '删除标志(0正常 1删除)'
) COMMENT '站点配置表';
```

**初始数据：**

```sql
INSERT INTO sys_config (id, config_key, config_value) VALUES
(1, 'site_name', '我的博客'),
(2, 'copyright', '© 2026 xxx'),
(3, 'github', 'https://github.com/xxx'),
(4, 'beian', '京ICP备xxxxx号');
```

### 数据实体

```java
@TableName("sys_config")
public class SysConfig {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String configKey;
    private String configValue;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}
```

## 接口设计

### 1. 公开文章列表

```
POST /project/sysArticle/public/list

Request Body:
{
  "pageNum": 1,        // 页码
  "pageSize": 10,      // 每页数量
  "categoryId": null,  // 可选：分类ID
  "tagId": null        // 可选：标签ID
}

Response:
{
  "code": "200",
  "msg": "操作成功",
  "data": {
    "rows": [
      {
        "id": "1234567890",
        "title": "文章标题",
        "cover": "https://xxx/cover.jpg"
      }
    ],
    "total": 50
  }
}
```

**SQL 逻辑：**
- 基础查询：`SELECT id, title, cover FROM sys_article WHERE is_deleted = 0`
- 按分类筛选：`JOIN sys_article_category ON ... WHERE category_id = ?`
- 按标签筛选：`JOIN sys_article_tag ON ... WHERE tag_id = ?`
- 排序：`ORDER BY create_time DESC`

### 2. 公开分类列表

```
GET /project/sysCategory/public/list

Response:
{
  "code": "200",
  "data": [
    { "id": 1, "name": "技术" },
    { "id": 2, "name": "生活" }
  ]
}
```

### 3. 公开标签列表

```
GET /project/sysTag/public/list

Response:
{
  "code": "200",
  "data": [
    { "id": 1, "name": "Java" },
    { "id": 2, "name": "Vue" }
  ]
}
```

### 4. 站点配置

```
GET /project/sysConfig/public/info

Response:
{
  "code": "200",
  "data": {
    "siteName": "我的博客",
    "copyright": "© 2026 xxx",
    "github": "https://github.com/xxx",
    "beian": "京ICP备xxxxx号"
  }
}
```

## 前端组件设计

### BlogLayout.vue

```vue
<template>
  <div class="blog-layout">
    <header class="blog-header">
      <div class="header-content">
        <router-link to="/blog/home" class="site-name">
          {{ siteName }}
        </router-link>
        <nav class="header-nav">
          <router-link to="/blog/home">首页</router-link>
          <el-dropdown @command="handleCategory">
            <span>分类 <el-icon><ArrowDown /></el-icon></span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="cat in categories" :key="cat.id" :command="cat.id">
                  {{ cat.name }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown @command="handleTag">
            <span>标签 <el-icon><ArrowDown /></el-icon></span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="tag in tags" :key="tag.id" :command="tag.id">
                  {{ tag.name }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </nav>
      </div>
    </header>

    <main class="blog-main">
      <router-view />
    </main>

    <footer class="blog-footer">
      <div class="footer-content">
        <span>{{ config.copyright }}</span>
        <a v-if="config.github" :href="config.github" target="_blank">GitHub</a>
        <span v-if="config.beian">{{ config.beian }}</span>
      </div>
    </footer>
  </div>
</template>
```

### home/index.vue

```vue
<template>
  <div class="blog-home">
    <div class="article-grid">
      <div v-for="article in articles" :key="article.id" class="article-card" @click="goDetail(article.id)">
        <div class="card-cover">
          <img :src="article.cover" :alt="article.title" />
        </div>
        <div class="card-title">{{ article.title }}</div>
      </div>
    </div>
    <div v-if="loading" class="loading-text">加载中...</div>
    <div v-if="noMore" class="no-more-text">没有更多了</div>
  </div>
</template>
```

**滚动加载逻辑：**

```typescript
const handleScroll = () => {
  const scrollTop = document.documentElement.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = document.documentElement.clientHeight

  if (scrollTop + clientHeight >= scrollHeight - 50) {
    loadMore()
  }
}

const loadMore = async () => {
  if (loading.value || noMore.value) return
  loading.value = true
  pageNum.value++
  await fetchArticles()
  loading.value = false
}
```

### detail/index.vue

```vue
<template>
  <div class="blog-detail">
    <div class="detail-header">
      <el-button @click="goBack">返回首页</el-button>
      <h1>{{ article.title }}</h1>
    </div>
    <div class="detail-meta">
      <span>阅读次数: {{ article.readNum }}</span>
      <span v-if="article.categories?.length">
        分类: <el-tag v-for="cat in article.categories" :key="cat.id" size="small">{{ cat.name }}</el-tag>
      </span>
      <span v-if="article.tags?.length">
        标签: <el-tag v-for="tag in article.tags" :key="tag.id" size="small" type="info">{{ tag.name }}</el-tag>
      </span>
    </div>
    <div class="detail-content markdown-body" v-html="htmlContent"></div>
  </div>
</template>
```

## 样式设计

### 卡片布局

```scss
.article-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.article-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  }
}

.card-cover {
  width: 100%;
  height: 200px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.card-title {
  padding: 16px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}
```

### 响应式

```scss
@media (max-width: 768px) {
  .article-grid {
    grid-template-columns: 1fr;
  }
}
```

## 安全设计

- 公开接口加入 Security 白名单
- 使用 MyBatis-Plus 参数化查询防止 SQL 注入
- 公开接口只返回必要字段，不暴露敏感信息
- `is_deleted` 字段过滤已删除数据

## 依赖关系

```
BlogLayout.vue
  ├── 依赖 publicApi 获取分类、标签、配置
  └── 包含 router-view

home/index.vue
  ├── 依赖 publicApi 获取文章列表
  └── 使用 BlogLayout 的 router-view

detail/index.vue
  ├── 依赖 sysArticleApi 获取文章详情
  ├── 依赖 sysArticleContentApi 获取 HTML 内容
  └── 使用 BlogLayout 的 router-view
```
