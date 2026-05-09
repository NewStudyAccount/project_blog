<template>
  <el-dialog v-model="visible" title="版本历史" width="650px" destroy-on-close>
    <el-table :data="historyList" v-loading="loading" empty-text="暂无历史版本">
      <el-table-column label="版本号" prop="version" width="80" align="center" />
      <el-table-column label="替换时间" prop="replacedAt" width="180" />
      <el-table-column label="操作人" prop="replacedBy" width="120" />
      <el-table-column label="备注" prop="remark" />
    </el-table>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getContentHistory, type ContentHistory } from '@/api/blog/sysArticleContentApi'

const visible = ref(false)
const loading = ref(false)
const historyList = ref<ContentHistory[]>([])

async function open(articleId: string) {
  visible.value = true
  loading.value = true
  historyList.value = []
  try {
    const res = await getContentHistory(articleId)
    historyList.value = res.data || []
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>
