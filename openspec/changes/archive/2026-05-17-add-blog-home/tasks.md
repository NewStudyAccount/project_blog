# Tasks: 新增博客首页

## 任务概览

- 总任务数: 12
- 预计总时长: 5-7 小时

---

## Phase 1: 后端基础 (2-3h)

### Task 1.1: 创建 sys_config 表
- [x] 编写建表 SQL
- [x] 插入初始配置数据（site_name, copyright, github, beian）
- **预计时长**: 15min

### Task 1.2: 创建 SysConfig 实体和 Mapper
- [x] 创建 `SysConfig.java` 实体类
- [x] 创建 `SysConfigMapper.java` 接口
- [x] 创建 `SysConfigMapper.xml`（如需要）
- **预计时长**: 20min
- **目录**: `myproject-blog/src/main/java/com/example/`

### Task 1.3: 创建 SysConfigService
- [x] 创建 `SysConfigService.java` 接口
- [x] 创建 `SysConfigServiceImpl.java` 实现
- [x] 实现 `getPublicConfig()` 方法，返回 Map 格式配置
- **预计时长**: 20min

### Task 1.4: 创建 SysConfigController
- [x] 创建 `SysConfigController.java`
- [x] 实现 `GET /project/sysConfig/public/info` 接口
- **预计时长**: 15min

### Task 1.5: 扩展 SysArticleController
- [x] 新增 `POST /project/sysArticle/public/list` 方法
- [x] 支持 categoryId 和 tagId 筛选参数
- [x] 只返回 id, title, cover 字段
- **预计时长**: 30min

### Task 1.6: 扩展 SysCategoryController
- [x] 新增 `GET /project/sysCategory/public/list` 方法
- [x] 只返回 id, name 字段
- **预计时长**: 15min

### Task 1.7: 扩展 SysTagController
- [x] 新增 `GET /project/sysTag/public/list` 方法
- [x] 只返回 id, name 字段
- **预计时长**: 15min

### Task 1.8: 更新 Security 白名单
- [x] 在 `SecurityConfig.WHITE_LIST` 中添加:
  - `/project/sysArticle/public/**`
  - `/project/sysCategory/public/**`
  - `/project/sysTag/public/**`
  - `/project/sysConfig/public/**`
- **预计时长**: 10min

---

## Phase 2: 前端实现 (3-4h)

### Task 2.1: 创建公开接口 API
- [x] 创建 `src/api/blog/publicApi.ts`
- [x] 定义接口类型
- [x] 实现以下方法:
  - `getPublicArticleList(params)`
  - `getPublicCategoryList()`
  - `getPublicTagList()`
  - `getPublicConfig()`
- **预计时长**: 20min

### Task 2.2: 创建 BlogLayout 组件
- [x] 创建 `src/components/layout/BlogLayout.vue`
- [x] 实现 Header:
  - 博客名称（链接到 /blog/home）
  - 导航：首页、分类下拉、标签下拉
- [x] 实现 Footer:
  - 版权信息、GitHub 链接、备案号
- [x] 加载分类、标签、配置数据
- **预计时长**: 45min

### Task 2.3: 创建博客首页
- [x] 创建 `src/views/blog/home/index.vue`
- [x] 实现两列网格卡片布局
- [x] 实现滚动加载分页
- [x] 支持 categoryId/tagId 筛选参数
- [x] 卡片 hover 动画效果
- **预计时长**: 60min

### Task 2.4: 创建文章详情页
- [x] 创建 `src/views/blog/detail/index.vue`
- [x] 实现返回首页按钮
- [x] 加载文章详情（标题、分类、标签、阅读次数）
- [x] 加载并渲染 HTML 内容
- [x] 复用 markdown-body 样式
- **预计时长**: 45min

### Task 2.5: 更新路由配置
- [x] 在 `src/router/index.ts` 添加路由:
  ```
  /blog → BlogLayout
    /blog/home → home/index.vue
    /blog/detail/:id → detail/index.vue
  ```
- [x] 设置 `requiresAuth: false`
- **预计时长**: 15min

---

## 依赖关系

```
Task 1.1 → 1.2 → 1.3 → 1.4
Task 1.5 (依赖现有 Service)
Task 1.6 (依赖现有 Service)
Task 1.7 (依赖现有 Service)
Task 1.8 (最后执行)

Task 2.1 → 2.2 → 2.3
                → 2.4
Task 2.5 (最后执行)
```

---

## 验证清单

- [ ] 未登录状态可访问 `/blog/home`
- [ ] 文章卡片正确显示封面和标题
- [ ] 滚动加载正常工作
- [ ] 分类/标签下拉显示正确
- [ ] 点击分类/标签可筛选文章
- [ ] 点击卡片跳转详情页
- [ ] 详情页显示完整信息
- [ ] 详情页返回按钮工作正常
- [ ] Header 和 Footer 在两个页面一致显示
- [ ] 移动端响应式布局正常
