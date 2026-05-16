import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/components/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { requiresAuth: true, title: '首页' },
      },
      {
        path: 'generator',
        name: 'generator',
        component: () => import('@/views/generator/index.vue'),
        meta: { requiresAuth: true, title: '代码生成器' },
      },
    ],
  },
  {
    path: '/blog/doc/:articleId',
    name: 'doc-display',
    component: () => import('@/views/blog/doc/index.vue'),
    meta: { requiresAuth: true, title: '文档展示' },
  },
  {
    path: '/blog',
    component: () => import('@/components/layout/BlogLayout.vue'),
    children: [
      {
        path: 'home',
        name: 'blog-home',
        component: () => import('@/views/blog/home/index.vue'),
        meta: { requiresAuth: false, title: '博客首页' },
      },
      {
        path: 'detail/:id',
        name: 'blog-detail',
        component: () => import('@/views/blog/detail/index.vue'),
        meta: { requiresAuth: false, title: '文章详情' },
      },
    ],
  },
]

const notFoundRoute: RouteRecordRaw = {
  path: '/:pathMatch(.*)*',
  name: 'not-found',
  component: () => import('@/views/NotFoundView.vue'),
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: constantRoutes,
})

export default router
export { constantRoutes, notFoundRoute }