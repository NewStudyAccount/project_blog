<template>
  <div class="blog-layout">
    <header class="blog-header">
      <div class="header-content">
        <router-link to="/blog/home" class="site-name">
          {{ siteName }}
        </router-link>
        <nav class="header-nav">
          <router-link to="/blog/home" class="nav-link">首页</router-link>
          <el-dropdown @command="handleCategory" trigger="click">
            <span class="nav-link dropdown-trigger">
              分类
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="cat in categories" :key="cat.id" :command="cat.id">
                  {{ cat.name }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown @command="handleTag" trigger="click">
            <span class="nav-link dropdown-trigger">
              标签
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="tag in tags" :key="tag.id" :command="tag.id">
                  {{ tag.name }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <router-link :to="adminPath" class="admin-btn">后台管理</router-link>
        </nav>
      </div>
    </header>

    <main class="blog-main">
      <router-view />
    </main>

    <footer class="blog-footer">
      <div class="footer-content">
        <span v-if="config.copyright">{{ config.copyright }}</span>
        <a v-if="config.github" :href="config.github" target="_blank" class="footer-link">GitHub</a>
        <span v-if="config.beian">{{ config.beian }}</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { getPublicCategoryList, getPublicTagList, getPublicConfig } from '@/api/blog/publicApi'
import type { PublicCategoryVo, PublicTagVo, SiteConfig } from '@/api/blog/publicApi'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const adminPath = computed(() => userStore.isLoggedIn ? '/dashboard' : '/login')

const siteName = ref('我的博客')
const categories = ref<PublicCategoryVo[]>([])
const tags = ref<PublicTagVo[]>([])
const config = ref<SiteConfig>({
  siteName: '我的博客',
  copyright: '',
  github: '',
  beian: ''
})

const loadData = async () => {
  try {
    const [catRes, tagRes, configRes] = await Promise.all([
      getPublicCategoryList(),
      getPublicTagList(),
      getPublicConfig()
    ])
    categories.value = catRes.data || []
    tags.value = tagRes.data || []
    if (configRes.data) {
      config.value = configRes.data
      siteName.value = configRes.data.siteName || '我的博客'
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const handleCategory = (categoryId: number) => {
  router.push({ path: '/blog/home', query: { categoryId } })
}

const handleTag = (tagId: number) => {
  router.push({ path: '/blog/home', query: { tagId } })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.blog-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7f9;
}

.blog-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .header-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .site-name {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    text-decoration: none;

    &:hover {
      color: #409eff;
    }
  }

  .header-nav {
    display: flex;
    align-items: center;
    gap: 24px;
  }

  .nav-link {
    font-size: 15px;
    color: #606266;
    text-decoration: none;
    cursor: pointer;
    transition: color 0.2s;

    &:hover {
      color: #409eff;
    }

    &.router-link-active {
      color: #409eff;
    }
  }

  .dropdown-trigger {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .admin-btn {
    font-size: 15px;
    color: #606266;
    text-decoration: none;
    cursor: pointer;
    transition: color 0.2s;

    &:hover {
      color: #409eff;
    }
  }
}

.blog-main {
  flex: 1;
}

.blog-footer {
  background: #fff;
  border-top: 1px solid #e4e7ed;
  padding: 20px 0;

  .footer-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 24px;
    font-size: 14px;
    color: #909399;
  }

  .footer-link {
    color: #909399;
    text-decoration: none;

    &:hover {
      color: #409eff;
    }
  }
}
</style>
