package com.github.freegeese.easymybatis.test.db1.domain;


import com.github.freegeese.easymybatis.annotation.Column;
import com.github.freegeese.easymybatis.annotation.GeneratedValue;
import com.github.freegeese.easymybatis.annotation.Id;
import com.github.freegeese.easymybatis.domain.Dateable;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;

@Data
public class User implements Dateable {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String phone;
    @Column(jdbcType = JdbcType.TIMESTAMP)
    private Date createdDate;
    private Date lastModifiedDate;
}
