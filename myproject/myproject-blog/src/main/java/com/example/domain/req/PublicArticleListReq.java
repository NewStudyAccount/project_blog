package com.example.domain.req;

import com.example.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公开文章列表请求对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PublicArticleListReq extends PageQuery {

    /**
     * 分类ID（可选）
     */
    private Long categoryId;

    /**
     * 标签ID（可选）
     */
    private Long tagId;
}
