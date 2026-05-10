package com.example.domain.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_article_content_history")
public class SysArticleContentHistory extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Long ossId;

    private Integer version;

    private String replacedBy;

    private LocalDateTime replacedAt;

    private String remark;
}
