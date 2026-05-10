import http from '@/utils/http'

export interface SysArticle {
  id: string
  title: string
  cover: string
  isDeleted: string
  readNum: number
}

export interface SysArticleVo {
  id: string
  title: string
  cover: string
  isDeleted: string
  readNum: number
  categories: { id: number; name: string }[]
  tags: { id: number; name: string }[]
}

export interface SysArticleReq {
  id?: string
  title?: string
  cover?: string
  tagIds?: number[]
  categoryIds?: number[]
  content?: string
}

export interface PageQuery {
  pageNum: number
  pageSize: number
}

export interface SysArticleListParams {
  title?: string
  cover?: string
  isDeleted?: string
  pageQuery: PageQuery
}

export function listSysArticle(params: SysArticleListParams) {
  return http({
    url: '/sysArticle/list',
    headers: {
      repeatSubmit: false
    },
    method: 'post',
    data: params
  })
}

export function getSysArticleById(id: string) {
  return http({
    url: `/sysArticle/${id}`,
    method: 'get'
  })
}

export function addSysArticle(data: SysArticleReq) {
  return http({
    url: '/sysArticle/add',
    method: 'post',
    data
  })
}

export function updateSysArticle(data: SysArticleReq) {
  return http({
    url: '/sysArticle/update',
    method: 'post',
    data
  })
}

export function deleteSysArticle(id: string) {
  return http({
    url: `/sysArticle/${id}`,
    method: 'delete'
  })
}
