## MODIFIED Requirements

### Requirement: 文章管理页面集成内容编辑
文章管理页面 SHALL 在操作列提供"编辑内容"按钮。点击后通过 `getSysArticleContentByArticleId` 接口（入参为文章 id）获取文章内容，并打开 Markdown 编辑器弹窗进行编辑。保存时调用 `updateSysArticleContent` 接口更新内容。文章管理页面 SHALL 同时提供"上传内容"按钮和"版本历史"按钮。

#### Scenario: 点击编辑内容按钮加载内容
- **WHEN** 用户在文章管理列表点击某篇文章的"编辑内容"按钮
- **THEN** 系统调用 `getSysArticleContentByArticleId` 接口获取该文章内容，并打开 Markdown 编辑器弹窗显示内容

#### Scenario: 文章无内容记录时打开编辑器
- **WHEN** 用户点击"编辑内容"按钮，但该文章尚无内容记录（接口返回空）
- **THEN** 系统打开空编辑器弹窗，articleId 预填，用户可新增内容

#### Scenario: 保存编辑的内容
- **WHEN** 用户在编辑器弹窗中编辑内容后点击保存
- **THEN** 系统调用 `updateSysArticleContent` 接口保存内容，保存成功后关闭弹窗并提示成功

#### Scenario: 上传内容文件
- **WHEN** 用户点击"上传内容"按钮
- **THEN** 系统打开文件选择对话框，用户选择 .md 文件后调用上传接口。若文章已有内容，系统显示确认弹窗提示将覆盖现有内容

#### Scenario: 覆盖确认弹窗
- **WHEN** 上传接口返回错误码 `4090`（需确认覆盖）
- **THEN** 前端显示确认弹窗，提示"该文章已有内容，确认覆盖？历史版本将被保留"。用户确认后携带 `confirmOverride=true` 重新调用覆盖上传接口

#### Scenario: 查看版本历史
- **WHEN** 用户点击"版本历史"按钮
- **THEN** 系统调用版本历史查询接口，展示该文章的所有历史版本列表（版本号、替换时间、文件名）
