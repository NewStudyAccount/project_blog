<template>
  <div class="blog-home">
    <div class="article-grid">
      <div
        v-for="article in articles"
        :key="article.id"
        class="article-card"
        @click="goDetail(article.id)"
      >
        <div class="card-cover">
          <img v-if="article.cover" :src="article.cover" :alt="article.title" />
          <div v-else class="no-cover">暂无封面</div>
        </div>
        <div class="card-body">
          <div class="card-title">{{ article.title }}</div>
          <div class="card-tags">
            <el-tag v-for="cat in article.categories" :key="cat.id" size="small">{{ cat.name }}</el-tag>
            <el-tag v-for="tag in article.tags" :key="tag.id" size="small" type="info">{{ tag.name }}</el-tag>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="loading-text">
      <el-icon class="is-loading"><Loading /></el-icon>
      加载中...
    </div>
    <div v-if="noMore && articles.length > 0" class="no-more-text">没有更多了</div>
    <div v-if="!loading && articles.length === 0" class="empty-text">暂无文章</div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { getPublicArticleList } from '@/api/blog/publicApi'
import type { PublicArticleVo } from '@/api/blog/publicApi'

const router = useRouter()
const route = useRoute()

const articles = ref<PublicArticleVo[]>([])
const loading = ref(false)
const noMore = ref(false)
const pageNum = ref(1)
const pageSize = 10

const categoryId = ref<number | undefined>(undefined)
const tagId = ref<number | undefined>(undefined)

const fetchArticles = async (reset = false) => {
  if (loading.value) return
  loading.value = true

  try {
    const res = await getPublicArticleList({
      pageNum: pageNum.value,
      pageSize,
      categoryId: categoryId.value,
      tagId: tagId.value
    })

    const rows = res.data?.rows || []
    if (reset) {
      articles.value = rows
    } else {
      articles.value.push(...rows)
    }

    if (rows.length < pageSize) {
      noMore.value = true
    }
  } catch (error) {
    console.error('加载文章失败:', error)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  if (loading.value || noMore.value) return
  pageNum.value++
  fetchArticles()
}

const handleScroll = () => {
  const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
  const scrollHeight = document.documentElement.scrollHeight || document.body.scrollHeight
  const clientHeight = document.documentElement.clientHeight || window.innerHeight

  if (scrollTop + clientHeight >= scrollHeight - 50) {
    loadMore()
  }
}

const goDetail = (id: string) => {
  router.push(`/blog/detail/${id}`)
}

const resetAndLoad = () => {
  pageNum.value = 1
  noMore.value = false
  articles.value = []
  fetchArticles(true)
}

watch(
  () => route.query,
  (query) => {
    categoryId.value = query.categoryId ? Number(query.categoryId) : undefined
    tagId.value = query.tagId ? Number(query.tagId) : undefined
    resetAndLoad()
  },
  { immediate: true }
)

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped lang="scss">
.blog-home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.article-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  }
}

.card-cover {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f7fa;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
  }

  .article-card:hover & img {
    transform: scale(1.05);
  }
}

.no-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
}

.card-body {
  padding: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;

  .el-tag {
    --el-tag-font-size: 11px;
  }
}

.loading-text,
.no-more-text,
.empty-text {
  text-align: center;
  padding: 24px;
  color: #909399;
  font-size: 14px;
}

.loading-text {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .article-grid {
    grid-template-columns: 1fr;
  }

  .card-cover {
    height: 180px;
  }
}
</style>
