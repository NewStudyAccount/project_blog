<template>
  <div class="sysArticle-container">
    <el-card class="search-card">
      <el-form :model="queryParams" ref="queryFormRef" label-width="100px">
        <el-row :gutter="20">
                  <el-col :span="8">
                    <el-form-item label="文章名" prop="title">
                      <el-input v-model="queryParams.title" placeholder="请输入文章名" clearable @keyup.enter="handleQuery" />
                    </el-form-item>
                  </el-col>
          <el-col :span="8" style="margin-top: 4px;">
            <el-button type="primary" @click="handleQuery">
              <el-icon><Search /></el-icon>搜索
            </el-button>
            <el-button @click="resetQuery">
              <el-icon><Refresh /></el-icon>重置
            </el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增
        </el-button>
        <el-button :disabled="single" type="danger" @click="handleDelete(selectedRow)">
          <el-icon><Delete /></el-icon>删除
        </el-button>
      </div>

      <el-table v-loading="loading" :data="dataList" @row-click="rowClick" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="文章名" align="center" prop="title" />
            <el-table-column label="预览图" align="center" width="100">
              <template #default="{ row }">
                <el-image v-if="row.cover" :src="row.cover" fit="cover" style="width: 60px; height: 60px; border-radius: 4px;" preview-teleported :preview-src-list="[row.cover]" />
              </template>
            </el-table-column>
            <el-table-column label="分类" align="center">
              <template #default="{ row }">
                <el-tag v-for="cat in row.categories" :key="cat.id" size="small" style="margin: 2px;">{{ cat.name }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="标签" align="center">
              <template #default="{ row }">
                <el-tag v-for="tag in row.tags" :key="tag.id" size="small" type="info" style="margin: 2px;">{{ tag.name }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="阅读次数" align="center" prop="readNum" />
        <el-table-column label="操作" width="300" align="center">
          <template #default="{ row }">
            <el-button type="text" @click.stop="handleView(row)">查看</el-button>
            <el-button type="text" @click.stop="handleEdit(row)">编辑</el-button>
            <el-button type="text" @click.stop="handleEditContent(row)">编辑内容</el-button>
            <el-button type="text" @click.stop="handleUploadContent(row)">上传内容</el-button>
            <el-button type="text" @click.stop="handleViewHistory(row)">版本历史</el-button>
            <el-button type="text" @click.stop="handleViewDoc(row)">查看文档</el-button>
            <el-button type="text" @click.stop="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
          :current-page="queryParams.pageQuery.pageNum"
          :page-size="queryParams.pageQuery.pageSize"
          :total="total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
      />
    </el-card>

    <el-dialog v-model="formDialogVisible" :title="dialogTitle" width="800px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
                <el-form-item label="文章名" prop="title">
                      <el-input v-model="form.title" placeholder="请输入文章名" />
                </el-form-item>
                <el-form-item label="预览图" prop="cover">
                      <ImageUpload v-model="form.cover" :limit="1" />
                </el-form-item>
                <el-form-item label="分类" prop="categoryIds">
                      <el-select v-model="form.categoryIds" multiple placeholder="请选择分类" style="width: 100%;">
                        <el-option v-for="cat in categoryOptions" :key="cat.id" :label="cat.name" :value="cat.id" />
                      </el-select>
                </el-form-item>
                <el-form-item label="标签" prop="tagIds">
                      <el-select v-model="form.tagIds" multiple placeholder="请选择标签" style="width: 100%;">
                        <el-option v-for="tag in tagOptions" :key="tag.id" :label="tag.name" :value="tag.id" />
                      </el-select>
                </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewDialogVisible" title="详情" width="800px" destroy-on-close>
      <el-descriptions :column="2" border>
            <el-descriptions-item label="文章id">{{ currentRow?.id }}</el-descriptions-item>
            <el-descriptions-item label="文章名">{{ currentRow?.title }}</el-descriptions-item>
            <el-descriptions-item label="预览图">
              <el-image v-if="currentRow?.cover" :src="currentRow.cover" fit="cover" style="max-width: 200px; max-height: 200px; border-radius: 4px;" preview-teleported :preview-src-list="[currentRow.cover]" />
            </el-descriptions-item>
            <el-descriptions-item label="分类">
              <el-tag v-for="cat in currentRow?.categories" :key="cat.id" size="small" style="margin: 2px;">{{ cat.name }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="标签">
              <el-tag v-for="tag in currentRow?.tags" :key="tag.id" size="small" type="info" style="margin: 2px;">{{ tag.name }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="阅读次数">{{ currentRow?.readNum }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="contentDialogVisible" title="编辑文章内容" fullscreen destroy-on-close>
      <MarkdownEditor v-model="contentForm.content" />
      <template #footer>
        <el-button @click="contentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="contentSaving" @click="handleContentSubmit">保存</el-button>
      </template>
    </el-dialog>

    <ContentUpload ref="contentUploadRef" @success="getList" />
    <ContentHistory ref="contentHistoryRef" />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted, defineAsyncComponent } from 'vue'
  import { useRouter } from 'vue-router'
  import { Search, Refresh, Plus, Delete } from '@element-plus/icons-vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { listSysArticle, getSysArticleById, deleteSysArticle, addSysArticle, updateSysArticle } from '@/api/blog/sysArticleApi'
  import type { SysArticleVo, SysArticleReq } from '@/api/blog/sysArticleApi'
  import { getSysArticleContentByArticleId, addSysArticleContent, updateSysArticleContent } from '@/api/blog/sysArticleContentApi'
  import type { SysArticleContent } from '@/api/blog/sysArticleContentApi'
  import { listSysCategory } from '@/api/blog/sysCategoryApi'
  import { listSysTag } from '@/api/blog/sysTagApi'

  const MarkdownEditor = defineAsyncComponent(() => import('@/components/MarkdownEditor/index.vue'))
  import ContentUpload from '@/components/ContentUpload/index.vue'
  import ContentHistory from '@/components/ContentHistory/index.vue'
  import ImageUpload from '@/components/ImageUpload/index.vue'

  const router = useRouter()

  const loading = ref(false)
  const dataList = ref<SysArticleVo[]>([])
  const total = ref(0)
  const queryParams = reactive({
    pageQuery: {
      pageNum: 1,
      pageSize: 10
    },
                  title: undefined,
                  cover: undefined,
                  isDeleted: undefined,
  })
  const queryFormRef = ref()
  const formDialogVisible = ref(false)
  const viewDialogVisible = ref(false)
  const dialogTitle = ref('')
  const currentRow = ref<SysArticleVo>()
  const selectedRow = ref<SysArticleVo>()
  const single = ref(true)
  const formRef = ref()
  const form = reactive<SysArticleReq>({
    title: undefined,
    cover: undefined,
    tagIds: [],
    categoryIds: [],
  })
  const rules = reactive<Record<string, any[]>>({})

  // 分类和标签选项
  const categoryOptions = ref<{ id: number; name: string }[]>([])
  const tagOptions = ref<{ id: number; name: string }[]>([])

  const loadOptions = async () => {
    try {
      const [catRes, tagRes] = await Promise.all([
        listSysCategory({ pageQuery: { pageNum: 1, pageSize: 100 } }),
        listSysTag({ pageQuery: { pageNum: 1, pageSize: 100 } }),
      ])
      categoryOptions.value = catRes.data.rows || []
      tagOptions.value = tagRes.data.rows || []
    } catch {}
  }

  const getList = async () => {
    loading.value = true
    try {
      const res = await listSysArticle(queryParams)
      dataList.value = res.data.rows
      total.value = res.data.total
    } finally {
      loading.value = false
    }
  }

  const handlePageChange = (page: number) => {
    queryParams.pageQuery.pageNum = page
    getList()
  }

  const handleSizeChange = (size: number) => {
    queryParams.pageQuery.pageSize = size
    queryParams.pageQuery.pageNum = 1
    getList()
  }

  const handleQuery = () => {
    queryParams.pageQuery.pageNum = 1
    getList()
  }

  const resetQuery = () => {
    queryFormRef.value?.resetFields()
    handleQuery()
  }

  const handleAdd = () => {
    dialogTitle.value = '新增文章'
    currentRow.value = undefined
    resetForm()
    formDialogVisible.value = true
  }

  const handleEdit = async(row: SysArticleVo) => {
    dialogTitle.value = '编辑文章'
    currentRow.value = row
    try {
      const res = await getSysArticleById(row.id)
      const data = res.data
      form.id = data.id
      form.title = data.title
      form.cover = data.cover
      form.tagIds = (data.tags || []).map((t: any) => t.id)
      form.categoryIds = (data.categories || []).map((c: any) => c.id)
    } catch (error) {
      ElMessage.error('获取数据失败')
      return
    }
    formDialogVisible.value = true
  }

  const handleView = async (row: SysArticleVo) => {
    try {
      const res = await getSysArticleById(row.id)
      currentRow.value = res.data
      viewDialogVisible.value = true
    } catch (error) {
      ElMessage.error('获取数据失败')
    }
  }

  const handleDelete = async (row?: SysArticleVo) => {
    if (!row) return
    try {
      await ElMessageBox.confirm('是否确认删除选中的数据?', '警告', { type: 'warning' })
      await deleteSysArticle(row.id)
      ElMessage.success('删除成功')
      await getList()
    } catch {}
  }

  const handleSelectionChange = (selection: SysArticleVo[]) => {
    single.value = selection.length !== 1
    if (selection.length === 1) {
      selectedRow.value = selection[0]
    }
  }

  const rowClick = (row: SysArticleVo) => {
    currentRow.value = row
  }

  const handleSubmit = async () => {
    const valid = await formRef.value?.validate()
    if (!valid) return

    try {
      const data: SysArticleReq = {
        id: form.id,
        title: form.title,
        cover: form.cover,
        tagIds: form.tagIds || [],
        categoryIds: form.categoryIds || [],
      }
      if (data.id) {
        await updateSysArticle(data)
        ElMessage.success('修改成功')
      } else {
        await addSysArticle(data)
        ElMessage.success('新增成功')
      }
      formDialogVisible.value = false
      await getList()
    } catch {}
  }

  // 重置表单函数
  const resetForm = () => {
    form.id = undefined
    form.title = undefined
    form.cover = undefined
    form.tagIds = []
    form.categoryIds = []
  }

  // 内容编辑相关
  const contentDialogVisible = ref(false)
  const contentSaving = ref(false)
  const contentForm = reactive<Partial<SysArticleContent>>({})
  const contentUploadRef = ref()
  const contentHistoryRef = ref()

  const handleEditContent = async (row: SysArticleVo) => {
    Object.keys(contentForm).forEach(key => {
      (contentForm as any)[key] = undefined
    })
    contentForm.articleId = row.id
    try {
      const res = await getSysArticleContentByArticleId(row.id)
      if (res.data) {
        Object.assign(contentForm, res.data)
      }
    } catch {
      // 文章无内容记录，显示空编辑器
    }
    contentDialogVisible.value = true
  }

  const handleViewDoc = (row: SysArticleVo) => {
    router.push(`/blog/doc/${row.id}`)
  }

  const handleUploadContent = (row: SysArticleVo) => {
    contentUploadRef.value?.open(row.id)
  }

  const handleViewHistory = (row: SysArticleVo) => {
    contentHistoryRef.value?.open(row.id)
  }

  const handleContentSubmit = async () => {
    contentSaving.value = true

    try {
      if (contentForm.articleId) {
        await updateSysArticleContent(contentForm)
        ElMessage.success('内容保存成功')
      } else {
        await addSysArticleContent(contentForm)
        ElMessage.success('内容新增成功')
      }
      contentDialogVisible.value = false
    } catch(error: any) {
      if (!error?.message) {
        ElMessage.error('内容保存失败')
      }
    } finally {
      contentSaving.value = false
    }
  }


  onMounted(() => {
    getList()
    loadOptions()
  })
</script>

<style scoped lang="scss">
    .sysArticle-container {
      padding: 20px;
    }
    .search-card {
      margin-bottom: 20px;
    }
    .table-card {
      .toolbar {
        padding: 10px 0;
      }
    }
</style>
