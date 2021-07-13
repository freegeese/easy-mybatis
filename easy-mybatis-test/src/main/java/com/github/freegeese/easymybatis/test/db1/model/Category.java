package com.github.freegeese.easymybatis.test.db1.model;

import com.github.freegeese.easymybatis.core.annotation.GeneratedValue;
import com.github.freegeese.easymybatis.core.annotation.GenerationType;
import com.github.freegeese.easymybatis.core.annotation.Id;
import com.github.freegeese.easymybatis.core.domain.Dateable;
import com.github.freegeese.easymybatis.core.domain.Treeable;
import lombok.Data;

import java.util.Date;

/**
 * category
 */
@Data
public class Category implements Dateable, Treeable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    // 主键
    private Long id;
    // 类别名称
    private String name;
    // 父节点ID
    private Long parentId;
    // 节点路径
    private String path;
    // 节点排序
    private Integer sort;
    // 创建日期
    private Date createdDate;
    // 最后修改日期
    private Date lastModifiedDate;
    /* KEEP_MARK_START */
    /* KEEP_MARK_END */
}