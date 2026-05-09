package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.pojo.SysArticleContentHistory;
import com.example.mapper.SysArticleContentHistoryMapper;
import com.example.service.SysArticleContentHistoryService;
import com.example.utils.SnowflakeIdUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysArticleContentHistoryServiceImpl extends ServiceImpl<SysArticleContentHistoryMapper, SysArticleContentHistory> implements SysArticleContentHistoryService {

    @Override
    public List<SysArticleContentHistory> queryHistoryByArticleId(Long articleId) {
        LambdaQueryWrapper<SysArticleContentHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysArticleContentHistory::getArticleId, articleId)
               .orderByDesc(SysArticleContentHistory::getVersion);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public int getNextVersion(Long articleId) {
        LambdaQueryWrapper<SysArticleContentHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysArticleContentHistory::getArticleId, articleId)
               .orderByDesc(SysArticleContentHistory::getVersion)
               .last("LIMIT 1");
        SysArticleContentHistory latest = baseMapper.selectOne(wrapper);
        return latest == null ? 1 : latest.getVersion() + 1;
    }

    @Override
    public void saveHistory(Long articleId, Long ossId, String replacedBy, String remark) {
        SysArticleContentHistory history = new SysArticleContentHistory();
        history.setId(SnowflakeIdUtil.ossNextId());
        history.setArticleId(articleId);
        history.setOssId(ossId);
        history.setVersion(getNextVersion(articleId));
        history.setReplacedBy(replacedBy);
        history.setReplacedAt(LocalDateTime.now());
        history.setRemark(remark);
        baseMapper.insert(history);
    }
}
