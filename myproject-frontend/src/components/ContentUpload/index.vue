<template>
  <el-dialog v-model="visible" title="上传文章内容" width="500px" destroy-on-close>
    <el-upload
      ref="uploadRef"
      :auto-upload="false"
      :limit="1"
      accept=".md"
      :on-change="handleFileChange"
      :on-exceed="handleExceed"
      drag
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">拖拽文件到此处，或 <em>点击选择</em></div>
      <template #tip>
        <div class="el-upload__tip">仅支持 .md 格式的 Markdown 文件</div>
      </template>
    </el-upload>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="uploading" @click="handleSubmit">上传</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import type { UploadFile, UploadInstance } from 'element-plus'
import { uploadContent, uploadOverride } from '@/api/blog/sysArticleContentApi'

const visible = ref(false)
const uploading = ref(false)
const uploadRef = ref<UploadInstance>()
const selectedFile = ref<File | null>(null)
const articleId = ref<string>('')

const emit = defineEmits<{
  (e: 'success'): void
}>()

function open(id: string) {
  articleId.value = id
  selectedFile.value = null
  visible.value = true
}

function handleFileChange(file: UploadFile) {
  if (file.raw) {
    selectedFile.value = file.raw
  }
}

function handleExceed() {
  ElMessage.warning('只能上传一个文件，请先移除已选文件')
}

async function handleSubmit() {
  if (!selectedFile.value) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  uploading.value = true
  try {
    await uploadContent(selectedFile.value, articleId.value)
    ElMessage.success('上传成功')
    visible.value = false
    emit('success')
  } catch (error: any) {
    const responseData = error?.response?.data
    // 4090 表示已有内容，需要确认覆盖
    if (responseData?.code === '4090') {
      try {
        await ElMessageBox.confirm(
          '该文章已有内容，确认覆盖？历史版本将被保留。',
          '确认覆盖',
          { confirmButtonText: '确认覆盖', cancelButtonText: '取消', type: 'warning' }
        )
        await uploadOverride(selectedFile.value, articleId.value, true)
        ElMessage.success('覆盖上传成功')
        visible.value = false
        emit('success')
      } catch {
        // 用户取消或覆盖上传失败
      }
    } else {
      ElMessage.error(responseData?.msg || '上传失败')
    }
  } finally {
    uploading.value = false
  }
}

defineExpose({ open })
</script>
