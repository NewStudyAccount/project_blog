# 文章预览图使用图片上传组件

## 背景

文章管理页的"预览图"字段（`cover`）当前是一个纯文本输入框，需要手动粘贴 URL。项目中已有 `ImageUpload` 组件支持图片上传到 OSS 并返回 URL，但未集成到文章表单中。

## 目标

- 文章新增/编辑对话框中，预览图字段改用 `ImageUpload` 组件（单图模式）
- 文章列表和详情中展示图片预览
- `cover` 字段继续存 URL（不改为 ossId）

## 设计决策：cover 存 URL

OSS 上传接口返回 `{ url, size }`，`ImageUpload` 组件围绕 URL 设计。存 URL 避免了额外查询和组件改造，显示图片零成本。OSS endpoint 部署后基本不变，URL 失效风险极低。

## 范围

- `myproject-frontend` — 文章管理页模板和脚本
- 后端无需改动（cover 字段类型和上传接口均不需修改）
