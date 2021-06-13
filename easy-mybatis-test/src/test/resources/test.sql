create schema test collate utf8mb4_unicode_ci;

create table category
(
    id                 bigint auto_increment comment '主键'
        primary key,
    name               varchar(255) null comment '类别名称',
    parent_id          bigint       null comment '父节点ID',
    path               varchar(255) null comment '节点路径',
    sort               int          null comment '节点排序',
    created_date       datetime     null comment '创建日期',
    last_modified_date datetime     null comment '最后修改日期'
)
    comment 'category';

create table user
(
    id                 bigint auto_increment comment '主键'
        primary key,
    name               varchar(64) null comment '名称',
    phone              varchar(13) null comment '手机号码',
    created_date       datetime    null comment '创建日期',
    last_modified_date datetime    null comment '最后修改日期'
)
    comment 'user';

