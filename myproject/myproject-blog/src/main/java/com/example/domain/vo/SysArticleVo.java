package com.example.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class SysArticleVo {

    private Long id;

    private String title;

    private String cover;

    private String isDeleted;

    private Integer readNum;

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
