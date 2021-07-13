package com.github.freegeese.easymybatis.test.db2.model;


import com.github.freegeese.easymybatis.core.annotation.GeneratedValue;
import com.github.freegeese.easymybatis.core.annotation.Id;
import com.github.freegeese.easymybatis.core.domain.Dateable;
import lombok.Data;

import java.util.Date;

@Data
public class Account implements Dateable {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private String password;
    private Date createdDate;
    private Date lastModifiedDate;
}
