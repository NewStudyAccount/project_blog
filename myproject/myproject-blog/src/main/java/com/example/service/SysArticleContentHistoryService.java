package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.pojo.SysArticleContentHistory;

import java.util.List;

public interface SysArticleContentHistoryService extends IService<SysArticleContentHistory> {

    List<SysArticleContentHistory> queryHistoryByArticleId(Long articleId);

    int getNextVersion(Long articleId);

    void saveHistory(Long articleId, Long ossId, String replacedBy, String remark);
}
