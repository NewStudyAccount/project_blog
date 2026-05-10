# 设计文档

## 前端改动

### 文章管理页 (`views/blog/article/index.vue`)

**模板改动：**

1. 新增/编辑对话框中，将 cover 的 `<el-input>` 替换为 `<ImageUpload v-model="form.cover" :limit="1" />`
2. 文章列表中，cover 列从纯文本改为 `<el-image>` 展示缩略图
3. 详情对话框中，cover 从纯文本改为 `<el-image>` 展示

**脚本改动：**

1. 引入 `ImageUpload` 组件
2. `form.cover` 类型保持 `string`（URL），无需改动

### ImageUpload 组件兼容性

`ImageUpload` 已支持 `v-model` 绑定单个 URL 字符串：
- `modelValue: string` — 单图模式
- 选图后自动上传到 OSS，返回 URL
- 无需修改组件本身

### 后端

无需改动。`SysArticle.cover` 字段已是 `varchar(255)`，存 URL 无问题。上传接口 `/sysOssFile/upload` 已返回 URL。
