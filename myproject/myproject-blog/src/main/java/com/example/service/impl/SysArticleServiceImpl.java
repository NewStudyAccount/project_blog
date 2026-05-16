package com.example.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.TableDataInfo;
import com.example.domain.pojo.*;
import com.example.domain.req.PublicArticleListReq;
import com.example.domain.req.SysArticleQueryPageReq;
import com.example.domain.req.SysArticleReq;
import com.example.domain.vo.PublicArticleVo;
import com.example.domain.vo.SysArticleVo;
import com.example.mapper.*;
import com.example.service.SysArticleService;
import com.example.utils.SnowflakeIdUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SysArticleServiceImpl extends ServiceImpl<SysArticleMapper, SysArticle> implements SysArticleService {

    @Autowired
    private SysArticleTagRelMapper articleTagRelMapper;

    @Autowired
    private SysArticleCategoryRelMapper articleCategoryRelMapper;

    @Autowired
    private SysCategoryMapper categoryMapper;

    @Autowired
    private SysTagMapper tagMapper;

    @Override
    public TableDataInfo<SysArticleVo> querySysArticleListPage(SysArticleQueryPageReq pageReq) {
        Page<SysArticle> page = baseMapper.selectPage(pageReq.getPageQuery().build(), null);
        List<SysArticleVo> voList = buildArticleVoList(page.getRecords());
        TableDataInfo<SysArticleVo> result = new TableDataInfo<>();
        result.setRows(voList);
        result.setTotal((int) page.getTotal());
        return result;
    }

    @Override
    public SysArticleVo queryVoById(Long id) {
        SysArticle article = baseMapper.selectById(id);
        if (article == null) {
            return null;
        }
        return buildArticleVo(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addSysArticle(SysArticleReq req) {
        SysArticle entity = new SysArticle();
        entity.setId(SnowflakeIdUtil.blogNextId());
        entity.setTitle(req.getTitle());
        entity.setCover(req.getCover());
        int result = baseMapper.insert(entity);

        // 写入标签关系
        if (!CollectionUtils.isEmpty(req.getTagIds())) {
            for (Long tagId : req.getTagIds()) {
                SysArticleTagRel rel = new SysArticleTagRel();
                rel.setArticleId(entity.getId());
                rel.setTagId(tagId);
                articleTagRelMapper.insert(rel);
            }
        }

        // 写入分类关系
        if (!CollectionUtils.isEmpty(req.getCategoryIds())) {
            for (Long categoryId : req.getCategoryIds()) {
                SysArticleCategoryRel rel = new SysArticleCategoryRel();
                rel.setArticleId(entity.getId());
                rel.setCategoryId(categoryId);
                articleCategoryRelMapper.insert(rel);
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSysArticleById(SysArticleReq req) {

        //更新文章
        SysArticle entity = new SysArticle();
        entity.setId(req.getId());
        LambdaUpdateWrapper<SysArticle> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(StringUtils.isNotEmpty(req.getTitle()), SysArticle::getTitle, req.getTitle())
                .set(StringUtils.isNotEmpty(req.getCover()), SysArticle::getCover, req.getCover())
                .eq(SysArticle::getId, req.getId());
        int result = baseMapper.update(updateWrapper);

        Long articleId = req.getId();

        // 先删后插标签关系
        articleTagRelMapper.delete(
                new LambdaQueryWrapper<SysArticleTagRel>().eq(SysArticleTagRel::getArticleId, articleId)
        );
        if (!CollectionUtils.isEmpty(req.getTagIds())) {
            for (Long tagId : req.getTagIds()) {
                SysArticleTagRel rel = new SysArticleTagRel();
                rel.setArticleId(articleId);
                rel.setTagId(tagId);
                articleTagRelMapper.insert(rel);
            }
        }

        // 先删后插分类关系
        articleCategoryRelMapper.delete(
                new LambdaQueryWrapper<SysArticleCategoryRel>().eq(SysArticleCategoryRel::getArticleId, articleId)
        );
        if (!CollectionUtils.isEmpty(req.getCategoryIds())) {
            for (Long categoryId : req.getCategoryIds()) {
                SysArticleCategoryRel rel = new SysArticleCategoryRel();
                rel.setArticleId(articleId);
                rel.setCategoryId(categoryId);
                articleCategoryRelMapper.insert(rel);
            }
        }

        return result;
    }

    private List<SysArticleVo> buildArticleVoList(List<SysArticle> articles) {
        if (CollectionUtils.isEmpty(articles)) {
            return Collections.emptyList();
        }

        List<Long> articleIds = articles.stream().map(SysArticle::getId).collect(Collectors.toList());

        // 批量查询关系
        List<SysArticleTagRel> tagRels = articleTagRelMapper.selectList(
                new LambdaQueryWrapper<SysArticleTagRel>().in(SysArticleTagRel::getArticleId, articleIds)
        );
        List<SysArticleCategoryRel> catRels = articleCategoryRelMapper.selectList(
                new LambdaQueryWrapper<SysArticleCategoryRel>().in(SysArticleCategoryRel::getArticleId, articleIds)
        );

        // 收集所有 tagId 和 categoryId
        Set<Long> tagIds = tagRels.stream().map(SysArticleTagRel::getTagId).collect(Collectors.toSet());
        Set<Long> catIds = catRels.stream().map(SysArticleCategoryRel::getCategoryId).collect(Collectors.toSet());

        // 批量查询 tag 和 category
        Map<Long, String> tagNameMap = CollectionUtils.isEmpty(tagIds) ? Collections.emptyMap() :
                tagMapper.selectBatchIds(tagIds).stream()
                        .collect(Collectors.toMap(SysTag::getId, SysTag::getName));
        Map<Long, String> catNameMap = CollectionUtils.isEmpty(catIds) ? Collections.emptyMap() :
                categoryMapper.selectBatchIds(catIds).stream()
                        .collect(Collectors.toMap(SysCategory::getId, SysCategory::getName));

        // 按 articleId 分组
        Map<Long, List<SysArticleTagRel>> tagRelMap = tagRels.stream()
                .collect(Collectors.groupingBy(SysArticleTagRel::getArticleId));
        Map<Long, List<SysArticleCategoryRel>> catRelMap = catRels.stream()
                .collect(Collectors.groupingBy(SysArticleCategoryRel::getArticleId));

        // 构建 VO
        return articles.stream().map(article -> {
            SysArticleVo vo = new SysArticleVo();
            vo.setId(article.getId());
            vo.setTitle(article.getTitle());
            vo.setCover(article.getCover());
            vo.setIsDeleted(article.getIsDeleted());
            vo.setReadNum(article.getReadNum());

            List<SysArticleVo.TagItem> tags = tagRelMap.getOrDefault(article.getId(), Collections.emptyList())
                    .stream().map(rel -> {
                        SysArticleVo.TagItem item = new SysArticleVo.TagItem();
                        item.setId(rel.getTagId());
                        item.setName(tagNameMap.get(rel.getTagId()));
                        return item;
                    }).collect(Collectors.toList());
            vo.setTags(tags);

            List<SysArticleVo.CategoryItem> categories = catRelMap.getOrDefault(article.getId(), Collections.emptyList())
                    .stream().map(rel -> {
                        SysArticleVo.CategoryItem item = new SysArticleVo.CategoryItem();
                        item.setId(rel.getCategoryId());
                        item.setName(catNameMap.get(rel.getCategoryId()));
                        return item;
                    }).collect(Collectors.toList());
            vo.setCategories(categories);

            return vo;
        }).collect(Collectors.toList());
    }

    private SysArticleVo buildArticleVo(SysArticle article) {
        List<SysArticleVo> vos = buildArticleVoList(Collections.singletonList(article));
        return vos.isEmpty() ? null : vos.get(0);
    }

    @Override
    public TableDataInfo<PublicArticleVo> queryPublicArticleList(PublicArticleListReq req) {
        Long categoryId = req.getCategoryId();
        Long tagId = req.getTagId();

        List<Long> articleIds = null;

        // 按分类筛选
        if (categoryId != null) {
            List<SysArticleCategoryRel> catRels = articleCategoryRelMapper.selectList(
                    new LambdaQueryWrapper<SysArticleCategoryRel>()
                            .eq(SysArticleCategoryRel::getCategoryId, categoryId)
            );
            articleIds = catRels.stream()
                    .map(SysArticleCategoryRel::getArticleId)
                    .collect(Collectors.toList());
            if (articleIds.isEmpty()) {
                TableDataInfo<PublicArticleVo> result = new TableDataInfo<>();
                result.setRows(Collections.emptyList());
                result.setTotal(0);
                return result;
            }
        }

        // 按标签筛选
        if (tagId != null) {
            List<SysArticleTagRel> tagRels = articleTagRelMapper.selectList(
                    new LambdaQueryWrapper<SysArticleTagRel>()
                            .eq(SysArticleTagRel::getTagId, tagId)
            );
            List<Long> tagArticleIds = tagRels.stream()
                    .map(SysArticleTagRel::getArticleId)
                    .collect(Collectors.toList());

            if (tagArticleIds.isEmpty()) {
                TableDataInfo<PublicArticleVo> result = new TableDataInfo<>();
                result.setRows(Collections.emptyList());
                result.setTotal(0);
                return result;
            }

            // 取交集
            if (articleIds != null) {
                articleIds.retainAll(tagArticleIds);
                if (articleIds.isEmpty()) {
                    TableDataInfo<PublicArticleVo> result = new TableDataInfo<>();
                    result.setRows(Collections.emptyList());
                    result.setTotal(0);
                    return result;
                }
            } else {
                articleIds = tagArticleIds;
            }
        }

        // 构建查询条件
        LambdaQueryWrapper<SysArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysArticle::getId, SysArticle::getTitle, SysArticle::getCover);
        wrapper.orderByDesc(SysArticle::getCreateTime);

        if (articleIds != null) {
            wrapper.in(SysArticle::getId, articleIds);
        }

        Page<SysArticle> page = baseMapper.selectPage(req.build(), wrapper);

        // 转换为 PublicArticleVo（包含分类和标签）
        List<PublicArticleVo> voList = buildPublicArticleVoList(page.getRecords());

        TableDataInfo<PublicArticleVo> result = new TableDataInfo<>();
        result.setRows(voList);
        result.setTotal((int) page.getTotal());
        return result;
    }

    private List<PublicArticleVo> buildPublicArticleVoList(List<SysArticle> articles) {
        if (CollectionUtils.isEmpty(articles)) {
            return Collections.emptyList();
        }

        List<Long> articleIds = articles.stream()
                .map(SysArticle::getId)
                .collect(Collectors.toList());

        // 批量查询关系
        List<SysArticleTagRel> tagRels = articleTagRelMapper.selectList(
                new LambdaQueryWrapper<SysArticleTagRel>()
                        .in(SysArticleTagRel::getArticleId, articleIds)
        );
        List<SysArticleCategoryRel> catRels = articleCategoryRelMapper.selectList(
                new LambdaQueryWrapper<SysArticleCategoryRel>()
                        .in(SysArticleCategoryRel::getArticleId, articleIds)
        );

        // 收集所有 tagId 和 categoryId
        Set<Long> tagIds = tagRels.stream()
                .map(SysArticleTagRel::getTagId)
                .collect(Collectors.toSet());
        Set<Long> catIds = catRels.stream()
                .map(SysArticleCategoryRel::getCategoryId)
                .collect(Collectors.toSet());

        // 批量查询 tag 和 category 名称
        Map<Long, String> tagNameMap = CollectionUtils.isEmpty(tagIds) ? Collections.emptyMap() :
                tagMapper.selectBatchIds(tagIds).stream()
                        .collect(Collectors.toMap(SysTag::getId, SysTag::getName));
        Map<Long, String> catNameMap = CollectionUtils.isEmpty(catIds) ? Collections.emptyMap() :
                categoryMapper.selectBatchIds(catIds).stream()
                        .collect(Collectors.toMap(SysCategory::getId, SysCategory::getName));

        // 按 articleId 分组
        Map<Long, List<SysArticleTagRel>> tagRelMap = tagRels.stream()
                .collect(Collectors.groupingBy(SysArticleTagRel::getArticleId));
        Map<Long, List<SysArticleCategoryRel>> catRelMap = catRels.stream()
                .collect(Collectors.groupingBy(SysArticleCategoryRel::getArticleId));

        // 构建 VO
        return articles.stream().map(article -> {
            PublicArticleVo vo = new PublicArticleVo();
            vo.setId(article.getId());
            vo.setTitle(article.getTitle());
            vo.setCover(article.getCover());

            // 设置标签
            List<PublicArticleVo.TagItem> tags = tagRelMap
                    .getOrDefault(article.getId(), Collections.emptyList())
                    .stream().map(rel -> {
                        PublicArticleVo.TagItem item = new PublicArticleVo.TagItem();
                        item.setId(rel.getTagId());
                        item.setName(tagNameMap.get(rel.getTagId()));
                        return item;
                    }).collect(Collectors.toList());
            vo.setTags(tags);

            // 设置分类
            List<PublicArticleVo.CategoryItem> categories = catRelMap
                    .getOrDefault(article.getId(), Collections.emptyList())
                    .stream().map(rel -> {
                        PublicArticleVo.CategoryItem item = new PublicArticleVo.CategoryItem();
                        item.setId(rel.getCategoryId());
                        item.setName(catNameMap.get(rel.getCategoryId()));
                        return item;
                    }).collect(Collectors.toList());
            vo.setCategories(categories);

            return vo;
        }).collect(Collectors.toList());
    }
}
