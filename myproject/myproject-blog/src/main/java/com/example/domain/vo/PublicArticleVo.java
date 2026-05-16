package com.example.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 公开文章列表VO（只包含展示需要的字段）
 */
@Data
public class PublicArticleVo {

    private Long id;

    private String title;

    private String cover;

    private List<CategoryItem> categories;

    private List<TagItem> tags;

    @Data
    public static class CategoryItem {
        private Long id;
        private String name;
    }

    @Data
    public static class TagItem {
        private Long id;
        private String name;
    }
}
