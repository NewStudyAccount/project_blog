<template>
  <div class="blog-detail">
    <div class="detail-container">
      <div class="detail-header">
<!--        <el-button @click="goBack" :icon="ArrowLeft" class="back-btn">返回首页</el-button>-->
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <span class="meta-item">
            <el-icon><View /></el-icon>
            阅读次数: {{ article.readNum || 0 }}
          </span>
          <span v-if="article.categories?.length" class="meta-item">
            分类:
            <el-tag
              v-for="cat in article.categories"
              :key="cat.id"
              size="small"
              class="meta-tag"
            >
              {{ cat.name }}
            </el-tag>
          </span>
          <span v-if="article.tags?.length" class="meta-item">
            标签:
            <el-tag
              v-for="tag in article.tags"
              :key="tag.id"
              size="small"
              type="info"
              class="meta-tag"
            >
              {{ tag.name }}
            </el-tag>
          </span>
        </div>
      </div>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>

      <div v-else-if="error" class="error-container">
        <el-result icon="error" :title="error" sub-title="请稍后重试或返回首页">
          <template #extra>
            <el-button type="primary" @click="goBack">返回首页</el-button>
            <el-button @click="fetchContent">重新加载</el-button>
          </template>
        </el-result>
      </div>

      <div v-else class="detail-body">
        <div class="detail-content">
          <div class="markdown-body" v-html="htmlContent"></div>
        </div>
        <div class="detail-sidebar">
          <aside v-if="article.categories?.length" class="sidebar-section">
            <div class="section-title">分类</div>
            <div class="tag-list">
              <el-tag v-for="cat in article.categories" :key="cat.id" size="small">{{ cat.name }}</el-tag>
            </div>
          </aside>
          <aside v-if="article.tags?.length" class="sidebar-section">
            <div class="section-title">标签</div>
            <div class="tag-list">
              <el-tag v-for="tag in article.tags" :key="tag.id" size="small" type="info">{{ tag.name }}</el-tag>
            </div>
          </aside>
          <aside v-if="tocItems.length" class="sidebar-section">
            <div class="section-title">目录</div>
            <nav class="toc-list">
              <a
                v-for="item in tocItems"
                :key="item.id"
                :class="['toc-item', { active: activeTocId === item.id }]"
                :style="{ paddingLeft: 12 + (item.level - 1) * 16 + 'px' }"
                @click.prevent="scrollToHeading(item.id)"
              >
                {{ item.text }}
              </a>
            </nav>
          </aside>
        </div>
      </div>
    </div>

    <transition name="fade">
      <div v-if="showBackToTop" class="back-to-top" @click="scrollToTop">
        <el-icon :size="20"><ArrowUp /></el-icon>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ArrowUp, View } from '@element-plus/icons-vue'
import { getPublicArticleById, getPublicArticleContentHtml } from '@/api/blog/publicApi'

interface CategoryItem {
  id: number
  name: string
}

interface TagItem {
  id: number
  name: string
}

interface ArticleDetail {
  id: string
  title: string
  readNum: number
  categories: CategoryItem[]
  tags: TagItem[]
}

interface TocItem {
  id: string
  text: string
  level: number
}

const route = useRoute()
const router = useRouter()

const articleId = route.params.id as string
const article = ref<ArticleDetail>({
  id: '',
  title: '',
  readNum: 0,
  categories: [],
  tags: []
})
const htmlContent = ref('')
const loading = ref(true)
const error = ref('')
const tocItems = ref<TocItem[]>([])
const activeTocId = ref('')
const showBackToTop = ref(false)

let observer: IntersectionObserver | null = null

const generateToc = () => {
  const container = document.querySelector('.markdown-body')
  if (!container) return

  const headings = container.querySelectorAll('h1, h2, h3, h4, h5, h6')
  const items: TocItem[] = []

  headings.forEach((heading, index) => {
    const id = `heading-${index}`
    heading.id = id
    items.push({
      id,
      text: heading.textContent || '',
      level: parseInt(heading.tagName[1], 10),
    })
  })

  tocItems.value = items
  if (items.length > 0) {
    activeTocId.value = items[0].id
  }
}

const setupObserver = () => {
  if (observer) observer.disconnect()

  observer = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          activeTocId.value = entry.target.id
        }
      }
    },
    {
      rootMargin: '-80px 0px -60% 0px',
      threshold: 0,
    }
  )

  const container = document.querySelector('.markdown-body')
  if (!container) return

  const headings = container.querySelectorAll('h1, h2, h3, h4, h5, h6')
  headings.forEach((heading) => {
    if (heading.id) observer!.observe(heading)
  })
}

const scrollToHeading = (id: string) => {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleScroll = () => {
  showBackToTop.value = window.scrollY > 300
}

const fetchArticleDetail = async () => {
  try {
    const res = await getPublicArticleById(articleId)
    if (res.data) {
      article.value = res.data
    }
  } catch (e: any) {
    console.error('获取文章详情失败:', e)
  }
}

const fetchContent = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await getPublicArticleContentHtml(articleId)
    htmlContent.value = res.data?.htmlContent || ''
  } catch (e: any) {
    error.value = e?.message || '加载文档内容失败'
  } finally {
    loading.value = false
    await nextTick()
    generateToc()
    setupObserver()
  }
}

const goBack = () => {
  router.push('/blog/home')
}

onMounted(() => {
  fetchArticleDetail()
  fetchContent()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped lang="scss">
.blog-detail {
  background: #f5f7f9;
  padding: 24px;
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
}

.detail-header {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  .back-btn {
    margin-bottom: 16px;
  }

  .article-title {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
    margin: 0 0 16px 0;
    line-height: 1.4;
  }

  .article-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    font-size: 14px;
    color: #909399;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .meta-tag {
      margin-left: 4px;
    }
  }
}

.loading-container,
.error-container {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.detail-body {
  display: flex;
  gap: 24px;
}

.detail-content {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 8px;
  padding: 32px 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.detail-sidebar {
  width: 240px;
  flex-shrink: 0;
  position: sticky;
  top: 84px;
  align-self: flex-start;
  max-height: calc(100vh - 108px);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sidebar-section {
  background: #fff;
  border-radius: 8px;
  padding: 16px 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  .section-title {
    padding: 0 16px 12px;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 8px;
  }

  .tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    padding: 0 16px;
  }
}

.toc-list {
  display: flex;
  flex-direction: column;
}

.toc-item {
  display: block;
  padding: 6px 16px;
  font-size: 13px;
  line-height: 1.5;
  color: #666;
  text-decoration: none;
  cursor: pointer;
  border-left: 2px solid transparent;
  transition: color 0.2s, border-color 0.2s, background 0.2s;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  &:hover {
    color: #409eff;
    background: #f5f7fa;
  }

  &.active {
    color: #409eff;
    border-left-color: #409eff;
    background: #ecf5ff;
    font-weight: 500;
  }
}

.markdown-body {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  word-wrap: break-word;

  :deep(h1) {
    font-size: 28px;
    font-weight: 700;
    margin: 24px 0 16px;
    padding-bottom: 8px;
    border-bottom: 2px solid #eaecef;
  }

  :deep(h2) {
    font-size: 24px;
    font-weight: 600;
    margin: 20px 0 14px;
    padding-bottom: 6px;
    border-bottom: 1px solid #eaecef;
  }

  :deep(h3) {
    font-size: 20px;
    font-weight: 600;
    margin: 18px 0 12px;
  }

  :deep(h4) {
    font-size: 17px;
    font-weight: 600;
    margin: 16px 0 10px;
  }

  :deep(h5),
  :deep(h6) {
    font-size: 15px;
    font-weight: 600;
    margin: 14px 0 8px;
  }

  :deep(p) {
    margin: 0 0 14px;
  }

  :deep(a) {
    color: #409eff;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  :deep(strong) {
    font-weight: 600;
  }

  :deep(blockquote) {
    margin: 0 0 14px;
    padding: 8px 16px;
    border-left: 4px solid #ddd;
    background: #f9f9f9;
    color: #666;

    p {
      margin: 0;
    }
  }

  :deep(ul),
  :deep(ol) {
    margin: 0 0 14px;
    padding-left: 28px;
  }

  :deep(li) {
    margin: 4px 0;
  }

  :deep(code) {
    padding: 2px 6px;
    font-size: 13px;
    background: #f0f0f0;
    border-radius: 4px;
    font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  }

  :deep(pre) {
    margin: 0 0 14px;
    padding: 16px;
    overflow: auto;
    font-size: 13px;
    line-height: 1.5;
    background: #f6f8fa;
    border-radius: 6px;

    code {
      padding: 0;
      background: none;
      font-size: inherit;
    }
  }

  :deep(table) {
    width: 100%;
    margin: 0 0 14px;
    border-collapse: collapse;
    border-spacing: 0;

    th,
    td {
      padding: 8px 12px;
      border: 1px solid #dfe2e5;
      text-align: left;
    }

    th {
      background: #f6f8fa;
      font-weight: 600;
    }

    tr:nth-child(even) {
      background: #fafafa;
    }
  }

  :deep(hr) {
    margin: 20px 0;
    border: none;
    border-top: 1px solid #eaecef;
  }

  :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: 4px;
  }
}

.back-to-top {
  position: fixed;
  right: 32px;
  bottom: 40px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.4);
  transition: transform 0.2s, opacity 0.2s;
  z-index: 200;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(64, 158, 255, 0.5);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .blog-detail {
    padding: 16px;
  }

  .detail-header {
    padding: 16px;

    .article-title {
      font-size: 22px;
    }
  }

  .detail-body {
    flex-direction: column;
  }

  .detail-sidebar {
    width: 100%;
    position: static;
    max-height: none;
    order: -1;
  }

  .detail-content {
    padding: 20px;
  }
}
</style>
