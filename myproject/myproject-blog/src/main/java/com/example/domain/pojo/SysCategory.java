package com.example.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 文章分类
 * @TableName sys_category
 */
@TableName(value ="sys_category")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysCategory extends BaseEntity {
    /**
     * 分类id
     */
    @TableId
    private Long id;

    /**
     * 分类名
     */
    private String name;

    /**
     * 删除标志
     */
    private String isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
