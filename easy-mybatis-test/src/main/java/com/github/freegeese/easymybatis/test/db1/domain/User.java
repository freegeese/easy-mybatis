package com.github.freegeese.easymybatis.test.db1.domain;


import com.github.freegeese.easymybatis.core.annotation.Column;
import com.github.freegeese.easymybatis.core.annotation.GeneratedValue;
import com.github.freegeese.easymybatis.core.annotation.GenerationType;
import com.github.freegeese.easymybatis.core.annotation.Id;
import com.github.freegeese.easymybatis.core.domain.Dateable;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;

@Data
public class User implements Dateable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String phone;
    @Column(jdbcType = JdbcType.TIMESTAMP)
    private Date createdDate;
    private Date lastModifiedDate;
}
