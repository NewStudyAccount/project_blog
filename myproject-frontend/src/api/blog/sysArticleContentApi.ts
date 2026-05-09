import http from '@/utils/http'

export interface SysArticleContent {
  articleId: string
  content: string
}

export interface SysArticleContentHtml {
  articleId: string
  htmlContent: string
}

export interface PageQuery {
  pageNum: number
  pageSize: number
}

export interface SysArticleContentListParams {
  content?: string
  pageQuery: PageQuery
}

export function listSysArticleContent(params: SysArticleContentListParams) {
  return http({
    url: '/sysArticleContent/list',
    headers: {
      repeatSubmit: false
    },
    method: 'post',
    data: params
  })
}

export function getSysArticleContentByArticleId(id: string) {
  return http({
    url: `/sysArticleContent/queryByArticleId/${id}`,
    method: 'get'
  })
}

export function getSysArticleContentHtmlByArticleId(id: string) {
  return http({
    url: `/sysArticleContent/queryHtmlByArticleId/${id}`,
    method: 'get'
  })
}

export function addSysArticleContent(data: Partial<SysArticleContent>) {
  return http({
    url: '/sysArticleContent/add',
    method: 'post',
    data
  })
}

export function updateSysArticleContent(data: Partial<SysArticleContent>) {
  return http({
    url: '/sysArticleContent/update',
    method: 'post',
    data
  })
}

export function uploadContent(file: File, articleId: string) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('articleId', articleId)
  return http({
    url: '/sysArticleContent/upload',
    method: 'post',
    data: formData,
    headers: { repeatSubmit: false }
  })
}

export function uploadOverride(file: File, articleId: string, confirmOverride: boolean = false) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('articleId', articleId)
  formData.append('confirmOverride', String(confirmOverride))
  return http({
    url: '/sysArticleContent/uploadOverride',
    method: 'post',
    data: formData,
    headers: { repeatSubmit: false }
  })
}

export interface ContentHistory {
  id: string
  articleId: string
  ossId: string
  version: number
  replacedBy: string
  replacedAt: string
  remark: string
}

export function getContentHistory(articleId: string) {
  return http({
    url: `/sysArticleContent/history/${articleId}`,
    method: 'get'
  })
}

