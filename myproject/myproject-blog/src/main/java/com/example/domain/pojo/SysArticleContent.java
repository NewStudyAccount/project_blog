package com.example.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_article_content")
public class SysArticleContent extends BaseEntity {


    private Long articleId;


    // 关联oss文件的id，用于后续直接查询并更新文件
    private Long ossId;

}
