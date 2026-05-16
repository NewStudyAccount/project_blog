# Proposal: 新增博客首页

## 概述

新增面向公众的博客首页，无需登录即可访问。包含文章展示、分类筛选、标签筛选功能。

## 动机

- 现有博客管理页面需要登录，无法公开展示文章
- 需要一个前台页面让访客浏览文章

## 范围

### 新增功能

1. **博客首页** (`/blog/home`)
   - 两列网格布局展示文章卡片（封面 + 标题）
   - 滚动加载分页
   - 支持按分类/标签筛选

2. **文章详情页** (`/blog/detail/:id`)
   - 展示渲染后的文章内容
   - 显示标题、分类、标签、阅读次数
   - 返回首页按钮

3. **公共布局** (`BlogLayout.vue`)
   - Header: 博客名称 + 导航（首页、分类下拉、标签下拉）
   - Footer: 版权信息 + 可配置链接

4. **站点配置** (`sys_config` 表)
   - 存储站点名称、版权信息、GitHub 链接、备案号等

### 新增接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/project/sysArticle/public/list` | POST | 公开文章列表（支持分类/标签筛选） |
| `/project/sysCategory/public/list` | GET | 公开分类列表 |
| `/project/sysTag/public/list` | GET | 公开标签列表 |
| `/project/sysConfig/public/info` | GET | 站点配置信息 |

### 新建表

```sql
CREATE TABLE sys_config (
  id BIGINT PRIMARY KEY,
  config_key VARCHAR(100) NOT NULL UNIQUE,
  config_value VARCHAR(500),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0
);
```

## 技术方案

### 后端

- 白名单: `/project/sysArticle/public/**`, `/project/sysCategory/public/**`, `/project/sysTag/public/**`, `/project/sysConfig/public/**`
- 复用现有 Service，新增公开查询方法
- 过滤已删除数据 (`is_deleted = 0`)

### 前端

- 路由: `/blog` 下配置 `home` 和 `detail/:id`
- 布局: 新建 `BlogLayout.vue` 包含 Header + Footer
- 滚动加载: 监听 scroll 事件，触底请求下一页

## 目录结构

```
后端新增/修改:
├── myproject-blog/
│   ├── controller/
│   │   ├── SysArticleController.java    # 新增 public/list
│   │   ├── SysCategoryController.java   # 新增 public/list
│   │   ├── SysTagController.java        # 新增 public/list
│   │   └── SysConfigController.java     # 新建
│   ├── domain/pojo/SysConfig.java       # 新建
│   ├── mapper/SysConfigMapper.java      # 新建
│   └── service/SysConfigService.java    # 新建
└── myproject-framework/
    └── config/SecurityConfig.java       # 白名单更新

前端新增/修改:
├── src/components/layout/BlogLayout.vue
├── src/views/blog/home/index.vue
├── src/views/blog/detail/index.vue
├── src/api/blog/publicApi.ts
└── src/router/index.ts
```

## 依赖

- 无外部依赖
- 复用现有 Flexmark 渲染逻辑

## 风险

- 低风险：新增功能，不影响现有代码
- 公开接口需注意 SQL 注入防护（使用 MyBatis-Plus 参数化查询）

## 时间估算

- 后端: 2-3 小时
- 前端: 3-4 小时
- 总计: 5-7 小时
