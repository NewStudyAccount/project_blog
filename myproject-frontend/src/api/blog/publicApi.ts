import http from '@/utils/http'

// 公开文章列表参数
export interface PublicArticleListParams {
  pageNum: number
  pageSize: number
  categoryId?: number
  tagId?: number
}

// 公开文章VO
export interface PublicArticleVo {
  id: string
  title: string
  cover: string
  categories: { id: number; name: string }[]
  tags: { id: number; name: string }[]
}

// 公开分类VO
export interface PublicCategoryVo {
  id: number
  name: string
}

// 公开标签VO
export interface PublicTagVo {
  id: number
  name: string
}

// 站点配置
export interface SiteConfig {
  siteName: string
  copyright: string
  github: string
  beian: string
}

/**
 * 获取公开文章列表
 */
export function getPublicArticleList(params: PublicArticleListParams) {
  return http({
    url: '/sysArticle/public/list',
    method: 'post',
    data: params
  })
}

/**
 * 获取公开分类列表
 */
export function getPublicCategoryList() {
  return http({
    url: '/sysCategory/public/list',
    method: 'get'
  })
}

/**
 * 获取公开标签列表
 */
export function getPublicTagList() {
  return http({
    url: '/sysTag/public/list',
    method: 'get'
  })
}

/**
 * 获取站点配置
 */
export function getPublicConfig() {
  return http({
    url: '/sysConfig/public/info',
    method: 'get'
  })
}

/**
 * 获取公开文章HTML内容
 */
export function getPublicArticleContentHtml(id: string) {
  return http({
    url: `/sysArticleContent/public/queryHtmlByArticleId/${id}`,
    method: 'get'
  })
}

/**
 * 获取公开文章详情
 */
export function getPublicArticleById(id: string) {
  return http({
    url: `/sysArticle/public/${id}`,
    method: 'get'
  })
}
