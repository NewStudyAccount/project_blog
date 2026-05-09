package com.example.domain.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_article_content_history")
public class SysArticleContentHistory implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Long ossId;

    private Integer version;

    private String replacedBy;

    private LocalDateTime replacedAt;

    private String remark;
}
