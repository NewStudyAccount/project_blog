package com.example.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("sys_article_tag_rel")
public class SysArticleTagRel implements Serializable {

    private Long articleId;

    private Long tagId;
}
