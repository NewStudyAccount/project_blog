package com.example.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 标签
 * @TableName sys_tag
 */
@TableName(value ="sys_tag")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysTag extends BaseEntity {
    /**
     *
     */
    @TableId
    private Long id;

    /**
     * 标签名
     */
    private String name;

    /**
     * 删除标志
     */
    @TableLogic
    private String isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
