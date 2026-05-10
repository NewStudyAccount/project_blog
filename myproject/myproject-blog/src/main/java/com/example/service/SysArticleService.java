package com.example.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.TableDataInfo;
import com.example.domain.pojo.SysArticle;
import com.example.domain.req.SysArticleQueryPageReq;
import com.example.domain.req.SysArticleReq;
import com.example.domain.vo.SysArticleVo;

import java.util.List;

public interface SysArticleService extends IService<SysArticle> {

    TableDataInfo<SysArticleVo> querySysArticleListPage(SysArticleQueryPageReq pageReq);

    SysArticleVo queryVoById(Long id);

    int addSysArticle(SysArticleReq req);

    int updateSysArticleById(SysArticleReq req);
}
