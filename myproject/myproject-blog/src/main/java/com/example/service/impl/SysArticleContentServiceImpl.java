package com.example.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.TableDataInfo;
import com.example.domain.pojo.SysArticle;
import com.example.domain.pojo.SysArticleContent;
import com.example.execption.BizException;
import com.example.domain.req.SysArticleContentQueryPageReq;
import com.example.domain.req.SysArticleContentReq;
import com.example.domain.vo.SysArticleContentHtmlVo;
import com.example.domain.vo.SysArticleContentVo;
import com.example.mapper.SysArticleContentMapper;
import com.example.oss.domain.SysOssFile;
import com.example.oss.service.OssFileService;
import com.example.service.SysArticleContentService;
import com.example.service.SysArticleContentHistoryService;
import com.example.service.SysArticleService;
import com.example.utils.MarkDownUtil;
import com.example.utils.SecurityUtils;
import com.example.utils.SnowflakeIdUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class SysArticleContentServiceImpl extends ServiceImpl<SysArticleContentMapper, SysArticleContent> implements SysArticleContentService {


    @Autowired
    private OssFileService ossFileService;

    @Autowired
    private SysArticleService sysArticleService;

    @Autowired
    private SysArticleContentHistoryService historyService;

    @Override
    public TableDataInfo<SysArticleContent> querySysArticleContentListPage(SysArticleContentQueryPageReq pageReq) {
        Page<SysArticleContent> page = baseMapper.selectPage(pageReq.getPageQuery().build(), null);
        return TableDataInfo.build(page);
    }


    @Override
    public SysArticleContentVo queryByArticleId(Long id) {
        SysArticleContentVo entity = new SysArticleContentVo();
        LambdaQueryWrapper<SysArticleContent> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysArticleContent::getArticleId, id);
        SysArticleContent sysArticleContent = baseMapper.selectOne(lambdaQueryWrapper);

        // 将 sysArticleContent 转为 sysArticleContentVo
        BeanUtils.copyProperties(sysArticleContent, entity);

        // 下载文件
        Long ossId = sysArticleContent.getOssId();
        SysOssFile sysOssFile = ossFileService.queryById(ossId);
        byte[] contentBytes = ossFileService.downloadFileContent(sysOssFile.getFileUrl());
        String content = new String(contentBytes, StandardCharsets.UTF_8);

        entity.setContent( content);

        return entity;
    }

    @Override
    public SysArticleContentHtmlVo queryHtmlByArticleId(Long id) {
        LambdaQueryWrapper<SysArticleContent> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysArticleContent::getArticleId, id);
        SysArticleContent sysArticleContent = baseMapper.selectOne(lambdaQueryWrapper);

        SysArticleContentHtmlVo htmlVo = new SysArticleContentHtmlVo();
        BeanUtils.copyProperties(sysArticleContent, htmlVo);

        Long ossId = sysArticleContent.getOssId();
        SysOssFile sysOssFile = ossFileService.queryById(ossId);
        byte[] contentBytes = ossFileService.downloadFileContent(sysOssFile.getFileUrl());
        String markdownContent = new String(contentBytes, StandardCharsets.UTF_8);

        String htmlContent = MarkDownUtil.toKramdownHtml(markdownContent);
        htmlVo.setHtmlContent(htmlContent);

        return htmlVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addSysArticleContent(SysArticleContentReq sysArticleContentReq) {
        SysArticleContent sysArticleContent = new SysArticleContent();
        sysArticleContent.setArticleId(sysArticleContentReq.getArticleId());

        Long articleId = sysArticleContentReq.getArticleId();
        SysArticle article = sysArticleService.getById(articleId);

        String content = sysArticleContentReq.getContent();
        // 将 String 转为字节数组
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        String fileName = article.getTitle() + ".md";
        Long ossId = ossFileService.uploadFile(null,fileName, "text/markdown", contentBytes);

        sysArticleContent.setOssId(ossId);

        return baseMapper.insert(sysArticleContent);
    }

    @Override
    public int updateSysArticleContentById(SysArticleContentReq sysArticleContentReq) {

        Long articleId = sysArticleContentReq.getArticleId();
        String content = sysArticleContentReq.getContent();

        if (content == null || content.isEmpty()) {
            throw new BizException("文章内容不能为空");
        }

        SysArticle article = sysArticleService.getById(articleId);
        if (article == null) {
            throw new BizException("文章不存在");
        }

        LambdaQueryWrapper<SysArticleContent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysArticleContent::getArticleId, articleId);
        SysArticleContent sysArticleContent = baseMapper.selectOne(queryWrapper);

        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        String fileName = article.getTitle() + ".md";
        Long ossId = null;

        if (sysArticleContent != null && sysArticleContent.getOssId() != null) {
            // 更新已有 OSS 文件
            ossFileService.uploadFile(sysArticleContent.getOssId(), fileName, "text/markdown", contentBytes);
            ossId = sysArticleContent.getOssId();
        } else {
            // 新建 OSS 文件
            ossId = ossFileService.uploadFile(null, fileName, "text/markdown", contentBytes);
        }

        LambdaUpdateWrapper<SysArticleContent> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .set(ossId!=null,SysArticleContent::getOssId, ossId)
                .eq(SysArticleContent::getArticleId, articleId);
        return baseMapper.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadContent(MultipartFile file, Long articleId) {
        // 校验文件类型
        validateFileType(file);

        // 检查是否已有内容
        LambdaQueryWrapper<SysArticleContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysArticleContent::getArticleId, articleId);
        SysArticleContent existing = baseMapper.selectOne(wrapper);
        if (existing != null) {
            throw new BizException("4090", "该文章已有内容，确认覆盖？历史版本将被保留");
        }

        // 上传新文件到 OSS
        SysArticle article = sysArticleService.getById(articleId);
        String fileName = article.getTitle() + ".md";
        Long ossId;
        try {
            ossId = ossFileService.uploadFile(null, fileName, "text/markdown", file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败", e);
        }

        // 创建文章内容记录
        SysArticleContent content = new SysArticleContent();
        content.setArticleId(articleId);
        content.setOssId(ossId);
        baseMapper.insert(content);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void overrideUploadContent(MultipartFile file, Long articleId, boolean confirmOverride) {
        // 校验文件类型
        validateFileType(file);

        // 校验确认参数
        if (!confirmOverride) {
            throw new BizException("4090", "该文章已有内容，确认覆盖？历史版本将被保留");
        }

        // 查询已有内容
        LambdaQueryWrapper<SysArticleContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysArticleContent::getArticleId, articleId);
        SysArticleContent existing = baseMapper.selectOne(wrapper);
        if (existing == null) {
            throw new BizException("该文章尚无内容，请使用首次上传接口");
        }

        // 将旧版本写入历史表
        String username = SecurityUtils.getUsername();
        historyService.saveHistory(articleId, existing.getOssId(), username, "内容覆盖更新");

        // 上传新文件到 OSS（新 ossId，不覆盖旧文件）
        SysArticle article = sysArticleService.getById(articleId);
        String fileName = article.getTitle() + ".md";
        Long newOssId;
        try {
            newOssId = ossFileService.uploadFile(null, fileName, "text/markdown", file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败", e);
        }

        // 更新文章内容指向新文件
        existing.setOssId(newOssId);
        LambdaUpdateWrapper<SysArticleContent> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysArticleContent::getArticleId, articleId);
//        311556546461171712
//        311555568341090304
        baseMapper.update(existing,updateWrapper);
    }

    private void validateFileType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".md")) {
            throw new BizException("仅支持上传 .md 文件");
        }
    }

}