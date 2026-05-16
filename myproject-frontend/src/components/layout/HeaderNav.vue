<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import {Fold, Expand, User, SwitchButton, Link, Files, Collection} from '@element-plus/icons-vue'

defineProps<{
  isCollapse: boolean
}>()

const emit = defineEmits<{
  (e: 'toggle-collapse'): void
}>()

const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  ElMessageBox.confirm('确定退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

const handleModuleSwitch = (path: string) => {
  router.push(path)
}
</script>

<template>
  <div class="header-nav">
    <div class="header-left">
      <el-icon class="collapse-btn" @click="emit('toggle-collapse')">
        <Fold v-if="!isCollapse" />
        <Expand v-else />
      </el-icon>
      <span class="breadcrumb">首页</span>
    </div>

    <div class="header-right">
      <el-dropdown trigger="click" @command="handleModuleSwitch">
        <span class="module-info">
          <el-icon><Link /></el-icon>
          <span class="module-name">跳转系统</span>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="/blog/home">
              <el-icon><Collection /></el-icon>
              博客前台
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <el-dropdown trigger="click">
        <span class="user-info">
          <el-icon><User /></el-icon>
          <span class="username">{{ userStore.userInfo?.user?.username || '用户' }}</span>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<style scoped>
.header-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  margin-right: 15px;
}

.breadcrumb {
  font-size: 14px;
  color: #606266;
}

.header-right {
  display: flex;
  align-items: center;
}

.module-info,
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
}

.module-info:hover,
.user-info:hover {
  background-color: #f5f7fa;
}

.module-name,
.username {
  margin-left: 8px;
  font-size: 14px;
}
</style>
